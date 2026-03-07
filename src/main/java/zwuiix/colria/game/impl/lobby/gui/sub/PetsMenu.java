package zwuiix.colria.game.impl.lobby.gui.sub;

import cn.nukkit.Server;
import cn.nukkit.block.BlockCopperBars;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemDoorDarkOak;
import cn.nukkit.item.enchantment.EnchantmentDurability;
import cn.nukkit.level.Sound;
import zwuiix.colria.EngineInfo;
import zwuiix.colria.database.DataBase;
import zwuiix.colria.database.dao.PlayerPetDao;
import zwuiix.colria.game.gui.sub.SubMenu;
import zwuiix.colria.game.impl.lobby.gui.GameShopGUI;
import zwuiix.colria.gui.shop.PurchaseGUI;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.player.cosmetic.CosmeticRegistry;
import zwuiix.colria.player.cosmetic.Pet;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Glyph;
import zwuiix.colria.util.Window;

import java.util.function.Consumer;

public class PetsMenu extends SubMenu {
    private final GameShopGUI gui;

    public PetsMenu(GameShopGUI gui, EnginePlayer player) {
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

        var pets = CosmeticRegistry.getInstance().getPets().values();

        long minCost = Long.MAX_VALUE;
        long maxCost = Long.MIN_VALUE;
        for (Pet c : pets) {
            long cost = c.getCost();
            if (cost < minCost) minCost = cost;
            if (cost > maxCost) maxCost = cost;
        }

        for (Pet c : pets) {
            Item reference = c.getReference();
            int rarity = computeRarity(c, minCost, maxCost);
            String rarityBar = buildRarityBar(rarity);

            String baseName = player.processTranslation(c.getName());
            reference.setCustomName(baseName + " §8[" + rarityBar + "§8]");


            String t2 = player.processTranslation(TranslationKeys.PARTICLE_OWNED_NOT);
            if(player.hasPet(c)) {
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

            var pet = CosmeticRegistry.getInstance().getPet(player.getPlayerDataInfo().getPet());
            if(pet != null && pet.equals(c)) {
                reference.addEnchantment(new EnchantmentDurability().setLevel(1));
                reference.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);
            }

            var slot = Window.nextSlot1(inv);
            inv.setItem(slot, reference).onClick((click) -> {
                inv.close(player);
                if(!player.hasPet(c)) {
                    var purchase = new PurchaseGUI(player.processTranslation(TranslationKeys.PURCHASE_GUI_PARTICLE, player.processTranslation(c.getName())),
                            player,
                            reference,
                            c.getCost(),
                            () -> {
                                if(player.getPlayerDataInfo().getShards() < c.getCost()) {
                                    player.sendMessage(TranslationKeys.PURCHASE_GUI_PET_NOTENOUGH, c.getCost() - player.getPlayerDataInfo().getShards());
                                    player.addSound(Sound.MOB_VILLAGER_NO,0.5f, 1f);
                                    return;
                                }

                                player.getPlayerDataInfo().decreaseShards(c.getCost());
                                player.addPet(c);
                                DataBase.getInstance().write(PlayerPetDao.class, (Consumer<PlayerPetDao>) dao -> dao.add(player.getXUID(), c.getIdentifier()));

                                player.sendMessage(TranslationKeys.PURCHASE_GUI_PET_SUCCESS, player.processTranslation(c.getName()), c.getCost());
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

                var cPet = CosmeticRegistry.getInstance().getPet(player.getPlayerDataInfo().getPet());
                if(cPet != null && cPet.equals(c)) {
                    player.getPlayerDataInfo().setPet("none");
                    player.sendMessage(TranslationKeys.PLAYER_LOBBY_PET_REMOVED, player.processTranslation(c.getName()));
                    player.addSound(Sound.RANDOM_ORB,0.5f, 1f);
                    return;
                }

                player.getPlayerDataInfo().setPet(c.getIdentifier());
                player.sendMessage(TranslationKeys.PLAYER_LOBBY_PET_SELECTED, player.processTranslation(c.getName()));
                player.addSound(Sound.RANDOM_LEVELUP,0.5f, 1f);
            });
        }
    }

    private static int computeRarity(Pet cape, long minCost, long maxCost) {
        double costNorm = (double) (cape.getCost() - minCost) / (double) (maxCost - minCost);
        double score = Math.min(1.0, costNorm);
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
