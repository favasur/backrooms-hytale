package com.favasur.wrongyellow.frame;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.BlockAccessor;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.logger.HytaleLogger;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongIterator;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.logging.Level;

/**
 * Ticking system that manages the full FramedBlocks lifecycle in Hytale.
 * <p>
 * This is the Hytale equivalent of {@code FramedBlockEntity} (per-block state
 * storage) + the interaction logic from {@code IFramedBlock.handleUse()}
 * + the discovery / tracking loop of {@code AbstractFramedBlock}.
 * <p>
 * <h3>How it works</h3>
 * <ol>
 *   <li><b>Discovery</b> — Every tick the system scans loaded chunks for
 *       known frame block IDs (see {@link FrameType}).  Newly discovered
 *       frames are added to the {@link #camoMap}.</li>
 *   <li><b>Tracking</b> — Each frame block is tracked as a {@link CamoData}
 *       instance keyed by its packed chunk+local position.</li>
 *   <li><b>Clean-up</b> — If a tracked position no longer holds a frame
 *       block, the entry is removed from the map (the frame was broken).</li>
 *   <li><b>Camouflage</b> — Camouflage state is stored in the {@code CamoData}
 *       and may be queried / modified via {@link #applyCamouflage(long, String)},
 *       {@link #removeCamouflage(long)}, and {@link #getCamouflage(long)}.</li>
 *   <li><b>Future visuals</b> — When the block-swapping visual layer is
 *       added, it will plug in here: applying camo would also swap the
 *       block ID to a pre-registered camouflaged variant.</li>
 * </ol>
 * <p>
 * <h3>Threading</h3>
 * Like all {@link TickingSystem} instances, this runs on the server tick
 * thread.  The {@link #camoMap} is accessed only from {@link #tick(float, int, Store)}.
 */
public class FramingSystem extends TickingSystem<ChunkStore> {

    // ---- configuration ----------------------------------------------------

    /** Chunks to scan per tick during discovery (lightweight). */
    private static final int CHUNKS_PER_TICK = 2;

    /** Y-slice size per chunk scan pass. */
    private static final int Y_SLICE_SIZE = 16;

    /** Maximum tracked frames to process per tick (clean-up / state check). */
    private static final int MAX_PROCESSED_PER_TICK = 200;

    // ---- state ------------------------------------------------------------

    /** The master state map: packedPosition → CamoData. */
    private final Long2ObjectOpenHashMap<CamoData> camoMap = new Long2ObjectOpenHashMap<>();

    // Round-robin scan cursors (same pattern as FlickeringTickingSystem)
    private int scanChunkIndex = 0;
    private int scanYStart = 0;

    /** Whether we've logged the first-tick bootstrap message. */
    private boolean hasLogged = false;

    /** Global tick counter — used for auto-camouflage cooldown. */
    private int currentTick = 0;

    /** Minimum ticks between auto-camo checks for the same frame. */
    private static final int AUTO_CAMO_COOLDOWN_TICKS = 40; // ~2 seconds

    /** Interval between saving dirty data to disk. */
    private static final int SAVE_INTERVAL_TICKS = 200; // ~10 seconds

    // ---- interaction queue -------------------------------------------------

    /**
     * Queued player interactions waiting to be processed.
     * FrameInteractionHandler pushes entries here; processInteractionQueue
     * drains them and calls the handler's logic.
     */
    private final Queue<QueuedInteraction> interactionQueue = new ArrayDeque<>();

    /**
     * A pending player right-click interaction on a frame block.
     */
    public static final class QueuedInteraction {
        public final long packedPosition;
        public final String heldItemId;
        public final String clickedBlockId;

        public QueuedInteraction(long packedPosition, String heldItemId, String clickedBlockId) {
            this.packedPosition = packedPosition;
            this.heldItemId = heldItemId;
            this.clickedBlockId = clickedBlockId;
        }
    }

    // ---- persistent store --------------------------------------------------

    private final CamoDataStore dataStore = CamoDataStore.getInstance();

    // ---- construction -----------------------------------------------------

    public FramingSystem() {
    }

    // ---- tick -------------------------------------------------------------

