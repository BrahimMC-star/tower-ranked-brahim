package zwuiix.colria.game.impl.lobby.gui.sub;

import cn.nukkit.Server;
import cn.nukkit.block.BlockCopperBars;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemDoorDarkOak;
import cn.nukkit.item.enchantment.EnchantmentDurability;
import cn.nukkit.level.Sound;
import zwuiix.colria.EngineInfo;
import zwuiix.colria.database.DataBase;
import zwuiix.colria.database.dao.PlayerParticleDao;
import zwuiix.colria.game.gui.sub.SubMenu;
import zwuiix.colria.game.impl.lobby.gui.GameShopGUI;
import zwuiix.colria.gui.shop.PurchaseGUI;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.player.particle.Particle;
import zwuiix.colria.player.particle.ParticleRegistry;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Glyph;
import zwuiix.colria.util.Window;

import java.util.function.Consumer;

public class ParticlesMenu extends SubMenu {
    private final GameShopGUI gui;

    public ParticlesMenu(GameShopGUI gui, EnginePlayer player) {
        super(Item.AIR_ITEM);
        this.gui = gui;
    }

    private static int computeRarity(Particle p, long minCost, long maxCost) {
        if (minCost >= maxCost) {
            return p.isFlying() ? 4 : 3;
        }

        double norm = (double) (p.getCost() - minCost) / (double) (maxCost - minCost);
        double bonus = p.isFlying() ? 0.2 : 0.0;

        double score = Math.min(1.0, norm + bonus);
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

        var currentParticle = ParticleRegistry.getInstance().getParticle(player.getPlayerDataInfo().getParticle());
        var particles = ParticleRegistry.getInstance().getParticles().values();

        long minCost = Long.MAX_VALUE;
        long maxCost = Long.MIN_VALUE;
        for (Particle p : particles) {
            long cost = p.getCost();
            if (cost < minCost) minCost = cost;
            if (cost > maxCost) maxCost = cost;
        }

        for (Particle p : particles) {
            Item reference = p.getReference();

            int rarity = computeRarity(p, minCost, maxCost);
            String rarityBar = buildRarityBar(rarity);
            String baseName = player.processTranslation(p.getName());

            reference.setCustomName(baseName + " §8[" + rarityBar + "§8]");

            String t2 = player.processTranslation(TranslationKeys.PARTICLE_OWNED_NOT);
            if (player.hasParticle(p)) {
                t2 = player.processTranslation(TranslationKeys.PARTICLE_OWNED);
            }

            reference.setLore(
                    "§r§f" + Glyph.vbar(EngineInfo.COLOR, 1) + " " + player.processTranslation(TranslationKeys.PARTICLE_AVAILABLE),
                    "§r§f",
                    player.processTranslation(p.getDescription()),
                    "§r§f",
                    t2,
                    player.processTranslation(TranslationKeys.PARTICLE_COST, p.getCost())
            );

            if (currentParticle != null && currentParticle.equals(p)) {
                reference.addEnchantment(new EnchantmentDurability().setLevel(1)));
                reference.setItemLockMode(Item.ItemLockMode.LOCK_IN_SLOT);
            }

            var slot = Window.nextSlot1(inv);
            inv.setItem(slot, reference).onClick((click) -> {
                inv.close(player);
                if (!player.hasParticle(p)) {
                    var purchase = new PurchaseGUI(
                            player.processTranslation(TranslationKeys.PURCHASE_GUI_PARTICLE, player.processTranslation(p.getName())),
                            player,
                            reference,
                            p.getCost(),
                            () -> {
                                if (player.getPlayerDataInfo().getShards() < p.getCost()) {
                                    player.sendMessage(TranslationKeys.PURCHASE_GUI_PARTICLE_NOTENOUGH,
                                            p.getCost() - player.getPlayerDataInfo().getShards());
                                    player.addSound(Sound.MOB_VILLAGER_NO, 0.5f, 1f);
                                    return;
                                }

                                player.getPlayerDataInfo().decreaseShards(p.getCost());
                                player.addParticle(p);
                                DataBase.getInstance().write(PlayerParticleDao.class,
                                        (Consumer<PlayerParticleDao>) dao -> dao.add(player.getXUID(), p.getIdentifier()));

                                player.sendMessage(TranslationKeys.PURCHASE_GUI_PARTICLE_SUCCESS,
                                        player.processTranslation(p.getName()), p.getCost());
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

                if (currentParticle == p) {
                    player.getPlayerDataInfo().setParticle("none");
                    player.sendMessage(TranslationKeys.PARTICLE_REMOVE_SUCCESS);
                    player.addSound(Sound.RANDOM_CLICK, 0.5f, 1f);
                    return;
                }

                player.getPlayerDataInfo().setParticle(p.getIdentifier());
                if (player.isSurvival() || player.isAdventure()) {
                    if (p.isFlying()) {
                        player.setAllowFlight(true);
                    } else {
                        player.setAllowFlight(false);
                        player.setFlying(false);
                    }
                }

                player.sendMessage(TranslationKeys.PLAYER_LOBBY_PARTICLE_SELECTED,
                        player.processTranslation(p.getName()));
                player.addSound(Sound.RANDOM_LEVELUP, 0.5f, 1f);
            });
        }
    }
}
