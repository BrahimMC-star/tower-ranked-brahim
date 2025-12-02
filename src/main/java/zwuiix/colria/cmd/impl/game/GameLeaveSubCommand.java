package zwuiix.colria.cmd.impl.game;

import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.GamePlayer;
import zwuiix.colria.game.GameRegistry;
import zwuiix.colria.game.impl.lobby.Lobby;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Map;

public class GameLeaveSubCommand extends ColriaPlayerSubCommand {
    public GameLeaveSubCommand() {
        super("leave");
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        Game game = player.getGame();
        if (game == null || game instanceof Lobby) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_NOT);
            return;
        }

        if(game.getHoster().equalsIgnoreCase(player.getName())) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_LEAVE_CANT_HOSTER);
            return;
        }

        GamePlayer gamePlayer = game.getPlayer(player.getName());
        if(gamePlayer != null) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_LEAVE_CANT);
            return;
        }

        game.cleanup(player);
        game.removeSpectator(player);

        player.sendMessage(TranslationKeys.PLAYER_GAME_LEAVE_SUCCESS);
        Lobby lobby = GameRegistry.getInstance().randomLobby();
        lobby.join(player);
    }
}
