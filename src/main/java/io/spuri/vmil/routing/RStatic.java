package io.spuri.vmil.routing;

import io.spuri.vmil.Main;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * Created by flyin on 4/1/2017.
 */
public class RStatic extends ARouting {
  public RStatic(Main main) {
    super(main);
  }

  @Override
  public void onReady() {

    main.router.route("/favicon.ico").method(HttpMethod.GET).handler(FaviconHandler.create("webroot/favicon.ico"));
    main.router.route("/blub").handler(ctx -> {
      ctx.response().end("blub back at ya");
    });
    main.router.route().method(HttpMethod.GET).handler(main.tTmpls.getHandler());

    // Static Resources should be considered last
    main.router.route().method(HttpMethod.GET).handler(StaticHandler.create());
  }
}
