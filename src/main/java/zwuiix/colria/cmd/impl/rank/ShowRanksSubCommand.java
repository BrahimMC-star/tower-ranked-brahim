package zwuiix.colria.cmd.impl.rank;

import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.database.DataBase;
import zwuiix.colria.database.dao.PlayerDataDao;
import zwuiix.colria.database.dao.PlayerRankDao;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.rank.RankRegistry;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Map;

public class ShowRanksSubCommand extends ColriaPlayerSubCommand {
    public ShowRanksSubCommand() {
        super("show");
    }

    @Override
    public void prepare() {
        setPermission(Permission.RANK_MANAGE.toString());
        registerArgument(0, new TargetArgument("target", false));
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        var target = args.get("target").toString();

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

                    processAsync(player, xuid.get(), target);
                });
    }

    private void processAsync(EnginePlayer player, String xuid, String targetName) {
        if(xuid.isEmpty()) {
            player.sendMessage(TranslationKeys.PLAYER_CANTFIND, targetName);
            return;
        }

        DataBase.getInstance()
                .query(PlayerRankDao.class, dao -> dao.listByXuid(xuid))
                .whenCompleteAsync((has, ex) -> {
                    if(ex != null) {
                        ex.printStackTrace();
                    }

                    if(has.isEmpty()) {
                        player.sendMessage(TranslationKeys.PLAYER_COMMAND_RANK_LIST_EMPTY, targetName);
                        return;
                    }

                    StringBuilder rankNames = new StringBuilder();
                    for (Integer rankId : has) {
                        RankRegistry.getInstance().getRank(rankId).ifPresent(rank -> {
                            if (!rankNames.isEmpty()) rankNames.append("§r§7, ");
                            rankNames.append(rank.getColoredName());
                        });
                    }

                    player.sendMessage(TranslationKeys.PLAYER_COMMAND_RANK_LIST, targetName, rankNames.toString());
                });
    }
}