    @Override
    public void tick(float deltaTime, int entityCount, Store<ChunkStore> store) {
        ChunkStore chunkStore = store.getExternalData();
        if (chunkStore == null) return;
        World world = chunkStore.getWorld();
        if (world == null) return;

        if (!hasLogged) {
            HytaleLogger.getLogger().at(Level.INFO).log(
                    "FramingSystem active on world: " + world.getName());
            hasLogged = true;
        }

        // 1. Discover newly-placed or newly-loaded frame blocks
        discoverNewFrames(world, chunkStore);

        // 2. Verify tracked frames still exist; remove stale entries
        cleanUpStaleFrames(world);

        // 3. Process queued player interactions (auto-camouflage + FrameInteractionHandler)
        currentTick++;
        processInteractionQueue(world);

        // 4. Periodically persist camouflage data to disk
        if (currentTick % SAVE_INTERVAL_TICKS == 0) {
            dataStore.flush();
        }
    }

    /**
     * Checks tracked empty frames for adjacent camouflage blocks and
     * auto-applies camouflage.  Also calls into
     * {@link FrameInteractionHandler} for the full interaction contract
     * (FramingTool apply/remove, held-block detection) once the Hytale
     * player-interact event API is available to provide item/click context.
     * <p>
     * <b>Auto-camouflage mechanic:</b> When a player places a solid block
     * (e.g. Block_Wool_Yellow) adjacent to an empty frame, the system
     * detects it and applies camouflage within ~1 second.  The camouflage
     * persists even after the source block is removed.
     * <p>
     * <b>To remove camouflage:</b> Break and re-place the frame block,
     * or (future) use the FramingTool when player right-click events
     * are wired via FrameInteractionHandler.
     */
    /**
     * Process interactions: drain the player interaction queue (wired to
     * FrameInteractionHandler) AND auto-camouflage from adjacent blocks.
     * <p>
     * <b>Queue interactions</b> — When Hytale's player right-click event is
     * hooked, call {@link #queueInteraction(long, String, String)}.  The
     * queued actions are processed here via FrameInteractionHandler.
     * <p>
     * <b>Auto-camouflage</b> — Empty frames scan adjacent blocks for valid
     * camouflage materials.  Place a block next to an empty frame and it
     * auto-camouflages within ~1 second.
     */
    private void processInteractionQueue(World world) {
        // 1. Drain the player interaction queue → FrameInteractionHandler
        QueuedInteraction qi;
        while ((qi = interactionQueue.poll()) != null) {
            FrameInteractionHandler.onPlayerRightClick(
                    this, qi.packedPosition, qi.heldItemId, qi.clickedBlockId);
        }

        // 2. Auto-camouflage: check empty frames for adjacent valid blocks
        if (camoMap.isEmpty()) return;

        int processed = 0;
        LongIterator iter = camoMap.keySet().iterator();

        while (iter.hasNext() && processed < MAX_PROCESSED_PER_TICK) {
            long packedKey = iter.nextLong();
            processed++;

            CamoData data = camoMap.get(packedKey);
            if (data == null || data.hasCamouflage()) continue; // only process empty frames

            // Cooldown: skip frames checked within the last ~2 seconds
            if (currentTick - data.lastInteractionTick() < AUTO_CAMO_COOLDOWN_TICKS) continue;

            checkAdjacentCamo(world, packedKey, data);
            data.setLastInteractionTick(currentTick);
        }
    }

    /**
     * Queue a player right-click interaction on a frame block.
     * Called by whatever Hytale event system is wired in.
     *
     * @param packedPosition packed chunk+local position of the clicked block
     * @param heldItemId     the block/item ID the player is holding
     * @param clickedBlockId the block ID that was clicked
     */
    public void queueInteraction(long packedPosition, String heldItemId, String clickedBlockId) {
        interactionQueue.add(new QueuedInteraction(packedPosition, heldItemId, clickedBlockId));
    }

