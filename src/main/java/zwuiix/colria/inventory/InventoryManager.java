package zwuiix.colria.inventory;

import cn.nukkit.Player;
import lombok.Getter;

import java.util.HashMap;

public class InventoryManager {
    @Getter
    private static final InventoryManager instance = new InventoryManager();

    private final HashMap<Player, VirtualInventory> inventories = new HashMap<>();
    private final HashMap<Player, InventoryDispatcher> dispatchers = new HashMap<>();

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
