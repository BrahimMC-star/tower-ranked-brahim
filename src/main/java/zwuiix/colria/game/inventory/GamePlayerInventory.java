package zwuiix.colria.game.inventory;

import cn.nukkit.inventory.ContainerInventory;
import cn.nukkit.inventory.InventoryType;
import zwuiix.colria.player.EnginePlayer;

public class GamePlayerInventory extends ContainerInventory {
    public GamePlayerInventory(EnginePlayer player) {
        super(player, InventoryType.CHEST_BOAT);
    }

    @Override
    public EnginePlayer getHolder() {
        return (EnginePlayer) super.getHolder();
    }
}
