package zwuiix.colria.gui.shop;

import cn.nukkit.block.BlockCopperBars;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemDyeLightGray;
import cn.nukkit.item.ItemDyeLime;
import zwuiix.colria.inventory.VirtualInventory;
import zwuiix.colria.inventory.impl.EntityInventory;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Window;

public class SettingsGUI {
    private final EnginePlayer player;
    public VirtualInventory inventory;

    public SettingsGUI(EnginePlayer player) {
        this.player = player;
        this.inventory = new EntityInventory(9 * 4, player.processTranslation(TranslationKeys.PLAYER_SETTINGS_GUI_TITLE));
    }

    public void send() {
        syncContents();
        inventory.open(player);
    }

    public void syncContents() {
        Window.fillVerticalLine(0, inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillHorizontalLine(0, inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillVerticalLine(8, inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillHorizontalLine(27, inventory, new BlockCopperBars().toItem().setCustomName("§r"));


    }
}
