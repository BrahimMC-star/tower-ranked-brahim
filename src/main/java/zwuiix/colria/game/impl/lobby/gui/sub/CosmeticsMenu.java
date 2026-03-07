package zwuiix.colria.game.impl.lobby.gui.sub;

import cn.nukkit.Server;
import cn.nukkit.block.BlockCopperBars;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemDoorDarkOak;
import cn.nukkit.item.enchantment.EnchantmentDurability;
import cn.nukkit.level.Sound;
import zwuiix.colria.EngineInfo;
import zwuiix.colria.database.DataBase;
import zwuiix.colria.database.dao.PlayerCosmeticDao;
import zwuiix.colria.game.gui.sub.SubMenu;
import zwuiix.colria.game.impl.lobby.gui.GameShopGUI;
import zwuiix.colria.gui.shop.PurchaseGUI;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.player.cosmetic.ArmorCosmetic;
import zwuiix.colria.player.cosmetic.CapeCosmetic;
import zwuiix.colria.player.cosmetic.Cosmetic;
import zwuiix.colria.player.cosmetic.CosmeticRegistry;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Glyph;
import zwuiix.colria.util.Window;

import java.util.function.Consumer;

public class CosmeticsMenu extends SubMenu {
    private final GameShopGUI gui;

    public CosmeticsMenu(GameShopGUI gui, EnginePlayer player) {
        super(Item.AIR_ITEM);
        this.gui = gui;
    }

    private static int computeRarity(Cosmetic c, long minCost, long maxCost) {
        if (minCost >= maxCost) {
            return 3;
        }

        double score = Math.min(1.0, (double) (c.getCost() - minCost) / (double) (maxCost - minCost));
        int rarity = (int) Math.round(score * 4) + 1;

        if (rarity < 1) rarity = 1;
        if (rarity > 5) rarity = 5;
        return rarity;
    }

