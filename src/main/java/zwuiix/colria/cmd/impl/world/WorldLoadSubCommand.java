package zwuiix.colria.cmd.impl.world;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.cmd.arguments.BoolArgument;
import zwuiix.colria.cmd.arguments.StringEnumArgument;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Fs;

import java.nio.file.Path;
import java.util.Map;

public class WorldLoadSubCommand extends ColriaPlayerSubCommand {
    public WorldLoadSubCommand() {
        super("load");
    }

    @Override
    public void prepare() {
        registerArgument(0, new StringEnumArgument("name", false, Fs.getFolders(Path.of(Server.getInstance().getFilePath(), "worlds")).toArray(String[]::new)));
        registerArgument(1, new BoolArgument("force", true));
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        String name = args.get("name").toString();

        Level level = player.getServer().getLevelByName(name);
        if(level != null) {
            player.sendMessage(TranslationKeys.WORLD_LOAD_ALREADY, name);
            return;
        }

        if(!player.getServer().loadLevel(name)) {
            player.sendMessage(TranslationKeys.WORLD_LOAD_ERROR, name);
            return;
        }

        player.sendMessage(TranslationKeys.WORLD_LOAD_SUCCESS, name);
    }
}
