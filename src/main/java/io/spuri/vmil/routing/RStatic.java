package io.spuri.vmil.routing;

import io.spuri.vmil.Main;
import io.spuri.vmil.constants.EventBusChannels;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.hjson.JsonValue;

import java.util.Map;

/**
 * Created by flyin on 4/1/2017.
 */
public class RStatic extends ARouting {
  public RStatic(Main main) {
    super(main, "config.hjson");
  }


  @Override
  public void createRoutes() {
    main.router.route("/favicon.ico").method(HttpMethod.GET).handler(FaviconHandler.create("webroot/favicon.ico"));
    //main.router.route().method(HttpMethod.GET).handler(main.tTmpls.getHandler().handle);

    JsonObject config = (JsonObject) data.get("config.hjson");

    // TODO: abstract the data fetching monstrosity below to the proper classes
    // TODO: fix templates so that they use the context variable
    config.getJsonArray("pages").forEach(jobj -> {
      JsonObject page = (JsonObject) jobj;
      main.router.route(page.getString("path")).method(HttpMethod.GET).handler(ctx -> {
        main.getVertx().fileSystem().readFile("data/RStatic/" + page.getString("file"), asyncResult -> {
          if (asyncResult.succeeded()) {
            JsonObject jsonObject = new JsonObject(JsonValue.readHjson(asyncResult.result().toString()).toString());
            for(Map.Entry<String, Object> entry : jsonObject.getMap().entrySet()) {
              ctx.put(entry.getKey(), entry.getValue().toString());
            }
            main.tTmpls.getEngine().render(ctx, "templates/static.peb", res -> {
              if (res.succeeded()) {
                ctx.response().end(res.result());
              } else {
                ctx.fail(res.cause());
              }
            });
          } else {
            main.getVertx().eventBus().publish(EventBusChannels.ERROR_LOG, "Failed to load data from " + page.getString("file"));
            ctx.fail(500);
          }
        });

      });
    });
    main.router.route("/blub").handler(ctx -> {
      ctx.response().end("blub back at ya");
    });

    // Static Resources should be considered last
    main.router.route().method(HttpMethod.GET).handler(StaticHandler.create());
  }
}
