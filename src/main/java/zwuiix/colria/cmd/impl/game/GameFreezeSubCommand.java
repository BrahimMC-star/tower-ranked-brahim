package zwuiix.colria.cmd.impl.game;

import cn.nukkit.command.CommandSender;
import cn.nukkit.network.protocol.LevelEventPacket;
import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.impl.lobby.Lobby;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Map;

public class GameFreezeSubCommand extends ColriaPlayerSubCommand {
    public GameFreezeSubCommand() {
        super("freeze");
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
        if(game == null || game instanceof Lobby) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_NOT);
            return;
        }

        if (!game.getHoster().equalsIgnoreCase(player.getName()) && !game.getHosts().contains(player.getName().toLowerCase()) && !player.inAdminMode()) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_NOT_OWNER);
            return;
        }

        if(game.getState().equals(Game.State.FINISHED)) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_STOPPED);
            return;
        }

        if(game.getState().equals(Game.State.PAUSE)) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_FREEZE_ALREADY);
            return;
        }

        if(!game.getState().equals(Game.State.RUNNING)) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_START_NEED);
            return;
        }

        game.setState(Game.State.PAUSE);
        game.broadcast(TranslationKeys.PLAYER_GAME_FREEZE_BROADCAST, player.getName());
        player.sendMessage(TranslationKeys.PLAYER_GAME_FREEZE_SUCCESS);

        LevelEventPacket pk = new LevelEventPacket();
        pk.evid = LevelEventPacket.EVENT_GLOBAL_PAUSE;
        pk.data = 1;
        pk.x = 1;
        pk.y = 0;
        pk.z = 0;
        game.broadcast(pk);
    }
}
