package io.spuri.vmil;

import io.spuri.vmil.constants.EventBusChannels;
import io.spuri.vmil.filesystem.ARequiresFiles;
import io.spuri.vmil.logging.ErrorLogger;
import io.spuri.vmil.filesystem.FileLoader;
import io.spuri.vmil.logging.InfoLogger;
import io.spuri.vmil.routing.ARouting;
import io.spuri.vmil.routing.RDynamic;
import io.spuri.vmil.routing.RError;
import io.spuri.vmil.routing.RStatic;
import io.spuri.vmil.templating.TTmpls;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

import java.util.Arrays;
import java.util.List;

public class Main extends AbstractVerticle {
  public Router router;
  public TTmpls tTmpls;

  private List<ARouting> routesList;

  public HttpServer httpServer;

  private int readyCount = 0;

  @Override
  public void start() {
    // Register ErrorLogger to event bus
    new ErrorLogger(this).register();
    new InfoLogger(this).register();

    vertx.eventBus().publish(EventBusChannels.INFO_LOG, "Starting...");

    router = Router.router(vertx);
    tTmpls = new TTmpls(vertx);

    vertx.eventBus().consumer(EventBusChannels.ROUTING_LOADED).handler(msg -> {
      vertx.eventBus().publish(EventBusChannels.INFO_LOG, msg.body());
      ++readyCount;
      if (readyCount == routesList.size()) {
        routesList.forEach(ARouting::createRoutes);
        httpServer = vertx.createHttpServer().requestHandler(router::accept).listen(8080);
        vertx.eventBus().publish(EventBusChannels.INFO_LOG, "Server listening!");
      }
    });

    routesList = Arrays.asList(
      new RStatic(this),
      new RDynamic(this),
      new RError(this)
    );

    vertx.deployVerticle(new FileLoader((ARequiresFiles[])routesList.toArray()));

  }

}
