package zwuiix.colria.cmd.impl;

import cn.nukkit.Server;
import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.gui.shop.SettingsGUI;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

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
