package zwuiix.colria.cmd.impl.shard;

import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.cmd.arguments.LongArgument;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.DB;

import java.util.Map;

public class RemoveShardSubCommand extends ColriaPlayerSubCommand {
    public RemoveShardSubCommand() {
        super("remove");
    }

    @Override
    public void prepare() {
        setPermission(Permission.SHARD_MANAGE.toString());
        registerArgument(0, new TargetArgument("target", false));
        registerArgument(1, new LongArgument("amount", false));
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        var target = args.get("target").toString();
        var amount = (long) args.get("amount");

        DB.getPlayerDataInfo(target).then(info -> {
            if(info.getShards() < amount) {
                player.sendMessage(TranslationKeys.PLAYER_COMMAND_SHARD_NOTENOUGH, target);
                return;
            }

            info.decreaseShards(amount);
            player.sendMessage(TranslationKeys.PLAYER_COMMAND_SHARD_REMOVE, amount, target);
        }).onCatch(err -> player.sendMessage(TranslationKeys.PLAYER_CANTFIND, target));
    }
}
