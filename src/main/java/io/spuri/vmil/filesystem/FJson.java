package io.spuri.vmil.filesystem;

import io.vertx.core.Vertx;

/**
 * Created by flyin on 03.04.2017.
 */
public class FJson {
  public static void readFiles(Vertx vertx) {
    vertx.fileSystem().readDirBlocking("resources/pages/static");
  }
}
