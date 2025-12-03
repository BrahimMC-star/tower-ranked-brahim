package zwuiix.colria.cmd.impl.rank;

import cn.nukkit.Server;
import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.cmd.arguments.StringEnumArgument;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.database.DataBase;
import zwuiix.colria.database.dao.PlayerDataDao;
import zwuiix.colria.database.dao.PlayerRankDao;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.rank.Rank;
import zwuiix.colria.rank.RankRegistry;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Map;
import java.util.function.Consumer;

public class RemoveRankCommand extends ColriaPlayerSubCommand {
    public RemoveRankCommand() {
        super("remove");
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
        var rank = RankRegistry.getInstance().getRank(rankName);
        if(rank.isEmpty()) {
            player.sendMessage(TranslationKeys.PLAYER_COMMAND_RANK_NOEXIST, rankName);
            return;
        }

        DataBase.getInstance()
                .query(PlayerDataDao.class, dao -> dao.getXuidByName((String)args.get("target")))
                .whenCompleteAsync((xuid, err) -> {
                    if(err != null) {
                        err.printStackTrace();
                    }

                    if(xuid.isEmpty()) {
                        player.sendMessage(TranslationKeys.PLAYER_CANTFIND, args.get("target"));
                        return;
                    }

                    processAsync(player, xuid.get(), rank.get(), target);
                });
    }

    private void processAsync(EnginePlayer player, String xuid, Rank rank, String targetName) {
        DataBase.getInstance()
                .query(PlayerRankDao.class, dao -> dao.has(xuid, rank.getId()))
                .whenCompleteAsync((has, ex) -> {
                    if(ex != null) {
                        ex.printStackTrace();
                    }

                    if(!has) {
                        player.sendMessage(TranslationKeys.PLAYER_COMMAND_RANK_NOTHAS, targetName, rank.getColoredName());
                        return;
                    }

                    DataBase.getInstance().write(PlayerRankDao.class, (Consumer<PlayerRankDao>) dao -> dao.remove(xuid, rank.getId()));
                    EnginePlayer targetPlayer = (EnginePlayer) Server.getInstance().getPlayerExact(targetName);
                    if(targetPlayer != null) targetPlayer.resync();

                    player.sendMessage(TranslationKeys.PLAYER_COMMAND_RANK_REMOVED, rank.getColoredName(), targetName);
                });
    }
}
