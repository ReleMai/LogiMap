import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/**
 * WorldSaveManager - Handles saving and loading of game worlds.
 * 
 * Save format:
 * - Compressed JSON-like format for efficiency
 * - Stores world seed, name, structures, and player state
 */
public class WorldSaveManager {
    
    // Save directory
    private static final String SAVE_DIR = "saves";
    private static final String SAVE_EXTENSION = ".logimap";
    
    /**
     * Saves the current world state to a file.
     */
    public static boolean saveWorld(DemoWorld world, String worldName, int viewX, int viewY, double zoom) {
        try {
            // Ensure save directory exists
            Files.createDirectories(Paths.get(SAVE_DIR));
            
            // Create save data
            SaveData data = new SaveData();
            data.version = 1;
            data.worldName = worldName;
            data.seed = world.getSeed();
            data.startX = world.getStartX();
            data.startY = world.getStartY();
            data.viewX = viewX;
            data.viewY = viewY;
            data.zoom = zoom;
            data.saveTime = System.currentTimeMillis();
            
            // Save structure positions
            data.structures = new ArrayList<>();
            for (MapStructure structure : world.getStructures()) {
                StructureSaveData ssd = new StructureSaveData();
                ssd.type = structure.getClass().getSimpleName();
                ssd.name = structure.getName();
                ssd.gridX = structure.getGridX();
                ssd.gridY = structure.getGridY();
                ssd.population = structure.getPopulation();
                ssd.productivity = structure.getProductivity();
                
                if (structure instanceof Town) {
                    ssd.isMajor = ((Town) structure).isMajor();
                }
                
                data.structures.add(ssd);
            }
            
            // Generate filename from world name
            String filename = sanitizeFilename(worldName) + SAVE_EXTENSION;
            Path savePath = Paths.get(SAVE_DIR, filename);
            
            // Write compressed data
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new GZIPOutputStream(new FileOutputStream(savePath.toFile())))) {
                oos.writeObject(data);
            }
            
            System.out.println("World saved: " + savePath);
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to save world: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Loads a world from a save file.
     */
    public static LoadResult loadWorld(String filename) {
        try {
            Path savePath = Paths.get(SAVE_DIR, filename);
            if (!Files.exists(savePath)) {
                System.err.println("Save file not found: " + savePath);
                return null;
            }
            
            // Read compressed data
            SaveData data;
            try (ObjectInputStream ois = new ObjectInputStream(
                    new GZIPInputStream(new FileInputStream(savePath.toFile())))) {
                data = (SaveData) ois.readObject();
            }
            
            // Create world from seed (this regenerates terrain deterministically)
            DemoWorld world = new DemoWorld(data.worldName, data.seed, data.startX, data.startY);
            
            // Restore structure states (population, productivity)
            restoreStructureStates(world, data.structures);
            
            LoadResult result = new LoadResult();
            result.world = world;
            result.worldName = data.worldName;
            result.viewX = data.viewX;
            result.viewY = data.viewY;
            result.zoom = data.zoom;
            
            System.out.println("World loaded: " + savePath);
            return result;
            
        } catch (Exception e) {
            System.err.println("Failed to load world: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Gets a list of all available save files.
     */
    public static List<SaveInfo> listSaves() {
        List<SaveInfo> saves = new ArrayList<>();
        
        try {
            Path saveDir = Paths.get(SAVE_DIR);
            if (!Files.exists(saveDir)) {
                return saves;
            }
            
            Files.list(saveDir)
                .filter(p -> p.toString().endsWith(SAVE_EXTENSION))
                .forEach(path -> {
                    try {
                        SaveData data;
                        try (ObjectInputStream ois = new ObjectInputStream(
                                new GZIPInputStream(new FileInputStream(path.toFile())))) {
                            data = (SaveData) ois.readObject();
                        }
                        
                        SaveInfo info = new SaveInfo();
                        info.filename = path.getFileName().toString();
                        info.worldName = data.worldName;
                        info.seed = data.seed;
                        info.saveTime = data.saveTime;
                        info.structureCount = data.structures != null ? data.structures.size() : 0;
                        
                        saves.add(info);
                    } catch (Exception e) {
                        // Skip corrupted save files
                    }
                });
            
            // Sort by save time (newest first)
            saves.sort((a, b) -> Long.compare(b.saveTime, a.saveTime));
            
        } catch (Exception e) {
            System.err.println("Failed to list saves: " + e.getMessage());
        }
        
        return saves;
    }
    
    /**
     * Deletes a save file.
     */
    public static boolean deleteSave(String filename) {
        try {
            Path savePath = Paths.get(SAVE_DIR, filename);
            Files.deleteIfExists(savePath);
            System.out.println("Save deleted: " + savePath);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to delete save: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Restores structure states from save data.
     */
    private static void restoreStructureStates(DemoWorld world, List<StructureSaveData> savedStructures) {
        if (savedStructures == null) return;
        
        for (StructureSaveData ssd : savedStructures) {
            // Find matching structure in world
            for (MapStructure structure : world.getStructures()) {
                if (structure.getGridX() == ssd.gridX && 
                    structure.getGridY() == ssd.gridY &&
                    structure.getName().equals(ssd.name)) {
                    structure.setPopulation(ssd.population);
                    structure.setProductivity(ssd.productivity);
                    break;
                }
            }
        }
    }
    
    /**
     * Sanitizes a filename to remove invalid characters.
     */
    private static String sanitizeFilename(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_").toLowerCase();
    }
    
    // ==================== Data Classes ====================
    
    /**
     * Main save data structure.
     */
    public static class SaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public int version;
        public String worldName;
        public long seed;
        public int startX;
        public int startY;
        public int viewX;
        public int viewY;
        public double zoom;
        public long saveTime;
        public List<StructureSaveData> structures;
    }
    
    /**
     * Structure save data.
     */
    public static class StructureSaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public String type;
        public String name;
        public int gridX;
        public int gridY;
        public double population;
        public double productivity;
        public boolean isMajor;
    }
    
    /**
     * Save file information for display.
     */
    public static class SaveInfo {
        public String filename;
        public String worldName;
        public long seed;
        public long saveTime;
        public int structureCount;
        
        public String getFormattedTime() {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
            return sdf.format(new Date(saveTime));
        }
    }
    
    /**
     * Result of loading a world.
     */
    public static class LoadResult {
        public DemoWorld world;
        public String worldName;
        public int viewX;
        public int viewY;
        public double zoom;
    }
}
