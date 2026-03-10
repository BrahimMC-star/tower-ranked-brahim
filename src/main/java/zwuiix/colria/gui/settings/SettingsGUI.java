package zwuiix.colria.gui.settings;

import cn.nukkit.block.BlockBeacon;
import cn.nukkit.block.BlockCopperBars;
import cn.nukkit.block.BlockHangingSign;
import cn.nukkit.block.BlockOakHangingSign;
import cn.nukkit.item.*;
import cn.nukkit.network.protocol.types.DisplaySlot;
import cn.nukkit.utils.TextFormat;
import zwuiix.colria.EngineInfo;
import zwuiix.colria.inventory.VirtualInventory;
import zwuiix.colria.inventory.impl.EntityInventory;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.BossBar;
import zwuiix.colria.util.Glyph;
import zwuiix.colria.util.Window;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SettingsGUI {
    private final EnginePlayer player;
    public VirtualInventory inventory;

    public SettingsGUI(EnginePlayer player) {
        this.player = player;
        this.inventory = new EntityInventory(9 * 3, player.processTranslation(TranslationKeys.PLAYER_SETTINGS_GUI_TITLE));

        // Scoreboard
        //Boss bar
        //Enable Join/Quit Messages
    }

    public void send() {
        syncContents();
        inventory.open(player);
    }

    public void syncContents() {
        String on = t(TranslationKeys.COMMON_ON);
        String off = t(TranslationKeys.COMMON_OFF);

        for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, Item.AIR_ITEM, false);

        Window.fillVerticalLine(0, inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillHorizontalLine(0, inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillVerticalLine(8, inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillHorizontalLine(27, inventory, new BlockCopperBars().toItem().setCustomName("§r"));

        var playerData = player.getPlayerDataInfo();
        var settings = playerData.getSettings();

        var fps = new BlockBeacon().toItem()
                .setCustomName(t(TranslationKeys.PLAYER_SETTINGS_GUI_FPS_NAME))
                .setLore(
                        t(TranslationKeys.PLAYER_SETTINGS_GUI_FPS_LORE),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        makeLore((Boolean) settings.getOrDefault("fps", "enabled", false), on, off),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                );

        var cps = new ItemSwordDiamond()
                .setCustomName(t(TranslationKeys.PLAYER_SETTINGS_GUI_CPS_NAME))
                .setLore(
                        t(TranslationKeys.PLAYER_SETTINGS_GUI_CPS_LORE),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        makeLore((Boolean) settings.getOrDefault("cps", "enabled", false), on, off),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                );

        var ping = new ItemRail()
                .setCustomName(t(TranslationKeys.PLAYER_SETTINGS_GUI_PING_NAME))
                .setLore(
                        t(TranslationKeys.PLAYER_SETTINGS_GUI_PING_LORE),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        makeLore((Boolean) settings.getOrDefault("ping", "enabled", false), on, off),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                );

        var privateMessage = new ItemPaper()
                .setCustomName(t(TranslationKeys.PLAYER_SETTINGS_GUI_PRIVATE_MESSAGES_NAME))
                .setLore(
                        t(TranslationKeys.PLAYER_SETTINGS_GUI_PRIVATE_MESSAGES_LORE),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        makeLore((Boolean) settings.getOrDefault("private_message", "enabled", true), on, off),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                );

        var scoreboard = new ItemArmorStand()
                .setCustomName(t(TranslationKeys.PLAYER_SETTINGS_GUI_SCOREBOARD_NAME))
                .setLore(
                        t(TranslationKeys.PLAYER_SETTINGS_GUI_SCOREBOARD_LORE),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        makeLore((Boolean) settings.getOrDefault("scoreboard", "enabled", true), on, off),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                );

        var bossBar = new BlockOakHangingSign().toItem()
                .setCustomName(t(TranslationKeys.PLAYER_SETTINGS_GUI_BOSS_BAR_NAME))
                .setLore(
                        t(TranslationKeys.PLAYER_SETTINGS_GUI_BOSS_BAR_LORE),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        makeLore((Boolean) settings.getOrDefault("boss_bar", "enabled", true), on, off),
                        Glyph.hbarThick(EngineInfo.COLOR, 1),
                        t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_SELECT)
                );

        addToggle(fps, () -> (Boolean) playerData.getSettings().getOrDefault("fps", "enabled", false), v -> {
            settings.set("fps", "enabled", v);
            playerData.setSettings(settings);
            syncContents();
        });
        addToggle(cps, () -> (Boolean) playerData.getSettings().getOrDefault("cps", "enabled", false), v -> {
            settings.set("cps", "enabled", v);
            playerData.setSettings(settings);
            syncContents();
        });
        addToggle(ping, () -> (Boolean) playerData.getSettings().getOrDefault("ping", "enabled", false), v -> {
            settings.set("ping", "enabled", v);
            playerData.setSettings(settings);
            syncContents();
        });
        addToggle(privateMessage, () -> (Boolean) playerData.getSettings().getOrDefault("private_message", "enabled", true), v -> {
            settings.set("private_message", "enabled", v);
            playerData.setSettings(settings);
            syncContents();
        });
        addToggle(scoreboard, () -> (Boolean) playerData.getSettings().getOrDefault("scoreboard", "enabled", true), v -> {
            settings.set("scoreboard", "enabled", v);
            playerData.setSettings(settings);
            syncContents();

            if (v) {
                player.scoreboard.addViewer(player, DisplaySlot.SIDEBAR);
            } else player.scoreboard.removeViewer(player, DisplaySlot.SIDEBAR);
        });
        addToggle(bossBar, () -> (Boolean) playerData.getSettings().getOrDefault("boss_bar", "enabled", true), v -> {
            settings.set("boss_bar", "enabled", v);
            playerData.setSettings(settings);
            syncContents();
        });
    }

    private String t(TranslationKeys k) { return player.processTranslation(k); }

    private <E extends Enum<E>> String makeLoreEnum(List<E> choices, Map<E, TranslationKeys> dict, E current) {
        StringBuilder sb = new StringBuilder("&r&7\n");
        for (E choice : choices) {
            sb.append("&7&8")
                    .append(Glyph.vbar(TextFormat.DARK_GRAY, 1))
                    .append(" ")
                    .append((choice == current) ? "&a&l" : "&c")
                    .append(t(dict.get(choice)))
                    .append("&r&7\n");
        }
        return TextFormat.colorize(sb.toString());
    }

    private static <T> String makeLore(List<T> choices, T current) {
        StringBuilder sb = new StringBuilder("&r&7\n");
        for (T c : choices) {
            boolean sel = c.equals(current);
            sb.append("&7&8")
                    .append(Glyph.vbar(TextFormat.DARK_GRAY, 1))
                    .append(" ")
                    .append(sel ? "&a" : "&c")
                    .append(c)
                    .append(sel ? "&r&7\n" : "&7\n");
        }
        return TextFormat.colorize(sb.toString());
    }

    public static String makeLore(Boolean current, String enabled, String disabled) {
        StringBuilder sb = new StringBuilder("&r&7\n");
        if (current) {
            sb.append("&7&8&l").append(Glyph.vbar(TextFormat.DARK_GRAY, 1)).append(" &a").append(enabled).append("&r&7\n");
            sb.append("&7&8").append(Glyph.vbar(TextFormat.DARK_GRAY, 1)).append(" &c").append(disabled).append("&r&7\n");
        } else {
            sb.append("&7&8").append(Glyph.vbar(TextFormat.DARK_GRAY, 1)).append(" &c").append(enabled).append("&r&7\n");
            sb.append("&7&8&l").append(Glyph.vbar(TextFormat.DARK_GRAY, 1)).append(" &a").append(disabled).append("&r&7\n");
        }
        return TextFormat.colorize(sb.toString());
    }

    private static <E extends Enum<E>> List<E> choicesOf(Class<E> enumType) {
        return Arrays.asList(enumType.getEnumConstants());
    }

    private static <T extends Comparable<? super T>> T nextOf(List<T> choices, T current) {
        int i = Collections.binarySearch(choices, current);
        int next = (i >= 0 ? i + 1 : -i - 1) % choices.size();
        return choices.get(next);
    }

    private <T extends Comparable<? super T>> void addCycler(
            Item item, List<T> choices, Supplier<T> getter, Consumer<T> setter
    ) {
        inventory.setItem(Window.nextSlot1(inventory), item).onClick(click -> {
            setter.accept(nextOf(choices, getter.get()));
            syncContents();
        });
    }

    private void addToggle(Item icon,
                           Supplier<Boolean> getter,
                           Consumer<Boolean> setter) {
        inventory.setItem(
                Window.nextSlot1(inventory),
                icon
        ).onClick(click -> {
            setter.accept(!getter.get());
            syncContents();
        });
    }
}
