package zwuiix.colria.gui;

import cn.nukkit.block.BlockCopperBars;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemDyeLightGray;
import cn.nukkit.item.ItemDyeLime;
import zwuiix.colria.inventory.VirtualInventory;
import zwuiix.colria.inventory.impl.EntityInventory;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Window;

public class PurchaseGUI {
    private final EnginePlayer player;
    public VirtualInventory inventory;

    private final Item reference;
    private final long cost;
    private final Runnable onPurchase;
    private final Runnable onClose;

    public PurchaseGUI(String title, EnginePlayer player, Item reference, long cost, Runnable onPurchase, Runnable onClose) {
        this.player = player;
        this.inventory = new EntityInventory(54, title);

        this.reference = reference;
        this.cost = cost;
        this.onPurchase = onPurchase;
        this.onClose = onClose;
    }

    public void send() {
        syncContents();
        inventory.open(player);
    }

    public void syncContents() {
        Window.fillVerticalLine(0, inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillHorizontalLine(0, inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillVerticalLine(8, inventory, new BlockCopperBars().toItem().setCustomName("§r"));
        Window.fillHorizontalLine(45, inventory, new BlockCopperBars().toItem().setCustomName("§r"));

        inventory.setItem(22, reference);
        inventory.setItem(30, new ItemDyeLime().setCustomName(player.processTranslation(TranslationKeys.PURCHASE_GUI_BUY, cost, player.getPlayerDataInfo().getShards())))
                .onClick((click -> {
                    inventory.close(player);
                    onPurchase.run();
                }));
        inventory.setItem(32, new ItemDyeLightGray().setCustomName(player.processTranslation(TranslationKeys.PURCHASE_GUI_CANCEL)))
                .onClick((click -> {;
                    inventory.close(player);
                    onClose.run();
                }));
    }
}
