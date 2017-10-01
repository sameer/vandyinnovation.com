package io.spuri.vmil;

import io.vertx.core.Vertx;
import io.vertx.ext.web.templ.PebbleTemplateEngine;

public class VTemplating {
  public PebbleTemplateEngine templateEngine;

  public VTemplating(Vertx v) {
    templateEngine = PebbleTemplateEngine.create(v);
  }
}
