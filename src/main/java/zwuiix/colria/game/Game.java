package zwuiix.colria.game;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.DataPacket;
import lombok.Getter;
import lombok.Setter;
import zwuiix.colria.game.component.GameComponent;
import zwuiix.colria.game.component.types.DiscordComponent;
import zwuiix.colria.game.gui.GameSettingsGUI;
import zwuiix.colria.game.impl.lobby.Lobby;
import zwuiix.colria.game.kit.WaitingKit;
import zwuiix.colria.permission.Permission;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Fs;
import zwuiix.colria.util.Random;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import static zwuiix.colria.util.Rotation.facePitchTowards;

abstract public class Game {
    @Getter
    private final String name;
    @Getter
    protected String identifier;
    @Getter
    @Setter
    private String hoster;
    @Getter
    private final GameKit kit;
    @Getter
    private final GameParameters parameters;
    @Getter
    private State state = State.LOBBY;
    @Getter
    @Setter
    private GameInventory gameInventoryType = GameInventory.VANILLA;

    private boolean private_ = true;
    @Getter
    private final ArrayList<String> whitelist = new ArrayList<>();
    @Getter
    private final ArrayList<String> blacklist = new ArrayList<>();
    @Getter
    private final ArrayList<String> hosts = new ArrayList<>();

    private final Map<Class<? extends GameComponent>, GameComponent> components = new HashMap<>();

    @Getter
    @Setter
    private long startTick = 0;
    @Getter
    private GameTask task;

    @Getter
    @Setter
    private Level waitingLevel;
    @Getter
    @Setter
    private Level gameLevel;

    @Getter
    @Setter
    private boolean ranked = false;
    @Getter
    @Setter
    private boolean announced = false;

    @Getter
    private final HashMap<String, GamePlayer> startedPlayers = new HashMap<>();
    @Getter
    private final HashMap<String, GamePlayer> players = new HashMap<>();
    @Getter
    private final HashMap<String, EnginePlayer> spectators = new HashMap<>();

    public Game(String name, GameKit kit) {
        this.name = name;
        this.identifier = Random.utf8(4);
        this.hoster = "CONSOLE";
        this.kit = kit;
        this.parameters = new GameParameters();
    }

    public Game(String name, String hoster, GameKit kit, GameParameters parameters) {
        this.name = name;
        this.identifier = Random.utf8(6);
        this.hoster = hoster;
        this.kit = kit;
        this.parameters = parameters;

        this.waitingLevel = new GameLevelGenerator("waiting").create(getIdentifier());
        setState(State.LOBBY);

        GameRegistry.GameMode gameMode = GameRegistry.getInstance().getGameMode(getName());
        if(gameMode == null) throw new NullPointerException("Game mode is null");

        List<GameLevel> levels = gameMode.levels();
        if(levels.isEmpty()) throw new NullPointerException("Game levels is empty");

        GameLevel gameLevel = levels.get(ThreadLocalRandom.current().nextInt(levels.size()));
        this.gameLevel = Server.getInstance().getLevelByName(gameLevel.name);

        Server.getInstance().getScheduler().scheduleRepeatingTask(task = new GameTask(this), 1);
    }

    public String getGameId() {
        return name + "#" + identifier;
    }

    public boolean isAutomatedHost() {
        return hoster.equalsIgnoreCase("CONSOLE");
    }

    public Level getCurrentLevel() {
        if(state.equals(State.RUNNING) || state.equals(State.PAUSE) || !players.isEmpty() || waitingLevel == null) {
            if(gameLevel == null) throw new IllegalArgumentException("gameLevel is null");
            return gameLevel;
        }

        return waitingLevel;
    }

    public void setState(State state) {
        this.state = state;
        applyRules(getCurrentLevel());
    }

    public void incrementTick() {
        startTick++;
    }

    public boolean isPrivate() {
        return private_;
    }

    public void setPrivate(boolean private_) {
        this.private_ = private_;
    }

    public GamePlayer getPlayer(String name) {
        return players.get(name);
    }

    public void rejoin(GamePlayer player) {
        players.put(name, player);
        EnginePlayer p = player.getNukkitPlayer();
        if(p != null) cleanup(p);

        broadcast(TranslationKeys.PLAYER_GAME_RUNNING_JOIN, name);
    }

    public void addPlayer(String name, GamePlayer player) {
        players.put(name, player);
    }

