package zwuiix.colria.game.impl.lobby.item;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.math.Vector3;
import zwuiix.colria.game.impl.lobby.gui.GameShopGUI;
import zwuiix.colria.player.EnginePlayer;

public class ItemShop extends Item {
    private GameShopGUI gui = null;

    public ItemShop() {
        super(ItemID.GOLD_INGOT);
    }

    @Override
    public boolean onClickAir(Player player, Vector3 directionVector) {
        EnginePlayer p = (EnginePlayer) player;
        if(p.isInLobby()) {
            if(gui == null) {
                gui = new GameShopGUI(p);
            }

            gui.send();
        }
        return true;
    }
}
