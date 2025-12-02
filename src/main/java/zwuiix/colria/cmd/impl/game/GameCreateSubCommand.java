package zwuiix.colria.cmd.impl.game;

import zwuiix.colria.cmd.ColriaPlayerSubCommand;
import zwuiix.colria.cmd.arguments.StringEnumArgument;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.GameRegistry;
import zwuiix.colria.game.gui.GameCreateGUI;
import zwuiix.colria.game.impl.lobby.Lobby;
import zwuiix.colria.game.impl.tower.TowerGame;
import zwuiix.colria.game.impl.tower.TowerGameParameters;
import zwuiix.colria.inventory.VirtualInventory;
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

        registerArgument(0, new StringEnumArgument("mode", true, names.toArray(String[]::new)));
    }

    @Override
    public void execute(EnginePlayer player, Map<String, Object> args) {
        if(player.getGame() != null && !(player.getGame() instanceof Lobby)) {
            player.sendMessage(TranslationKeys.PLAYER_GAME_IN);
            return;
        }

        if(args.containsKey("mode")) {
            GameRegistry.GameMode mode = GameRegistry.getInstance().getGameMode(args.get("mode").toString());
            if(mode == null) {
                throw new IllegalArgumentException("Invalid mode " + args.get("mode"));
            }

            processCreation(player, mode);
            return;
        }

        VirtualInventory gui = GameCreateGUI.create(player);
        gui.open(player);
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

        game.join(player);
        GameRegistry.getInstance().addGame(game);
    }
}
