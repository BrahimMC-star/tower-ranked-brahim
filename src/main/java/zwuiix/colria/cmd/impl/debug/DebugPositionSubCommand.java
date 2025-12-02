package zwuiix.colria.cmd.impl.debug;

import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Map;

public class DebugPositionSubCommand extends ColriaPlayerSubCommand {
    public DebugPositionSubCommand() {
        super("position");
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        player.sendMessage(TranslationKeys.PLAYER_DEBUG_POSITION, player.getFloorX(), player.getFloorY(), player.getFloorZ());
    }
}
