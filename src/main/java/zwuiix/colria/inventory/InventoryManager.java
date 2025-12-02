package zwuiix.colria.inventory;

import cn.nukkit.Player;

import java.util.HashMap;

public class InventoryManager {
    private static InventoryManager instance = new InventoryManager();
    public static InventoryManager getInstance() {
        return instance;
    }

    private HashMap<Player, VirtualInventory> inventories = new HashMap<>();
    private HashMap<Player, InventoryDispatcher> dispatchers = new HashMap<>();

    public VirtualInventory getInventory(Player player) {
        return inventories.get(player);
    }

    public void setInventory(Player player, VirtualInventory inventory) {
        inventories.put(player, inventory);
    }

    public void resetInventory(Player player) {
        if(!inventories.containsKey(player)) return;
        inventories.remove(player);
    }

    public InventoryDispatcher getDispatcher(Player player) {
        return dispatchers.get(player);
    }

    public void setDispatcher(Player player, InventoryDispatcher dispatcher) {
        dispatchers.put(player, dispatcher);
    }

    public void resetDispatcher(Player player) {
        if(!dispatchers.containsKey(player)) return;
        dispatchers.remove(player);
    }
}
