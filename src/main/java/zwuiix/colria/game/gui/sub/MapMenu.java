package zwuiix.colria.game.gui.sub;

import cn.nukkit.Server;
import cn.nukkit.block.BlockCopperBars;
import cn.nukkit.block.BlockMoss;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.GameLevel;
import zwuiix.colria.game.GameRegistry;
import zwuiix.colria.game.gui.GameSettingsGUI;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Window;

public class MapMenu extends SubMenu {
    private final GameSettingsGUI parent;

    public MapMenu(GameSettingsGUI parent) {
        super(new BlockMoss().toItem().setCustomName(parent.player.processTranslation(
                TranslationKeys.PLAYER_GAME_CONFIGURATIONS_GUI_MAP_NAME)));
        this.parent = parent;
    }

    @Override
    public void sync() {
        Game g = parent.game;

        Window.fillVerticalLine(9, parent.inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillVerticalLine(17, parent.inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillHorizontalLine(45, parent.inventory, new BlockCopperBars().toItem().setCustomName("§r"));

        GameRegistry.GameMode gameMode = GameRegistry.getInstance().getGameMode(g.getName());
        if(gameMode != null) {
            for (GameLevel level : gameMode.levels()) {
                String name = level.name;
                String fixedName = (name == null) ? null
                        : (name.length() <= 2) ? ""
                        : name.substring(2, 3).toUpperCase(java.util.Locale.ROOT) + name.substring(3);

                Item item = level.reference.clone().setCustomName(TextFormat.RESET + fixedName);
                //if(g.getGameLevel().getFolderName().equalsIgnoreCase(name)) //item.addEnchantment(new EnchantmentDurability().setLevel(1));

                parent.inventory.setItem(Window.nextSlot(parent.inventory), item)
                        .onClick((click) -> {
                            g.setGameLevel(Server.getInstance().getLevelByName(level.name));
                            parent.syncContents();
                        }
                );
            }
        }
    }
}
