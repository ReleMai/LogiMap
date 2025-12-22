/**
 * Represents a stack of items in an inventory slot.
 * Handles quantity management and stacking logic.
 */
public class ItemStack {
    
    private Item item;
    private int quantity;
    
    public ItemStack(Item item) {
        this(item, 1);
    }
    
    public ItemStack(Item item, int quantity) {
        this.item = item;
        this.quantity = Math.min(quantity, item.getMaxStackSize());
    }
    
    // === Getters ===
    
    public Item getItem() { return item; }
    public int getQuantity() { return quantity; }
    
    public boolean isEmpty() {
        return item == null || quantity <= 0;
    }
    
    public boolean isFull() {
        return item != null && quantity >= item.getMaxStackSize();
    }
    
    public int getMaxStackSize() {
        return item != null ? item.getMaxStackSize() : 0;
    }
    
    public int getRemainingSpace() {
        return item != null ? item.getMaxStackSize() - quantity : 0;
    }
    
    // === Operations ===
    
    /**
     * Tries to add items to this stack.
     * @return The number of items that couldn't be added (overflow)
     */
    public int add(int amount) {
        if (item == null) return amount;
        
        int canAdd = Math.min(amount, getRemainingSpace());
        quantity += canAdd;
        return amount - canAdd;
    }
    
    /**
     * Tries to remove items from this stack.
     * @return The number of items actually removed
     */
    public int remove(int amount) {
        int canRemove = Math.min(amount, quantity);
        quantity -= canRemove;
        return canRemove;
    }
    
    /**
     * Checks if this stack can merge with another stack of the same item.
     */
    public boolean canMergeWith(ItemStack other) {
        if (other == null || other.isEmpty()) return false;
        if (this.item == null) return true; // Empty slot can accept anything
        return this.item.equals(other.item) && !isFull();
    }
    
    /**
     * Merges another stack into this one.
     * @return Remaining items that couldn't be merged (as a new stack, or null)
     */
    public ItemStack merge(ItemStack other) {
        if (other == null || other.isEmpty()) return null;
        
        if (this.item == null) {
            // This slot is empty, take everything we can
            this.item = other.item;
            int canTake = Math.min(other.quantity, item.getMaxStackSize());
            this.quantity = canTake;
            int remaining = other.quantity - canTake;
            return remaining > 0 ? new ItemStack(other.item, remaining) : null;
        }
        
        if (!this.item.equals(other.item)) return other; // Can't merge different items
        
        int overflow = add(other.quantity);
        return overflow > 0 ? new ItemStack(other.item, overflow) : null;
    }
    
    /**
     * Splits this stack, removing half (or specified amount).
     * @return A new stack with the split items
     */
    public ItemStack split(int amount) {
        if (isEmpty()) return null;
        
        int toSplit = Math.min(amount, quantity);
        quantity -= toSplit;
        return new ItemStack(item, toSplit);
    }
    
    /**
     * Splits the stack in half.
     */
    public ItemStack splitHalf() {
        return split(quantity / 2);
    }
    
    /**
     * Gets the total value of this stack in copper.
     */
    public long getTotalValue() {
        return item != null ? item.getBaseValue() * quantity : 0;
    }
    
    /**
     * Creates a copy of this stack.
     */
    public ItemStack copy() {
        if (isEmpty()) return null;
        return new ItemStack(item, quantity);
    }
    
    /**
     * Clears this stack.
     */
    public void clear() {
        this.item = null;
        this.quantity = 0;
    }
    
    @Override
    public String toString() {
        if (isEmpty()) return "Empty";
        return item.getName() + " x" + quantity;
    }
}
