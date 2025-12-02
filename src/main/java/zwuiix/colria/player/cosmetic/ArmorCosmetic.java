package zwuiix.colria.player.cosmetic;

import cn.nukkit.item.Item;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

public class ArmorCosmetic extends Cosmetic {
    public static final int SLOT_HELMET = 0;
    public static final int SLOT_CHESTPLATE = 1;
    public static final int SLOT_LEGGINGS = 2;
    public static final int SLOT_BOOTS = 3;

    private int slot;

    ArmorCosmetic(String identifier, TranslationKeys name, TranslationKeys description, Item reference, long cost, int slot) {
        super(identifier, name, description, reference, cost);
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }

    @Override
    public void apply(EnginePlayer player) {
        var item = this.getReference().clone().setCustomName(player.processTranslation(getName()));
        item.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);
        player.getInventory().setArmorItem(this.slot, item);
        player.setNeedSendInventory(true);
    }
}
