package zwuiix.colria.game.impl.lobby.gui.sub;

import cn.nukkit.block.BlockCopperBars;
import cn.nukkit.block.BlockTerracotta;
import cn.nukkit.item.ItemBookAndQuill;
import cn.nukkit.item.data.DyeColor;
import cn.nukkit.utils.TextFormat;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.GameRegistry;
import zwuiix.colria.game.gui.sub.SubMenu;
import zwuiix.colria.game.impl.lobby.gui.GameSelectGUI;
import zwuiix.colria.game.impl.towerbridge.TowerBridgeGame;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Window;

import java.util.Comparator;
import java.util.List;

public class TowerBridgeMenu extends SubMenu {
    private final GameSelectGUI gui;

    public TowerBridgeMenu(GameSelectGUI gui) {
        super(GameRegistry.getInstance().getGameMode("towerbridge").reference().clone().setCustomName(TextFormat.RESET.toString() + TextFormat.DARK_AQUA + GameRegistry.getInstance().getGameMode("towerbridge").name()).setLore());
        this.gui = gui;
    }

    @Override
    public void sync() {
        var player = gui.player;
        var inv = gui.inventory;

        Window.fillVerticalLine(9, inv, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillVerticalLine(17, inv, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillHorizontalLine(45, inv, new BlockCopperBars().toItem().setCustomName("§r"));

        var registry = GameRegistry.getInstance();
        var towerBridge = registry.getGameMode("TowerBridge");
        List<TowerBridgeGame> games = registry.getGames(towerBridge).stream()
                .filter(g -> g instanceof TowerBridgeGame)
                .map(g -> (TowerBridgeGame) g)
                .filter(g -> !g.getBlacklist().contains(player.getName()))
                .filter(g -> !g.isPrivate() || g.getWhitelist().contains(player.getName()))
                .sorted(Comparator.comparing(TowerBridgeGame::isPrivate).thenComparingInt(g -> -(g.getSpectators().size() + g.getPlayers().size())))
                .toList();

        for (Game game : games) {
            if (!(game instanceof TowerBridgeGame g)) continue;

            var item = new BlockTerracotta(DyeColor.WHITE).toItem();
            item.setCustomName(game.isAutomatedHost() ? player.processTranslation(TranslationKeys.PLAYER_LOBBY_GAMES_GUI_TOWERFAST_AUTO_NAME) : player.processTranslation(TranslationKeys.PLAYER_LOBBY_GAMES_GUI_TOWERFAST_NAME, game.getHoster()));
            item.setLore(player.processTranslation(TranslationKeys.PLAYER_LOBBY_GAMES_GUI_TOWERFAST_LORE, g.getIdentifier(), game.getState().equals(Game.State.LOBBY) ? game.getSpectators().size() : game.getPlayers().size(), g.getParameters().maxPlayers * 2));

            var slot = Window.nextSlot(inv);
            inv.setItem(slot, item).onClick((click) -> {
                inv.close(player);
                player.sudo("game join " + g.getGameId().toLowerCase());
            });
        }

        if (player.hasPermission(Permission.GAME_HOSTER.toString())) {
            var item = new ItemBookAndQuill();
            item.setCustomName(player.processTranslation(TranslationKeys.PLAYER_LOBBY_GAMES_GUI_TOWERFAST_CREATE_NAME));
            var slot = Window.nextSlot(inv);
            inv.setItem(slot, item).onClick((click) -> {
                inv.close(player);
                player.sudo("game create towerbridge");
            });
        }
    }
}