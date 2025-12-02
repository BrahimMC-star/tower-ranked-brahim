package zwuiix.colria.cmd;

import cn.nukkit.command.CommandSender;
import zwuiix.colria.player.EnginePlayer;

import java.util.Map;

abstract public class ColriaPlayerSubCommand extends ColriaSubCommand {
    public ColriaPlayerSubCommand(String name, String description) {
        super(name, description);
    }

    public ColriaPlayerSubCommand(String name) {
        super(name);
    }

    @Override
    public void run(CommandSender sender, Map<String, Object> args) {
        if(!(sender instanceof EnginePlayer)) {
            sender.sendMessage("You must be a player to run this command.");
            return;
        }

        execute((EnginePlayer) sender, args);
    }

    abstract public void execute(EnginePlayer player, Map<String, Object> args);
}
