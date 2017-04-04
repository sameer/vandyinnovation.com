package io.spuri.vmil.logging;

import io.spuri.vmil.Main;

/**
 * Created by flyin on 4/4/2017.
 */
public class InfoLogger extends ALogger {

  public InfoLogger(Main main) {
    super(main);
  }

  @Override
  public InfoLogger register() {
    main.getVertx().eventBus().consumer("info").handler(msg -> {
      System.out.println("[INFO]: " + msg.body());
    });
    return this;
  }
}
