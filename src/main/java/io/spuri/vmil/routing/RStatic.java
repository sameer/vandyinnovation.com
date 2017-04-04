package io.spuri.vmil.routing;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.TemplateHandler;

/**
 * Created by flyin on 4/1/2017.
 */
public class RStatic extends IRoutes {
  private TemplateHandler templateHandler;
  public RStatic(Router router, TemplateHandler templateHandler) {
    super(router);
    this.templateHandler = templateHandler;
  }

  public void attach() {
    router.route("/favicon.ico").method(HttpMethod.GET).handler(FaviconHandler.create("webroot/favicon.ico"));
    router.route().method(HttpMethod.GET).handler(StaticHandler.create());

    router.route().method(HttpMethod.GET).handler(templateHandler);


    router.route("/blub").handler(ctx -> {
      ctx.response().end("blub back at ya");
    });
  }
}
