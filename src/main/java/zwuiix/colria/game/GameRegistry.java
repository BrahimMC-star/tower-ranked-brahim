package zwuiix.colria.game;

import cn.nukkit.Server;
import cn.nukkit.block.*;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;
import lombok.Getter;
import zwuiix.colria.Loader;
import zwuiix.colria.game.impl.lobby.Lobby;
import zwuiix.colria.game.impl.team.TeamGameLevel;
import zwuiix.colria.game.impl.team.TeamSpawnPoint;
import zwuiix.colria.game.impl.tower.TowerGame;
import zwuiix.colria.game.impl.towerbridge.TowerBridgeGame;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class GameRegistry {
    @Getter
    private static final GameRegistry instance  = new GameRegistry();

    public GameRegistry() {}

    private final LinkedHashMap<String, Game> games = new LinkedHashMap<>();
    private final LinkedHashMap<String, Lobby> lobbies = new LinkedHashMap<>();

    public Lobby randomLobby() {
        int size = lobbies.size();
        if (size == 0) return null;

        int target = ThreadLocalRandom.current().nextInt(size);

        Iterator<Lobby> it = lobbies.values().iterator();
        for (int i = 0; i < target; i++) {
            it.next();
        }
        return it.next();
    }

    public Lobby getLobby(String name) {
        return lobbies.getOrDefault(name, null);
    }

    public void addLobby(Lobby lobby) {
        lobbies.put(lobby.getGameId(), lobby);
    }

    public void removeLobby(Lobby lobby) {
        if(!lobbies.containsKey(lobby.getGameId())) return;
        lobbies.remove(lobby.getGameId());
    }

    public LinkedList<Game> getGames(GameMode gameMode) {
        LinkedList<Game> list = new LinkedList<>();
        for(Game game : games.values()) {
            if(game.getName().equalsIgnoreCase(gameMode.name)) {
                list.add(game);
            }
        }
        return list;
    }

    public Game getGame(String name) {
        for(Game key : games.values()) {
            if(key.getGameId().equalsIgnoreCase(name)) {
                return key;
            }
        }

        return null;
    }

    public void addGame(Game game) {
        games.put(game.getGameId().toLowerCase(), game);
    }

    public void removeGame(Game game) {
        if(!games.containsKey(game.getGameId().toLowerCase())) return;
        games.remove(game.getGameId().toLowerCase());
    }

    private final ArrayList<GameMode> gameModes = new ArrayList<>();

    public GameMode getGameMode(String name) {
        for (GameMode mode : gameModes) {
            if (mode.name().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return null;
    }

    public void register(GameMode gameMode) {
        gameModes.add(gameMode);

        for (GameLevel level : gameMode.levels) {
            Server.getInstance().getLogger().info("Loading level " + level.name + " for gamemode " + gameMode.name);
            Server.getInstance().loadLevel(level.name);
        }
    }

    public void invoke(Loader loader) {
        for (int i = 0; i < 5; i++) {
            addLobby(new Lobby());
        }

        Item bricks = new BlockBricks().toItem();
        bricks.setCustomName("player.gamemode.towerfast.name");
        bricks.setLore("player.gamemode.towerfast.description");

        Item netherBricks = new BlockNetherBrick().toItem();
        netherBricks.setCustomName("player.gamemode.towerbridge.name");
        netherBricks.setLore("player.gamemode.towerbridge.description");

        register(new GameMode("TowerFast", bricks, TowerGame.class, List.of(
                new TeamGameLevel("tfcastle", new BlockBricksStone().toItem(), new Vector3(0, 5, 0), new TeamSpawnPoint(new Vector3(84, 5, 0), new Vector3(-84, 5, 0), null, null)),
                new TeamGameLevel("tfalius", new BlockAmethyst().toItem(), new Vector3(0, 2, 0), new TeamSpawnPoint(new Vector3(-84, 2, 0), new Vector3(84, 2, 0), null, null)),
                new TeamGameLevel("tfbabylone", new BlockSandstone().toItem(), new Vector3(0, 11, 0), new TeamSpawnPoint(new Vector3(-84, 11, 0), new Vector3(84, 11, 0), null, null)),
                new TeamGameLevel("tfclassic", new BlockSprucePlanks().toItem(), new Vector3(0, 13, 0), new TeamSpawnPoint(new Vector3(-84, 13, 0), new Vector3(84, 13, 0), null, null)),
                new TeamGameLevel("tfcyclone", new BlockNetherBrick().toItem(), new Vector3(0, 11, 0), new TeamSpawnPoint(new Vector3(-77, 11, 31), new Vector3(77, 11, -31), null, null))/*,
                new TeamGameLevel("towerfast_race", new BlockBlackstone().toItem(), new Vector3(4, 13, 0), new TeamSpawnPoint(new Vector3(90, 13, 0), new Vector3(-84, 13, 0), null, null)),
                new TeamGameLevel("towerfast_japan", new BlockAcaciaPlanks().toItem(), new Vector3(1, 13, 0), new TeamSpawnPoint(new Vector3(-85, 13, 0), new Vector3(84, 13, 0), null, null)),
                new TeamGameLevel("towerfast_manoir", new BlockDarkOakLog().toItem(), new Vector3(0, 13, -85), new TeamSpawnPoint(new Vector3(0, 13, 1), new Vector3(0, 13, -170), null, null)),
                new TeamGameLevel("towerfast_f1", new BlockCoal().toItem(), new Vector3(0, 13, 0), new TeamSpawnPoint(new Vector3(-84, 13, 0), new Vector3(84, 13, 0), null, null)),
                new TeamGameLevel("towerfast_nether", new BlockNetherrack().toItem(), new Vector3(11, 13, 0), new TeamSpawnPoint(new Vector3(108, 13, 0), new Vector3(-85, 13, 0), null, null))*/
        )));

        register(new GameMode("TowerBridge", netherBricks, TowerBridgeGame.class, List.of(
                new TeamGameLevel("tbmario", new BlockTerracotta(13).toItem(), new Vector3(0, 13, 0), new TeamSpawnPoint(new Vector3(34, 18, 0), new Vector3(-34, 18, 0), null, null)),
                new TeamGameLevel("tbsakai", new BlockPrismarine().toItem(), new Vector3(0, 13, 0), new TeamSpawnPoint(new Vector3(-35, 17, 0), new Vector3(35, 17, 0), null, null))
        )));
    }

    public record GameMode(String name, Item reference, Class<? extends Game> gameClass, List<GameLevel> levels) {}
}
