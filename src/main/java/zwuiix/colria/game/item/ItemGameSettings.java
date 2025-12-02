package zwuiix.colria.game.item;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.math.Vector3;
import zwuiix.colria.player.EnginePlayer;

public class ItemGameSettings extends Item {

    public ItemGameSettings() {
        super(ItemID.TOTEM);
    }

    @Override
    public boolean onClickAir(Player player, Vector3 directionVector) {
        EnginePlayer p = (EnginePlayer)player;
        p.sudo("game edit");
        return true;
    }
}
