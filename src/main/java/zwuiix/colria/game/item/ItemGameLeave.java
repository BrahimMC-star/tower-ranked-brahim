package zwuiix.colria.game.item;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.math.Vector3;
import zwuiix.colria.player.EnginePlayer;

public class ItemGameLeave extends Item {
    public ItemGameLeave() {
        super(ItemID.DARK_OAK_DOOR);
    }

    @Override
    public boolean onClickAir(Player player, Vector3 directionVector) {
        EnginePlayer p = (EnginePlayer)player;
        p.sudo("game leave");
        return true;
    }

    @Override
    public boolean onAttack(Player player, Entity entity) {
        onClickAir(player, null);
        return false;
    }
}
