package zwuiix.colria.cmd.impl.world;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.cmd.arguments.BoolArgument;
import zwuiix.colria.cmd.arguments.StringEnumArgument;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Fs;

import java.nio.file.Path;
import java.util.Map;

public class WorldTeleportSubCommand extends ColriaPlayerSubCommand {
    public WorldTeleportSubCommand() {
        super("teleport");
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
        if(level == null) {
            player.sendMessage(TranslationKeys.WORLD_TELEPORT_DONTEXIST, name);
            return;
        }

        Position position = level.getSpawnLocation();
        boolean hasForce = args.containsKey("force");

        if(position == null) {
            if(!hasForce) {
                player.sendMessage(TranslationKeys.WORLD_TELEPORT_ERROR, name);
                return;
            }

            boolean force = (boolean) args.get("force");
            if(!force) {
                player.sendMessage(TranslationKeys.WORLD_TELEPORT_ERROR, name);
                return;
            }

            player.teleport(Position.fromObject(new Vector3(0, 120, 0), level));
            player.sendMessage(TranslationKeys.WORLD_TELEPORT_SUCCESS_FORCE, name);
            return;
        }

        player.teleport(position);
        player.sendMessage(TranslationKeys.WORLD_TELEPORT_SUCCESS, name);
    }
}