    /**
     * Checks the 6 adjacent positions of an empty frame for valid
     * camouflage materials and auto-applies if found.
     * <p>
     * This gives players a practical interaction: place a frame block,
     * then place a solid block next to it — the frame auto-camouflages.
     */
    private void checkAdjacentCamo(World world, long packedKey, CamoData data) {
        long chunkIndex = CamoData.unpackChunkIndex(packedKey);
        int localPos = CamoData.unpackLocalPos(packedKey);
        int lx = localPos & 0xF;
        int ly = (localPos >> 4) & 0xFF;
        int lz = (localPos >> 12) & 0xF;
        int chunkX = (int) (chunkIndex >> 32);
        int chunkZ = (int) (chunkIndex & 0xFFFFFFFFL);
        int wx = (chunkX << 4) | lx;
        int wy = ly;
        int wz = (chunkZ << 4) | lz;

        // 6 adjacent offsets: -Y, +Y, -X, +X, -Z, +Z
        int[][] offsets = {
            {0, -1, 0}, {0, 1, 0},
            {-1, 0, 0}, {1, 0, 0},
            {0, 0, -1}, {0, 0, 1}
        };

        for (int[] off : offsets) {
            int ax = wx + off[0];
            int ay = wy + off[1];
            int az = wz + off[2];

            // Resolve chunk for the adjacent position
            int adjChunkX = ax >> 4;
            int adjChunkZ = az >> 4;
            long adjChunkIndex = ((long) adjChunkX << 32) | (adjChunkZ & 0xFFFFFFFFL);

            BlockAccessor adjAccessor = world.getChunk(adjChunkIndex);
            if (adjAccessor == null) continue;

            String adjBlockId = adjAccessor.getBlockType(ax, ay, az).getId();
            if (adjBlockId == null || adjBlockId.isEmpty() || adjBlockId.equals("air")) continue;

            // Skip frame blocks, vanilla utility blocks
            if (adjBlockId.startsWith("Frame_") || adjBlockId.startsWith("Build_")) continue;

            // Apply! This is a valid camouflage material
            data.applyCamouflage(adjBlockId);
            HytaleLogger.getLogger().at(Level.INFO).log(
                    "FramingSystem: auto-camo " + data.frameType().name() +
                    " at [" + wx + "," + wy + "," + wz + "]" +
                    " as '" + adjBlockId + "'");
            // Future: swap block ID to camouflaged variant for visual feedback
            return; // first match only
        }
    }

    // ---- discovery --------------------------------------------------------

    /**
     * Scans loaded chunks for frame blocks (empty or camouflaged) that are
     * not yet in the tracking map and adds them.
     */
    private void discoverNewFrames(World world, ChunkStore chunkStore) {
        LongSet chunkIndexes = chunkStore.getChunkIndexes();
        if (chunkIndexes.isEmpty()) return;

        long[] indexes = new long[chunkIndexes.size()];
        int idx = 0;
        LongIterator iter = chunkIndexes.iterator();
        while (iter.hasNext()) {
            indexes[idx++] = iter.nextLong();
        }
        if (indexes.length == 0) return;

        for (int c = 0; c < CHUNKS_PER_TICK && scanChunkIndex < indexes.length; c++) {
            long chunkIndex = indexes[scanChunkIndex];
            scanChunkIndex++;
            if (scanChunkIndex >= indexes.length) {
                scanChunkIndex = 0;
                scanYStart = (scanYStart + Y_SLICE_SIZE) % 256;
            }

            BlockAccessor blockAccessor = world.getChunk(chunkIndex);
            if (blockAccessor == null) continue;

            int chunkX = (int) (chunkIndex >> 32);
            int chunkZ = (int) (chunkIndex & 0xFFFFFFFFL);
            int baseX = chunkX << 4;
            int baseZ = chunkZ << 4;

            for (int dy = 0; dy < Y_SLICE_SIZE && (scanYStart + dy) < 256; dy++) {
                int y = scanYStart + dy;
                for (int lx = 0; lx < 16; lx++) {
                    for (int lz = 0; lz < 16; lz++) {
                        int wx = baseX | lx;
                        int wz = baseZ | lz;
                        String blockId = blockAccessor.getBlockType(wx, y, wz).getId();

                        // Check if this is a known frame block
                        FrameType frameType = FrameType.fromBlockId(blockId);
                        if (frameType == null) continue; // not a frame block

                        int localPos = lx | (y << 4) | (lz << 12);
                        long packedKey = CamoData.packPosition(chunkIndex, localPos);

                        // Only add if not already tracked
                        if (!camoMap.containsKey(packedKey)) {
                            CamoData data = new CamoData(packedKey, frameType);

                            // Restore saved camouflage state (blockstate-like persistence)
                            String savedCamo = dataStore.getCamo(packedKey);
                            if (savedCamo != null && !savedCamo.isEmpty()) {
                                data.applyCamouflage(savedCamo);
                                HytaleLogger.getLogger().at(Level.FINE).log(
                                        "FramingSystem: restored camo for " + frameType.name() +
                                        " at [" + wx + ", " + y + ", " + wz + "]" +
                                        " as '" + savedCamo + "'");
                            }

                            camoMap.put(packedKey, data);

                            HytaleLogger.getLogger().at(Level.FINE).log(
                                    "FramingSystem: discovered " + frameType.name() +
                                    " at [" + wx + ", " + y + ", " + wz + "]");
                        }
                    }
                }
            }
        }
    }

    // ---- clean-up ---------------------------------------------------------

