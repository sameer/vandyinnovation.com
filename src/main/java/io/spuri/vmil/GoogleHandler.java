package io.spuri.vmil;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.impl.Utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.*;

public class GoogleHandler implements Handler<RoutingContext> {
  private static final String APPLICATION_NAME = "VandyInnovation";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/vandyinnovation";
  private static final java.io.File DATA_STORE_DIR = new java.io.File(TEMP_DIR);
  private static final Logger logger = LoggerFactory.getLogger(GoogleHandler.class);
  private static HttpTransport httpTransport;
  private static FileDataStoreFactory dataStoreFactory;
  private static Credential credential;
  private static Drive drive;
  private static boolean tempDirInit;
  private static boolean staticInit;
  private static boolean staticInitFailed;
  private final String THIS_TEMP_DIR;
  private final String FOLDER_NAME;
  public Map<String, File> files = new HashMap<>();
  private boolean initFailed;

  private GoogleHandler(Vertx vertx, String folderName) {
    this.FOLDER_NAME = folderName;
    this.THIS_TEMP_DIR = TEMP_DIR + "/" + FOLDER_NAME;
    if (!staticInitFailed) {
      try {
        Drive.Files.List req = drive.files().list();
        req.setCorpus("user");
        req.setQ("mimeType = 'application/vnd.google-apps.folder' and name = '" + folderName + "'");
        FileList fileList = req.execute();
        File folder = fileList.getFiles().get(0);
        req = drive.files().list();
        req.setCorpus("user");
        req.setQ("'" + folder.getId() + "' in parents");
        FileList folderFileList = req.execute();
        for (File f : folderFileList.getFiles()) {
          logger.info("Adding file " + f.getName());
          files.put(f.getName(), f);
        }
      } catch (Exception e) {
        logger.fatal("GoogleHandler init failed", e);
        initFailed = true;
      }
    } else {
      initFailed = true;
    }
  }

  public static GoogleHandler create(Vertx vertx, String folderName) {
    try {
      if (!tempDirInit && !staticInitFailed) {
        if (!vertx.fileSystem().existsBlocking(TEMP_DIR))
          vertx.fileSystem().mkdirBlocking(TEMP_DIR);
        tempDirInit = true;
      }
    } catch (Exception e) {
      logger.fatal("GoogleHandler static init failed", e);
      staticInitFailed = true;
    }
    try {
      if (!staticInit && !staticInitFailed) {
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);

        GoogleClientSecrets googleClientSecrets = GoogleClientSecrets.load(
            JSON_FACTORY, new InputStreamReader(GoogleHandler.class.getResourceAsStream(
                              "/data/client_secrets.json")));
        GoogleAuthorizationCodeFlow flow =
            new GoogleAuthorizationCodeFlow
                .Builder(httpTransport, JSON_FACTORY, googleClientSecrets,
                    Collections.singleton(DriveScopes.DRIVE_READONLY))
                .setDataStoreFactory(dataStoreFactory)
                .setAccessType("offline")
                .setApprovalPrompt("auto")
                .build();
        credential =
            new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("me");
        drive = new Drive.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        staticInit = true;
      }
    } catch (GoogleJsonResponseException e) {
      logger.error(e.getDetails());
    } catch (Exception e) {
      logger.fatal("GoogleHandler static init failed", e);
      staticInitFailed = true;
    }

    return new GoogleHandler(vertx, folderName);
  }
  private LinkedHashMap<String, Buffer> fileCache = new LinkedHashMap<>();

  // TODO: implement cache evicting

  @Override
  public void handle(RoutingContext ctx) {
    if (!initFailed) {
      HttpServerRequest req = ctx.request();
      if (req.method() == HttpMethod.GET || req.method() == HttpMethod.HEAD) {
        String path = Utils.removeDots(Utils.urlDecode(ctx.normalisedPath(), false));
        logger.info("Request is " + path);
        if (path == null) {
          logger.warn("Invalid path: " + ctx.request().path());
          ctx.next();
          return;
        }
        path = path.substring(path.lastIndexOf('/') + 1);
        logger.info("Looking for " + path);
        File toSend;
        if ("/".equals(path)) {
          ctx.next();
        } else if ((toSend = files.get(path)) != null) {
          try {
            fileCache.computeIfAbsent(toSend.getId(), id -> {
              Buffer buffer = Buffer.buffer();
              try {
                ByteArrayOutputStream file = new ByteArrayOutputStream();
                drive.files().get(toSend.getId()).executeMediaAndDownloadTo(file);
                buffer.appendBytes(file.toByteArray());
              } catch (Exception e) {
                logger.error("Failed to fetch link for file " + toSend.getName(), e);
              }
              return buffer;
            });

            ctx.response().end(fileCache.get(toSend.getId()));
          } catch (Exception e) {
            logger.error("Failed to fetch link for file " + toSend.getName(), e);
            ctx.response().end();
          }
        } else {
          logger.info("Couldn't find " + path);
        }
      }
    } else {
      ctx.next();
    }
  }
}
