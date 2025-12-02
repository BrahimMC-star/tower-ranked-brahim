package zwuiix.colria.game;

import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import zwuiix.colria.player.EnginePlayer;

import java.util.HashMap;

abstract public class GameKit {
    private HashMap<Integer, Item> armors = new HashMap<>();
    private HashMap<Integer, Item> inventory = new HashMap<>();

    public GameKit(HashMap<Integer, Item>  armors, HashMap<Integer, Item> inventory) {
        this.armors = armors;
        this.inventory = inventory;
    }

    public HashMap<Integer, Item> getArmors() {
        return armors;
    }

    public HashMap<Integer, Item> getInventory() {
        return inventory;
    }

    public Item getHelmet() {
        return armors.getOrDefault(0, Item.AIR_ITEM);
    }

    public void setHelmet(Item item) {
        armors.put(0, item);
    }

    public Item getChestplate() {
        return armors.getOrDefault(1, Item.AIR_ITEM);
    }

    public void setChestplate(Item item) {
        armors.put(1, item);
    }

    public Item getLeggings() {
        return armors.getOrDefault(2, Item.AIR_ITEM);
    }

    public void setLeggings(Item item) {
        armors.put(2, item);
    }

    public Item getBoots() {
        return armors.getOrDefault(3, Item.AIR_ITEM);
    }

    public void setBoots(Item item) {
        armors.put(3, item);
    }

    public Item getItemAt(Integer slot) {
        return inventory.getOrDefault(slot, Item.AIR_ITEM);
    }

    public void setItemAt(Integer slot, Item item) {
        inventory.put(slot, item);
    }

    public void apply(GamePlayer gamePlayer, EnginePlayer player) {
        apply(player);
    }

    public void apply(EnginePlayer player) {
        PlayerInventory inv = player.getInventory();

        Item helmet = armors.getOrDefault(0, Item.AIR_ITEM);
        Item chestplate = armors.getOrDefault(1, Item.AIR_ITEM);
        Item leggings = armors.getOrDefault(2, Item.AIR_ITEM);
        Item boots = armors.getOrDefault(3, Item.AIR_ITEM);

        inv.setHelmet(helmet);
        inv.setChestplate(chestplate);
        inv.setLeggings(leggings);
        inv.setBoots(boots);

        for (Integer id : inventory.keySet()) {
            inv.setItem(id, getItemAt(id));
        }
    }
}
