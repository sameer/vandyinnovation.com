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

import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.file.FileProps;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.impl.LRUCache;
import io.vertx.ext.web.impl.Utils;

import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class GoogleHandler implements StaticHandler {
  private static final String APPLICATION_NAME = "VandyInnovation";
  private static HttpTransport httpTransport;
  private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("java.io.tmpdir"));
  private static FileDataStoreFactory dataStoreFactory;
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static Credential credential;
  private static Drive drive;

  private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/vandyinnovation";
  private static boolean tempDirInit;
  private static boolean staticInit;
  private static boolean staticInitFailed;

  private boolean initFailed;
  private final String THIS_TEMP_DIR;
  private StaticHandler staticHandler;

  private static final Logger logger = LoggerFactory.getLogger(GoogleHandler.class);


  public static GoogleHandler create(Vertx vertx, String folderName) {
    try {
      if (!tempDirInit && !staticInitFailed) {
        if (vertx.fileSystem().existsBlocking(TEMP_DIR)) {
          vertx.fileSystem().deleteRecursiveBlocking(TEMP_DIR, true);
        }
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

        GoogleClientSecrets googleClientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
          new InputStreamReader(GoogleHandler.class.getResourceAsStream("/data/client_secrets.json")));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
          httpTransport, JSON_FACTORY, googleClientSecrets,
          Collections.singleton(DriveScopes.DRIVE_READONLY)).setDataStoreFactory(dataStoreFactory).setAccessType("offline").setApprovalPrompt("auto").build();
        credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("me");
        drive = new Drive.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
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

  private GoogleHandler(Vertx vertx, String folderName) {
    this.THIS_TEMP_DIR = TEMP_DIR + "/" + folderName;
    if (!staticInitFailed) {
      try {
        Drive.Files.List req = drive.files().list();
        req.setCorpus("user");
        req.setQ("name = '" + folderName + "'");
        FileList fileList = req.execute();
        File folder = fileList.getFiles().get(0);
        req = drive.files().list();
        req.setCorpus("user");
        req.setQ("'" + folder.getId() + "' in parents");
        FileList folderFileList = req.execute();
        staticHandler = StaticHandler.create().setAllowRootFileSystemAccess(true).setWebRoot(THIS_TEMP_DIR);
        vertx.fileSystem().mkdir(THIS_TEMP_DIR, future -> {
          if (future.failed()) {
            initFailed = true;
            return;
          }
          try {
            for (File f : folderFileList.getFiles()) {
              OutputStream outputStream = new FileOutputStream(THIS_TEMP_DIR + "/" + f.getName());
              logger.info("Downloading file " + f.getName());
              try {
                drive.files().get(f.getId()).executeMediaAndDownloadTo(outputStream);
              } catch (Exception e) {
                logger.error("Couldn't download " + f.getName(), e);
              }
            }
          } catch (Exception e) {
            initFailed = true;
            logger.fatal("GoogleHandler init failed", e);
          }
        });
      } catch (Exception e) {
        logger.fatal("GoogleHandler init failed", e);
        initFailed = true;
      }
    } else {
      initFailed = true;
    }
  }

  @Override
  public StaticHandler setAllowRootFileSystemAccess(boolean allowRootFileSystemAccess) {
    if (!initFailed) staticHandler.setAllowRootFileSystemAccess(allowRootFileSystemAccess);
    return this;
  }

  @Override
  public StaticHandler setWebRoot(String webRoot) {
    if (!initFailed) staticHandler.setWebRoot(webRoot);
    return this;
  }

  @Override
  public StaticHandler setFilesReadOnly(boolean readOnly) {
    if (!initFailed) staticHandler.setFilesReadOnly(readOnly);
    return this;
  }

  @Override
  public StaticHandler setMaxAgeSeconds(long maxAgeSeconds) {
    if (!initFailed) staticHandler.setMaxAgeSeconds(maxAgeSeconds);
    return this;
  }

  @Override
  public StaticHandler setCachingEnabled(boolean enabled) {
    if (!initFailed) staticHandler.setCachingEnabled(enabled);
    return this;
  }

  @Override
  public StaticHandler setDirectoryListing(boolean directoryListing) {
    if (!initFailed) staticHandler.setDirectoryListing(directoryListing);
    return this;
  }

  @Override
  public StaticHandler setIncludeHidden(boolean includeHidden) {
    if (!initFailed) staticHandler.setIncludeHidden(includeHidden);
    return this;
  }

  @Override
  public StaticHandler setCacheEntryTimeout(long timeout) {
    if (!initFailed) staticHandler.setCacheEntryTimeout(timeout);
    return this;
  }

  @Override
  public StaticHandler setIndexPage(String indexPage) {
    if (!initFailed) staticHandler.setIndexPage(indexPage);
    return this;
  }

  @Override
  public StaticHandler setMaxCacheSize(int maxCacheSize) {
    if (!initFailed) staticHandler.setMaxCacheSize(maxCacheSize);
    return this;
  }

  @Override
  public StaticHandler setAlwaysAsyncFS(boolean alwaysAsyncFS) {
    if (!initFailed) staticHandler.setAlwaysAsyncFS(alwaysAsyncFS);
    return this;
  }

  @Override
  public StaticHandler setEnableFSTuning(boolean enableFSTuning) {
    if (!initFailed) staticHandler.setEnableFSTuning(enableFSTuning);
    return this;
  }

  @Override
  public StaticHandler setMaxAvgServeTimeNs(long maxAvgServeTimeNanoSeconds) {
    if (!initFailed) staticHandler.setMaxAvgServeTimeNs(maxAvgServeTimeNanoSeconds);
    return this;
  }

  @Override
  public StaticHandler setDirectoryTemplate(String directoryTemplate) {
    if (!initFailed) staticHandler.setDirectoryTemplate(directoryTemplate);
    return this;
  }

  @Override
  public StaticHandler setEnableRangeSupport(boolean enableRangeSupport) {
    if (!initFailed) staticHandler.setEnableRangeSupport(enableRangeSupport);
    return this;
  }

  @Override
  public StaticHandler setSendVaryHeader(boolean varyHeader) {
    if (!initFailed) staticHandler.setSendVaryHeader(varyHeader);
    return this;
  }

  @Override
  public StaticHandler setDefaultContentEncoding(String contentEncoding) {
    if (!initFailed) staticHandler.setDefaultContentEncoding(contentEncoding);
    return this;
  }

  @Override
  public void handle(RoutingContext ctx) {
    if (!initFailed) staticHandler.handle(ctx);
  }

}
