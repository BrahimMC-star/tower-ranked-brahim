package zwuiix.colria.game.impl.lobby.gui.sub;

import cn.nukkit.block.*;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemArmorStand;
import cn.nukkit.item.ItemRedstone;
import cn.nukkit.item.ItemSpawnEgg;
import cn.nukkit.level.Sound;
import zwuiix.colria.game.gui.sub.SubMenu;
import zwuiix.colria.game.impl.lobby.gui.GameShopGUI;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Window;

public class ShopHomeMenu extends SubMenu {
    private final GameShopGUI gui;

    public ShopHomeMenu(GameShopGUI gui, EnginePlayer player) {
        super(Item.AIR_ITEM);
        this.gui = gui;
    }

    @Override
    public void sync() {
        var player = gui.player;
        var inv = gui.inventory;

        Item glass = new BlockCopperBars().toItem().setCustomName("§r");
        inv.setItem(1, glass);
        Window.fillVerticalLine(0, inv, glass);
        Window.fillVerticalLine(8, inv, glass);
        Window.fillHorizontalLine(0, inv, glass);
        Window.fillHorizontalLine(45, inv, glass);

        Item ranks = new BlockBlueIce().toItem();
        ranks.setCustomName(gui.player.processTranslation(TranslationKeys.PLAYER_LOBBY_SHOP_GUI_RANKS_NAME));
        ranks.setLore(gui.player.processTranslation(TranslationKeys.PLAYER_LOBBY_SHOP_GUI_RANKS_LORE));
        gui.inventory.setItem(22, ranks).onClick((click -> {
            gui.state = 1;
            gui.syncContents();
            gui.player.addSound(Sound.RANDOM_CLICK,0.5f, 1f);
        }));

        Item cosmetics = new BlockHeadPlayer().toItem();
        cosmetics.setCustomName(gui.player.processTranslation(TranslationKeys.PLAYER_LOBBY_SHOP_GUI_COSMETICS_NAME));
        cosmetics.setLore(gui.player.processTranslation(TranslationKeys.PLAYER_LOBBY_SHOP_GUI_COSMETICS_LORE));
        gui.inventory.setItem(30, cosmetics).onClick((click -> {
            gui.state = 2;
            gui.syncContents();
            gui.player.addSound(Sound.RANDOM_CLICK,0.5f, 1f);
        }));

        Item particles = new ItemRedstone();
        particles.setCustomName(gui.player.processTranslation(TranslationKeys.PLAYER_LOBBY_SHOP_GUI_PARTICLES_NAME));
        particles.setLore(gui.player.processTranslation(TranslationKeys.PLAYER_LOBBY_SHOP_GUI_PARTICLES_LORE));
        gui.inventory.setItem(31, particles).onClick((click -> {
            gui.state = 3;
            gui.syncContents();
            gui.player.addSound(Sound.RANDOM_CLICK,0.5f, 1f);
        }));

        Item capes = new ItemArmorStand();
        capes.setCustomName(gui.player.processTranslation(TranslationKeys.PLAYER_LOBBY_SHOP_GUI_CAPES_NAME));
        capes.setLore(gui.player.processTranslation(TranslationKeys.PLAYER_LOBBY_SHOP_GUI_CAPES_LORE));
        gui.inventory.setItem(32, capes).onClick((click -> {
            gui.state = 4;
            gui.syncContents();
            gui.player.addSound(Sound.RANDOM_CLICK,0.5f, 1f);
        }));

        Item pets = new BlockMobSpawner().toItem();
        pets.setCustomName(gui.player.processTranslation(TranslationKeys.PLAYER_LOBBY_SHOP_GUI_CAPES_NAME));
        pets.setLore(gui.player.processTranslation(TranslationKeys.PLAYER_LOBBY_SHOP_GUI_CAPES_LORE));
        gui.inventory.setItem(29, pets).onClick((click -> {
            gui.state = 5;
            gui.syncContents();
            gui.player.addSound(Sound.RANDOM_CLICK,0.5f, 1f);
        }));
    }
}
