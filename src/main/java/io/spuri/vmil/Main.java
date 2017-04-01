package io.spuri.vmil;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;

public class Main extends AbstractVerticle {

  @Override
  public void start() {
    Router router = Router.router(vertx);
    router.route().handler(rCtxt -> {
      HttpServerResponse hsRes = rCtxt.response();
      hsRes.putHeader("coontent-type", "text/plain");
      hsRes.end("Haha you found me");
    });

    vertx.createHttpServer()
        .requestHandler(router::accept)
        .listen(8080);
  }

}
