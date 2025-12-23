import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Grid-based inventory system.
 * Supports stacking, sorting, and item management.
 */
public class Inventory {
    
    public enum SortMode {
        NAME,
        RARITY,
        VALUE,
        CATEGORY,
        QUANTITY
    }
    
    private final int rows;
    private final int cols;
    private final ItemStack[] slots;
    private final String name;
    
    // Events
    private Consumer<Integer> onSlotChanged;
    private Runnable onInventoryChanged;
    
    public Inventory(String name, int rows, int cols) {
        this.name = name;
        this.rows = rows;
        this.cols = cols;
        this.slots = new ItemStack[rows * cols];
    }
    
    // === Getters ===
    
    public String getName() { return name; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getSize() { return slots.length; }
    
    public ItemStack getSlot(int index) {
        if (index < 0 || index >= slots.length) return null;
        return slots[index];
    }
    
    public ItemStack getSlot(int row, int col) {
        return getSlot(row * cols + col);
    }
    
    public boolean isSlotEmpty(int index) {
        return slots[index] == null || slots[index].isEmpty();
    }
    
    // === Item Operations ===
    
    /**
     * Adds an item stack to the inventory.
     * First tries to merge with existing stacks, then finds an empty slot.
     * @return The remaining items that couldn't be added (or null if all added)
     */
    public ItemStack addItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        
        ItemStack remaining = stack.copy();
        
        // First, try to merge with existing stacks of the same item
        if (remaining.getItem().isStackable()) {
            for (int i = 0; i < slots.length && remaining != null && !remaining.isEmpty(); i++) {
                if (slots[i] != null && slots[i].canMergeWith(remaining)) {
                    remaining = slots[i].merge(remaining);
                    notifySlotChanged(i);
                }
            }
        }
        
        // Then, try to place in empty slots
        while (remaining != null && !remaining.isEmpty()) {
            int emptySlot = findEmptySlot();
            if (emptySlot == -1) break;
            
            int maxToAdd = Math.min(remaining.getQuantity(), remaining.getMaxStackSize());
            slots[emptySlot] = new ItemStack(remaining.getItem(), maxToAdd);
            remaining.remove(maxToAdd);
            notifySlotChanged(emptySlot);
            
            if (remaining.isEmpty()) remaining = null;
        }
        
        notifyInventoryChanged();
        return remaining;
    }
    
    /**
     * Adds an item by ID with specified quantity.
     * @return Number of items that couldn't be added
     */
    public int addItem(String itemId, int quantity) {
        ItemStack stack = ItemRegistry.createStack(itemId, quantity);
        if (stack == null) return quantity;
        
        ItemStack remaining = addItem(stack);
        return remaining != null ? remaining.getQuantity() : 0;
    }
    
    /**
     * Sets a specific slot's contents.
     */
    public void setSlot(int index, ItemStack stack) {
        if (index < 0 || index >= slots.length) return;
        slots[index] = stack;
        notifySlotChanged(index);
        notifyInventoryChanged();
    }
    
    /**
     * Removes items from a specific slot.
     * @return The removed items as a stack
     */
    public ItemStack removeFromSlot(int index, int amount) {
        if (index < 0 || index >= slots.length || slots[index] == null) return null;
        
        ItemStack removed = slots[index].split(amount);
        if (slots[index].isEmpty()) {
            slots[index] = null;
        }
        notifySlotChanged(index);
        notifyInventoryChanged();
        return removed;
    }
    
    /**
     * Removes all items from a slot.
     */
    public ItemStack clearSlot(int index) {
        if (index < 0 || index >= slots.length) return null;
        ItemStack removed = slots[index];
        slots[index] = null;
        notifySlotChanged(index);
        notifyInventoryChanged();
        return removed;
    }
    
    /**
     * Swaps the contents of two slots.
     */
    public void swapSlots(int index1, int index2) {
        if (index1 < 0 || index1 >= slots.length) return;
        if (index2 < 0 || index2 >= slots.length) return;
        
        ItemStack temp = slots[index1];
        slots[index1] = slots[index2];
        slots[index2] = temp;
        
        notifySlotChanged(index1);
        notifySlotChanged(index2);
        notifyInventoryChanged();
    }
    
    /**
     * Transfers items between inventories (shift-click behavior).
     * @return true if any items were transferred
     */
    public boolean transferTo(Inventory target, int sourceSlot) {
        if (target == null || sourceSlot < 0 || sourceSlot >= slots.length) return false;
        if (slots[sourceSlot] == null || slots[sourceSlot].isEmpty()) return false;
        
        ItemStack toTransfer = slots[sourceSlot].copy();
        ItemStack remaining = target.addItem(toTransfer);
        
        if (remaining == null || remaining.getQuantity() < toTransfer.getQuantity()) {
            // Some or all items were transferred
            int transferred = toTransfer.getQuantity() - (remaining != null ? remaining.getQuantity() : 0);
            removeFromSlot(sourceSlot, transferred);
            return true;
        }
        
        return false;
    }
    
    // === Utility Methods ===
    
    /**
     * Finds the first empty slot.
     * @return Slot index, or -1 if no empty slot
     */
    public int findEmptySlot() {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null || slots[i].isEmpty()) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Counts the total quantity of a specific item.
     */
    public int countItem(String itemId) {
        int count = 0;
        for (ItemStack slot : slots) {
            if (slot != null && slot.getItem() != null && 
                slot.getItem().getId().equals(itemId)) {
                count += slot.getQuantity();
            }
        }
        return count;
    }
    
