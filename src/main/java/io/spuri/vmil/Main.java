package io.spuri.vmil;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.Router;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.spuri.vmil.VRoutes.dynamicroutes;

public class Main extends AbstractVerticle {
  static final Map<String, String> defaultProperties = new LinkedHashMap<>(); // Maintain insertion order
  static {
    defaultProperties.put("vertx.port", "8080");
    defaultProperties.put("vertx.host", "localhost");
    defaultProperties.put("vertx.ssl", "false");
  }
  protected Router router;
  protected VTemplating vTemplating;

  @Override
  public void start() throws Exception {
    for (Map.Entry<String, String> entry : defaultProperties.entrySet()) {
      if (System.getProperty(entry.getKey()) == null) {
        System.setProperty(entry.getKey(), entry.getValue());
      }
    }
    vTemplating = new VTemplating(vertx);
    router = Router.router(vertx);

    for (RouteMaker rm : dynamicroutes) {
      rm.make(this);
    }
    for (RouteMaker rm : VRoutes.addstaticroutes(this)) {
      rm.make(this);
    }

    if (Boolean.parseBoolean(System.getProperty("vertx.ssl"))) {
      vertx
        .createHttpServer(new HttpServerOptions().setSsl(true).setPemKeyCertOptions(
          new PemKeyCertOptions()
            .addKeyPath(System.getProperty("vertx.key"))
            .addCertPath(System.getProperty("vertx.cert"))))
        .requestHandler(router::accept)
        .listen(
          Integer.parseInt(System.getProperty("vertx.port")), System.getProperty("vertx.host"));
    } else {
      vertx
        .createHttpServer(new HttpServerOptions())
        .requestHandler(router::accept)
        .listen(
          Integer.parseInt(System.getProperty("vertx.port")), System.getProperty("vertx.host"));
    }
  }
}
