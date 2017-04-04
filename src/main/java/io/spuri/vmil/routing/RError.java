package io.spuri.vmil.routing;

import io.vertx.ext.web.Router;

/**
 * Created by flyin on 4/1/2017.
 */
public class RError extends IRoutes {
  public RError(Router router) {
    super(router);
  }

  public void attach() {
    router.get().failureHandler(ctx -> {
      if (ctx.statusCode() == 404) {
        ctx.reroute("/not-found");
        ctx.response()
          .end("Page not found!");
      } else if(ctx.statusCode() == 500) {
        ctx.reroute("/internal-error");
        ctx.response()
          .end("Internal server error!");
      } else {
        ctx.next();
      }
    });
  }
}
