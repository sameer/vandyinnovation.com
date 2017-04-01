package io.spuri.vmil.routing;

import io.vertx.ext.web.Router;

/**
 * Created by flyin on 4/1/2017.
 */
public abstract class RRoutes {
  protected Router router;

  RRoutes(Router router) {
    this.router = router;
  }

  public abstract void attach();


}
