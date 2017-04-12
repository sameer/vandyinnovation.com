package io.spuri.vmil.routing;

import io.spuri.vmil.Main;
import io.spuri.vmil.constants.EventBusChannels;
import io.spuri.vmil.filesystem.ARequiresFiles;

/**
 * Created by flyin on 4/1/2017.
 */
public abstract class ARouting extends ARequiresFiles {
  protected ARouting(Main main, String... requiredFiles) {
    super(main, requiredFiles);
  }

  @Override
  public void onLoad() {
    if (data.size() == 0) {
      main.getVertx().eventBus().publish(EventBusChannels.ROUTING_LOADED,
          this.getClass().getSimpleName() + " routing w/o data loaded");
    } else {
      main.getVertx().eventBus().publish(EventBusChannels.ROUTING_LOADED,
          this.getClass().getSimpleName() + " routing with data loaded");
    }
  }
  /**
   * Executes route creation behavior of the routing class. Should only be called after onLoad()
   * succeeds.
   * Client is expected to call this to properly determine route priority.
   */
  public abstract void createRoutes();
}
