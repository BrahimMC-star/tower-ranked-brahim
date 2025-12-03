package zwuiix.colria.cmd.impl.capes;

import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;

import java.util.Map;

public class CapesCommand extends ColriaPlayerCommand {
    public CapesCommand() {
        super("capes", "commands.capes.description");
    }

    @Override
    public void prepare() {
        setPermission(Permission.CAPE_MANAGE.toString());

        registerSubCommand(new ShowCapesSubCommand());
        registerSubCommand(new AddCapeSubCommand());
        registerSubCommand(new RemoveCapeSubCommand());
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        throw new UnsupportedOperationException("This command requires a subcommand.");
    }
}
