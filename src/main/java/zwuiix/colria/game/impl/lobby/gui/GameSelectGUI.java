package zwuiix.colria.game.impl.lobby.gui;

import cn.nukkit.block.BlockCopperBars;
import cn.nukkit.block.BlockCopperBarsExposed;
import cn.nukkit.item.Item;
import zwuiix.colria.game.gui.sub.SubMenu;
import zwuiix.colria.game.impl.lobby.gui.sub.LobbyMenu;
import zwuiix.colria.game.impl.lobby.gui.sub.TowerBridgeMenu;
import zwuiix.colria.game.impl.lobby.gui.sub.TowerFastMenu;
import zwuiix.colria.inventory.VirtualInventory;
import zwuiix.colria.inventory.impl.EntityInventory;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Window;

import java.util.ArrayList;

public class GameSelectGUI {
    public EnginePlayer player;

    public VirtualInventory inventory;

    private int state = 1;
    private final ArrayList<SubMenu> subMenus = new ArrayList<>();

    public GameSelectGUI(EnginePlayer player) {
        this.player = player;
        inventory = new EntityInventory(54, player.processTranslation(TranslationKeys.PLAYER_LOBBY_GAMES_GUI_TITLE));

        subMenus.add(new LobbyMenu(this, player));
        subMenus.add(new TowerFastMenu(this));
        subMenus.add(new TowerBridgeMenu(this));

        syncContents();
    }

    public void send() {
        syncContents();
        inventory.open(player);
    }

    public void syncContents() {
        Item glass = new BlockCopperBars().toItem().setCustomName("§r");
        inventory.setItem(1, glass);
        Window.fillSecondLine(inventory, glass);

        int k = 0;
        for (SubMenu menu : subMenus) {
            int finalK = k;
            if(k == 0) {
                inventory.setItem(k, menu.getReference()).onClick((click) -> {
                    state = finalK;
                    syncContents();
                });
            } else {
                inventory.setItem(k + 1, menu.getReference()).onClick((click) -> {
                    state = finalK;
                    syncContents();
                });
            }
            k++;
        }

        SubMenu subMenu = subMenus.get(state);
        if(subMenu != null) {
            for (int i = 18; i < inventory.getSize(); i++) {
                inventory.setItem(i, Item.AIR_ITEM, false);
            }

            if(state == 0) {
                inventory.setItem(state + 9, new BlockCopperBarsExposed().toItem().setCustomName("§r"));
            } else {
                inventory.setItem(state + 10, new BlockCopperBarsExposed().toItem().setCustomName("§r"));
            }

            subMenu.sync();
        }
    }
}
