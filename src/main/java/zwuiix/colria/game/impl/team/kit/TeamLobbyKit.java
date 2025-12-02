package zwuiix.colria.game.impl.team.kit;

import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import zwuiix.colria.game.impl.team.item.ItemTeamSelector;
import zwuiix.colria.game.kit.WaitingKit;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

public class TeamLobbyKit extends WaitingKit {
    public static TeamLobbyKit INSTANCE = new TeamLobbyKit();

    @Override
    public void apply(EnginePlayer player) {
        super.apply(player);

        PlayerInventory inv = player.getInventory();

        Item team = new ItemTeamSelector();
        team.setCustomName(player.processTranslation(TranslationKeys.PLAYER_LOBBY_TEAM));
        inv.setItem(0, team);
    }
}
