package zwuiix.colria.cmd.impl.game;

import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.impl.lobby.Lobby;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Map;

public class GameDisbandSubCommand extends ColriaPlayerSubCommand {
    public GameDisbandSubCommand() {
        super("disband");
    }

    @Override
    public void prepare() {
        setPermission(Permission.GAME_HOSTER.toString());
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        Game game = player.getGame();
        if(game == null || game instanceof Lobby) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_NOT);
            return;
        }

        if(!game.getHoster().equalsIgnoreCase(player.getName()) && !player.inAdminMode()) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_NOT_OWNER);
            return;
        }

        if(!game.getState().equals(Game.State.LOBBY)) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_START_ALREADY);
            return;
        }

        game.stop();
    }
}
