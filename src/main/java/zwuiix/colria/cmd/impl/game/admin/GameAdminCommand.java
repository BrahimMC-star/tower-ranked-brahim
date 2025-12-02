package zwuiix.colria.cmd.impl.game.admin;

import zwuiix.colria.cmd.ColriaPlayerCommand;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.Map;

public class GameAdminCommand extends ColriaPlayerCommand {
    public GameAdminCommand() {
        super("gameadmin", "Manage game with admin permissions");
    }


    @Override
    public void prepare() {
        setPermission(Permission.GAME_ADMIN.toString());
        registerSubCommand(new GameAdminStopSubCommand());
        registerSubCommand(new GameAdminOwnerShipSubCommand());
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        boolean now = !player.inAdminMode();
        if(now) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_ADMIN_MODE_ENABLED);
        } else player.sendMessage(TranslationKeys.PLAYER_GAME_ADMIN_MODE_DISABLED);

        player.setInAdminMode(now);
    }
}
