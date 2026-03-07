package zwuiix.colria.game.impl.lobby.gui.sub;

import cn.nukkit.block.BlockCopperBars;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemDyeGray;
import cn.nukkit.item.ItemDyeLime;
import cn.nukkit.item.enchantment.EnchantmentDurability;
import cn.nukkit.utils.TextFormat;
import zwuiix.colria.game.GameRegistry;
import zwuiix.colria.game.gui.sub.SubMenu;
import zwuiix.colria.game.impl.lobby.Lobby;
import zwuiix.colria.game.impl.lobby.gui.GameSelectGUI;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Window;

public class LobbyMenu extends SubMenu {
    private final GameSelectGUI gui;

    public LobbyMenu(GameSelectGUI gui, EnginePlayer player) {
        super(new ItemDyeGray()
                .setCustomName(player.processTranslation(TranslationKeys.PLAYER_LOBBY_GAMES_GUI_LOBBY_NAME))
                .setLore(player.processTranslation(TranslationKeys.PLAYER_LOBBY_GAMES_GUI_LOBBY_LORE))
        );
        this.gui = gui;
    }

    @Override
    public void sync() {
        Window.fillVerticalLine(9, gui.inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillVerticalLine(17, gui.inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillHorizontalLine(45, gui.inventory, new BlockCopperBars().toItem().setCustomName("§r"));

        for (Lobby lobby : GameRegistry.getInstance().getLobbies().values()) {
            Item item = new ItemDyeGray();
            if (lobby.equals(gui.player.getGame())) {
                item = new ItemDyeLime();
                item.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);
                item.addEnchantment(new EnchantmentDurability().setLevel(1));
            }

            item.setCustomName(TextFormat.colorize("&r&3" + lobby.getGameId() + " &8[&7" + lobby.getSpectators().size() + "&8]"));

            var slot = Window.nextSlot(gui.inventory);
            gui.inventory.setItem(slot, item)
                    .onClick((click) -> {
                        if(lobby.equals(gui.player.getGame()))
                            return; // Already in this lobby

                        gui.inventory.close(gui.player);
                        gui.player.getGame().removeSpectator(gui.player);
                        lobby.join(gui.player);
                    });
        }
    }
}
