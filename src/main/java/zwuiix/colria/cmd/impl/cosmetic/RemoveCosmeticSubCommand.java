package zwuiix.colria.cmd.impl.cosmetic;

import cn.nukkit.Server;
import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.cmd.arguments.StringEnumArgument;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.database.DataBase;
import zwuiix.colria.database.dao.PlayerCosmeticDao;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.player.cosmetic.Cosmetic;
import zwuiix.colria.player.cosmetic.CosmeticRegistry;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.DB;

import java.util.Map;
import java.util.function.Consumer;

public class RemoveCosmeticSubCommand extends ColriaPlayerSubCommand {
    public RemoveCosmeticSubCommand() {
        super("remove");
    }

    @Override
    public void prepare() {
        setPermission(Permission.COSMETIC_MANAGE.toString());
        registerArgument(0, new TargetArgument("target", false));
        registerArgument(1, new StringEnumArgument("cosmetic", false, CosmeticRegistry.getInstance().getCosmetics().keySet().toArray(String[]::new)));
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        var target = args.get("target").toString();
        var identifier = args.get("cosmetic").toString().toLowerCase();
        var cosmetic = CosmeticRegistry.getInstance().getCosmetic(identifier);
        if (cosmetic == null) {
            player.sendMessage(TranslationKeys.PLAYER_COMMAND_COSMETIC_NOEXIST, identifier);
            return;
        }

        DB.getPlayerDataInfo(target).then(info -> {
            processAsync(player, info.getXuid(), cosmetic, target);
        }).onCatch(err -> player.sendMessage(TranslationKeys.PLAYER_CANTFIND, target));
    }

    private void processAsync(EnginePlayer player, String xuid, Cosmetic cosmetic, String targetName) {
        if(xuid.isEmpty()) {
            player.sendMessage(TranslationKeys.PLAYER_CANTFIND, targetName);
            return;
        }

        DataBase.getInstance()
                .query(PlayerCosmeticDao.class, dao -> dao.has(xuid, cosmetic.getIdentifier()))
                .whenCompleteAsync((has, err) -> {
                    if(err != null) {
                        err.printStackTrace();
                    }

                    if(!has) {
                        player.sendMessage(TranslationKeys.PLAYER_COMMAND_COSMETIC_NOTHAS, targetName, player.processTranslation(cosmetic.getName()));
                        return;
                    }

                    DataBase.getInstance().write(PlayerCosmeticDao.class, (Consumer<PlayerCosmeticDao>) dao -> dao.remove(xuid, cosmetic.getIdentifier()));
                    EnginePlayer targetPlayer = (EnginePlayer) Server.getInstance().getPlayerExact(targetName);
                    if(targetPlayer != null) targetPlayer.resync();

                    player.sendMessage(TranslationKeys.PLAYER_COMMAND_COSMETIC_REMOVED, player.processTranslation(cosmetic.getName()), targetName);
                });
    }
}
