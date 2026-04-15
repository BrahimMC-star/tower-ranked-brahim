package zwuiix.colria.cmd.impl.game;

import cn.nukkit.command.CommandSender;
import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.cmd.arguments.MessageArgument;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.impl.lobby.Lobby;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Chat;

import java.util.Map;

public class GameSaySubCommand extends ColriaPlayerSubCommand {
    public GameSaySubCommand() {
        super("say");
    }

    @Override
    public void prepare() {
        setPermission(Permission.GAME_HOSTER.toString());
        registerArgument(0, new MessageArgument("message", false));
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

        String message = args.get("message").toString();
        game.broadcast(TranslationKeys.PLAYER_GAME_HOSTER_SAY, player.getName(), Chat.clean(message));
    }
}
