package com.favasur.wrongyellow;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.BlockAccessor;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.logger.HytaleLogger;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongIterator;

import java.util.Random;
import java.util.logging.Level;

/**
 * Ticking system that drives the fluorescent flicker effect on
 * White Build Lightsource blocks.
 * <p>
 * Behavior:
 * - Most of the time the light stays at full brightness.
 * - Every 2–5 seconds it may enter a short flicker burst:
 *   rapidly cycling between DIMMED and OFF states.
 * - After the burst it returns to FULL.
 * - All timings include random variation for an organic feel.
 * <p>
 * The system discovers White Build Lightsource blocks by scanning
 * a few loaded chunks per tick (round-robin). Once discovered, it
 * tracks each block's flicker state using a position-indexed map.
 * Block swapping uses {@code BlockAccessor.setBlock(x, y, z, blockId)}.
 */
public class FlickeringTickingSystem extends TickingSystem<ChunkStore> {

    public static final String BLOCK_FULL = "Block_White_Build_Lightsource";
    public static final String BLOCK_DIM  = "Block_White_Build_Lightsource_Dimmed";
    public static final String BLOCK_OFF  = "Block_White_Build_Lightsource_Off";

    // State constants (mirrored from FlickeringLightComponent for convenience)
    private static final int STATE_FULL   = 0;
    private static final int STATE_DIMMED = 1;
    private static final int STATE_OFF    = 2;

    // Block ID strings for each state
    private static final String[] STATE_BLOCK_IDS = { BLOCK_FULL, BLOCK_DIM, BLOCK_OFF };

    // Chunks to scan per tick for block discovery
    private static final int CHUNKS_PER_TICK = 2;

    // Blocks scanned per chunk per tick during discovery
    private static final int Y_SLICE_SIZE = 16;

    private final Random random = new Random();

    // Maps a packed position (chunkIndex << 32 | localPos) -> flicker data
    // where localPos = (x & 0xF) | ((y & 0xFF) << 4) | ((z & 0xF) << 12)
    // and data encodes: bits 0-3 = currentState, bits 4-15 = ticksUntilChange,
    // bit 16 = isFlickering
    private final Long2IntOpenHashMap flickerMap = new Long2IntOpenHashMap();

    // Round-robin counters for chunk and Y-slice scanning
    private int scanChunkIndex = 0;
    private int scanYStart = 0;

    // Whether the system has logged its first tick
    private boolean hasLogged = false;

    public FlickeringTickingSystem() {
    }

    @Override
    public void tick(float deltaTime, int entityCount, Store<ChunkStore> store) {
        ChunkStore chunkStore = store.getExternalData();
        if (chunkStore == null) return;

        World world = chunkStore.getWorld();
        if (world == null) return;

        if (!hasLogged) {
            HytaleLogger.getLogger().at(Level.INFO).log(
                    "WrongYellow FlickerSystem ticking on world: " + world.getName());
            hasLogged = true;
        }

        // --- Step 1: Process all tracked blocks ---
        processTrackedBlocks(world, chunkStore);

        // --- Step 2: Scan a few chunks to discover new blocks ---
        discoverNewBlocks(world, chunkStore);
    }

    /**
     * Iterates over all tracked block positions and decides whether to
     * transition to a different light state. Uses {@code BlockAccessor.setBlock()}
     * to swap block types in the world.
     */
    private void processTrackedBlocks(World world, ChunkStore chunkStore) {
        LongSet chunkIndexes = chunkStore.getChunkIndexes();
        LongIterator iter = flickerMap.keySet().iterator();
        int maxProcessed = 50; // limit per tick to avoid lag spikes
        int processed = 0;

        while (iter.hasNext() && processed < maxProcessed) {
            long packedKey = iter.nextLong();
            int data = flickerMap.get(packedKey);
            processed++;

            // Decode the packed key
            long chunkIndex = packedKey >> 32;
            int localPos = (int) (packedKey & 0xFFFFFFFFL);
            int lx = localPos & 0xF;
            int ly = (localPos >> 4) & 0xFF;
            int lz = (localPos >> 12) & 0xF;

            // Decode the data
            int currentState = data & 0xF;
            int ticksUntilChange = (data >> 4) & 0xFFF;
            boolean isFlickering = ((data >> 16) & 1) == 1;

            // Decrement ticks
            if (ticksUntilChange > 0) {
                ticksUntilChange--;
                // Save updated ticks
                int newData = currentState | (ticksUntilChange << 4) | ((isFlickering ? 1 : 0) << 16);
                flickerMap.put(packedKey, newData);
                continue;
            }

            // Time for a state change
            String targetBlockId;
            int nextState;
            boolean nextIsFlickering;

            if (!isFlickering) {
                // Decide whether to start flickering
                if (random.nextFloat() < 0.08f) {
                    // Start flicker burst
                    nextState = STATE_DIMMED;
                    nextIsFlickering = true;
                    ticksUntilChange = random.nextInt(4) + 2;
                } else {
                    // Stay at full
                    nextState = STATE_FULL;
                    nextIsFlickering = false;
                    ticksUntilChange = random.nextInt(60) + 40; // ~2-5 seconds
                }
            } else {
                // Currently in a flicker burst
                nextState = getNextFlickerState(currentState);

                if (currentState == STATE_FULL && random.nextFloat() < 0.3f) {
                    // End the flicker burst
                    nextState = STATE_FULL;
                    nextIsFlickering = false;
                    ticksUntilChange = random.nextInt(200) + 100; // ~5-15 seconds cooldown
                } else {
                    nextIsFlickering = true;
                    ticksUntilChange = random.nextInt(4) + 1; // rapid transitions
                }
            }

            targetBlockId = STATE_BLOCK_IDS[nextState];

            // Get the chunk's absolute position
            int chunkX = (int) (chunkIndex >> 32);
            int chunkZ = (int) (chunkIndex & 0xFFFFFFFFL);
            int worldX = (chunkX << 4) | lx;
            int worldY = ly;
            int worldZ = (chunkZ << 4) | lz;

            // Swap the block via BlockAccessor
            BlockAccessor blockAccessor = world.getChunk(chunkIndex);
            if (blockAccessor != null) {
                blockAccessor.setBlock(worldX, worldY, worldZ, targetBlockId);
            }

            // Save updated state
            int newData = nextState | (ticksUntilChange << 4) | ((nextIsFlickering ? 1 : 0) << 16);
            flickerMap.put(packedKey, newData);

            // If back to FULL and not flickering, optionally stop tracking
            // to let the map shrink. Blocks will be re-discovered on next scan.
            if (nextState == STATE_FULL && !nextIsFlickering && random.nextFloat() < 0.1f) {
                flickerMap.remove(packedKey);
            }
        }
    }

