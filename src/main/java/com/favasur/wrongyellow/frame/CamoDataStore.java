package com.favasur.wrongyellow.frame;

import com.hypixel.hytale.logger.HytaleLogger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Persistent store for frame camouflage data — the Hytale equivalent of
 * Minecraft's chunk-based block entity storage (like blockstates in saved
 * chunks).
 * <p>
 * Stores a map of packedPosition → camoBlockId in a JSON file on disk.
 * Data survives server restarts and chunk load/unload cycles.
 * <p>
 * The store is a simple key-value file written to the plugin's data directory:
 * <pre>plugins/Backrooms/frame_camo_data.json</pre>
 * <p>
 * <h3>How it works</h3>
 * <ol>
 *   <li><b>Load on startup</b> — {@link #load()} reads all entries from the
 *       JSON file into an in-memory map.</li>
 *   <li><b>Restore on discovery</b> — When {@link FramingSystem} discovers a
 *       frame block, it checks the store for saved camouflage data and
 *       restores it.</li>
 *   <li><b>Save on change</b> — When camouflage is applied or removed,
 *       the store is updated and flushed to disk.</li>
 *   <li><b>Remove on break</b> — When a frame block is broken, the entry
 *       is deleted from the store.</li>
 * </ol>
 * <p>
 * This provides blockstate-like persistence without requiring Hytale's
 * built-in block component system (which doesn't expose a public API for
 * custom per-block data in the current SDK).
 */
public class CamoDataStore {

    // ────────────────────────────────────────────────────────────
    //  Configuration
    // ────────────────────────────────────────────────────────────

    private static final String DATA_FILE_NAME = "frame_camo_data.json";
    private static final String DATA_DIR = "plugins/Backrooms";

    /** In-memory store: packedKey (as hex string) → camoBlockId. */
    private final Map<String, String> data = new HashMap<>();

    /** Path to the JSON data file. */
    private final Path filePath;

    /** Whether the store has been modified since last save. */
    private boolean dirty = false;

    // ────────────────────────────────────────────────────────────
    //  Singleton
    // ────────────────────────────────────────────────────────────

    private static CamoDataStore instance;

    /**
     * Get the global store instance, loading data from disk if needed.
     */
    public static synchronized CamoDataStore getInstance() {
        if (instance == null) {
            instance = new CamoDataStore();
            instance.load();
        }
        return instance;
    }

    /**
     * Reset the singleton (for testing / reload).
     */
    public static synchronized void reset() {
        instance = null;
    }

    // ────────────────────────────────────────────────────────────
    //  Construction
    // ────────────────────────────────────────────────────────────

    private CamoDataStore() {
        // Determine the data file path relative to the server root
        String serverDir = System.getProperty("user.dir", ".");
        this.filePath = Paths.get(serverDir, DATA_DIR, DATA_FILE_NAME);
    }

    /**
     * Package-private constructor for testing with a custom path.
     */
    CamoDataStore(Path testPath) {
        this.filePath = testPath;
    }

    // ────────────────────────────────────────────────────────────
    //  Public API
    // ────────────────────────────────────────────────────────────

    /**
     * Get the saved camouflage block ID for a packed position.
     *
     * @param packedPosition the packed chunk+local position
     * @return the camo block ID, or {@code null} if not saved
     */
    @Nullable
    public String getCamo(long packedPosition) {
        return data.get(Long.toHexString(packedPosition));
    }

    /**
     * Store camouflage for a packed position.  Marks the store as dirty;
     * call {@link #flush()} to persist.
     *
     * @param packedPosition the packed chunk+local position
     * @param camoBlockId    the camouflage block ID, or {@code null} to clear
     */
    public void setCamo(long packedPosition, @Nullable String camoBlockId) {
        String key = Long.toHexString(packedPosition);
        if (camoBlockId == null) {
            data.remove(key);
        } else {
            data.put(key, camoBlockId);
        }
        dirty = true;
    }

    /**
     * Remove camouflage data for a packed position.
     *
     * @param packedPosition the packed chunk+local position
     */
    public void removeCamo(long packedPosition) {
        String key = Long.toHexString(packedPosition);
        if (data.remove(key) != null) {
            dirty = true;
        }
    }

    /**
     * Returns {@code true} if the store has unsaved changes.
     */
    public boolean isDirty() {
        return dirty;
    }

    // ────────────────────────────────────────────────────────────
    //  Persistence
    // ────────────────────────────────────────────────────────────

    /**
     * Load all camouflage data from the JSON file on disk.
     * Called once at plugin startup.
     */
    public void load() {
        data.clear();
        if (!Files.exists(filePath)) {
            HytaleLogger.getLogger().at(Level.INFO).log(
                    "CamoDataStore: no existing data file at " + filePath);
            return;
        }

        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = reader.read(buf)) != -1) {
                sb.append(buf, 0, n);
            }
            parseJson(sb.toString());
            HytaleLogger.getLogger().at(Level.INFO).log(
                    "CamoDataStore: loaded " + data.size() + " entries from " + filePath);
        } catch (IOException e) {
            HytaleLogger.getLogger().at(Level.SEVERE).log(
                    "CamoDataStore: failed to load: " + e.getMessage());
        }
    }

    /**
     * Write all camouflage data to the JSON file on disk.
     * Call after any state change, or periodically from the tick loop.
     */
    public synchronized void flush() {
        if (!dirty) return;

        try {
            // Ensure the data directory exists
            Files.createDirectories(filePath.getParent());

            // Write to a temp file, then atomically rename
            Path tmpFile = filePath.resolveSibling(filePath.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(tmpFile, StandardCharsets.UTF_8)) {
                writer.write(toJson());
                writer.flush();
            }
            Files.move(tmpFile, filePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

            dirty = false;
            HytaleLogger.getLogger().at(Level.FINE).log(
                    "CamoDataStore: flushed " + data.size() + " entries");
        } catch (IOException e) {
            HytaleLogger.getLogger().at(Level.SEVERE).log(
                    "CamoDataStore: failed to flush: " + e.getMessage());
        }
    }

    /**
     * Total number of entries in the store.
     */
    public int size() {
        return data.size();
    }

    // ────────────────────────────────────────────────────────────
    //  JSON parsing (lightweight, no dependency)
    // ────────────────────────────────────────────────────────────

    /**
     * Parse a simple JSON object: {"hexKey": "blockId", ...}
     */
    private void parseJson(String json) {
        // Very simple parser — assumes well-formed output from toJson()
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) return;

        String inner = json.substring(1, json.length() - 1).trim();
        if (inner.isEmpty()) return;

        // Split on commas, but not inside strings
        int depth = 0;
        int start = 0;
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '"') depth ^= 1; // toggle: 0 = outside string, 1 = inside
            if (c == ',' && depth == 0) {
                parseEntry(inner.substring(start, i));
                start = i + 1;
            }
        }
        if (start < inner.length()) {
            parseEntry(inner.substring(start));
        }
    }

    private void parseEntry(String entry) {
        entry = entry.trim();
        int colon = entry.indexOf(':');
        if (colon < 0) return;

        String key = unquote(entry.substring(0, colon).trim());
        String value = unquote(entry.substring(colon + 1).trim());
        if (key != null && value != null && !value.equals("null")) {
            data.put(key, value);
        }
    }

    @Nullable
    private static String unquote(String s) {
        if (s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    /**
     * Serialize the map to a compact JSON object string.
     */
    private String toJson() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> e : data.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append('"').append(e.getKey()).append("\":\"")
              .append(e.getValue().replace("\\", "\\\\").replace("\"", "\\\""))
              .append('"');
        }
        sb.append('}');
        return sb.toString();
    }
}
