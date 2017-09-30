package io.spuri.vmil;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import static io.spuri.vmil.VRoutes.*;

public class Main extends AbstractVerticle {
  protected Router router;
  protected VTemplating vTemplating;
  @Override
  public void start() throws Exception {
    vTemplating = new VTemplating(vertx);
    router = Router.router(vertx);

    for (RouteMaker rm : dynamicroutes) {
      rm.make(this);
    }
    for (RouteMaker rm : VRoutes.addstaticroutes(this)) {
      rm.make(this);
    }
    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
  }
}
