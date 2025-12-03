package zwuiix.colria.cmd.impl.rank;

import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;

import java.util.Map;

public class RanksCommand extends ColriaPlayerCommand {
    public RanksCommand() {
        super("ranks", "commands.ranks.description");
    }

    @Override
    public void prepare() {
        setPermission(Permission.RANK_MANAGE.toString());
        registerSubCommand(new ShowRanksSubCommand());
        registerSubCommand(new AddRankCommand());
        registerSubCommand(new RemoveRankCommand());
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        throw new IllegalArgumentException("This command requires a subcommand.");
    }
}
