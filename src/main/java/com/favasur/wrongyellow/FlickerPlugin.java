package com.favasur.wrongyellow;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.util.logging.Level;

/**
 * Backrooms Flicker Plugin
 * <p>
 * Adds rapid ON↔OFF flickering to the vanilla Build_Lightsource_White
 * block. Swaps between the ON variant (with light and buzz sound)
 * and the OFF variant (same texture, no light, no sound).
 * <p>
 * Block IDs:
 * - Build_Lightsource_White      (ON, has light + buzz sound)
 * - Block_Lightsource_White_Off  (OFF, same texture, no light/sound)
 */
public class FlickerPlugin extends JavaPlugin {

    public FlickerPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        log("Backrooms FlickerPlugin constructed.");
    }

    @Override
    protected void setup() {
        log("Backrooms FlickerPlugin setting up...");

        // Register the ticking system that drives the flicker effect
        FlickeringTickingSystem tickingSystem = new FlickeringTickingSystem();
        getChunkStoreRegistry().registerSystem(tickingSystem);
        log("Registered FlickeringTickingSystem");

        log("Backrooms FlickerPlugin setup complete.");
        log(String.format(
                "Flickering ON↔OFF enabled for: %s <-> %s",
                FlickeringTickingSystem.BLOCK_ON,
                FlickeringTickingSystem.BLOCK_OFF
        ));
    }

    /**
     * Convenience helper to log at INFO level using FluentLogger pattern.
     */
    private void log(String message) {
        getLogger().at(Level.INFO).log(message);
    }
}
