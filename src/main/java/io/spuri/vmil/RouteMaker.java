package io.spuri.vmil;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

@FunctionalInterface
public interface RouteMaker {
  Route make(Main main);
}
