package io.spuri.vmil.routing;

import io.spuri.vmil.Main;

/**
 * Created by flyin on 4/1/2017.
 */
public class RError extends ARouting {
  public RError(Main main) {
    super(main);
  }


  @Override
  public void createRoutes() {
    main.router.get().failureHandler(ctx -> {
      main.getVertx().eventBus().publish("error", ctx.request().connection().remoteAddress().host() + " encountered a " + ctx.statusCode());
      if (ctx.statusCode() == 404) {
        ctx.put("message", "Page not found!");
        ctx.next();
//        ctx.reroute("/RStatic.html");
//        ctx.response().end("Page not found!");
      } else if(ctx.statusCode() == 500) {
        // TODO make 500 page ctx.reroute("/internal-error.html");
        ctx.response()
          .end("Internal server error!");
      } else {
        ctx.next();
      }
    });
  }
}
