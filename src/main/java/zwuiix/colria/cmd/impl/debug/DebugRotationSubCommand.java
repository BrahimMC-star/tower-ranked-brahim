package zwuiix.colria.cmd.impl.debug;

import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Map;

public class DebugRotationSubCommand extends ColriaPlayerSubCommand {
    public DebugRotationSubCommand() {
        super("rotation");
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        player.sendMessage(TranslationKeys.PLAYER_DEBUG_ROTATION, Math.floor(player.getYaw()), Math.floor(player.getPitch()));
    }
}
