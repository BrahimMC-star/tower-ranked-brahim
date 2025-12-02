package zwuiix.colria.game.impl.tower;

import cn.nukkit.block.BlockBricks;
import cn.nukkit.item.*;
import zwuiix.colria.EngineInfo;
import zwuiix.colria.game.GameKit;
import zwuiix.colria.game.GamePlayer;
import zwuiix.colria.player.EnginePlayer;

import java.util.HashMap;

public class TowerGameKit extends GameKit {
    public TowerGameKit(HashMap<Integer, Item> armors, HashMap<Integer, Item> inventory) {
        super(armors, inventory);
    }

    public TowerGameKit() {
        super(new HashMap<>(), new HashMap<>());

        ItemHelmetIron helmet = new ItemHelmetIron();
        helmet.setCustomName(EngineInfo.NAME);
        helmet.setUnbreakable();
        helmet.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);

        ItemChestplateLeather chestplate = new ItemChestplateLeather();
        chestplate.setCustomName(EngineInfo.NAME);
        chestplate.setUnbreakable();
        chestplate.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);

        ItemLeggingsChain  leggings = new ItemLeggingsChain();
        leggings.setCustomName(EngineInfo.NAME);
        leggings.setUnbreakable();
        leggings.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);

        ItemBootsIron  boots = new ItemBootsIron();
        boots.setCustomName(EngineInfo.NAME);
        boots.setUnbreakable();
        boots.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);

        setHelmet(helmet);
        setChestplate(chestplate);
        setLeggings(leggings);
        setBoots(boots);

        Item sword = new ItemSwordStone();
        sword.setUnbreakable();

        Item bricks = new BlockBricks().toItem();
        bricks.setCount(64);

        Item apple = new ItemAppleGold();
        apple.setCount(4);

        Item pickaxe = new ItemPickaxeIron();
        pickaxe.setUnbreakable();

        setItemAt(0, sword);
        setItemAt(1, bricks);
        setItemAt(2, apple);
        setItemAt(3, pickaxe);
        setItemAt(4, bricks);
    }

    @Override
    public void apply(EnginePlayer player) {
        super.apply(player);

        GamePlayer gamePlayer = player.getGamePlayer();
        if(gamePlayer instanceof TowerPlayer towerPlayer) {
            ItemChestplateLeather chestplate = new ItemChestplateLeather();
            chestplate.setCustomName(EngineInfo.NAME);
            chestplate.setUnbreakable();
            chestplate.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);
            chestplate.setColor(towerPlayer.getTeam().dyeColor());
            player.getInventory().setChestplate(chestplate);
        }
    }
}
