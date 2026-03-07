package zwuiix.colria.cmd.impl.game;

import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.cmd.arguments.StringArgument;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.GameRegistry;
import zwuiix.colria.game.component.types.DiscordComponent;
import zwuiix.colria.game.impl.lobby.Lobby;
import zwuiix.colria.game.impl.team.TeamGame;
import zwuiix.colria.game.impl.team.TeamGameParameters;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Map;

public class GameJoinSubCommand extends ColriaPlayerSubCommand {
    public GameJoinSubCommand() {
        super("join");
    }

    @Override
    public void prepare() {
        registerArgument(0, new StringArgument("game", false));
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        if(player.getGame() != null && !(player.getGame() instanceof Lobby)) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_IN);
            return;
        }

        String gameId = (String)args.get("game");
        Game game = GameRegistry.getInstance().getGame(gameId);
        if(game == null) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_INVALID, gameId);
            return;
        }

        if(game.getBlacklist().contains(player.getName().toLowerCase()) && !player.inAdminMode()) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_BLACKLISTED);
            return;
        }

        if(game.isPrivate() && !game.getWhitelist().contains(player.getName().toLowerCase()) && !player.inAdminMode()) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_PRIVATE, gameId);
            return;
        }

        if(!game.getState().equals(Game.State.LOBBY)) {
            game.addSpectator(player);
            player.sendMessage(TranslationKeys.PLAYER_GAME_JOINED_SPECTATOR);
            return;
        }

        if(game instanceof TeamGame teamGame) {
            TeamGameParameters params = (TeamGameParameters) teamGame.getParameters();
            if(teamGame.getSpectators().size() > params.maxPlayers * 2) {
                player.sendMessage(TranslationKeys.PLAYER_GAME_FULL);
                return;
            }
        }

        if (game.hasComponent(DiscordComponent.class)) {
            var info = player.getPlayerDataInfo();
            if (info.getDiscordId().isEmpty()) {
                player.sendMessage(TranslationKeys.PLAYER_GAME_REQUIRE_DISCORD_LINK);
                return;
            }
        }

        game.join(player);
    }
}
