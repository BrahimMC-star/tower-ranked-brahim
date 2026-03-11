package zwuiix.colria.gui;

import cn.nukkit.block.BlockBeacon;
import cn.nukkit.block.BlockCopperBars;
import cn.nukkit.block.BlockOakHangingSign;
import cn.nukkit.item.*;
import cn.nukkit.network.protocol.types.DisplaySlot;
import cn.nukkit.utils.TextFormat;
import zwuiix.colria.EngineInfo;
import zwuiix.colria.game.GameRegistry;
import zwuiix.colria.inventory.VirtualInventory;
import zwuiix.colria.inventory.impl.EntityInventory;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Glyph;
import zwuiix.colria.util.Window;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StatsGUI {
    private final EnginePlayer player;
    private final EnginePlayer target;
    public VirtualInventory inventory;

    public StatsGUI(EnginePlayer player, EnginePlayer target) {
        this.player = player;
        this.target = target;
        this.inventory = new EntityInventory(9 * 5, player.processTranslation(TranslationKeys.PLAYER_STATS_GUI_TITLE, target.getName()));
    }

    public void send() {
        syncContents();
        inventory.open(player);
    }

    public void syncContents() {
        Window.fillVerticalLine(0, inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillHorizontalLine(0, inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillVerticalLine(8, inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillHorizontalLine(inventory.getSize() - 9, inventory, new BlockCopperBars().toItem().setCustomName("§r"));

        var gamemodes = GameRegistry.getInstance().getGameModes();
        for (GameRegistry.GameMode gm : gamemodes) {
            var item = gm.reference().setCustomName(TextFormat.RESET.toString() + EngineInfo.COLOR + gm.name());
            item.setLore(switch (gm.name()) {
                case "TowerFast" -> makeTowerLore("towerfast");
                case "TowerBridge" -> makeTowerLore("towerbridge");
                default -> "";
            });

            inventory.setItem(Window.nextSlot1(inventory), item);
        }

        Window.fill(inventory, new BlockCopperBars().toItem().setCustomName("§r"));
    }

    private String translate(TranslationKeys key, Object ...args) { return player.processTranslation(key, args); }

    private String makeTowerLore(String mode) {
        var builder = new StringBuilder();

        var stats = target.getPlayerDataInfo().getStats();

        var unrankedPlays = (int)stats.getOrDefault(mode, "plays", 0);
        var unrankedWins = (int)stats.getOrDefault(mode, "wins", 0);
        float unrankedWinrate = unrankedPlays == 0 ? 0 : (float) ((unrankedWins * 100.0) / unrankedPlays);

        var unrankedKills = (int)stats.getOrDefault(mode, "kills", 0);
        var unrankedDeaths = (int)stats.getOrDefault(mode, "deaths", 0);
        var unrankedAssists = (int)stats.getOrDefault(mode, "assists", 0);
        var unrankedRatio = unrankedDeaths == 0 ? unrankedKills : ((float) unrankedKills / (float) unrankedDeaths);

        var unrankedHits = (int)stats.getOrDefault(mode, "hits", 0);
        var unrankedCrits = (int)stats.getOrDefault(mode, "crits", 0);

        var rankedPlays = (int)stats.getOrDefault(mode + "_ranked", "plays", 0);
        var rankedWins = (int)stats.getOrDefault(mode + "_ranked", "wins", 0);
        float rankedWinrate = rankedPlays == 0 ? 0 : (float) ((rankedWins * 100.0) / rankedPlays);

        var rankedKills = (int)stats.getOrDefault(mode + "_ranked", "kills", 0);
        var rankedDeaths = (int)stats.getOrDefault(mode + "_ranked", "deaths", 0);
        var rankedAssists = (int)stats.getOrDefault(mode + "_ranked", "assists", 0);
        var rankedRatio = rankedDeaths == 0 ? rankedKills : ((float) rankedKills / (float) rankedDeaths);

        var rankedHits = (int)stats.getOrDefault(mode + "_ranked", "hits", 0);
        var rankedCrits = (int)stats.getOrDefault(mode + "_ranked", "crits", 0);

        return builder
                .append(translate(TranslationKeys.PLAYER_STATS_GUI_TOWERFAST_LORE,
                        // unranked
                        unrankedPlays,
                        unrankedWinrate,
                        TextFormat.GREEN.toString() + unrankedKills + "&8/" + TextFormat.RED + unrankedDeaths + "&8/" + TextFormat.AQUA + unrankedAssists,
                        unrankedRatio,
                        unrankedHits,
                        unrankedCrits,

                        // ranked
                        rankedPlays,
                        rankedWinrate,
                        TextFormat.GREEN.toString() + rankedKills + "&8/" + TextFormat.RED + rankedDeaths + "&8/" + TextFormat.AQUA + rankedAssists,
                        rankedRatio,
                        rankedHits,
                        rankedCrits
                ))
                .append("\n")
                .toString().replace("{BAR}", Glyph.vbar(EngineInfo.COLOR, 1));
    }
}
