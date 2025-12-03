package zwuiix.colria.cmd.impl.rank;

import cn.nukkit.Server;
import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.cmd.arguments.StringEnumArgument;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.database.DataBase;
import zwuiix.colria.database.dao.PlayerRankDao;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.rank.Rank;
import zwuiix.colria.rank.RankRegistry;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.DB;

import java.util.Map;
import java.util.function.Consumer;

public class AddRankCommand extends ColriaPlayerSubCommand {
    public AddRankCommand() {
        super("add");
    }

    @Override
    public void prepare() {
        setPermission(Permission.RANK_MANAGE.toString());
        registerArgument(0, new TargetArgument("target", false));
        registerArgument(1, new StringEnumArgument("rank", false, RankRegistry.getInstance().getRanks().values().stream().map(Rank::getName).map(String::toLowerCase).toArray(String[]::new)));
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        var target = args.get("target").toString();
        var rankName = args.get("rank").toString().toLowerCase();
        var r = RankRegistry.getInstance().getRank(rankName);
        if(r.isEmpty()) {
            player.sendMessage(TranslationKeys.PLAYER_COMMAND_RANK_NOEXIST, rankName);
            return;
        }

        var rank = r.get();
        DB.getPlayerDataInfo(target).then(info -> {
            DataBase.getInstance()
                    .query(PlayerRankDao.class, dao -> dao.has(info.getXuid(), rank.getId()))
                    .whenCompleteAsync((has, err) -> {
                        if(err != null) {
                            err.printStackTrace();
                        }

                        if(has) {
                            player.sendMessage(TranslationKeys.PLAYER_COMMAND_RANK_ALREADYHAS, target, rank.getColoredName());
                            return;
                        }

                        DataBase.getInstance().write(PlayerRankDao.class, (Consumer<PlayerRankDao>) dao -> dao.add(info.getXuid(), rank.getId()));
                        EnginePlayer targetPlayer = (EnginePlayer) Server.getInstance().getPlayerExact(target);
                        if(targetPlayer != null) targetPlayer.resync();

                        player.sendMessage(TranslationKeys.PLAYER_COMMAND_RANK_ADDED, rank.getColoredName(), target);
                    });
        }).onCatch(err -> player.sendMessage(TranslationKeys.PLAYER_CANTFIND, target));
    }
}
