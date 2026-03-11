package zwuiix.colria.cmd.impl;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import zwuiix.colria.cmd.ColriaCommand;
import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.cmd.arguments.MessageArgument;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.gui.StatsGUI;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.translator.Translator;
import zwuiix.colria.util.Chat;

import java.util.Map;

public class StatsCommand extends ColriaPlayerCommand {
    public StatsCommand() {
        super("stats", "commands.stats.description");
    }

    @Override
    public void prepare() {
        registerArgument(0, new TargetArgument("target", true));
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        EnginePlayer target = player;
        if (args.containsKey("target")) {
            String name = args.get("target").toString();

            target = (EnginePlayer) Server.getInstance().getPlayerExact(name);
            if(target == null) {
                player.sendMessage(TranslationKeys.PLAYER_CANTFIND, name);
                return;
            }
        }

        var gui = new StatsGUI(player, target);
        gui.send();
    }
}
