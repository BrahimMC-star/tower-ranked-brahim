package zwuiix.colria.game.impl.team;

import cn.nukkit.Server;
import cn.nukkit.utils.TextFormat;
import lombok.Setter;
import zwuiix.colria.game.*;
import zwuiix.colria.game.impl.team.gui.TeamSelectorGUI;
import zwuiix.colria.game.impl.team.kit.TeamLobbyKit;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.HashMap;
import java.util.Map;

abstract public class TeamGame extends Game {
    @Setter
    private Team teamA;
    @Setter
    private Team teamB;

    @Setter
    private TeamSpawnPoint spawnPoint;
    private final TeamSelectorGUI teamSelector;

    private final HashMap<EnginePlayer, Team> teams = new HashMap<>();

    public TeamGame(String name, String hoster, GameKit kit, TeamGameParameters parameters, Team teamA, Team teamB) {
        super(name, hoster, kit, parameters);
        this.teamA = teamA;
        this.teamB = teamB;

        this.teamSelector = new TeamSelectorGUI(this);
        new TeamListener(this);
    }

    public Team getTeamA() {
        return teamA;
    }

    public Team getTeamB() {
        return teamB;
    }

    public TeamSpawnPoint getSpawnPoint() {
        return spawnPoint;
    }

    public HashMap<EnginePlayer, Team> getTeams() {
        return teams;
    }

    public TeamSelectorGUI getTeamSelector() {
        return teamSelector;
    }

    @Override
    public GameKit getLobbyKit() {
        return TeamLobbyKit.INSTANCE;
    }

    public void createPlayer(EnginePlayer player, Team team) {
        addPlayer(player.getName(), new TeamPlayer(player.getName(), this, team));
    }

    public void editGameWorld(Runnable runnable) {
        runnable.run();
    }
    @Override
    public void prepare() {
        GameRegistry.GameMode gameMode = GameRegistry.getInstance().getGameMode(getName());
        TeamSpawnPoint spawnPoint = null;
        for (GameLevel gameLevel : gameMode.levels()) {
            if (gameLevel instanceof TeamGameLevel && gameLevel.name.equalsIgnoreCase(getGameLevel().getFolderName())) {
                spawnPoint = ((TeamGameLevel) gameLevel).spawnPoint;
            }
        }

        if (spawnPoint == null) {
            stop();
            return;
        }

        setSpawnPoint(spawnPoint);

        setGameLevel(new GameLevelGenerator(getGameLevel().getFolderName()).create(getIdentifier()));

        broadcast(TranslationKeys.PLAYER_GAME_START_PREPARE);
        Server.getInstance().getScheduler().scheduleDelayedTask(() -> {
            editGameWorld(() -> {
                start();
                for (Map.Entry<EnginePlayer, Team> entry : getTeams().entrySet()) {
                    EnginePlayer player = entry.getKey();
                    Team team = entry.getValue();

                    createPlayer(player, team);
                    removeSpectator(player);
                    getStartedPlayers().put(player.getName(), getPlayer(player.getName()));

                    cleanup(player);
                    preparePlayer(player);
                    getKit().apply(player);
                    player.setNameTag(team.color() + player.getName());
                }

                for(EnginePlayer player : getSpectators().values()) {
                    cleanup(player, 3);
                    player.teleport(getGameLevel().getSpawnLocation());
                    player.setNameTag(TextFormat.GRAY + player.getName());
                }
            });
        }, 20);
    }

    abstract public void preparePlayer(EnginePlayer player);
}
