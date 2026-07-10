package com.favasur.wrongyellow;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.util.logging.Level;

/**
 * Wrong Yellow Flicker Plugin
 * <p>
 * Part of the Wrong Yellow mod pack. Adds flickering light effects
 * to the White Build Lightsource block by registering a ticking
 * system that periodically swaps between full, dimmed, and off
 * block variants.
 * <p>
 * Block variant IDs (defined in Server/Item/Items/):
 * - Block_White_Build_Lightsource       (full brightness, has buzz sound)
 * - Block_White_Build_Lightsource_Dimmed (dim light, no sound)
 * - Block_White_Build_Lightsource_Off    (no light, no sound)
 */
public class FlickerPlugin extends JavaPlugin {

    public FlickerPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        log("WrongYellow FlickerPlugin constructed.");
    }

    @Override
    protected void setup() {
        log("WrongYellow FlickerPlugin setting up...");

        // Register the ticking system that drives the flicker effect
        FlickeringTickingSystem tickingSystem = new FlickeringTickingSystem();
        getChunkStoreRegistry().registerSystem(tickingSystem);
        log("Registered FlickeringTickingSystem");

        log("WrongYellow FlickerPlugin setup complete.");
        log(String.format(
                "Flickering enabled for block variants: %s / %s / %s",
                FlickeringTickingSystem.BLOCK_FULL,
                FlickeringTickingSystem.BLOCK_DIM,
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
