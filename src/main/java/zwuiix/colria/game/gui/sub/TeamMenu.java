package zwuiix.colria.game.gui.sub;

import cn.nukkit.block.BlockCopperBars;
import cn.nukkit.item.*;
import cn.nukkit.item.enchantment.EnchantmentDurability;
import cn.nukkit.utils.TextFormat;
import zwuiix.colria.game.gui.GameSettingsGUI;
import zwuiix.colria.game.impl.team.Team;
import zwuiix.colria.game.impl.team.TeamColor;
import zwuiix.colria.game.impl.team.TeamGame;
import zwuiix.colria.inventory.VirtualInventory;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Window;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TeamMenu extends SubMenu {
    private final GameSettingsGUI parent;
    private int index = 0; // 0 = teamA, 1 = teamB

    public TeamMenu(GameSettingsGUI parent) {
        super(new ItemChestplateCopper().setCustomName(
                parent.player.processTranslation(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_TEAMS_NAME)
        ));
        this.parent = parent;
    }

    @Override
    public void sync() {
        final TeamGame game = (TeamGame) parent.game;

        Window.fillVerticalLine(19, parent.inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillHorizontalLine(45, parent.inventory, new BlockCopperBars().toItem().setCustomName("§r"));

        ItemArmorStand switchItem = (ItemArmorStand) new ItemArmorStand()
                .setCustomName(parent.player.processTranslation(
                        TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_TEAMS_SELECT_OTHER, index + 1
                ));
        parent.inventory.setItem(18, switchItem).onClick(click -> {
            index ^= 1; // toggle 0<->1
            parent.syncContents();
        });

        ItemEmerald teams = (ItemEmerald) new ItemEmerald()
                .setCustomName(parent.player.processTranslation(
                        TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_TEAMS_SELECT_NAME))
                .setLore(parent.player.processTranslation(
                        TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_TEAMS_SELECT_LORE,
                        game.getTeamA().color() + parent.player.processTranslation(game.getTeamA().name()),
                        game.getTeamB().color() + parent.player.processTranslation(game.getTeamB().name())
                ));
        parent.inventory.setItem(27, teams);

        ItemEnderEye random = (ItemEnderEye) new ItemEnderEye()
                .setCustomName(parent.player.processTranslation(
                        TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_TEAMS_RANDOM_NAME));
        parent.inventory.setItem(36, random).onClick(click -> {
            Team teamA = TeamColor.ALL.get(ThreadLocalRandom.current().nextInt(TeamColor.ALL.size()));
            Team teamB = TeamColor.oppositeOf(teamA);
            game.setTeamA(teamA);
            game.setTeamB(teamB);
            game.getTeams().clear();
            game.getTeamSelector().syncContents();
            parent.syncContents();
        });

        final Team blocked = (index == 0) ? game.getTeamB() : game.getTeamA();
        final Team current = (index == 0) ? game.getTeamA() : game.getTeamB();

        final List<Team> all = TeamColor.ALL;
        for (int k = 0; k < all.size(); k++) {
            int slot = slotForIndex(k);
            if (slot < 0) break;

            Team color = all.get(k);
            Item item = color.reference().clone();

            boolean isBlocked = color.equals(current);
            if(isBlocked) item.addEnchantment(new EnchantmentDurability().setLevel(1)));

            if(color.equals(blocked)) {
                item = Item.get("barrier");
                item.addEnchantment(new EnchantmentDurability().setLevel(1)));
                isBlocked = true;
            }

            item.setCustomName(TextFormat.RESET + color.color() + parent.player.processTranslation(color.name()));

            VirtualInventory.SlotSetResult res = parent.inventory.setItem(slot, item);
            if (!isBlocked) {
                res.onClick(click -> {
                    if (index == 0) game.setTeamA(color);
                    else game.setTeamB(color);
                    parent.syncContents();
                });
            }
        }
    }

    private static int slotForIndex(int k) {
        int row = k / 7;
        if (row >= 3) return -1;
        int col = k % 7;
        return 20 + row * 9 + col;
    }
}
