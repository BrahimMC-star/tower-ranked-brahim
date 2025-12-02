package zwuiix.colria.item;

import cn.nukkit.item.Item;
import cn.nukkit.item.customitem.CustomItem;
import cn.nukkit.registry.Registries;
import zwuiix.colria.Loader;

import java.util.HashMap;

public class ItemRegistry {
    private static ItemRegistry INSTANCE = new ItemRegistry();

    public static ItemRegistry getInstance() { return INSTANCE; }

    private HashMap<String, Item> items = new HashMap<>();

    public HashMap<String, Item> getItems() {
        return items;
    }

    public ItemRegistry() {
        INSTANCE = this;
    }

    public Item getItem(String identifier) {
        return items.get(identifier);
    }

    public void register(CustomItem item) {
        items.put(item.getNamespaceId(), (Item) item);
        Registries.ITEM.registerCustom(item.getClass());
    }

    public void invoke(Loader loader) {
        register(new ItemCosmeticArrowQuiver());
        register(new ItemCosmeticBandolier());
        register(new ItemCosmeticCakeManPlushie());
        register(new ItemCosmeticCaptainHat());
        register(new ItemCosmeticChristmasHat());
        register(new ItemCosmeticCowboyHat());
        register(new ItemCosmeticCrown());
        register(new ItemCosmeticDragonSkull());
        register(new ItemCosmeticHermesBoots());
        register(new ItemCosmeticHolsteredBelt());
        register(new ItemCosmeticHorns());
        register(new ItemCosmeticKasaHat());
        register(new ItemCosmeticPickelHaube());
        register(new ItemCosmeticPirateHat());
        register(new ItemCosmeticPumpkinHat());
        register(new ItemCosmeticSheathedKatana());
        register(new ItemCosmeticSkull());
        register(new ItemCosmeticSombrero());
        register(new ItemCosmeticStrawHat());
        register(new ItemCosmeticTopHat());
        register(new ItemCosmeticWings());
        register(new ItemCosmeticWizardsHat());

    }
}
