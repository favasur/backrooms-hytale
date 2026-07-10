package com.favasur.wrongyellow;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.util.logging.Level;

/**
 * Backrooms Flicker Plugin
 * <p>
 * Adds rapid ON↔OFF flickering to the dedicated flickering variant
 * of the White Build Lightsource block. The vanilla lightsource
 * block is never touched — it stays statically on.
 * <p>
 * Block variant IDs (defined in Server/Item/Items/):
 * - Block_White_Build_Lightsource_Flickering (ON, has buzz sound)
 * - Block_White_Build_Lightsource_Off         (OFF, no sound)
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
                FlickeringTickingSystem.BLOCK_FLICKER,
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
