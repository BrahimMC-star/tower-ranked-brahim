package zwuiix.colria.game.impl.lobby.gui.sub;

import cn.nukkit.Server;
import cn.nukkit.block.BlockCopperBars;
import cn.nukkit.block.BlockSnow;
import cn.nukkit.item.*;
import cn.nukkit.level.Sound;
import zwuiix.colria.EngineInfo;
import zwuiix.colria.database.DataBase;
import zwuiix.colria.database.dao.PlayerRankDao;
import zwuiix.colria.game.gui.sub.SubMenu;
import zwuiix.colria.game.impl.lobby.gui.GameShopGUI;
import zwuiix.colria.gui.PurchaseGUI;
import zwuiix.colria.item.enchant.DummyEnchantment;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.rank.Rank;
import zwuiix.colria.rank.RankRegistry;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Glyph;
import zwuiix.colria.util.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RanksMenu extends SubMenu {
    private final GameShopGUI gui;

    public RanksMenu(GameShopGUI gui, EnginePlayer player) {
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
            player.addSound(Sound.RANDOM_CLICK, 0.5f, 1f);
        });

        RankRegistry registry = RankRegistry.getInstance();

        Rank ice = registry.getRank("ice").orElse(null);
        Rank frost = registry.getRank("frost").orElse(null);
        Rank blizzard = registry.getRank("blizzard").orElse(null);
        Rank storm = registry.getRank("storm").orElse(null);

        assert ice != null;
        assert frost != null;
        assert blizzard != null;
        assert storm != null;

        List<RankInfo> ranksInfo = new ArrayList<>();
        ranksInfo.add(new RankInfo(storm, TranslationKeys.RANK_STORM_DESCRIPTION, new ItemWindCharge(), 200_000));
        ranksInfo.add(new RankInfo(blizzard, TranslationKeys.RANK_BLIZZARD_DESCRIPTION, new ItemSnowball(), 100_000));
        ranksInfo.add(new RankInfo(frost, TranslationKeys.RANK_FROST_DESCRIPTION, new BlockSnow().toItem(), 75_000));
        ranksInfo.add(new RankInfo(ice, TranslationKeys.RANK_ICE_DESCRIPTION, new ItemBucket(ItemBucket.POWDER_SNOW_BUCKET), 50000));

        for (RankInfo info : ranksInfo) {
            var rank = info.rank;
            String t2 = player.processTranslation(player.getHighestRank().getId() >= rank.getId() ? TranslationKeys.PARTICLE_OWNED : TranslationKeys.PARTICLE_OWNED_NOT);
            String t3 = player.processTranslation(TranslationKeys.RANK_COST, info.cost);

            long cost = info.cost;
            for (Rank r : registry.getRanks().values()) {
                if (player.hasRank(r.getId()) && r.getId() < rank.getId()) {
                    long reducedCost = (long) (info.cost * 0.8);
                    cost = reducedCost;
                    t3 = player.processTranslation(TranslationKeys.RANK_COST_REDUCED, info.cost, reducedCost);
                    break;
                }
            }

            Item item = info.item.setCustomName("§r" + rank.getColoredName());
            item.setLore(
                    "§r§f" + Glyph.vbar(EngineInfo.COLOR, 1) + " " + player.processTranslation(TranslationKeys.PARTICLE_AVAILABLE),
                    "§r§f",
                    player.processTranslation(info.description),
                    "§r§f",
                    player.processTranslation(TranslationKeys.RANK_REDUCEDPRICE),
                    "§r§f",
                    t2,
                    t3
            );

            if (player.getHighestRank().getId() >= rank.getId()) {
                item.addEnchantment(new DummyEnchantment().setLevel(1));
            }

            long finalCost = cost;
            inv.setItem(switch (rank.getId()) {
                case 51 -> 30;
                case 52 -> 31;
                case 53 -> 32;
                default -> 22;
            }, item).onClick(c -> {
                inv.close(player);
                if (player.getHighestRank().getId() >= rank.getId()) {
                    player.sendMessage(player.processTranslation(TranslationKeys.RANK_ALREADY_OWNED));
                    player.addSound(Sound.MOB_ZOMBIE_VILLAGER_SAY,0.5f, 1f);
                    return;
                }

                var purchase = new PurchaseGUI(
                        player.processTranslation(TranslationKeys.PURCHASE_GUI_PARTICLE),
                        player,
                        item,
                        finalCost,
                        () -> {
                            var pinfo = player.getPlayerDataInfo();
                            if (pinfo.getShards() < finalCost) {
                                inv.close(player);
                                player.sendMessage(player.processTranslation(TranslationKeys.RANK_PURCHASE_NOTENOUGH, finalCost - pinfo.getShards()));
                                player.addSound(Sound.MOB_VILLAGER_NO,0.5f, 1f);
                                return;
                            }

                            pinfo.decreaseShards(finalCost);
                            player.addRank(rank);
                            DataBase.getInstance().write(PlayerRankDao.class, (Consumer<PlayerRankDao>) dao -> dao.add(pinfo.getXuid(), rank.getId()));
                            player.resync();
                            player.addSound(Sound.RANDOM_LEVELUP,0.5f, 1f);
                            player.sendMessage(TranslationKeys.RANK_PURCHASE_SUCCESS, rank.getColoredName(), finalCost);
                        },
                        () -> {
                            Server.getInstance().getScheduler().scheduleDelayedTask(gui::send, 10);
                            player.addSound(Sound.RANDOM_CLICK, 0.5f, 1f);
                        }
                );

                Server.getInstance().getScheduler().scheduleDelayedTask(purchase::send, 10);
                return;
            });
        }
    }

    public record RankInfo(Rank rank, TranslationKeys description, Item item, long cost) {}
}
