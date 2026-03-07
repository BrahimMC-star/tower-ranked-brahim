package zwuiix.colria.game.gui.sub;

import cn.nukkit.block.*;
import cn.nukkit.item.*;
import cn.nukkit.level.Sound;
import cn.nukkit.utils.TextFormat;
import zwuiix.colria.EngineInfo;
import zwuiix.colria.game.component.types.DiscordComponent;
import zwuiix.colria.game.gui.GameSettingsGUI;
import zwuiix.colria.game.impl.team.TeamGame;
import zwuiix.colria.game.impl.team.TeamGameParameters;
import zwuiix.colria.game.impl.tower.TowerGameParameters;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Glyph;
import zwuiix.colria.util.Window;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.LongStream;

import static java.util.Map.entry;

public class DiscordMenu extends SubMenu {
    private final GameSettingsGUI parent;

    public DiscordMenu(GameSettingsGUI parent) {
        super(new ItemEchoShard().setCustomName(parent.player.processTranslation(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_DISCORD_NAME)));
        this.parent = parent;
    }

    @Override
    public void sync() {
        var game = parent.game;
        var player = parent.player;

        String on = t(TranslationKeys.COMMON_ON);
        String off = t(TranslationKeys.COMMON_OFF);

        Window.fillVerticalLine(9, parent.inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillVerticalLine(17, parent.inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillHorizontalLine(45, parent.inventory, new BlockCopperBars().toItem().setCustomName("§r"));

        if (!game.hasComponent(DiscordComponent.class)) {
            Window.fill(parent.inventory, new BlockCopperBars().toItem().setCustomName("§r"));

            var center = 18;
            var item = new ItemBannerPattern().setCustomName(t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_DISCORD_CLICK_FOR_ENABLE));

            parent.inventory.setItem(center, item).onClick(click -> {
                var info = player.getPlayerDataInfo();
                var discordId = info.getDiscordId();
                if (discordId.isEmpty()) {
                    click.inventory().close(player);
                    player.sendMessage(t(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_DISCORD_NO_ID));
                    player.addSound(Sound.MOB_VILLAGER_NO,0.5f, 1f);
                    return;
                }

                for (var p : game.getSpectators().values()) {
                    var sInfo = p.getPlayerDataInfo();
                    if (sInfo.getDiscordId().isEmpty()) {
                        click.inventory().close(player);
                        player.sendMessage(TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_DISCORD_SPECTATOR_NO_ID, p.getName());
                        player.addSound(Sound.MOB_VILLAGER_NO,0.5f, 1f);
                        return;
                    }
                }

                game.addComponent(new DiscordComponent((TeamGame) game));
                parent.syncContents();
            });
            return;
        }

        var discord = game.tryGetComponent(DiscordComponent.class);

    }

    private String t(TranslationKeys k) { return parent.player.processTranslation(k); }

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

    private void addToggle(Item icon,
                           Supplier<Boolean> getter,
                           Consumer<Boolean> setter) {
        parent.inventory.setItem(
                Window.nextSlot(parent.inventory),
                icon
        ).onClick(click -> {
            setter.accept(!getter.get());
            parent.syncContents();
        });
    }
}