    public void removePlayer(EnginePlayer player) {
        if(!players.containsKey(player.getName())) {
            return;
        }

        players.remove(player.getName());
        if(state.equals(State.RUNNING) || state.equals(State.PAUSE)) {
            broadcast(TranslationKeys.PLAYER_GAME_RUNNING_LEAVE, player.getName());
        }
    }

    public EnginePlayer getSpectator(String name) {
        return spectators.get(name);
    }

    public void addSpectator(EnginePlayer player) {
        spectators.put(player.getName(), player);
        player.setGame(this);

        if(!player.inAdminMode() && !player.isInLobby()) broadcast(TranslationKeys.PLAYER_GAME_SPECTATOR_JOIN, player.getName());

        cleanup(player, (state.equals(State.RUNNING) || state.equals(State.PAUSE)) ? 3 : 0);

        var pos = Position.fromObject(getCurrentLevel().getSpawnLocation().floor().add(0.5, 0, 0.5), getCurrentLevel());
        player.teleport(pos);
        player.setPosition(pos);

        if (hasComponent(DiscordComponent.class)) {
            tryGetComponent(DiscordComponent.class).onPlayerJoin(player.getPlayerDataInfo().getDiscordId());
        }
    }

    public void removeSpectator(EnginePlayer player) {
        if(!spectators.containsKey(player.getName())) {
            return;
        }

        spectators.remove(player.getName());
        if(players.containsKey(player.getName())) {
            return;
        }

        if(!player.inAdminMode() && !player.isInLobby()) broadcast(TranslationKeys.PLAYER_GAME_SPECTATOR_LEAVE, player.getName());
        player.setGame(null);

        if (hasComponent(DiscordComponent.class)) {
            tryGetComponent(DiscordComponent.class).onPlayerQuit(player.getPlayerDataInfo().getDiscordId());
        }
    }

    abstract public void prepare();

    public void start() {
        startTick = 0;
        setState(State.RUNNING);

        broadcast(TranslationKeys.PLAYER_GAME_START_BROADCAST);
        this.global((player) -> player.addSound("game.start", 1.0f, 1.0f));

        if (hasComponent(DiscordComponent.class)) {
            tryGetComponent(DiscordComponent.class).onGameStart();
        }
    }

    public void stop() {
        GameEvent.unsubscribeAll(this);
        disband(!getState().equals(State.LOBBY));
        setState(State.FINISHED);

        if (hasComponent(DiscordComponent.class)) {
            tryGetComponent(DiscordComponent.class).onGameEnd();
        }
    }

    public void disband(boolean force) {
        if (!force) {
            broadcast(TranslationKeys.PLAYER_GAME_DISBAND_BROADCAST);

            EnginePlayer hoster = (EnginePlayer) Server.getInstance().getPlayerExact(getHoster());
            if (hoster != null) {
                hoster.sendMessage(TranslationKeys.PLAYER_GAME_DISBAND_SUCCESS);
            }
        }

        var playerSnapshot = new ArrayList<>(players.values());
        for (GamePlayer gamePlayer : playerSnapshot) {
            EnginePlayer player = gamePlayer.getNukkitPlayer();
            if (player == null) continue;

            removeSpectator(player);
            removePlayer(player);

            cleanup(player, 0);
            Lobby lobby = GameRegistry.getInstance().randomLobby();
            lobby.join(player);
        }

        var spectatorSnapshot = new ArrayList<>(spectators.values());
        for (EnginePlayer player : spectatorSnapshot) {
            if (player == null) continue;

            removeSpectator(player);
            removePlayer(player);

            cleanup(player, 0);

            Lobby lobby = GameRegistry.getInstance().randomLobby();
            lobby.join(player);
        }

        Server server = Server.getInstance();
        if (waitingLevel != null && waitingLevel.getFolderName().contains(getIdentifier())) {
            String name = waitingLevel.getFolderName();
            if (server.isLevelLoaded(name)) {
                server.unloadLevel(waitingLevel);
            }
            Fs.deleteDirectory(Path.of(server.getDataPath(), "worlds", name));
        }

        if (gameLevel != null && gameLevel.getFolderName().contains(getIdentifier())) {
            String name = gameLevel.getFolderName();
            if (server.isLevelLoaded(name)) {
                server.unloadLevel(gameLevel);
            }
            Fs.deleteDirectory(Path.of(server.getDataPath(), "worlds", name));
        }

        GameRegistry.getInstance().removeGame(this);
    }

