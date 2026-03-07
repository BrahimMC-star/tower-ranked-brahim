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
import zwuiix.colria.player.cosmetic.CapeCosmetic;
import zwuiix.colria.player.cosmetic.CosmeticRegistry;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Glyph;
import zwuiix.colria.util.Window;

import java.util.function.Consumer;

public class CapesMenu extends SubMenu {
    private final GameShopGUI gui;

    public CapesMenu(GameShopGUI gui, EnginePlayer player) {
        super(Item.AIR_ITEM);
        this.gui = gui;
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
            player.addSound(Sound.RANDOM_CLICK,0.5f, 1f);
        });

        var capes = CosmeticRegistry.getInstance().getCapes().values();

        long minCost = Long.MAX_VALUE;
        long maxCost = Long.MIN_VALUE;
        for (CapeCosmetic c : capes) {
            long cost = c.getCost();
            if (cost < minCost) minCost = cost;
            if (cost > maxCost) maxCost = cost;
        }

        for (CapeCosmetic c : capes) {
            Item reference = c.getReference();
            int rarity = computeRarity(c, minCost, maxCost);
            String rarityBar = buildRarityBar(rarity);

            String baseName = player.processTranslation(c.getName());
            reference.setCustomName(baseName + " §8[" + rarityBar + "§8]");


            String t2 = player.processTranslation(TranslationKeys.PARTICLE_OWNED_NOT);
            if(player.hasCosmetic(c)) {
                t2 = player.processTranslation(TranslationKeys.PARTICLE_OWNED);
            }

            String flag = player.processTranslation(c.isAnimated() ? TranslationKeys.CAPE_ANIMATED : TranslationKeys.CAPE_STATIC);

            reference.setLore(
                    "§r§f" + Glyph.vbar(EngineInfo.COLOR, 1) + " " + player.processTranslation(TranslationKeys.PARTICLE_AVAILABLE),
                    "§r§f",
                    player.processTranslation(c.getDescription()),
                    "§r§f",
                    "§r§f" + Glyph.vbar(EngineInfo.COLOR, 1) + " " + flag,
                    "§r§f",
                    t2,
                    player.processTranslation(TranslationKeys.PARTICLE_COST, c.getCost())
            );

            var cape = CosmeticRegistry.getInstance().getCape(player.getPlayerDataInfo().getCape());
            if(cape != null && cape.equals(c)) {
                //reference.addEnchantment(new EnchantmentDurability().setLevel(1));
                reference.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);
            }

            var slot = Window.nextSlot1(inv);
            inv.setItem(slot, reference).onClick((click) -> {
                inv.close(player);
                if(!player.hasCosmetic(c)) {
                    var purchase = new PurchaseGUI(player.processTranslation(TranslationKeys.PURCHASE_GUI_PARTICLE, player.processTranslation(c.getName())),
                            player,
                            reference,
                            c.getCost(),
                            () -> {
                                if(player.getPlayerDataInfo().getShards() < c.getCost()) {
                                    player.sendMessage(TranslationKeys.PURCHASE_GUI_COSMETIC_NOTENOUGH, c.getCost() - player.getPlayerDataInfo().getShards());
                                    player.addSound(Sound.MOB_VILLAGER_NO,0.5f, 1f);
                                    return;
                                }

                                player.getPlayerDataInfo().decreaseShards(c.getCost());
                                player.addCosmetic(c);
                                DataBase.getInstance().write(PlayerCosmeticDao.class, (Consumer<PlayerCosmeticDao>) dao -> dao.add(player.getXUID(), c.getIdentifier()));

                                player.sendMessage(TranslationKeys.PURCHASE_GUI_COSMETIC_SUCCESS, player.processTranslation(c.getName()), c.getCost());
                                player.addSound(Sound.MOB_VILLAGER_YES,0.5f, 1f);
                            },
                            () -> {
                                Server.getInstance().getScheduler().scheduleDelayedTask(gui::send, 10);
                                player.addSound(Sound.RANDOM_CLICK,0.5f, 1f);
                            }
                    );
                    Server.getInstance().getScheduler().scheduleDelayedTask(purchase::send, 10);
                    return;
                }

                var cCape = CosmeticRegistry.getInstance().getCape(player.getPlayerDataInfo().getCape());
                if(cCape != null && cCape.equals(c)) {
                    player.updateCape("none");

                    player.sendMessage(TranslationKeys.PLAYER_LOBBY_COSMETIC_REMOVED, player.processTranslation(c.getName()));
                    player.addSound(Sound.RANDOM_ORB,0.5f, 1f);
                    return;
                }

                player.updateCape(c.getIdentifier());
                player.sendMessage(TranslationKeys.PLAYER_LOBBY_COSMETIC_SELECTED, player.processTranslation(c.getName()));
                player.addSound(Sound.RANDOM_LEVELUP,0.5f, 1f);
            });
        }
    }

    private static int computeRarity(CapeCosmetic cape, long minCost, long maxCost) {
        if (minCost >= maxCost) {
            return cape.isAnimated() ? 4 : 3;
        }

        double costNorm = (double) (cape.getCost() - minCost) / (double) (maxCost - minCost);
        double animBonus = cape.isAnimated() ? 0.2 : 0.0;

        double score = Math.min(1.0, costNorm + animBonus);
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
}