    /**
     * Determines the next state in a flicker burst.
     * Cycles: FULL -> DIMMED -> OFF -> DIMMED -> FULL -> ...
     */
    private int getNextFlickerState(int currentState) {
        return switch (currentState) {
            case STATE_FULL -> STATE_DIMMED;
            case STATE_DIMMED -> random.nextBoolean() ? STATE_OFF : STATE_FULL;
            case STATE_OFF -> STATE_DIMMED;
            default -> STATE_FULL;
        };
    }

    /**
     * Scans a few loaded chunks each tick to discover White Build Lightsource
     * blocks and add them to the tracking map.
     */
    private void discoverNewBlocks(World world, ChunkStore chunkStore) {
        LongSet chunkIndexes = chunkStore.getChunkIndexes();
        if (chunkIndexes.isEmpty()) return;

        // Collect all chunk indexes into an array for indexed access
        long[] indexes = new long[chunkIndexes.size()];
        int idx = 0;
        LongIterator iter = chunkIndexes.iterator();
        while (iter.hasNext()) {
            indexes[idx++] = iter.nextLong();
        }

        if (indexes.length == 0) return;

        // Scan CHUNKS_PER_TICK chunks each tick (round-robin)
        for (int c = 0; c < CHUNKS_PER_TICK && scanChunkIndex < indexes.length; c++) {
            long chunkIndex = indexes[scanChunkIndex];
            scanChunkIndex++;
            if (scanChunkIndex >= indexes.length) {
                scanChunkIndex = 0;
                // When we wrap around, advance the Y slice too
                scanYStart = (scanYStart + Y_SLICE_SIZE) % 256;
            }

            BlockAccessor blockAccessor = world.getChunk(chunkIndex);
            if (blockAccessor == null) continue;

            // Scan a Y-slice of this chunk (Y_SLICE_SIZE blocks high)
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

                        if (BLOCK_FULL.equals(blockId)) {
                            // Discovered a full-brightness lightsource
                            int localPos = lx | (y << 4) | (lz << 12);
                            long packedKey = (chunkIndex << 32) | (localPos & 0xFFFFFFFFL);

                            if (!flickerMap.containsKey(packedKey)) {
                                int data = STATE_FULL
                                        | ((random.nextInt(100) + 20) << 4) // initial staggered ticks
                                        | (0 << 16); // not flickering
                                flickerMap.put(packedKey, data);
                            }
                        } else if (BLOCK_DIM.equals(blockId) || BLOCK_OFF.equals(blockId)) {
                            // Discovered a dim/off variant that somehow wasn't tracked
                            int localPos = lx | (y << 4) | (lz << 12);
                            long packedKey = (chunkIndex << 32) | (localPos & 0xFFFFFFFFL);

                            if (!flickerMap.containsKey(packedKey)) {
                                int initialState = BLOCK_DIM.equals(blockId) ? STATE_DIMMED : STATE_OFF;
                                int data = initialState
                                        | ((random.nextInt(10) + 2) << 4) // will change soon
                                        | (1 << 16); // is flickering (in mid-burst)
                                flickerMap.put(packedKey, data);
                            }
                        }
                    }
                }
            }
        }
    }
}
