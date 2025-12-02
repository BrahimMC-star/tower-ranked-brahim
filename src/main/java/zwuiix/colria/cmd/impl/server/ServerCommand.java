package zwuiix.colria.cmd.impl.server;

import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.gui.server.ServerManageGUI;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;

import java.util.Map;

public class ServerCommand extends ColriaPlayerCommand {
    public ServerCommand() {
        super("server", "Manage the server");
    }

    @Override
    public void prepare() {
        setPermission(Permission.SERVER_MANAGE.toString());
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        ServerManageGUI gui = new ServerManageGUI(player);
        gui.send();
    }
}
