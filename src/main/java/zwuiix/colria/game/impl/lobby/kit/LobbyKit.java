package zwuiix.colria.game.impl.lobby.kit;

import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import zwuiix.colria.game.GameKit;
import zwuiix.colria.game.impl.lobby.item.ItemGames;
import zwuiix.colria.game.impl.lobby.item.ItemShop;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.HashMap;

public class LobbyKit extends GameKit {
    public static LobbyKit INSTANCE = new LobbyKit();
    public LobbyKit() {
        super(new HashMap<>(), new HashMap<>());
    }

    @Override
    public void apply(EnginePlayer player) {
        super.apply(player);

        PlayerInventory inv = player.getInventory();
        if (inv == null) return;

        Item games = new ItemGames();
        games.setCustomName(player.processTranslation(TranslationKeys.PLAYER_LOBBY_GAMES));
        games.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);
        inv.setItem(0, games);

        Item shards = new ItemShop();
        shards.setCustomName(player.processTranslation(TranslationKeys.PLAYER_LOBBY_SHOP));
        shards.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);
        inv.setItem(1, shards);
    }
}
