package zwuiix.colria.player.cosmetic;

import cn.nukkit.block.BlockHeadPlayer;
import cn.nukkit.entity.data.property.EntityProperty;
import cn.nukkit.entity.data.property.EnumEntityProperty;
import zwuiix.colria.Loader;
import zwuiix.colria.item.*;
import zwuiix.colria.translator.TranslationKeys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CosmeticRegistry {
    private static CosmeticRegistry INSTANCE = new CosmeticRegistry();

    public static CosmeticRegistry getInstance() {
        return INSTANCE;
    }

    private HashMap<String, Cosmetic> cosmetics = new HashMap<>();
    private HashMap<String, CapeCosmetic> capes = new HashMap<>();

    public HashMap<String, Cosmetic> getCosmetics() {
        return cosmetics;
    }

    public Cosmetic getCosmetic(String identifier) {
        return cosmetics.get(identifier);
    }

    public void register(Cosmetic cosmetic) {
        cosmetics.put(cosmetic.getIdentifier(), cosmetic);
    }

    public HashMap<String, CapeCosmetic> getCapes() {
        return capes;
    }

    public CapeCosmetic getCape(String identifier) {
        return capes.get(identifier);
    }

    public void register(CapeCosmetic cape) {
        capes.put(cape.getIdentifier(), cape);
    }

    public void invoke(Loader loader) {
        register(new ArmorCosmetic("arrow_quiver", TranslationKeys.COSMETIC_ARROW_QUIVER_NAME, TranslationKeys.COSMETIC_ARROW_QUIVER_LORE, new ItemCosmeticArrowQuiver(), 1000, ArmorCosmetic.SLOT_CHESTPLATE));
        register(new ArmorCosmetic("bandolier", TranslationKeys.COSMETIC_BANDOLIER_NAME, TranslationKeys.COSMETIC_BANDOLIER_LORE, new ItemCosmeticBandolier(), 1500, ArmorCosmetic.SLOT_CHESTPLATE));
        register(new ArmorCosmetic("cakeman_plushie", TranslationKeys.COSMETIC_CAKEMAN_PLUSHIE_NAME, TranslationKeys.COSMETIC_CAKEMAN_PLUSHIE_LORE, new ItemCosmeticCakeManPlushie(), 2000, ArmorCosmetic.SLOT_HELMET));
        register(new ArmorCosmetic("captain_hat", TranslationKeys.COSMETIC_CAPTAIN_HAT_NAME, TranslationKeys.COSMETIC_CAPTAIN_HAT_LORE, new ItemCosmeticCaptainHat(), 3000, ArmorCosmetic.SLOT_HELMET));
        register(new ArmorCosmetic("christmas_hat", TranslationKeys.COSMETIC_CHRISTMAS_HAT_NAME, TranslationKeys.COSMETIC_CHRISTMAS_HAT_LORE, new ItemCosmeticChristmasHat(), 3000, ArmorCosmetic.SLOT_HELMET));
        register(new ArmorCosmetic("cowboy_hat", TranslationKeys.COSMETIC_COWBOY_HAT_NAME, TranslationKeys.COSMETIC_COWBOY_HAT_LORE, new ItemCosmeticCowboyHat(), 2500, ArmorCosmetic.SLOT_HELMET));
        register(new ArmorCosmetic("crown", TranslationKeys.COSMETIC_CROWN_NAME, TranslationKeys.COSMETIC_CROWN_LORE, new ItemCosmeticCrown(), 9000, ArmorCosmetic.SLOT_HELMET));
        register(new ArmorCosmetic("dragon_skull", TranslationKeys.COSMETIC_DRAGON_SKULL_NAME, TranslationKeys.COSMETIC_DRAGON_SKULL_LORE, new ItemCosmeticDragonSkull(), 10000, ArmorCosmetic.SLOT_HELMET));
        register(new ArmorCosmetic("hermes_boots", TranslationKeys.COSMETIC_HERMES_BOOTS_NAME, TranslationKeys.COSMETIC_HERMES_BOOTS_LORE, new ItemCosmeticHermesBoots(), 7000, ArmorCosmetic.SLOT_BOOTS));
        register(new ArmorCosmetic("holstered_belt", TranslationKeys.COSMETIC_HOLSTERED_BELT_NAME, TranslationKeys.COSMETIC_HOLSTERED_BELT_LORE, new ItemCosmeticHolsteredBelt(), 2500, ArmorCosmetic.SLOT_LEGGINGS));
        register(new ArmorCosmetic("horns", TranslationKeys.COSMETIC_HORNS_NAME, TranslationKeys.COSMETIC_HORNS_LORE, new ItemCosmeticHorns(), 5000, ArmorCosmetic.SLOT_HELMET));
        register(new ArmorCosmetic("kasa_hat", TranslationKeys.COSMETIC_KASA_HAT_NAME, TranslationKeys.COSMETIC_KASA_HAT_LORE, new ItemCosmeticKasaHat(), 2500, ArmorCosmetic.SLOT_HELMET));
        register(new ArmorCosmetic("pickel_haube", TranslationKeys.COSMETIC_PICKEL_HAUBE_NAME, TranslationKeys.COSMETIC_PICKEL_HAUBE_LORE, new ItemCosmeticPickelHaube(), 5500, ArmorCosmetic.SLOT_HELMET));
        register(new ArmorCosmetic("pirate_hat", TranslationKeys.COSMETIC_PIRATE_HAT_NAME, TranslationKeys.COSMETIC_PIRATE_HAT_LORE, new ItemCosmeticPirateHat(), 3500, ArmorCosmetic.SLOT_HELMET));
        register(new ArmorCosmetic("pumpkin_hat", TranslationKeys.COSMETIC_PUMPKIN_HAT_NAME, TranslationKeys.COSMETIC_PUMPKIN_HAT_LORE, new ItemCosmeticPumpkinHat(), 2000, ArmorCosmetic.SLOT_HELMET));
        register(new ArmorCosmetic("sheathed_katana", TranslationKeys.COSMETIC_SHEATHED_KATANA_NAME, TranslationKeys.COSMETIC_SHEATHED_KATANA_LORE, new ItemCosmeticSheathedKatana(), 6000, ArmorCosmetic.SLOT_CHESTPLATE));
        register(new ArmorCosmetic("skull", TranslationKeys.COSMETIC_SKULL_NAME, TranslationKeys.COSMETIC_SKULL_LORE, new ItemCosmeticSkull(), 2000, ArmorCosmetic.SLOT_HELMET));
        register(new ArmorCosmetic("sombrero", TranslationKeys.COSMETIC_SOMBRERO_NAME, TranslationKeys.COSMETIC_SOMBRERO_LORE, new ItemCosmeticSombrero(), 2000, ArmorCosmetic.SLOT_HELMET));
        register(new ArmorCosmetic("straw_hat", TranslationKeys.COSMETIC_STRAW_HAT_NAME, TranslationKeys.COSMETIC_STRAW_HAT_LORE, new ItemCosmeticStrawHat(), 1500, ArmorCosmetic.SLOT_HELMET));
        register(new ArmorCosmetic("top_hat", TranslationKeys.COSMETIC_TOP_HAT_NAME, TranslationKeys.COSMETIC_TOP_HAT_LORE, new ItemCosmeticTopHat(), 6500, ArmorCosmetic.SLOT_HELMET));
        register(new ArmorCosmetic("wings", TranslationKeys.COSMETIC_WINGS_NAME, TranslationKeys.COSMETIC_WINGS_LORE, new ItemCosmeticWings(), 10000, ArmorCosmetic.SLOT_CHESTPLATE));
        register(new ArmorCosmetic("wizards_hat", TranslationKeys.COSMETIC_WIZARDS_HAT_NAME, TranslationKeys.COSMETIC_WIZARDS_HAT_LORE, new ItemCosmeticWizardsHat(), 9500, ArmorCosmetic.SLOT_HELMET));

        register(new CapeCosmetic("colria",       TranslationKeys.COSMETIC_CAPE_COLRIA_NAME,       TranslationKeys.COSMETIC_CAPE_COLRIA_LORE,       new BlockHeadPlayer().toItem(), 1500,  false)); // très cheap
        register(new CapeCosmetic("bobwhite",     TranslationKeys.COSMETIC_CAPE_BOBWHITE_NAME,     TranslationKeys.COSMETIC_CAPE_BOBWHITE_LORE,     new BlockHeadPlayer().toItem(), 2200,  false));
        register(new CapeCosmetic("penguin",      TranslationKeys.COSMETIC_CAPE_PENGUIN_NAME,      TranslationKeys.COSMETIC_CAPE_PENGUIN_LORE,      new BlockHeadPlayer().toItem(), 2600,  false));
        register(new CapeCosmetic("ghost",        TranslationKeys.COSMETIC_CAPE_GHOST_NAME,        TranslationKeys.COSMETIC_CAPE_GHOST_LORE,        new BlockHeadPlayer().toItem(), 2800,  false));
        register(new CapeCosmetic("alien",        TranslationKeys.COSMETIC_CAPE_ALIEN_NAME,        TranslationKeys.COSMETIC_CAPE_ALIEN_LORE,        new BlockHeadPlayer().toItem(), 3000,  false));
        register(new CapeCosmetic("grandingo",    TranslationKeys.COSMETIC_CAPE_GRANDINGO_NAME,    TranslationKeys.COSMETIC_CAPE_GRANDINGO_LORE,    new BlockHeadPlayer().toItem(), 3200,  false));

        register(new CapeCosmetic("easy",         TranslationKeys.COSMETIC_CAPE_EASY_NAME,         TranslationKeys.COSMETIC_CAPE_EASY_LORE,         new BlockHeadPlayer().toItem(), 7000,  true));  // entrée de gamme anim
        register(new CapeCosmetic("heart",        TranslationKeys.COSMETIC_CAPE_HEART_NAME,        TranslationKeys.COSMETIC_CAPE_HEART_LORE,        new BlockHeadPlayer().toItem(), 8000,  true));

        register(new CapeCosmetic("nightbears",   TranslationKeys.COSMETIC_CAPE_NIGHTBEARS_NAME,   TranslationKeys.COSMETIC_CAPE_NIGHTBEARS_LORE,   new BlockHeadPlayer().toItem(), 9000,  true));
        register(new CapeCosmetic("changingmoon", TranslationKeys.COSMETIC_CAPE_CHANGINGMOON_NAME, TranslationKeys.COSMETIC_CAPE_CHANGINGMOON_LORE, new BlockHeadPlayer().toItem(), 9000,  true));

        register(new CapeCosmetic("blackrose",    TranslationKeys.COSMETIC_CAPE_BLACKROSE_NAME,    TranslationKeys.COSMETIC_CAPE_BLACKROSE_LORE,    new BlockHeadPlayer().toItem(), 10000, true));
        register(new CapeCosmetic("winterheart",  TranslationKeys.COSMETIC_CAPE_WINTERHEART_NAME,  TranslationKeys.COSMETIC_CAPE_WINTERHEART_LORE,  new BlockHeadPlayer().toItem(), 10000, true));

        register(new CapeCosmetic("bicoloreyes",  TranslationKeys.COSMETIC_CAPE_BICOLOREYES_NAME,  TranslationKeys.COSMETIC_CAPE_BICOLOREYES_LORE,  new BlockHeadPlayer().toItem(), 11500, true));
        register(new CapeCosmetic("ladygaga",     TranslationKeys.COSMETIC_CAPE_LADYGAGA_NAME,     TranslationKeys.COSMETIC_CAPE_LADYGAGA_LORE,     new BlockHeadPlayer().toItem(), 11500, true));

        register(new CapeCosmetic("averyloser",   TranslationKeys.COSMETIC_CAPE_AVERYLOSER_NAME,   TranslationKeys.COSMETIC_CAPE_AVERYLOSER_LORE,   new BlockHeadPlayer().toItem(), 14000, true));
        register(new CapeCosmetic("z",            TranslationKeys.COSMETIC_CAPE_Z_NAME,            TranslationKeys.COSMETIC_CAPE_Z_LORE,            new BlockHeadPlayer().toItem(), 14500, true));
        register(new CapeCosmetic("meow",         TranslationKeys.COSMETIC_CAPE_MEOW_NAME,         TranslationKeys.COSMETIC_CAPE_MEOW_LORE,         new BlockHeadPlayer().toItem(), 14500, true));
        register(new CapeCosmetic("smile",        TranslationKeys.COSMETIC_CAPE_SMILE_NAME,        TranslationKeys.COSMETIC_CAPE_SMILE_LORE,        new BlockHeadPlayer().toItem(), 15000, true)); // max

        putCapes();
    }

    public void putCapes() {
        ArrayList<String> list = new ArrayList<>(List.of("none"));
        for (CapeCosmetic cape : capes.values()) list.add(cape.getIdentifier());

        EntityProperty.register("minecraft:player", new EnumEntityProperty("colria:cape", list.toArray(String[]::new), "none"));
    }
}