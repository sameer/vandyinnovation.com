package io.spuri.vmil.templating;

import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.templ.PebbleTemplateEngine;

/**
 * Created by flyin on 4/1/2017.
 */
public class TTmpls {
  private PebbleTemplateEngine mEngine;
  private TemplateHandler mHandler;
  public TTmpls(Vertx vertx) {
    mEngine = PebbleTemplateEngine.create(vertx);
    TemplateHandler.create(mEngine);
  }

  public PebbleTemplateEngine getEngine() {
    return mEngine;
  }

  public TemplateHandler getHandler() {
    return mHandler;
  }
}