    /**
     * Iterates over tracked frame positions and removes entries where the
     * block is no longer a frame block (i.e., it was broken or replaced).
     */
    private void cleanUpStaleFrames(World world) {
        if (camoMap.isEmpty()) return;

        int processed = 0;
        LongIterator iter = camoMap.keySet().iterator();

        while (iter.hasNext() && processed < MAX_PROCESSED_PER_TICK) {
            long packedKey = iter.nextLong();
            processed++;

            CamoData data = camoMap.get(packedKey);
            if (data == null) continue;

            // Resolve world position from packed key
            long chunkIndex = CamoData.unpackChunkIndex(packedKey);
            int localPos = CamoData.unpackLocalPos(packedKey);
            int lx = localPos & 0xF;
            int ly = (localPos >> 4) & 0xFF;
            int lz = (localPos >> 12) & 0xF;

            int chunkX = (int) (chunkIndex >> 32);
            int chunkZ = (int) (chunkIndex & 0xFFFFFFFFL);
            int worldX = (chunkX << 4) | lx;
            int worldY = ly;
            int worldZ = (chunkZ << 4) | lz;

            BlockAccessor blockAccessor = world.getChunk(chunkIndex);
            if (blockAccessor == null) {
                // Chunk unloaded — remove from tracking
                iter.remove();
                continue;
            }

            String currentBlockId = blockAccessor.getBlockType(worldX, worldY, worldZ).getId();

            // If the block is no longer any kind of frame, remove it
            if (!FrameType.isFrameBlock(currentBlockId)) {
                iter.remove();

                if (data.hasCamouflage()) {
                    HytaleLogger.getLogger().at(Level.FINE).log(
                            "FramingSystem: frame broken at [" + worldX + ", " + worldY + ", " + worldZ +
                            "] — was camouflaged as '" + data.camoBlockId() + "'");
                    // onFrameBroken calls removeCamouflage() which handles store cleanup
                    FrameInteractionHandler.onFrameBroken(this, packedKey);
                }
                // Note: empty frames have no persistent state to clean up
            }
        }
    }

    // ---- camouflage API (called from interaction handler / tool) ----------

    /**
     * Apply camouflage to a tracked frame block.
     *
     * @param packedKey   the packed chunk+local position of the frame
     * @param camoBlockId the block ID of the camouflage material
     * @return {@code true} if camouflage was applied, {@code false} if the
     *         position is not tracked or already has this camouflage
     */
    public boolean applyCamouflage(long packedKey, String camoBlockId) {
        CamoData data = camoMap.get(packedKey);
        if (data == null) return false;
        if (camoBlockId.equals(data.camoBlockId())) return false; // already applied

        data.applyCamouflage(camoBlockId);
        dataStore.setCamo(packedKey, camoBlockId);
        dataStore.flush(); // immediate persist for blockstate-like durability
        HytaleLogger.getLogger().at(Level.INFO).log(
                "FramingSystem: camouflaged " + data.frameType().name() +
                " as '" + camoBlockId + "'");

        // Future: swap block ID to camouflaged variant for visual feedback
        return true;
    }

    /**
     * Remove camouflage from a tracked frame block, reverting it to empty.
     *
     * @param packedKey the packed chunk+local position of the frame
     * @return {@code true} if camouflage was removed (was present), {@code false}
     *         if the position is not tracked or had no camouflage
     */
    public boolean removeCamouflage(long packedKey) {
        CamoData data = camoMap.get(packedKey);
        if (data == null) return false;
        if (!data.hasCamouflage()) return false;

        String oldCamo = data.camoBlockId();
        data.removeCamouflage();
        dataStore.removeCamo(packedKey);
        dataStore.flush(); // immediate persist
        HytaleLogger.getLogger().at(Level.INFO).log(
                "FramingSystem: removed camouflage '" + oldCamo + "' from " +
                data.frameType().name());

        // Future: swap block ID back to empty frame variant
        return true;
    }

    /**
     * Get the camouflage data for a tracked frame block.
     *
     * @param packedKey the packed chunk+local position
     * @return the CamoData if tracked, or {@code null} if the position is not
     *         known to be a frame block
     */
    @Nullable
    public CamoData getCamouflage(long packedKey) {
        return camoMap.get(packedKey);
    }

    /**
     * Returns {@code true} if the given position is tracked as a frame block.
     */
    public boolean isTracked(long packedKey) {
        return camoMap.containsKey(packedKey);
    }

    /**
     * Total number of frame blocks currently tracked across all loaded chunks.
     */
    public int trackedCount() {
        return camoMap.size();
    }
}
