package zwuiix.colria.game.gui;

import cn.nukkit.block.BlockCopperBars;
import cn.nukkit.block.BlockCopperBarsExposed;
import cn.nukkit.item.Item;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.GameRegistry;
import zwuiix.colria.game.gui.sub.*;
import zwuiix.colria.game.impl.team.TeamGame;
import zwuiix.colria.game.impl.tower.TowerGame;
import zwuiix.colria.inventory.VirtualInventory;
import zwuiix.colria.inventory.impl.EntityInventory;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Window;

import java.util.ArrayList;

public class GameSettingsGUI {
    public Game game;
    public EnginePlayer player;

    public VirtualInventory inventory;

    private int state = 0;
    private final ArrayList<SubMenu> subMenus = new ArrayList<>();

    public GameSettingsGUI(Game game, EnginePlayer player) {
        this.game = game;
        this.player = player;

        inventory = new EntityInventory(54, player.processTranslation(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_TITLE));
        subMenus.add(new MapMenu(this));
        subMenus.add(new ComponentsMenu(this));

        if(game instanceof TeamGame) subMenus.add(new TeamMenu(this));
        if(game instanceof TowerGame) subMenus.add(new DiscordMenu(this));

        syncContents();
    }

    public void send() {
        syncContents();
        inventory.open(player);
    }

    public void syncContents() {
        Item glass = new BlockCopperBars().toItem().setCustomName("§r");

        Item gameInfo = GameRegistry.getInstance().getGameMode(game.getName()).reference().clone();
        gameInfo.setCustomName(player.processTranslation(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_INFO_NAME));
        gameInfo.setLore(player.processTranslation(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_INFO_LORE, game.getGameId(), game.getHoster(), game.getPlayers().size() + game.getSpectators().size()));

        inventory.setItem(0, gameInfo);
        inventory.setItem(1, glass);
        Window.fillSecondLine(inventory, glass);

        int k = 0;
        for (SubMenu menu : subMenus) {
            int finalK = k;
            inventory.setItem(k + 2, menu.getReference()).onClick((click) -> {
                state = finalK;
                syncContents();
            });
            k++;
        }

        SubMenu subMenu = subMenus.get(state);
        if(subMenu != null) {
            for (int i = 18; i < inventory.getSize(); i++) {
                inventory.setItem(i, Item.AIR_ITEM, false);
            }

            inventory.setItem(state + 11, new BlockCopperBarsExposed().toItem().setCustomName("§r"));
            subMenu.sync();
        }
    }
}
