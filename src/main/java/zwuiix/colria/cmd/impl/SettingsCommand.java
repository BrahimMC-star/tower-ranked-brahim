package zwuiix.colria.cmd.impl;

import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.gui.SettingsGUI;
import zwuiix.colria.player.EnginePlayer;

import java.util.Map;

public class SettingsCommand extends ColriaPlayerCommand {
    public SettingsCommand() {
        super("settings", "commands.settings.description");
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        var gui = new SettingsGUI(player);
        gui.send();
    }
}
