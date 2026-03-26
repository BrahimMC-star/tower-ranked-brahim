package zwuiix.colria.game.impl.team.item;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.math.Vector3;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.impl.team.TeamGame;
import zwuiix.colria.player.EnginePlayer;

public class ItemTeamSelector extends Item {
    public ItemTeamSelector() {
        super(ItemID.TOTEM);
    }

    @Override
    public boolean onClickAir(Player player, Vector3 directionVector) {
        EnginePlayer p = (EnginePlayer)player;
        Game game = p.getGame();
        if(!(game instanceof TeamGame teamGame)) return false;

        teamGame.getTeamSelector().send(p);
        return true;
    }

    @Override
    public boolean onAttack(Player player, Entity entity) {
        onClickAir(player, null);
        return false;
    }
}
