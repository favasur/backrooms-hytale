package com.favasur.wrongyellow.frame;

import javax.annotation.Nullable;

/**
 * Per-block camouflage state — the Hytale equivalent of
 * {@code FramedBlockEntity}'s stored data in the FramedBlocks mod.
 * <p>
 * Each instance is keyed by a <em>packed position</em> (see
 * {@link #packPosition(long, int)} / {@link #unpackPosition(long)})
 * and stored in the {@link FramingSystem}'s state map.
 * <p>
 * When {@link #camoBlockId} is {@code null} the frame is "empty"
 * (showing its default grid texture).  Otherwise it is camouflaged
 * with the visual identity of the referenced block.
 * <p>
 * Future flags (glowing, intangible, etc.) mirror the FramedBlocks
 * {@code FramedBlockEntity.StateFlag} enum and its bit-field packing.
 */
public class CamoData {

    // ---- constants for flag bit-packing (future use) ----------------------
    public static final int FLAG_GLOWING    = 1 << 0;
    public static final int FLAG_INTANGIBLE = 1 << 1;
    public static final int FLAG_REINFORCED = 1 << 2;
    public static final int FLAG_EMISSIVE   = 1 << 3;

    // ---- position encoding helpers ----------------------------------------
    // Pack a chunk-index (long) and local-position (int) into a single long key.
    // Matches the encoding used in FlickeringTickingSystem for consistency.
    //   chunkIndex holds the chunk's global index.
    //   localPos   encodes (lx | (ly << 4) | (lz << 12)).

    /**
     * Pack a chunk index and local block position into a single long key.
     */
    public static long packPosition(long chunkIndex, int localPos) {
        return (chunkIndex << 32) | (localPos & 0xFFFFFFFFL);
    }

    /**
     * Extract the chunk index from a packed position.
     */
    public static long unpackChunkIndex(long packedKey) {
        return packedKey >> 32;
    }

    /**
     * Extract the local position from a packed position.
     */
    public static int unpackLocalPos(long packedKey) {
        return (int) (packedKey & 0xFFFFFFFFL);
    }

    // ---- instance fields --------------------------------------------------

    /** Packed key: (chunkIndex << 32) | localPos. */
    private final long packedPosition;

    /** The frame shape type (SLAB, PANEL, etc.). */
    private final FrameType frameType;

    /**
     * The block ID of the camouflage material, or {@code null} if this
     * frame is empty (un-camouflaged).
     */
    @Nullable
    private String camoBlockId;

    /** Bit-field of boolean flags (glowing, intangible, etc.). */
    private int flags;

    /** Tick counter for cooldown — prevents re-checking adjacent blocks every tick. */
    private int lastInteractionTick = 0;

    // ---- construction -----------------------------------------------------

    /**
     * Create a new CamoData instance for an empty (un-camouflaged) frame.
     */
    public CamoData(long packedPosition, FrameType frameType) {
        this.packedPosition = packedPosition;
        this.frameType = frameType;
        this.camoBlockId = null;
        this.flags = 0;
    }

    /**
     * Full constructor for deserialisation / testing.
     */
    public CamoData(long packedPosition, FrameType frameType,
                    @Nullable String camoBlockId, int flags) {
        this.packedPosition = packedPosition;
        this.frameType = frameType;
        this.camoBlockId = camoBlockId;
        this.flags = flags;
    }

    // ---- accessors --------------------------------------------------------

    public long packedPosition() {
        return packedPosition;
    }

    public FrameType frameType() {
        return frameType;
    }

    @Nullable
    public String camoBlockId() {
        return camoBlockId;
    }

    public boolean hasCamouflage() {
        return camoBlockId != null;
    }

    // ---- camouflage mutation ----------------------------------------------

    /**
     * Apply a camouflage material to this frame.
     *
     * @param camoBlockId the block ID of the camouflage material (non-null)
     */
    public void applyCamouflage(String camoBlockId) {
        this.camoBlockId = camoBlockId;
    }

    /**
     * Remove camouflage from this frame, reverting it to the "empty" state.
     */
    public void removeCamouflage() {
        this.camoBlockId = null;
    }

    // ---- flags (future: glowing, intangible, etc.) ------------------------

    public boolean hasFlag(int flag) {
        return (flags & flag) != 0;
    }

    public void setFlag(int flag, boolean value) {
        if (value) {
            flags |= flag;
        } else {
            flags &= ~flag;
        }
    }

    public int flags() {
        return flags;
    }

    /** The last server tick when adjacent blocks were scanned for auto-camouflage. */
    public int lastInteractionTick() {
        return lastInteractionTick;
    }

    /** Set the tick counter for auto-camouflage cooldown. */
    public void setLastInteractionTick(int tick) {
        this.lastInteractionTick = tick;
    }

    // ---- object -----------------------------------------------------------

    @Override
    public String toString() {
        return "CamoData{" +
                "pos=" + Long.toHexString(packedPosition) +
                ", type=" + frameType.name() +
                ", camo=" + (camoBlockId != null ? camoBlockId : "<empty>") +
                ", flags=" + Integer.toBinaryString(flags) +
                '}';
    }
}
