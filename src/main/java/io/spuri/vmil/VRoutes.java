package io.spuri.vmil;

import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.io.File;
import java.util.*;

public class VRoutes {
  private static final Logger logger = LoggerFactory.getLogger(VRoutes.class);
  static final List<NI> navItems = new ArrayList<>();

  static List<RouteMaker> dynamicroutes = new ArrayList<>();

  static {
    dynamicroutes.addAll(
      Arrays.asList(
        (Main m) -> m.router.route("/assets/*").handler(StaticHandler.create("assets/").setCachingEnabled(false)),
        (Main m) -> m.router.route("/favicon.ico")
          .method(HttpMethod.GET).handler(FaviconHandler.create("assets/favicon.ico")),
        (Main m) -> m.router.route("/").method(HttpMethod.GET).handler(ctx -> {
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
        })
//    rmstatic("/projects", "projects.md", "Projects", "What project's VMIL msembers are taking on", NI.identity()),
//    rmstatic("/aboutus", "aboutus.md", "About Us", "About VMIL", NI.identity()),
//    rmstatic("/events", "events.md", "Events", "Upcoming events", NI.identity()),
//    rmstatic("/gallery", "gallery.md", "Gallery", "Pictures of VMIL members in action", NI.identity()),
//    rmstatic("/sponsors", "sponsors.md", "Sponsors", "Organizations that sponsor VMIL", NI.identity()),


      ));
  }

  static List<RouteMaker> addstaticroutes(Main m) {
    List<RouteMaker> staticroutes = new ArrayList<>();
    GoogleHandler googleHandler = GoogleHandler.create(m.getVertx(), "Pages");
    staticroutes.add((Main m2) -> m2.router.route("/pages/*").handler(googleHandler));
    for (String file : googleHandler.files.keySet()) {
      File f = new File(file);
      String pageName = f.getName().substring(0, f.getName().lastIndexOf('.'));
      logger.info("Adding path for " + pageName);
      staticroutes.add(rmstatic("/" + pageName.replace(' ', '_'), f.getName(), pageName, "VMIL: " + pageName, NI.identity()));
    }
    staticroutes.add((Main m2)
      -> m2.router.route().handler(
      ctx -> {
        ctx.response().putHeader("content-type", "text/html").end("hihihi");
      }));
    return staticroutes;
  }

  private static Map<String, RouteMaker> staticPaths = new HashMap<>();

  private static RouteMaker rmstatic(String path, String markupFile, String title, String description, NIModifier niModifier) {
    if (staticPaths.containsKey(path)) {
      return (Main m) -> null; // This looks horrible but please bear with me for now
    } else {
      RouteMaker rm = (Main m) -> {
        navItems.add(niModifier.modify(NI.navItem().link(path).title(title)));
        return m.router.route(path).method(HttpMethod.GET).handler(ctx -> {
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

        });
      };
      staticPaths.put(path, rm);
      return rm;
    }
  }


  //private static RouteMaker rmdynamic(String path,
}
