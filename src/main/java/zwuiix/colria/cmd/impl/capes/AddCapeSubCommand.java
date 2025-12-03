package zwuiix.colria.cmd.impl.capes;

import cn.nukkit.Server;
import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.cmd.arguments.StringEnumArgument;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.database.DataBase;
import zwuiix.colria.database.dao.PlayerCosmeticDao;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.player.cosmetic.CapeCosmetic;
import zwuiix.colria.player.cosmetic.CosmeticRegistry;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.DB;

import java.util.Map;
import java.util.function.Consumer;

public class AddCapeSubCommand extends ColriaPlayerSubCommand {
    public AddCapeSubCommand() {
        super("add");
    }

    @Override
    public void prepare() {
        setPermission(Permission.COSMETIC_MANAGE.toString());
        registerArgument(0, new TargetArgument("target", false));
        registerArgument(1, new StringEnumArgument("cape", false, CosmeticRegistry.getInstance().getCapes().keySet().toArray(String[]::new)));
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        var target = args.get("target").toString();
        var identifier = args.get("cape").toString().toLowerCase();
        var cape = CosmeticRegistry.getInstance().getCape(identifier);
        if(cape == null) {
            player.sendMessage(TranslationKeys.PLAYER_COMMAND_CAPE_NOEXIST, identifier);
            return;
        }

        DB.getPlayerDataInfo(target).then(info -> {
            processAsync(player, info.getXuid(), cape, target);
        }).onCatch(err -> player.sendMessage(TranslationKeys.PLAYER_CANTFIND, target));
    }

    private void processAsync(EnginePlayer player, String xuid, CapeCosmetic cape, String targetName) {
        DataBase.getInstance().query(PlayerCosmeticDao.class, dao -> dao.has(xuid, cape.getIdentifier()))
                .whenCompleteAsync((has, err) -> {
                    if(err != null) {
                        err.printStackTrace();
                    }

                    if(has) {
                        player.sendMessage(TranslationKeys.PLAYER_COMMAND_CAPE_ALREADYHAS, targetName, player.processTranslation(cape.getName()));
                        return;
                    }

                    DataBase.getInstance().write(PlayerCosmeticDao.class, (Consumer<PlayerCosmeticDao>) dao -> dao.add(xuid, cape.getIdentifier()));
                    EnginePlayer targetPlayer = (EnginePlayer) Server.getInstance().getPlayerExact(targetName);
                    if(targetPlayer != null) targetPlayer.resync();

                    player.sendMessage(TranslationKeys.PLAYER_COMMAND_CAPE_ADDED, player.processTranslation(cape.getName()), targetName);
                });
    }
}