    public void broadcast(TranslationKeys translatable, Object ...params) {
        for(GamePlayer gamePlayer : players.values()) {
            EnginePlayer player = gamePlayer.getNukkitPlayer();
            if(player == null) continue;

            player.sendMessage(translatable, params);
        }

        for(EnginePlayer player : spectators.values()) {
            player.sendMessage(translatable, params);
        }
    }

    public void broadcast(String message) {
        global(player -> player.sendMessage(message));
    }

    public void broadcast(String title, String subTitle) {
        global(player -> player.sendTitle(title, subTitle));
    }

    public void broadcast(DataPacket pk) {
        for(GamePlayer gamePlayer : players.values()) {
            EnginePlayer player = gamePlayer.getNukkitPlayer();
            if(player == null) continue;

            player.dataPacket(pk);
        }

        for(EnginePlayer player : spectators.values()) {
            player.dataPacket(pk);
        }
    }

    public void broadcast(Consumer<GamePlayer> callback) {
        for (GamePlayer gp : players.values()) {
            callback.accept(gp);
        }
    }

    public void global(Consumer<EnginePlayer> callback) {
        for (GamePlayer gp : players.values()) {
            Player p = Server.getInstance().getPlayerExact(gp.getUsername());
            if (p instanceof EnginePlayer ep) {
                callback.accept(ep);
            }
        }

        for (EnginePlayer ep : spectators.values()) {
            if (ep != null) {
                callback.accept(ep);
            }
        }
    }

    public void teleport(GamePlayer gamePlayer, Position position) {
        EnginePlayer p = gamePlayer.getNukkitPlayer();
        if(p == null) return;

        Vector3 from = new Vector3(Math.floor(position.x) + 0.5, position.y, Math.floor(position.z) + 0.5);
        Vector3 target3D = getCurrentLevel().getSafeSpawn();

        float yaw = 90.0f;
        float pitch = facePitchTowards(from, target3D);

        p.teleport(new Location(from.x, from.y, from.z, yaw, pitch, getCurrentLevel()));
    }

    public void respawn(GamePlayer gamePlayer, Position position) {
        EnginePlayer p = gamePlayer.getNukkitPlayer();
        if(p == null) {
            return;
        }

        cleanup(p);
        kit.apply(gamePlayer, p);
        p.setImmobile(true);

        spawn(gamePlayer, position);
        gamePlayer.respawnTicks = 20 * getParameters().respawnTime;
    }

    public void spawn(GamePlayer gamePlayer, Position position) {
        teleport(gamePlayer, position);
    }

    public void cleanup(EnginePlayer player) {
        cleanup(player, getState().equals(State.RUNNING) ? 0 : 2);
    }

    public void cleanup(EnginePlayer player, int gamemode) {
        player.removeAllWindows();

        try {
            player.getInventory().clearAll();
            player.getEnderChestInventory().clearAll();
            player.getOffhandInventory().clearAll();
            player.getCursorInventory().clearAll();
            player.getCraftingGrid().clearAll();

            player.resetFallDistance();
            player.resetInAirTicks();
            player.resetCraftingGridType();
            player.resetTitleSettings();

            player.getFoodData().setFood(20);
            player.getFoodData().setSaturation(20);
            player.getFoodData().setExhaustion(0);
            player.getFoodData().setEnabled(false);

            player.setMaxHealth(20);
            player.setHealth(20);
            player.setAbsorption(0);
            player.setExperience(0, 0);
            player.setNoClip(false);
            player.setAllowFlight(false);
            player.setCanPickupXP(false);
            player.setCrawling(false);
            player.setSprinting(false);
            player.setSneaking(false);
            player.setSwimming(false);
            player.setGliding(false);
            player.setBlocking(false);
            player.setCanClimbWalls(false);
            player.setCanClimb(true);
            player.setImmobile(false);
            player.setScale(1.0f);
            player.setUsingItem(false);
            player.setPassThroughBarrier(false);
            player.setAllowInteract(true);
            player.setAllowModifyWorld(true);

            player.setGamemode(gamemode);
        } catch (Exception ignored) {}
    }

