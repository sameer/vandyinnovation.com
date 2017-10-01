package io.spuri.vmil;

import io.vertx.ext.web.Route;

@FunctionalInterface
public interface RouteMaker {
  Route make(Main main);
}
