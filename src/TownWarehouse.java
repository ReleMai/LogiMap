import java.util.*;

/**
 * Town warehouse system for storing items between visits.
 * Can be purchased and upgraded at villages/cities.
 * 
 * Upgrade tiers:
 * - Tier 1: 5 slots (1 silver)
 * - Tier 2: 10 slots (2 silver)
 * - Tier 3: 15 slots (3 silver)
 * - ... up to Tier 10: 50 slots (10 silver total)
 */
public class TownWarehouse {
    
    private static final int SLOTS_PER_TIER = 5;
    private static final int MAX_TIER = 10;
    private static final int BASE_UPGRADE_COST = 100; // copper (1 silver)
    
    private int tier = 0; // 0 = not purchased, 1-10 = upgrade level
    private Inventory storage;
    private String townName;
    private boolean isCity;
    
    /**
     * Creates a new warehouse for a town.
     * @param townName The name of the town
     * @param isCity True if this is a city (gets discount)
     */
    public TownWarehouse(String townName, boolean isCity) {
        this.townName = townName;
        this.isCity = isCity;
        this.storage = null; // Created when purchased
    }
    
    /**
     * Checks if the warehouse has been purchased.
     */
    public boolean isPurchased() {
        return tier > 0;
    }
    
    /**
     * Gets the current tier level (0 = not purchased).
     */
    public int getTier() {
        return tier;
    }
    
    /**
     * Gets the maximum tier level.
     */
    public int getMaxTier() {
        return MAX_TIER;
    }
    
    /**
     * Gets the current storage capacity.
     */
    public int getCapacity() {
        return tier * SLOTS_PER_TIER;
    }
    
    /**
     * Gets the maximum possible capacity.
     */
    public int getMaxCapacity() {
        return MAX_TIER * SLOTS_PER_TIER;
    }
    
    /**
     * Gets the cost for the next upgrade in copper.
     * Returns 0 if already at max tier.
     */
    public int getNextUpgradeCost() {
        if (tier >= MAX_TIER) return 0;
        
        int cost = BASE_UPGRADE_COST;
        // Cities offer 20% discount
        if (isCity) {
            cost = (int)(cost * 0.8);
        }
        return cost;
    }
    
    /**
     * Gets the cost for initial purchase in copper.
     */
    public int getPurchaseCost() {
        return getNextUpgradeCost();
    }
    
    /**
     * Gets the total investment so far in copper.
     */
    public int getTotalInvestment() {
        return tier * BASE_UPGRADE_COST * (isCity ? 80 : 100) / 100;
    }
    
    /**
     * Purchases or upgrades the warehouse.
     * @return true if successful, false if already at max tier
     */
    public boolean upgrade() {
        if (tier >= MAX_TIER) return false;
        
        tier++;
        
        // Create storage with new capacity (recreate to expand)
        // Store old items if any
        List<ItemStack> oldItems = new ArrayList<>();
        if (storage != null) {
            for (int i = 0; i < storage.getSize(); i++) {
                ItemStack stack = storage.getSlot(i);
                if (stack != null && !stack.isEmpty()) {
                    oldItems.add(stack);
                }
            }
        }
        
        // Create new larger storage (1 row of SLOTS_PER_TIER cols per tier)
        int rows = tier;
        int cols = SLOTS_PER_TIER;
        storage = new Inventory(townName + " Warehouse", rows, cols);
        
        // Restore old items
        for (ItemStack stack : oldItems) {
            storage.addItem(stack);
        }
        
        return true;
    }
    
    /**
     * Gets the storage inventory.
     * @return The inventory, or null if not purchased
     */
    public Inventory getStorage() {
        return storage;
    }
    
    /**
     * Counts free slots in storage.
     */
    private int countFreeSlots() {
        if (storage == null) return 0;
        int count = 0;
        for (int i = 0; i < storage.getSize(); i++) {
            if (storage.isSlotEmpty(i)) count++;
        }
        return count;
    }
    
