package zwuiix.colria.item;

import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustomArmor;
import cn.nukkit.network.protocol.types.inventory.creative.CreativeItemCategory;

public class ItemCosmetic extends ItemCustomArmor {
    public ItemCosmetic(String identifier, String name) {
        super(identifier, name, identifier);
    }

    @Override
    public CustomItemDefinition getDefinition() {
        return CustomItemDefinition
                .armorBuilder(this, CreativeItemCategory.ITEMS)
                .build();
    }

    @Override
    public boolean isHelmet() {
        return true;
    }

    @Override
    public boolean isChestplate() {
        return true;
    }

    @Override
    public boolean isLeggings() {
        return true;
    }

    @Override
    public boolean isBoots() {
        return true;
    }
}
