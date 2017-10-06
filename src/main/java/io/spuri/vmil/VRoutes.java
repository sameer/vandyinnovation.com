package io.spuri.vmil;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.handler.FaviconHandler;

import java.util.*;

public class VRoutes {
  static final List<NI> navItems = new ArrayList<>();
  private static final Logger logger = LoggerFactory.getLogger(VRoutes.class);
  static List<RouteMaker> dynamicroutes = new ArrayList<>();
  private static Map<String, RouteMaker> staticPaths = new HashMap<>();

  static {
    dynamicroutes.addAll(Arrays.asList(
        (Main m)
            -> m.router.route("/assets/sponsors/*")
                   .handler(GoogleHandler.create(m.getVertx(), "Sponsors").setPath("/assets/sponsors")),
        (Main m)
            -> m.router.route("/assets/*").handler(GoogleHandler.create(m.getVertx(), "Assets").setPath("/assets")),
        (Main m)
            -> m.router.route("/favicon.ico")
                   .method(HttpMethod.GET)
                   .handler(FaviconHandler.create("assets/favicon.ico")),
        (Main m) -> m.router.route("/").method(HttpMethod.GET).handler(ctx -> {
          ctx.put("desc", "VMIL Homepage");
          ctx.put("navItems", navItems);
          m.vTemplating.templateEngine.render(ctx, "templates/", "index.peb", result -> {
            if (result.succeeded()) {
              ctx.response().end(result.result());
            } else {
              ctx.fail(result.cause());
            }
          });
        })));

    navItems.add(
        NI.create()
            .link("https://anchorlink.vanderbilt.edu/organization/medicalinnovationlab/events")
            .title("Events"));
  }

  static List<RouteMaker> addstaticroutes(Main m) {
    List<RouteMaker> staticroutes = new ArrayList<>();
    GoogleHandler googleHandler = GoogleHandler.create(m.getVertx(), "Pages");
    googleHandler.setPath("/pages");
    staticroutes.add((Main m2) -> m2.router.route("/pages/*").handler(googleHandler));
    int i;

    for (GoogleHandler.GoogleDriveFileTreeNode treeNode : googleHandler.rootTreeNode.children) {
      buildStaticRouteNavItems(staticroutes, null, treeNode);
    }

    staticroutes.add((Main m2) -> m2.router.route().handler(ctx -> {
      // TODO: make this actually nice
      ctx.response().putHeader("content-type", "text/html").end("404!");
    }));
    return staticroutes;
  }

  private static NI buildStaticRouteNavItems(List<RouteMaker> staticroutes, NI parentNI, GoogleHandler.GoogleDriveFileTreeNode fileTreeNode) {
    String filePathNoExtension = VUtils.googleClipExtension(fileTreeNode.data.getLeft());
    NI myNI = NI.create().title(VUtils.googleClipPath(filePathNoExtension)).parent(parentNI);
    for (GoogleHandler.GoogleDriveFileTreeNode treeNode : fileTreeNode.children) {
      myNI.child(buildStaticRouteNavItems(staticroutes, myNI, treeNode));
    }
    if (myNI.parent == null) { // I am groot
      myNI.link(VUtils.googleCleanPath(filePathNoExtension));
      if (fileTreeNode.children.isEmpty()) {
        logger.info("Adding root NI " + myNI.title);

        for (NI ni : navItems) {
         if (ni.link.equals(myNI.link)) {
           ni.title(myNI.title);
           ni.parent(parentNI);
           logger.info("Matched file to NI entry " + ni.title + " with children " + ni.children.get(0).toString());
           staticroutes.add(rmstatic(ni, fileTreeNode.data.getLeft(),
             "VMIL: " + ni.title));
           return ni;
         }
        }
        staticroutes.add(rmstatic(myNI, fileTreeNode.data.getLeft(),
          "VMIL: " + myNI.title));
        navItems.add(myNI);
      } else {
        for(NI ni : navItems) { // Find a matching top-level nav item if any and attach this folder's children to it
          if (ni.link.equals(myNI.link)) {
            logger.info("Matched folder to NI entry " + ni.title);
            ni.children = myNI.children;
            return ni;
          }
        }
        navItems.add(myNI);
      }
    } else if (fileTreeNode.children.isEmpty()) { // Leaf node
      myNI.link(fileTreeNode.data.getLeft());
    }
    return myNI;
  }

  private static RouteMaker rmstatic(
      NI navItem, String markupFile, String description) {
    if (staticPaths.containsKey(navItem.link)) {
      return (Main m) -> null; // This looks horrible but please bear with me for now
    } else {
      RouteMaker rm = (Main m) -> {
        return m.router.route(navItem.link).method(HttpMethod.GET).handler(ctx -> {
          ctx.put("myNavItem", navItem);
          ctx.put("title", navItem.title);
          ctx.put("desc", description);
          ctx.put("navItems", navItems);
          ctx.put("text", "/pages" + markupFile);
          m.vTemplating.templateEngine.render(ctx, "templates/", "static.peb", result -> {
            if (result.succeeded()) {
              ctx.response().end(result.result());
            } else {
              ctx.fail(result.cause());
            }
          });

        });
      };
      staticPaths.put(navItem.link, rm);
      return rm;
    }
  }

  // private static RouteMaker rmdynamic(String path,
}
