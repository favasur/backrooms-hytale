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

    /** Only tracks the dedicated flickering variant block. */
    public static final String BLOCK_FLICKER = "Block_White_Build_Lightsource_Flickering";
    /** The OFF variant used during flicker bursts. */
    public static final String BLOCK_OFF     = "Block_White_Build_Lightsource_Off";

    // State constants: only ON (FULL) and OFF
    private static final int STATE_ON  = 0;
    private static final int STATE_OFF = 1;

    // Block ID strings for each state
    private static final String[] STATE_BLOCK_IDS = { BLOCK_FLICKER, BLOCK_OFF };

    // Chunks to scan per tick for block discovery
    private static final int CHUNKS_PER_TICK = 2;

    // Blocks scanned per chunk per tick during discovery
    private static final int Y_SLICE_SIZE = 16;

    private final Random random = new Random();

    // Maps a packed position (chunkIndex << 32 | localPos) -> flicker data
    private final Long2IntOpenHashMap flickerMap = new Long2IntOpenHashMap();

    // Round-robin counters
    private int scanChunkIndex = 0;
    private int scanYStart = 0;

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
                    "Backrooms FlickerSystem ticking on world: " + world.getName());
            hasLogged = true;
        }

        processTrackedBlocks(world);
        discoverNewBlocks(world, chunkStore);
    }

    /**
     * Rapid ON↔OFF cycling with realistic chaos.
     * The buzz sound on BLOCK_FLICKER (ON) turns on/off naturally with block swaps.
     * Vanilla Block_White_Build_Lightsource is NEVER tracked — it stays static.
     */
    private void processTrackedBlocks(World world) {
        LongIterator iter = flickerMap.keySet().iterator();
        int maxProcessed = 50;
        int processed = 0;

        while (iter.hasNext() && processed < maxProcessed) {
            long packedKey = iter.nextLong();
            int data = flickerMap.get(packedKey);
            processed++;

            long chunkIndex = packedKey >> 32;
            int localPos = (int) (packedKey & 0xFFFFFFFFL);
            int lx = localPos & 0xF;
            int ly = (localPos >> 4) & 0xFF;
            int lz = (localPos >> 12) & 0xF;

            int currentState = data & 0xF;
            int ticksUntilChange = (data >> 4) & 0xFFF;
            boolean isFlickering = ((data >> 16) & 1) == 1;

            if (ticksUntilChange > 0) {
                ticksUntilChange--;
                flickerMap.put(packedKey,
                        currentState | (ticksUntilChange << 4) | ((isFlickering ? 1 : 0) << 16));
                continue;
            }

            int nextState;
            boolean nextIsFlickering;

            if (!isFlickering) {
                // Brief pause — high chance to resume chaotic flickering
                if (random.nextFloat() < 0.5f) {
                    nextState = STATE_OFF;
                    nextIsFlickering = true;
                    ticksUntilChange = random.nextInt(3) + 1;
                } else {
                    nextState = STATE_ON;
                    nextIsFlickering = false;
                    ticksUntilChange = random.nextInt(15) + 8; // ~0.5-1 sec pause
                }
            } else {
                // Rapid ON↔OFF cycling
                nextState = (currentState == STATE_ON) ? STATE_OFF : STATE_ON;

                if (nextState == STATE_ON) {
                    // Just cycled back to ON — chance to end the burst
                    if (random.nextFloat() < 0.4f) {
                        nextIsFlickering = false;
                        ticksUntilChange = random.nextInt(20) + 10; // ~0.5-1.5 sec rest
                    } else {
                        nextIsFlickering = true;
                        ticksUntilChange = random.nextInt(4) + 1; // 1-4 ticks
                    }
                } else {
                    // Going OFF — continue burst
                    nextIsFlickering = true;
                    ticksUntilChange = random.nextInt(3) + 1; // 1-3 ticks
                }
            }

            int chunkX = (int) (chunkIndex >> 32);
            int chunkZ = (int) (chunkIndex & 0xFFFFFFFFL);
            int worldX = (chunkX << 4) | lx;
            int worldY = ly;
            int worldZ = (chunkZ << 4) | lz;

            BlockAccessor blockAccessor = world.getChunk(chunkIndex);
            if (blockAccessor != null) {
                blockAccessor.setBlock(worldX, worldY, worldZ,
                        STATE_BLOCK_IDS[nextState]);
            }

            flickerMap.put(packedKey,
                    nextState | (ticksUntilChange << 4) | ((nextIsFlickering ? 1 : 0) << 16));
        }
    }

    /**
     * Scans loaded chunks for the flickering variant block only.
     * The vanilla Block_White_Build_Lightsource is never discovered — it stays static.
     */
    private void discoverNewBlocks(World world, ChunkStore chunkStore) {
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

                        if (BLOCK_FLICKER.equals(blockId)) {
                            int localPos = lx | (y << 4) | (lz << 12);
                            long packedKey = (chunkIndex << 32) | (localPos & 0xFFFFFFFFL);
                            if (!flickerMap.containsKey(packedKey)) {
                                int data = STATE_ON
                                        | ((random.nextInt(20) + 5) << 4)
                                        | (0 << 16);
                                flickerMap.put(packedKey, data);
                            }
                        } else if (BLOCK_OFF.equals(blockId)) {
                            // Discovered an OFF variant mid-flicker
                            int localPos = lx | (y << 4) | (lz << 12);
                            long packedKey = (chunkIndex << 32) | (localPos & 0xFFFFFFFFL);
                            if (!flickerMap.containsKey(packedKey)) {
                                int data = STATE_OFF
                                        | ((random.nextInt(6) + 2) << 4)
                                        | (1 << 16);
                                flickerMap.put(packedKey, data);
                            }
                        }
                    }
                }
            }
        }
    }
}
