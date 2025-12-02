package zwuiix.colria.game.gui;

import cn.nukkit.item.Item;
import zwuiix.colria.game.GameRegistry;
import zwuiix.colria.inventory.InventoryClick;
import zwuiix.colria.inventory.VirtualInventory;
import zwuiix.colria.inventory.impl.EntityInventory;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

public class GameCreateGUI {
    public static VirtualInventory create(EnginePlayer player) {
        EntityInventory inventory = new EntityInventory(9, player.processTranslation(TranslationKeys.PLAYER_GAME_CREATE_GUI_TITLE));

        int k = 0;
        for (GameRegistry.GameMode mode : GameRegistry.getInstance().getGameModes()) {
            Item item = mode.reference().clone();
            item.setCustomName(player.processTranslation(item.getCustomName()));
            item.setLore(player.processTranslation(item.getLore()[0]));

            VirtualInventory.SlotSetResult result = inventory.setItem(k, item);
            result.onClick(click -> onClick(click, mode));
            k++;
        }

        return inventory;
    }

    private static void onClick(InventoryClick click, GameRegistry.GameMode mode) {
        EnginePlayer p = (EnginePlayer) click.player();
        click.inventory().close(p);
        p.sudo("game create " + mode.name());
    }
}
