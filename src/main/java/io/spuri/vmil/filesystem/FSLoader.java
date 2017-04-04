package io.spuri.vmil.filesystem;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;

import java.util.Arrays;
import java.util.List;

/**
 * Created by flyin on 4/4/2017.
 */
public class FSLoader extends AbstractVerticle {
  public static final List<String> files = Arrays.asList(new String[]{"RDynamic.json", "RStatic.json", "RError.json"});
  @Override
  public void start() {
    files.forEach(fileStr -> {
      vertx.fileSystem().readFile("data/" + fileStr, new Handler<AsyncResult<Buffer>>() {
        @Override
        public void handle(AsyncResult<Buffer> asyncResult) {
          if (asyncResult.failed()) {
            vertx.eventBus().publish("error", "Failed to load JSON file " + fileStr);
          } else {
            vertx.eventBus().publish(fileStr.replace(".json","") + ".data", asyncResult.result().toJsonObject());
          }
        }
      });
    });
  }
}
