package zwuiix.colria.cmd.impl;

import cn.nukkit.Server;
import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.cmd.arguments.IntegerArgument;
import zwuiix.colria.cmd.arguments.StringEnumArgument;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.GameRegistry;
import zwuiix.colria.game.impl.lobby.Lobby;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.player.cosmetic.CosmeticRegistry;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Map;

public class HubCommand extends ColriaPlayerCommand {
    public HubCommand() {
        super("hub", "commands.hub.description");
        setAliases(new String[]{"lobby"});
    }

    @Override
    public void prepare() {
            registerArgument(0, new StringEnumArgument("lobby", true, GameRegistry.getInstance().getLobbies().values().stream().map(Lobby::getIdentifier).toArray(String[]::new)));
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        var game = player.getGame();
        Lobby lobby = GameRegistry.getInstance().randomLobby();

        if (args.containsKey("lobby")) {
            String lobbyId = args.get("lobby").toString().toLowerCase();
            lobby = GameRegistry.getInstance().getLobby("Lobby#" + lobbyId);
            if (lobby == null) {
                player.sendMessage(TranslationKeys.PLAYER_COMMAND_HUB_LOBBY_NOEXIST, lobbyId);
                return;
            }
        }

        if (player.isInLobby() || game == lobby) {
            player.sendMessage(TranslationKeys.PLAYER_COMMAND_HUB_ALREADY, game.getIdentifier());
            return;
        }

        if (game != null) {
            if (!game.getState().equals(Game.State.LOBBY)) {
                player.sendMessage(TranslationKeys.PLAYER_COMMAND_HUB_CANT);
                return;
            }

            game.removePlayer(player);
        }

        lobby.join(player);
        player.sendMessage(TranslationKeys.PLAYER_COMMAND_HUB_SUCCESS, lobby.getIdentifier());
    }
}