    /**
     * Checks if the inventory contains at least the specified quantity of an item.
     */
    public boolean hasItem(String itemId, int quantity) {
        return countItem(itemId) >= quantity;
    }
    
    /**
     * Removes a specific quantity of an item from anywhere in the inventory.
     * @return The number of items actually removed
     */
    public int removeItem(String itemId, int quantity) {
        int remaining = quantity;
        
        for (int i = 0; i < slots.length && remaining > 0; i++) {
            if (slots[i] != null && slots[i].getItem() != null &&
                slots[i].getItem().getId().equals(itemId)) {
                int removed = slots[i].remove(remaining);
                remaining -= removed;
                if (slots[i].isEmpty()) {
                    slots[i] = null;
                }
                notifySlotChanged(i);
            }
        }
        
        notifyInventoryChanged();
        return quantity - remaining;
    }
    
    /**
     * Gets the total value of all items in copper.
     */
    public long getTotalValue() {
        long total = 0;
        for (ItemStack slot : slots) {
            if (slot != null) {
                total += slot.getTotalValue();
            }
        }
        return total;
    }
    
    /**
     * Counts the number of used slots.
     */
    public int getUsedSlots() {
        int count = 0;
        for (ItemStack slot : slots) {
            if (slot != null && !slot.isEmpty()) count++;
        }
        return count;
    }
    
    /**
     * Checks if the inventory is full.
     */
    public boolean isFull() {
        return findEmptySlot() == -1;
    }
    
    /**
     * Checks if the inventory is empty.
     */
    public boolean isEmpty() {
        for (ItemStack slot : slots) {
            if (slot != null && !slot.isEmpty()) return false;
        }
        return true;
    }
    
    /**
     * Clears all items from the inventory.
     */
    public void clear() {
        for (int i = 0; i < slots.length; i++) {
            slots[i] = null;
            notifySlotChanged(i);
        }
        notifyInventoryChanged();
    }
    
    // === Sorting ===
    
    /**
     * Sorts the inventory by the specified mode.
     */
    public void sort(SortMode mode) {
        // Collect all non-empty stacks
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack slot : slots) {
            if (slot != null && !slot.isEmpty()) {
                items.add(slot);
            }
        }
        
        // Sort based on mode
        Comparator<ItemStack> comparator = getComparator(mode);
        items.sort(comparator);
        
        // Clear and refill
        for (int i = 0; i < slots.length; i++) {
            slots[i] = null;
        }
        
        for (int i = 0; i < items.size() && i < slots.length; i++) {
            slots[i] = items.get(i);
            notifySlotChanged(i);
        }
        
        notifyInventoryChanged();
    }
    
    /**
     * Consolidates stacks of the same item.
     */
    public void stackItems() {
        // Collect all items
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack slot : slots) {
            if (slot != null && !slot.isEmpty()) {
                // Try to merge with existing
                boolean merged = false;
                for (ItemStack existing : items) {
                    if (existing.canMergeWith(slot)) {
                        ItemStack remainder = existing.merge(slot);
                        if (remainder != null && !remainder.isEmpty()) {
                            items.add(remainder);
                        }
                        merged = true;
                        break;
                    }
                }
                if (!merged) {
                    items.add(slot.copy());
                }
            }
        }
        
        // Clear and refill
        for (int i = 0; i < slots.length; i++) {
            slots[i] = i < items.size() ? items.get(i) : null;
            notifySlotChanged(i);
        }
        
        notifyInventoryChanged();
    }
    
    private Comparator<ItemStack> getComparator(SortMode mode) {
        switch (mode) {
            case NAME:
                return Comparator.comparing(s -> s.getItem().getName());
            case RARITY:
                return Comparator.comparing((ItemStack s) -> s.getItem().getRarity().ordinal()).reversed()
                        .thenComparing(s -> s.getItem().getName());
            case VALUE:
                return Comparator.comparing((ItemStack s) -> s.getItem().getBaseValue()).reversed()
                        .thenComparing(s -> s.getItem().getName());
            case CATEGORY:
                return Comparator.comparing((ItemStack s) -> s.getItem().getCategory().ordinal())
                        .thenComparing(s -> s.getItem().getName());
            case QUANTITY:
                return Comparator.comparing((ItemStack s) -> s.getQuantity()).reversed()
                        .thenComparing(s -> s.getItem().getName());
            default:
                return Comparator.comparing(s -> s.getItem().getName());
        }
    }
    
    // === Events ===
    
    public void setOnSlotChanged(Consumer<Integer> callback) {
        this.onSlotChanged = callback;
    }
    
    public void setOnInventoryChanged(Runnable callback) {
        this.onInventoryChanged = callback;
    }
    
    private void notifySlotChanged(int index) {
        if (onSlotChanged != null) {
            onSlotChanged.accept(index);
        }
    }
    
    private void notifyInventoryChanged() {
        if (onInventoryChanged != null) {
            onInventoryChanged.run();
        }
    }
    
    // === Serialization ===
    
    /**
     * Gets a list of all non-empty item stacks for saving.
     */
    public List<ItemStack> getAllItems() {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack slot : slots) {
            if (slot != null && !slot.isEmpty()) {
                items.add(slot);
            }
        }
        return items;
    }
}
