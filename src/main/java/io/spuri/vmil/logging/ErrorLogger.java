package io.spuri.vmil.logging;

import io.spuri.vmil.Main;

/**
 * Created by flyin on 4/4/2017.
 */
public class ErrorLogger extends ALogger {
  public ErrorLogger(Main main) {
    super(main);
  }

  @Override
  public ErrorLogger register() {
    main.getVertx().eventBus().consumer("error").handler(msg -> {
      System.out.println("[ERROR]: " + msg.body());
    });
    return this;
  }

}
