package zwuiix.colria.cmd.impl.game;

import cn.nukkit.command.CommandSender;
import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.gui.GameSettingsGUI;
import zwuiix.colria.game.impl.lobby.Lobby;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Map;

public class GameEditSubCommand extends ColriaPlayerSubCommand {
    public GameEditSubCommand() {
        super("edit");
    }

    @Override
    public void prepare() {
        setPermission(Permission.GAME_HOSTER.toString());
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
        if (game == null || game instanceof Lobby) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_NOT);
            return;
        }

        if (!game.getHoster().equalsIgnoreCase(player.getName()) && !game.getHosts().contains(player.getName().toLowerCase()) && !player.inAdminMode()) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_NOT_OWNER);
            return;
        }

        if (!game.getState().equals(Game.State.LOBBY)) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_START_ALREADY);
            return;
        }

        GameSettingsGUI gui = game.getSettingsGUI(player);
        gui.send();
    }
}