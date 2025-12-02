package zwuiix.colria.cmd.impl.shard;

import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Map;

public class ShardCommand extends ColriaPlayerCommand {
    public ShardCommand() {
        super("shard", "commands.shard.description");
    }

    @Override
    public void prepare() {
        registerSubCommand(new ShowShardSubCommand());
        registerSubCommand(new AddShardSubCommand());
        registerSubCommand(new RemoveShardSubCommand());
        registerSubCommand(new SetShardSubCommand());
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        player.sendMessage(TranslationKeys.PLAYER_COMMAND_SHARD, player.getPlayerDataInfo().getShards());
    }
}
