package io.spuri.vmil.routing;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * Created by flyin on 4/1/2017.
 */
public class RStatic extends RRoutes {
  public RStatic(Router router) {
    super(router);
  }

  public void attach() {
    router.route().method(HttpMethod.GET).handler(StaticHandler.create());

    //router.get().handler(StaticHandler.create("webroot"));
    router.route("/blub").handler(ctx -> {
      ctx.response().end("blub back at ya");
    });
  }
}
