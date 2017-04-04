package io.spuri.vmil.filesystem;

import io.vertx.core.Vertx;

/**
 * Created by flyin on 03.04.2017.
 */
public class HjsonReader {
  public static void readFiles(Vertx vertx, String path) {
    vertx.fileSystem().readDirBlocking(path);
  }
}
