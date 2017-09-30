package io.spuri.vmil;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.ArrayList;
import java.util.List;

public class VRoutes {
  static final List<NI> navItems = new ArrayList<>();

  static RouteMaker[] vroutes = {
    (Main m) -> m.router.route("/assets/*").handler(StaticHandler.create("assets/").setCachingEnabled(false)),
//    (Main m) -> m.router.route("/pages/*").handler(ctx -> {
//      ctx.response().putHeader("content-type", "text/markdown").sendFile(ctx.request().path());
//      // ctx.response().putHeader("content-type", "text/markdown").sendFile(ctx.request().path());
//    }),
    (Main m) -> m.router.route("/pages/*").handler(GoogleHandler.create(m.getVertx(), "Pages").setCachingEnabled(false)),
    (Main m) -> m.router.route("/favicon.ico")
      .method(HttpMethod.GET).handler(FaviconHandler.create("assets/favicon.ico")),
    (Main m) -> m.router.route("/").method(HttpMethod.GET).handler(ctx -> {
      ctx.put("title", null);
      ctx.put("desc", "VMIL Homepage");
      ctx.put("navItems", navItems);
      m.vTemplating.templateEngine.render(ctx, "templates/", "index.peb",
        result -> {
          if (result.succeeded()) {
            ctx.response().end(result.result());
          } else {
            ctx.fail(result.cause());
          }
        });
    }),
//    rmstatic("/projects", "projects.md", "Projects", "What project's VMIL msembers are taking on", NI.identity()),
    rmstatic("/aboutus", "aboutus.md", "About Us", "About VMIL", NI.identity()),
    rmstatic("/events", "events.md", "Events", "Upcoming events", NI.identity()),
//    rmstatic("/gallery", "gallery.md", "Gallery", "Pictures of VMIL members in action", NI.identity()),
    rmstatic("/sponsors", "sponsors.md", "Sponsors", "Organizations that sponsor VMIL", NI.identity()),

    (Main m)
      -> m.router.route().handler(
      ctx -> {
        ctx.response().putHeader("content-type", "text/html").end("hihihi");
      }),
  };

  private static RouteMaker rmstatic(String path, String markupFile, String title, String description, NIModifier niModifier) {
    return (Main m) -> {
      Future<Boolean> markupFileFuture = Future.future();
      m.getVertx().fileSystem().exists("pages/" + markupFile, markupFileFuture.completer());
      navItems.add(niModifier.modify(NI.navItem().link(path).title(title)));
      return m.router.route(path).method(HttpMethod.GET).handler(ctx -> {
        if (markupFileFuture.succeeded() && markupFileFuture.result()) {
          ctx.put("title", title);
          ctx.put("desc", description);
          ctx.put("navItems", navItems);
          ctx.put("text", "/pages/" + markupFile);
          m.vTemplating.templateEngine.render(ctx, "templates/", "static.peb",
            result -> {
              if (result.succeeded()) {
                ctx.response().end(result.result());
              } else {
                ctx.fail(result.cause());
              }
            });
        } else {
          ctx.fail(markupFileFuture.cause());
        }
      });
    };
  }



  //private static RouteMaker rmdynamic(String path,
}
