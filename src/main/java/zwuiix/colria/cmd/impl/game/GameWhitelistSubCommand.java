package zwuiix.colria.cmd.impl.game;

import cn.nukkit.command.CommandSender;
import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.cmd.arguments.StringEnumArgument;
import zwuiix.colria.cmd.arguments.TargetArgument;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.impl.lobby.Lobby;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Map;

public class GameWhitelistSubCommand extends ColriaPlayerSubCommand {
    public GameWhitelistSubCommand() {
        super("whitelist");
    }

    @Override
    public void prepare() {
        setPermission(Permission.GAME_HOSTER.toString());
        registerArgument(0, new StringEnumArgument("action", false, "add", "remove", "list"));
        registerArgument(1, new TargetArgument("target", true));
    }

    @Override
    public boolean hasConditions(CommandSender sender) {
        if(!(sender instanceof EnginePlayer player)) return false;
        var game = player.getGame();
        if(game == null) return false;

        return game.getHosts().contains(sender.getName().toLowerCase());
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        Game game = player.getGame();
        if(game == null || game instanceof Lobby) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_NOT);
            return;
        }

        if (!game.getHoster().equalsIgnoreCase(player.getName()) && !game.getHosts().contains(player.getName().toLowerCase()) && !player.inAdminMode()) {
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
                if(game.getWhitelist().contains(target)) {
                    player.sendMessage(TranslationKeys.PLAYER_GAME_WHITELIST_ADD_ALREADY, target);
                    return;
                }

                game.getWhitelist().add(target);
                player.sendMessage(TranslationKeys.PLAYER_GAME_WHITELIST_ADD_SUCCESS, target);
                return;
            }
            case "remove": {
                if(!args.containsKey("target")) throw new IllegalArgumentException("Missing target argument");

                String target = args.get("target").toString().toLowerCase();
                if(!game.getWhitelist().contains(target)) {
                    player.sendMessage(TranslationKeys.PLAYER_GAME_WHITELIST_REMOVE_ALREADY, target);
                    return;
                }

                game.getWhitelist().remove(target);
                player.sendMessage(TranslationKeys.PLAYER_GAME_WHITELIST_REMOVE_SUCCESS, target);
                return;
            }
            case "list": {
                if(game.getWhitelist().isEmpty()) {
                    player.sendMessage(TranslationKeys.PLAYER_GAME_WHITELIST_LIST_EMPTY);
                    return;
                }

                player.sendMessage(TranslationKeys.PLAYER_GAME_WHITELIST_LIST, String.join(", ", game.getWhitelist()));
            }
        }
    }
}
