package zwuiix.colria.cmd.impl.game;

import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.cmd.arguments.StringEnumArgument;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.impl.lobby.Lobby;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Map;

public class GameCoHostSubCommand extends ColriaPlayerSubCommand {
    public GameCoHostSubCommand() {
        super("cohost");
    }

    @Override
    public void prepare() {
        setPermission(Permission.GAME_HOSTER.toString());
        registerArgument(0, new StringEnumArgument("action", false, "add", "remove", "list"));
        registerArgument(1, new TargetArgument("target", true));
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

        String action = args.get("action").toString().toLowerCase();
        switch(action) {
            case "add": {
                if(!args.containsKey("target")) throw new IllegalArgumentException("Missing target argument");

                String target = args.get("target").toString().toLowerCase();
                if(game.getHosts().contains(target)) {
                    player.sendMessage(TranslationKeys.PLAYER_GAME_COHOST_ADD_ALREADY, target);
                    return;
                }

                var pl = game.getSpectator(target);
                if(pl != null) {
                    pl.sendCommandData();
                }

                game.getHosts().add(target);
                player.sendMessage(TranslationKeys.PLAYER_GAME_COHOST_ADD_SUCCESS, target);
                return;
            }
            case "remove": {
                if(!args.containsKey("target")) throw new IllegalArgumentException("Missing target argument");

                String target = args.get("target").toString().toLowerCase();
                if(!game.getHosts().contains(target)) {
                    player.sendMessage(TranslationKeys.PLAYER_GAME_COHOST_REMOVE_ALREADY, target);
                    return;
                }

                var pl = game.getSpectator(target);
                if(pl != null) {
                    pl.sendCommandData();
                }

                game.getHosts().remove(target);
                player.sendMessage(TranslationKeys.PLAYER_GAME_COHOST_REMOVE_SUCCESS, target);
                return;
            }
            case "list": {
                if(game.getHosts().isEmpty()) {
                    player.sendMessage(TranslationKeys.PLAYER_GAME_COHOST_LIST_EMPTY);
                    return;
                }

                player.sendMessage(TranslationKeys.PLAYER_GAME_COHOST_LIST, String.join(", ", game.getHosts()));
            }
        }
    }
}
