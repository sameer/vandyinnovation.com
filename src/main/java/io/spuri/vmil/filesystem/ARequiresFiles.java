package io.spuri.vmil.filesystem;

import io.spuri.vmil.Main;
import org.hjson.JsonObject;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by Sameer on 4/9/2017.
 */
public abstract class ARequiresFiles {
  protected Main main;
  protected HashMap<String, Object> data = new HashMap<>();
  private int filesLeft;
  protected ARequiresFiles(Main main, String... requiredFiles) {
    this.main = main;
    if (requiredFiles.length == 0) {
      onLoad();
    } else {
      this.filesLeft = requiredFiles.length;
      String className = this.getClass().getSimpleName();
      for (String requiredFilename : requiredFiles) {
        this.data.put(requiredFilename, null);
        main.getVertx().eventBus().consumer(className + "." + requiredFilename).handler(msg -> {
          if (msg.body() != null) {
            --filesLeft;
            this.data.put(requiredFilename, msg.body());
            if (filesLeft == 0) {
              onLoad();
            }
          }
        });
      }
    }
  }

  /**
   * Behavior that is run when data has been fully loaded
   * This will be executed during object construction if no files are required.
   * NOTE: means that parameters passed to the constructors of subclasses might not be available!
   */
  protected abstract void onLoad();

  public Set<String> getRequiredFiles() {
    return this.data.keySet();
  }
}
