package zwuiix.colria.cmd.impl.cosmetic;

import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.database.DataBase;
import zwuiix.colria.database.dao.PlayerCosmeticDao;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.player.cosmetic.CosmeticRegistry;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.DB;

import java.util.Map;

public class ShowCosmeticsSubCommand extends ColriaPlayerSubCommand {
    public ShowCosmeticsSubCommand() {
        super("show");
    }

    @Override
    public void prepare() {
        setPermission(Permission.COSMETIC_MANAGE.toString());
        registerArgument(0, new TargetArgument("target", false));
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        var target = args.get("target").toString();

        DB.getPlayerDataInfo(target).then(info -> {
            processAsync(player, info.getXuid(), target);
        }).onCatch(err -> player.sendMessage(TranslationKeys.PLAYER_CANTFIND, target));
    }

    private void processAsync(EnginePlayer player, String xuid, String targetName) {
        DataBase.getInstance()
                .query(PlayerCosmeticDao.class, dao -> dao.listByXuid(xuid))
                .whenCompleteAsync((cosmetics, ex) -> {
                    if(ex != null) {
                        ex.printStackTrace();
                    }

                    if(cosmetics.isEmpty()) {
                        player.sendMessage(TranslationKeys.PLAYER_COMMAND_CAPE_LIST_EMPTY, targetName);
                        return;
                    }

                    StringBuilder names = new StringBuilder();
                    for (String identifier : cosmetics) {
                        var cosmetic = CosmeticRegistry.getInstance().getCosmetic(identifier);
                        if(cosmetic != null) {
                            if (!names.isEmpty()) names.append("§r§7, ");
                            names.append(player.processTranslation(cosmetic.getName()));
                        }
                    }

                    player.sendMessage(TranslationKeys.PLAYER_COMMAND_CAPE_LIST, targetName, names.toString());
                });
    }
}
