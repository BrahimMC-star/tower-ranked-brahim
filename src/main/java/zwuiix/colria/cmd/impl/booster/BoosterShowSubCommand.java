package zwuiix.colria.cmd.impl.booster;

import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.DB;

import java.util.Map;

public class BoosterShowSubCommand extends ColriaPlayerSubCommand {
    public BoosterShowSubCommand() {
        super("add");
    }

    @Override
    public void prepare() {
        setPermission(Permission.BOOSTER_MANAGE.toString());
        registerArgument(0, new TargetArgument("target", false));
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        var target = args.get("target").toString();

        DB.getPlayerDataInfo(target).then(info -> {
            player.sendMessage(TranslationKeys.PLAYER_COMMAND_BOOSTER_SHOW, target, info.getBooster());
        }).onCatch(err -> player.sendMessage(TranslationKeys.PLAYER_CANTFIND, target));
    }
}
