package io.spuri.vmil;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.Router;

import static io.spuri.vmil.VRoutes.dynamicroutes;

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
    vertx.createHttpServer(
      new HttpServerOptions().setSsl(true).setPemKeyCertOptions(new PemKeyCertOptions().addKeyPath(System.getProperty("vertx.key")).addCertPath(System.getProperty("vertx.cert"))))
      .requestHandler(router::accept).listen(Integer.parseInt(System.getProperty("vertx.port")), System.getProperty("vertx.host"));
  }
}
