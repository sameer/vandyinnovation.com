package io.spuri.vmil;

import java.util.function.Function;

public class VUtils {
  private static String clipHelper(
      String str, char toClipAt, Function<Integer, String> clipHandler) {
    int i;
    return (i = str.lastIndexOf(toClipAt)) == -1 ? str : clipHandler.apply(i);
  }

  static String googleClipPath(String filePath) {
    return clipHelper(filePath, '/', (i) -> filePath.substring(i + 1));
  }

  static String googleClipExtension(String file) {
    return clipHelper(file, '.', (i) -> file.substring(0, i));
  }

  static String googleCleanPath(String path) {
    path = path.replaceAll(" ", "_");
    if (path.length() > 1 && path.endsWith("/")) {
      return path.substring(0, path.length() - 1);
    } else {
      return path;
    }
  }

  static String googleMarkAsDir(String filePath) {
    return filePath.endsWith(".dir") ? filePath : filePath + ".dir";
  }

  static boolean googleIsDir(String filePath) {
    return filePath.endsWith(".dir");
  }
}
