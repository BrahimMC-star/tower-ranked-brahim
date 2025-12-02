package zwuiix.colria.cmd.impl.debug;

import cn.nukkit.permission.Permission;
import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.player.EnginePlayer;

import java.util.Map;

public class DebugCommand extends ColriaPlayerCommand {
    public DebugCommand() {
        super("debug", "Debug utilities");
    }

    @Override
    public void prepare() {
        setPermission(Permission.DEFAULT_PERMISSION);
        registerSubCommand(new DebugPositionSubCommand());
        registerSubCommand(new DebugRotationSubCommand());
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
