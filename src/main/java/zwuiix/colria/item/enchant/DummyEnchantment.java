package zwuiix.colria.item.enchant;

import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.item.enchantment.EnchantmentRarity;
import cn.nukkit.item.enchantment.EnchantmentType;
import org.jetbrains.annotations.NotNull;

public class DummyEnchantment extends Enchantment {
    public DummyEnchantment() {
        super(
                0,
                "dummy",
                "Dummy Enchantment",
                EnchantmentRarity.COMMON,
                EnchantmentType.ALL
        );
    }
}
