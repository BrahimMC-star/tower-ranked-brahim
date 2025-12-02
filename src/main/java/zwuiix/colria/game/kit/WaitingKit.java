package zwuiix.colria.game.kit;

import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import zwuiix.colria.game.GameKit;
import zwuiix.colria.game.item.ItemGameLeave;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.HashMap;

public class WaitingKit extends GameKit {
    public static WaitingKit INSTANCE = new WaitingKit();

    public WaitingKit() {
        super(new HashMap<>(), new HashMap<>());
    }

    @Override
    public void apply(EnginePlayer player) {
        super.apply(player);

        PlayerInventory inv = player.getInventory();

        Item leave = new ItemGameLeave();
        leave.setCustomName(player.processTranslation(TranslationKeys.PLAYER_LOBBY_LEAVE));
        inv.setItem(8, leave);
    }
}
