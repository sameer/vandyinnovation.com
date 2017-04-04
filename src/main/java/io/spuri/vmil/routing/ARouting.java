package io.spuri.vmil.routing;

import io.spuri.vmil.Main;
import io.spuri.vmil.constants.EventBusChannels;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

/**
 * Created by flyin on 4/1/2017.
 */
public abstract class ARouting {
  protected Main main;
  protected JsonObject data;

  ARouting(Main main) {
    this.main = main;
    makeMessageConsumer();
  }

  /**
   * Makes a MessageConsumer that receives JsonObject data for this router and notifies when ready
   */
  private void makeMessageConsumer() {
    main.getVertx().eventBus().consumer(this.getClass().getSimpleName() + ".data").handler(data -> {
      Object o = data.body();
      if (o instanceof JsonObject) {
        this.data = (JsonObject) o;
        onReady(); // TODO - figure out how to avoid parallel calling by routing classes that messes up ordering
        main.getVertx().eventBus().publish(EventBusChannels.ROUTING_READY, this.getClass().getSimpleName() + " routing is ready!");

      } else {
        main.getVertx().eventBus().publish("error", this.getClass().getSimpleName() + " received non JsonObject data!");
      }
    });
  }

  /**
   * Behavior that is run when the object is ready to implement its routing behavior.
   */
  protected abstract void onReady();

}
