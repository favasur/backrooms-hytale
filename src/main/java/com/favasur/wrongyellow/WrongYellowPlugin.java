package com.favasur.wrongyellow;

import com.favasur.wrongyellow.frame.FramingSystem;
import com.favasur.wrongyellow.frame.FrameType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.util.logging.Level;

/**
 * Wrong Yellow — Main Plugin
 * <p>
 * Adds rapid ON↔OFF flickering to the vanilla Build_Lightsource_White
 * block. Swaps between the ON variant (with light and buzz sound)
 * and the OFF variant (same texture, no light, no sound).
 * <p>
 * Block IDs:
 * - Build_Lightsource_White      (ON, has light + buzz sound)
 * - Block_Lightsource_White_Off  (OFF, same texture, no light/sound)
 */
public class WrongYellowPlugin extends JavaPlugin {

    public WrongYellowPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        log("WrongYellowPlugin constructed.");
    }

    @Override
    protected void setup() {
        log("WrongYellowPlugin — booting systems...");

        // Register the ticking system that drives the flicker effect
        FlickeringTickingSystem tickingSystem = new FlickeringTickingSystem();
        getChunkStoreRegistry().registerSystem(tickingSystem);
        log("Registered FlickeringTickingSystem");

        // Register the FramedBlocks camouflage system (non-visual mode)
        // Camouflage state is tracked in memory; block-swapping visuals
        // can be added later via FrameInteractionHandler.
        FramingSystem framingSystem = new FramingSystem();
        getChunkStoreRegistry().registerSystem(framingSystem);
        log("Registered FramingSystem — FramedBlocks port active with " +
                FrameType.all().length + " shape types registered");

        log("WrongYellowPlugin — all systems running.");
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
