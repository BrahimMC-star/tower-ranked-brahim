package zwuiix.colria.cmd.impl.game.admin;

import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.impl.lobby.Lobby;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Map;

public class GameAdminStopSubCommand extends ColriaPlayerSubCommand {
    public GameAdminStopSubCommand() {
        super("stop");
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        Game game = player.getGame();
        if(game == null || game instanceof Lobby) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_NOT);
            return;
        }

        game.broadcast(TranslationKeys.PLAYER_GAME_ADMIN_STOP_BROADCAST, player.getName());
        game.stop();
        player.sendMessage(TranslationKeys.PLAYER_GAME_ADMIN_STOP_SUCCESS);
    }
}