    /**
     * Finds a slot containing the given item ID.
     */
    private int findSlotWithItem(String itemId) {
        if (storage == null) return -1;
        for (int i = 0; i < storage.getSize(); i++) {
            ItemStack stack = storage.getSlot(i);
            if (stack != null && !stack.isEmpty() && stack.getItem().getId().equals(itemId)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Stores an item from the player's inventory.
     * @param playerInventory The player's inventory
     * @param slotIndex The slot to transfer from
     * @return true if successful
     */
    public boolean storeItem(Inventory playerInventory, int slotIndex) {
        if (!isPurchased() || storage == null) return false;
        
        ItemStack stack = playerInventory.getSlot(slotIndex);
        if (stack == null || stack.isEmpty()) return false;
        
        // Try to add to warehouse
        Item item = stack.getItem();
        int qty = stack.getQuantity();
        
        // Check if warehouse has room
        int freeSlots = countFreeSlots();
        int existingSlot = findSlotWithItem(item.getId());
        
        if (existingSlot >= 0) {
            // Can stack with existing
            ItemStack existing = storage.getSlot(existingSlot);
            existing.add(qty);
            playerInventory.setSlot(slotIndex, null);
            return true;
        } else if (freeSlots > 0) {
            // Add to new slot
            storage.addItem(stack.copy());
            playerInventory.setSlot(slotIndex, null);
            return true;
        }
        
        return false; // No room
    }
    
    /**
     * Retrieves an item from the warehouse to player inventory.
     * @param playerInventory The player's inventory
     * @param slotIndex The warehouse slot to transfer from
     * @return true if successful
     */
    public boolean retrieveItem(Inventory playerInventory, int slotIndex) {
        if (!isPurchased() || storage == null) return false;
        
        ItemStack stack = storage.getSlot(slotIndex);
        if (stack == null || stack.isEmpty()) return false;
        
        // Try to add to player inventory
        ItemStack remaining = playerInventory.addItem(stack.copy());
        if (remaining == null || remaining.isEmpty()) {
            // All transferred
            storage.setSlot(slotIndex, null);
            return true;
        } else if (remaining.getQuantity() < stack.getQuantity()) {
            // Partial transfer
            stack.remove(stack.getQuantity() - remaining.getQuantity());
            return true;
        }
        
        return false; // No room in player inventory
    }
    
    /**
     * Gets display info about the warehouse.
     */
    public String getInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("📦 ").append(townName).append(" Warehouse\n");
        
        if (!isPurchased()) {
            sb.append("Status: Not purchased\n");
            sb.append("Cost: ").append(formatCost(getPurchaseCost())).append("\n");
        } else {
            sb.append("Tier: ").append(tier).append("/").append(MAX_TIER).append("\n");
            sb.append("Capacity: ").append(getCapacity()).append(" slots\n");
            
            if (storage != null) {
                int used = getCapacity() - countFreeSlots();
                sb.append("Used: ").append(used).append("/").append(getCapacity()).append("\n");
            }
            
            if (tier < MAX_TIER) {
                sb.append("Upgrade: ").append(formatCost(getNextUpgradeCost())).append("\n");
            } else {
                sb.append("(Maximum capacity)\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Formats a copper cost as silver/gold.
     */
    private String formatCost(int copper) {
        if (copper >= 1000) {
            return (copper / 1000) + " gold " + ((copper % 1000) / 100) + " silver";
        } else if (copper >= 100) {
            return (copper / 100) + " silver";
        }
        return copper + " copper";
    }
    
    /**
     * Gets the town name for this warehouse.
     */
    public String getTownName() {
        return townName;
    }
    
    /**
     * Checks if this is a city warehouse.
     */
    public boolean isCity() {
        return isCity;
    }
    
    // ==================== Serialization ====================
    
    /**
     * Saves warehouse data to a map for serialization.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("townName", townName);
        data.put("isCity", isCity);
        data.put("tier", tier);
        
        if (storage != null) {
            List<Map<String, Object>> items = new ArrayList<>();
            for (int i = 0; i < storage.getSize(); i++) {
                ItemStack stack = storage.getSlot(i);
                if (stack != null && !stack.isEmpty()) {
                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("slot", i);
                    itemData.put("itemId", stack.getItem().getId());
                    itemData.put("quantity", stack.getQuantity());
                    items.add(itemData);
                }
            }
            data.put("items", items);
        }
        
        return data;
    }
    
    /**
     * Loads warehouse data from a map.
     */
    @SuppressWarnings("unchecked")
    public static TownWarehouse fromMap(Map<String, Object> data) {
        String townName = (String) data.get("townName");
        boolean isCity = (Boolean) data.getOrDefault("isCity", false);
        int tier = ((Number) data.getOrDefault("tier", 0)).intValue();
        
        TownWarehouse warehouse = new TownWarehouse(townName, isCity);
        
        // Restore tier
        for (int i = 0; i < tier; i++) {
            warehouse.upgrade();
        }
        
        // Restore items
        if (data.containsKey("items") && warehouse.storage != null) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
            for (Map<String, Object> itemData : items) {
                int slot = ((Number) itemData.get("slot")).intValue();
                String itemId = (String) itemData.get("itemId");
                int qty = ((Number) itemData.get("quantity")).intValue();
                
                ItemStack stack = ItemRegistry.createStack(itemId, qty);
                if (stack != null && slot < warehouse.storage.getSize()) {
                    warehouse.storage.setSlot(slot, stack);
                }
            }
        }
        
        return warehouse;
    }
}
