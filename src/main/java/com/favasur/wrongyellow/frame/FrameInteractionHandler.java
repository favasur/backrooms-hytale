package com.favasur.wrongyellow.frame;

import com.hypixel.hytale.logger.HytaleLogger;

import javax.annotation.Nullable;
import java.util.logging.Level;

/**
 * Handles player interaction with frame blocks — the Hytale equivalent of
 * {@code IFramedBlock.handleUse()} / {@code AbstractFramedBlock.useItemOn()}
 * in FramedBlocks.
 * <p>
 * <h3>Planned interaction flow (right-click on a frame block while holding
 * the Framing Tool)</h3>
 * <ol>
 *   <li>Player right-clicks a block.</li>
 *   <li>Hytale's interaction event fires (exact API TBD — see
 *       {@link #onPlayerRightClick(FramingSystem, long, String, String)}) .</li>
 *   <li>We check if the clicked block is a frame (via {@link FrameType#fromBlockId}).</li>
 *   <li>If the player is holding a <b>block</b> (not the Framing Tool) →
 *      <em>apply camouflage</em>: store the block ID as the camouflage material.</li>
 *   <li>If the player is holding the <b>Framing Tool</b> and the frame is
 *       camouflaged → <em>remove camouflage</em>: revert to empty.</li>
 *   <li>If the player is holding the <b>Framing Tool</b> and the frame is
 *       empty → cycle through or show a menu (future).</li>
 * </ol>
 * <p>
 * <b>Current state:</b> This class documents the interaction contract.
 * The {@link #onPlayerRightClick} method is ready to be wired into whatever
 * event system Hytale provides for player-block interaction.
 */
public final class FrameInteractionHandler {

    private FrameInteractionHandler() {
        // utility class — no instances
    }

    // ────────────────────────────────────────────────────────────
    //  Public API — call this from the Hytale interaction event
    // ────────────────────────────────────────────────────────────

    /**
     * Called when a player right-clicks on a block while holding an item.
     * <p>
     * <b>To wire this up:</b> Register this method as a listener for the
     * appropriate Hytale player-interact event.  The exact event class /
     * listener interface is not yet determined — replace the parameters
     * below with the actual event object once known.
     *
     * @param system         the active {@link FramingSystem} instance
     * @param packedPosition the packed chunk+local position of the clicked block
     * @param heldItemId     the block/item ID in the player's main hand
     *                       (e.g. {@code "Block_Wool_Yellow"}, {@code "Framing_Tool"})
     * @param clickedBlockId the block ID of the clicked block
     *                       (e.g. {@code "Frame_Empty_Slab"})
     * @return {@code true} if the interaction was handled (camouflage applied /
     *         removed), {@code false} if it should fall through to default behavior
     */
    public static boolean onPlayerRightClick(
            FramingSystem system,
            long packedPosition,
            String heldItemId,
            String clickedBlockId
    ) {
        // 1. Is the clicked block a frame at all?
        if (!FrameType.isFrameBlock(clickedBlockId)) {
            return false; // not our block — let other handlers process it
        }

        // 2. Determine what the player is holding
        boolean holdingFramingTool = "Framing_Tool".equals(heldItemId);

        if (holdingFramingTool) {
            // ── Framing Tool actions ──────────────────────────────

            // Check current camouflage state
            CamoData current = system.getCamouflage(packedPosition);
            if (current != null && current.hasCamouflage()) {
                // Remove camouflage
                system.removeCamouflage(packedPosition);
                log("Removed camouflage from frame at " + Long.toHexString(packedPosition));
                // Future: play a sound effect, spawn particles, swap block ID
                return true;
            }

            // Frame is empty and player used the tool — nothing to remove
            // Future: open a camouflage-selection UI or cycle through favourites
            log("Frame at " + Long.toHexString(packedPosition) + " has no camouflage to remove");
            return false;
        }

        // ── Block-held actions (apply camouflage) ────────────────

        // The held item IS a potential camouflage material
        // Future: verify the held item is a valid block (non-null BlockType)
        boolean success = system.applyCamouflage(packedPosition, heldItemId);
        if (success) {
            log("Camouflaged frame at " + Long.toHexString(packedPosition)
                    + " as '" + heldItemId + "'");
            // Future: play placement sound, swap block ID to visual variant
        }
        return success;
    }

    /**
     * Called when a frame block is broken — should be wired into the block
     * break event so we can drop the camouflage item.
     * <p>
     * <b>TODO:</b> Wire this into the block-break event once the Hytale API
     * is known.  Currently the {@link FramingSystem#cleanUpStaleFrames} loop
     * detects broken frames post-factum but does not drop the camo item.
     *
     * @param system         the active {@link FramingSystem}
     * @param packedPosition the position of the broken frame block
     * @return the camouflage block ID that was stored, or {@code null} if empty
     */
    @Nullable
    public static String onFrameBroken(FramingSystem system, long packedPosition) {
        CamoData data = system.getCamouflage(packedPosition);
        if (data == null || !data.hasCamouflage()) return null;

        String camoBlockId = data.camoBlockId();
        system.removeCamouflage(packedPosition);
        // Note: system.removeCamouflage() already logs the removal
        // Future: spawn the camouflage block item in the world at the break position
        return camoBlockId;
    }

    // ────────────────────────────────────────────────────────────
    //  Internal helpers
    // ────────────────────────────────────────────────────────────

    private static void log(String message) {
        HytaleLogger.getLogger().at(Level.INFO).log("[FrameInteraction] " + message);
    }
}
