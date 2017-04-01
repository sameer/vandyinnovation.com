package io.spuri.vmil;

import io.spuri.vmil.routing.RDynamic;
import io.spuri.vmil.routing.RError;
import io.spuri.vmil.routing.RStatic;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

public class Main extends AbstractVerticle {
  @Override
  public void start() {
    Router router = Router.router(vertx);

    RStatic staticRoutes = new RStatic(router);
    RDynamic dynamicRoutes = new RDynamic(router);
    RError errorRoutes = new RError(router);
    staticRoutes.attach();
    dynamicRoutes.attach();
    errorRoutes.attach();

    HttpServer server = vertx.createHttpServer();
    server.requestHandler(router::accept).listen(8080);
  }

}
