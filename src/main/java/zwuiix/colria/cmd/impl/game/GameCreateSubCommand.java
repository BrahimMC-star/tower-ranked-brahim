package zwuiix.colria.cmd.impl.game;

import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.cmd.arguments.StringEnumArgument;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.GameRegistry;
import zwuiix.colria.game.impl.lobby.Lobby;
import zwuiix.colria.game.impl.tower.TowerGame;
import zwuiix.colria.game.impl.tower.TowerGameParameters;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.ArrayList;
import java.util.Map;

public class GameCreateSubCommand extends ColriaPlayerSubCommand {
    public GameCreateSubCommand() {
        super("create");
    }

    @Override
    public void prepare() {
        setPermission(Permission.GAME_HOSTER.toString());
        ArrayList<String> names = new ArrayList<>();
        for (GameRegistry.GameMode mode : GameRegistry.getInstance().getGameModes()) {
            names.add(mode.name().toLowerCase());
        }

        registerArgument(0, new StringEnumArgument("mode", false, names.toArray(String[]::new)));
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        if(player.getGame() != null && !(player.getGame() instanceof Lobby)) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_IN);
            return;
        }

        GameRegistry.GameMode mode = GameRegistry.getInstance().getGameMode(args.get("mode").toString());
        if(mode == null) {
            throw new IllegalArgumentException("Invalid mode " + args.get("mode"));
        }

        processCreation(player, mode);
    }

    private void processCreation(EnginePlayer player, GameRegistry.GameMode mode) {
        Class<? extends Game> gameClass = mode.gameClass();

        Game game = null;
        if(gameClass.equals(TowerGame.class)) {
            game = new TowerGame(mode.name(), player.getName(), new TowerGameParameters());
        }

        if(game == null) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_CREATE_ERROR);
            return;
        }

        var cd = player.getCooldown("game_create");
        if(!cd.isExpired() && !player.inAdminMode()) {
            var secs = cd.getRemainingTime() / 1000;
            long minutes = secs / 60;
            long seconds = secs % 60;

            String format = String.format("%d:%02dm", minutes, seconds);
            if(minutes == 0) {
                format = String.format("%02ds", seconds);
            }

            player.sendMessage(TranslationKeys.PLAYER_GAME_ANNOUNCE_COOLDOWN, format);
            return;
        }

        cd.refresh(300); // 5 minutes cooldown

        game.join(player);
        GameRegistry.getInstance().addGame(game);
    }
}
