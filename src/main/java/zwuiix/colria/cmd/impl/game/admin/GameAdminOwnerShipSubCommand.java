package zwuiix.colria.cmd.impl.game.admin;

import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.impl.lobby.Lobby;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Map;

public class GameAdminOwnerShipSubCommand extends ColriaPlayerSubCommand {
    public GameAdminOwnerShipSubCommand() {
        super("ownership");
    }

    @Override
    public void prepare() {
        registerArgument(0, new TargetArgument("target", false));
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        Game game = player.getGame();
        if(game == null || game instanceof Lobby) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_NOT);
            return;
        }

        if(!game.getState().equals(Game.State.LOBBY)) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_START_ALREADY);
            return;
        }

        String target = args.get("target").toString();
        game.setHoster(target);

        var targetSpectator = game.getSpectator(target);
        var targetPlayer = game.getPlayer(target);
        if(targetSpectator != null) {
            targetSpectator.sendMessage(TranslationKeys.PLAYER_GAME_OWNERSHIP_RECEIVE);
        } else if(targetPlayer != null) {
            var nukkit = targetPlayer.getNukkitPlayer();
            if(nukkit != null) {
                nukkit.sendMessage(TranslationKeys.PLAYER_GAME_OWNERSHIP_RECEIVE);
            }
        }

        player.sendMessage(TranslationKeys.PLAYER_GAME_OWNERSHIP, target);
    }
}
