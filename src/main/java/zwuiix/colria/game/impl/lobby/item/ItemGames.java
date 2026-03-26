package zwuiix.colria.game.impl.lobby.item;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.math.Vector3;
import zwuiix.colria.game.impl.lobby.gui.GameSelectGUI;
import zwuiix.colria.player.EnginePlayer;

public class ItemGames extends Item {
    private GameSelectGUI gui = null;

    public ItemGames() {
        super(ItemID.COMPASS);
    }

    @Override
    public boolean onClickAir(Player player, Vector3 directionVector) {
        EnginePlayer p = (EnginePlayer) player;
        if(p.isInLobby()) {
            if(gui == null) {
                gui = new GameSelectGUI(p);
            }

            gui.send();
        }
        return true;
    }

    @Override
    public boolean onAttack(Player player, Entity entity) {
        onClickAir(player, null);
        return false;
    }
}
