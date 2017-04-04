package io.spuri.vmil.logging;

import io.spuri.vmil.Main;

/**
 * Created by flyin on 4/4/2017.
 */
public abstract class ALogger {
  protected Main main;
  public ALogger(Main main) {
    this.main = main;
  }
  public abstract ALogger register();
}