    public void applyRules(Level level) {
        level.getGameRules().setGameRule(GameRule.COMMAND_BLOCKS_ENABLED, false);
        level.getGameRules().setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        level.getGameRules().setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        level.getGameRules().setGameRule(GameRule.DO_ENTITY_DROPS, false);
        level.getGameRules().setGameRule(GameRule.DO_INSOMNIA, false);
        level.getGameRules().setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        level.getGameRules().setGameRule(GameRule.DO_MOB_LOOT, false);
        level.getGameRules().setGameRule(GameRule.DO_MOB_SPAWNING, false);
        level.getGameRules().setGameRule(GameRule.DO_TILE_DROPS, false);
        level.getGameRules().setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        level.getGameRules().setGameRule(GameRule.DROWNING_DAMAGE, false);
        level.getGameRules().setGameRule(GameRule.FIRE_DAMAGE, false);
        level.getGameRules().setGameRule(GameRule.FREEZE_DAMAGE, false);
        level.getGameRules().setGameRule(GameRule.KEEP_INVENTORY, false);
        level.getGameRules().setGameRule(GameRule.LOCATOR_BAR, false);
        level.getGameRules().setGameRule(GameRule.MOB_GRIEFING, false);
        level.getGameRules().setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        level.getGameRules().setGameRule(GameRule.SEND_COMMAND_FEEDBACK,false);
        level.getGameRules().setGameRule(GameRule.SHOW_COORDINATES,false);
        level.getGameRules().setGameRule(GameRule.SHOW_DAYS_PLAYED,false);
        level.getGameRules().setGameRule(GameRule.SHOW_DEATH_MESSAGES,false);
        level.getGameRules().setGameRule(GameRule.SPAWN_RADIUS,0);
        level.getGameRules().setGameRule(GameRule.TNT_EXPLODES,false);
        level.getGameRules().setGameRule(GameRule.TNT_EXPLOSION_DROP_DECAY,false);
        level.getGameRules().setGameRule(GameRule.SHOW_BORDER_EFFECT,false);
        level.getGameRules().setGameRule(GameRule.RECIPES_UNLOCK,true);
        level.getGameRules().setGameRule(GameRule.RESPAWN_BLOCKS_EXPLODE,false);
        level.getGameRules().setGameRule(GameRule.DO_LIMITED_CRAFTING,false);
        level.getGameRules().setGameRule(GameRule.SHOW_RECIPE_MESSAGE,false);
        level.getGameRules().setGameRule(GameRule.PROJECTILES_CAN_BREAK_BLOCKS,false);
        level.getGameRules().setGameRule(GameRule.SHOW_TAGS,false);

        level.getGameRules().setGameRule(GameRule.FALL_DAMAGE, parameters.fallDamage);
        level.getGameRules().setGameRule(GameRule.NATURAL_REGENERATION, parameters.naturalGeneration);
        level.getGameRules().setGameRule(GameRule.PVP, state.equals(State.RUNNING));
    }

    public void join(EnginePlayer player) {
        addSpectator(player);

        if(
                player.hasPermission(Permission.LOBBY_JOIN_SAYINGS.toString()) &&
                getName().equalsIgnoreCase("lobby") &&
                player.getHighestRank().getId() != 0)
        {
            broadcast(TranslationKeys.PLAYER_LOBBY_BROADCAST_JOIN, player.getHighestRank().getColoredName() + " " + player.getName());
        }

        if(getState().equals(State.LOBBY)) {
            getLobbyKit().apply(player);
            return;
        }
    }

    public <T extends GameComponent> void addComponent(T component) {
        components.put(component.getClass(), component);
    }

    public <T extends GameComponent> void removeComponent(Class<T> clazz) {
        components.remove(clazz);
    }

    public boolean hasComponent(Class<? extends GameComponent> clazz) {
        return components.containsKey(clazz);
    }

    public <T extends GameComponent> T tryGetComponent(Class<T> clazz) {
        return clazz.cast(components.get(clazz));
    }

    public GameKit getLobbyKit() {
        return WaitingKit.INSTANCE;
    }

    public GameSettingsGUI getSettingsGUI(EnginePlayer player) {
        return new GameSettingsGUI(this, player);
    }

    public enum State {
        LOBBY,
        RUNNING,
        PAUSE,
        FINISHED,
    }

    public enum GameInventory {
        VANILLA,
        DONKEY,
    }
}