    private static String buildRarityBar(int rarity) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < rarity) {
                sb.append("§e■");
            } else {
                sb.append("§f■");
            }
        }
        return sb.toString();
    }

    @Override
    public void sync() {
        var player = gui.player;
        var inv = gui.inventory;

        Item glass = new BlockCopperBars().toItem().setCustomName("§r");
        inv.setItem(1, glass);
        Window.fillVerticalLine(0, inv, glass);
        Window.fillVerticalLine(8, inv, glass);
        Window.fillHorizontalLine(0, inv, glass);
        Window.fillHorizontalLine(45, inv, glass);

        Item back = new ItemDoorDarkOak();
        back.setCustomName(player.processTranslation(TranslationKeys.GUI_BACK_NAME));

        inv.setItem(49, back).onClick((click) -> {
            gui.back();
            player.addSound(Sound.RANDOM_CLICK, 0.5f, 1f);
        });

        var allCosmetics = CosmeticRegistry.getInstance().getCosmetics().values();

        long minCost = Long.MAX_VALUE;
        long maxCost = Long.MIN_VALUE;
        for (Cosmetic c : allCosmetics) {
            long cost = c.getCost();
            if (cost < minCost) minCost = cost;
            if (cost > maxCost) maxCost = cost;
        }

        for (Cosmetic c : allCosmetics) {
            Item reference = c.getReference();

            int rarity = computeRarity(c, minCost, maxCost);
            String rarityBar = buildRarityBar(rarity);
            String baseName = player.processTranslation(c.getName());

            reference.setCustomName(baseName + " §8[" + rarityBar + "§8]");

            String t2 = player.processTranslation(TranslationKeys.PARTICLE_OWNED_NOT);
            if (player.hasCosmetic(c)) {
                t2 = player.processTranslation(TranslationKeys.PARTICLE_OWNED);
            }

            reference.setLore(
                    "§r§f" + Glyph.vbar(EngineInfo.COLOR, 1) + " " + player.processTranslation(TranslationKeys.PARTICLE_AVAILABLE),
                    "§r§f",
                    player.processTranslation(c.getDescription()),
                    "§r§f",
                    t2,
                    player.processTranslation(TranslationKeys.PARTICLE_COST, c.getCost())
            );

            for (String cosmeticId : player.getPlayerDataInfo().getCosmetics()) {
                Cosmetic cosmetic = CosmeticRegistry.getInstance().getCosmetic(cosmeticId);
                if (cosmetic != null && cosmetic.equals(c)) {
                    reference.addEnchantment(new EnchantmentDurability().setLevel(1));
                    reference.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);
                }
            }

            var slot = Window.nextSlot1(inv);
            inv.setItem(slot, reference).onClick((click) -> {
                inv.close(player);
                if (!player.hasCosmetic(c)) {
                    var purchase = new PurchaseGUI(
                            player.processTranslation(TranslationKeys.PURCHASE_GUI_PARTICLE, player.processTranslation(c.getName())),
                            player,
                            reference,
                            c.getCost(),
                            () -> {
                                if (player.getPlayerDataInfo().getShards() < c.getCost()) {
                                    player.sendMessage(TranslationKeys.PURCHASE_GUI_COSMETIC_NOTENOUGH,
                                            c.getCost() - player.getPlayerDataInfo().getShards());
                                    player.addSound(Sound.MOB_VILLAGER_NO, 0.5f, 1f);
                                    return;
                                }

                                player.getPlayerDataInfo().decreaseShards(c.getCost());
                                player.addCosmetic(c);
                                DataBase.getInstance().write(PlayerCosmeticDao.class,
                                        (Consumer<PlayerCosmeticDao>) dao -> dao.add(player.getXUID(), c.getIdentifier()));

                                player.sendMessage(TranslationKeys.PURCHASE_GUI_COSMETIC_SUCCESS,
                                        player.processTranslation(c.getName()), c.getCost());
                                player.addSound(Sound.MOB_VILLAGER_YES, 0.5f, 1f);
                            },
                            () -> {
                                Server.getInstance().getScheduler().scheduleDelayedTask(gui::send, 10);
                                player.addSound(Sound.RANDOM_CLICK, 0.5f, 1f);
                            }
                    );
                    Server.getInstance().getScheduler().scheduleDelayedTask(purchase::send, 10);
                    return;
                }

                var owned = player.getPlayerDataInfo().getCosmetics();
                if (owned.contains(c.getIdentifier())) {
                    player.getPlayerDataInfo().removeCosmetic(c.getIdentifier());
                    if (c instanceof ArmorCosmetic ac) {
                        player.getInventory().setArmorItem(ac.getSlot(), Item.AIR_ITEM);
                    }

                    player.sendMessage(TranslationKeys.PLAYER_LOBBY_COSMETIC_REMOVED,
                            player.processTranslation(c.getName()));
                    player.addSound(Sound.RANDOM_ORB, 0.5f, 1f);
                    return;
                }

                for (String id : owned) {
                    Cosmetic cosmetic = CosmeticRegistry.getInstance().getCosmetic(id);
                    if (cosmetic instanceof ArmorCosmetic armorCosmetic && c instanceof ArmorCosmetic c2) {
                        if (armorCosmetic.getSlot() == c2.getSlot()) {
                            player.getPlayerDataInfo().removeCosmetic(cosmetic.getIdentifier());
                        }
                    }

                    if (cosmetic instanceof CapeCosmetic && c instanceof CapeCosmetic) {
                        player.getPlayerDataInfo().removeCosmetic(cosmetic.getIdentifier());
                    }
                }

                c.apply(player);
                player.getPlayerDataInfo().addCosmetic(c.getIdentifier());
                player.sendMessage(TranslationKeys.PLAYER_LOBBY_COSMETIC_SELECTED,
                        player.processTranslation(c.getName()));
                player.addSound(Sound.RANDOM_LEVELUP, 0.5f, 1f);
            });
        }
    }
}
