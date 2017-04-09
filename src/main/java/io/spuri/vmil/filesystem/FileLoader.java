package io.spuri.vmil.filesystem;

import io.spuri.vmil.constants.EventBusChannels;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import org.hjson.JsonValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by flyin on 4/4/2017.
 */
public class FileLoader extends AbstractVerticle {
  private List<ARequiresFiles> requesters = new ArrayList<>();
  public FileLoader(ARequiresFiles ... args) {
    requesters = Arrays.asList(args);
  }

  @Override
  public void start() {
    requesters.forEach(requester -> {
      String className = requester.getClass().getSimpleName();
      requester.getRequiredFiles().forEach(fileStr -> {
        vertx.eventBus().publish(EventBusChannels.INFO_LOG, "Loading " + fileStr + " requested by " + className);
        vertx.fileSystem().readFile("data/" + className + "/" + fileStr, new Handler<AsyncResult<Buffer>>() {
          @Override
          public void handle(AsyncResult<Buffer> asyncResult) {
            if (asyncResult.failed()) {
              vertx.eventBus().publish(EventBusChannels.ERROR_LOG, "Failed to load " + fileStr);
            } else {
              Object data = null;
              try {
                if (fileStr.endsWith(".hjson")) {
                  data = new JsonObject(JsonValue.readHjson(asyncResult.result().toString()).toString());
                } else if (fileStr.endsWith(".json")) {
                  data = asyncResult.result().toJsonObject();
                } else
                  data = asyncResult.result().toString();
              } catch(Throwable t) {
                vertx.eventBus().publish(EventBusChannels.ERROR_LOG, "Failed to load file " + fileStr);
                t.printStackTrace();

              }
              vertx.eventBus().publish(className + "." + fileStr, data);
            }
          }
        });

      });
    });
  }
}
