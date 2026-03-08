package zwuiix.colria.cmd.impl;

import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.cmd.arguments.StringEnumArgument;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.GameRegistry;
import zwuiix.colria.game.impl.lobby.Lobby;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Map;

public class SpawnCommand extends ColriaPlayerCommand {
    public SpawnCommand() {
        super("spawn", "commands.spawn.description");
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        var game = player.getGame();
        if (!player.isInLobby()) {
            player.sendMessage(TranslationKeys.PLAYER_COMMAND_SPAWN_NOT_IN_LOBBY);
            return;
        }

        game.join(player);
        player.sendMessage(TranslationKeys.PLAYER_COMMAND_SPAWN_SUCCESS);
    }
}
