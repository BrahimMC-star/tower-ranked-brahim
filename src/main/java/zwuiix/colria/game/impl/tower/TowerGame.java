package zwuiix.colria.game.impl.tower;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemAppleGold;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.MovePlayerPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;
import zwuiix.colria.EngineInfo;
import zwuiix.colria.booster.BoosterManager;
import zwuiix.colria.game.Game;
import zwuiix.colria.game.GamePlayer;
import zwuiix.colria.game.impl.team.*;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.player.PlayerDataInfo;
import zwuiix.colria.shape.Renderer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.DB;
import zwuiix.colria.util.Fade;
import zwuiix.colria.util.Glyph;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;

import static zwuiix.colria.util.Rotation.facePitchTowards;

public class TowerGame extends TeamGame {
    @Getter
    private final TowerPoints towerPoints = new TowerPoints();
    private final Renderer renderer = Renderer.create();
    private int nextAppleTick = -1;

    public TowerGame(String name, String hoster, TowerGameParameters parameters) {
        super(name, hoster, new TowerGameKit(), parameters, TeamColor.BLUE, TeamColor.RED);
        setGameInventoryType(GameInventory.DONKEY);
        Team teamA = TeamColor.ALL.get(ThreadLocalRandom.current().nextInt(TeamColor.ALL.size()));
        Team teamB = TeamColor.oppositeOf(teamA);
        setTeamA(teamA);
        setTeamB(teamB);
        getTeams().clear();

        new TowerListener(this);
        getTask().addTicker(i -> {
            Vector3 pos = getGameLevel().getSpawnLocation().floor().add(0.5, 0, 0.5);

            if(i % 10 == 0) {
                global((player) -> {
                    int apple = 0;
                    int maxApples = 48;
                    for (Item item : player.getInventory().getContents().values()) {
                        if (item instanceof ItemAppleGold) {
                            if(apple > maxApples) {
                                item.setCount(0);
                                continue;
                            }

                            if(apple + item.getCount() > maxApples) {
                                item.setCount(maxApples - apple);
                            }

                            apple += item.getCount();
                        }
                    }

                    if(player.protocol == ProtocolInfo.v1_21_120) return; // Minecraft be like.

                    ArrayList<String> lines = new ArrayList<>();
                    lines.add(player.processTranslation(TranslationKeys.PLAYER_GAME_TOWER_GENERATOR_TITLE));
                    lines.add("");
                    lines.add(player.processTranslation(TranslationKeys.PLAYER_GAME_TOWER_GENERATOR_IN, nextAppleTick / 20));

                    var textPos = pos.add(0, 2, 0);
                    int alpha = Fade.alpha(player, textPos, 7.0, 15.0, 0, 255);
                    var listRenderer = renderer.drawTextList("applegenerator", textPos, new Color(255, 255, 255, alpha), lines.reversed());
                    if (alpha <= 10) {
                        listRenderer.remove(player);
                    } else renderer.send(player);
                });
            }

            if(nextAppleTick == -1) nextAppleTick = parameters.appleGenerator * 20;
            if(nextAppleTick <= 0) {
                getGameLevel().dropItem(pos.add(0, 0.5, 0), new ItemAppleGold(), Vector3.ZERO);
                nextAppleTick = parameters.appleGenerator * 20;
                return;
            }

            if(towerPoints.first >= parameters.maxPoints || towerPoints.second >= parameters.maxPoints) {
                setState(State.FINISHED);
                return;
            }

            --nextAppleTick;
        });
    }

    @Override
    public TowerGameParameters getParameters() {
        return (TowerGameParameters) super.getParameters();
    }

    public void initTickerFor(EnginePlayer player) {
        player.addTicker(getGameId(), i -> {
            Game game = player.getGame();
            if(game != this) {
                player.removeTicker(getGameId());
                player.scoreboard.clear();
                return;
            }

            TeamGameParameters parameters = (TeamGameParameters) game.getParameters();
            TowerPlayer gamePlayer = (TowerPlayer) getPlayer(player.getName());

            ArrayList<String> lines = new ArrayList<>();
            switch (game.getState()) {
                case LOBBY -> {
                    var cTeamA = getTeams().values().stream().filter((team) -> team.equals(getTeamA())).count();
                    var cTeamB = getTeams().values().stream().filter((team) -> team.equals(getTeamB())).count();

                    var teamA = getTeamA().color() + player.processTranslation(getTeamA().name()) + TextFormat.WHITE + ": " + EngineInfo.COLOR + cTeamA + "/" + parameters.maxPlayers;
                    var teamB = getTeamB().color() + player.processTranslation(getTeamB().name()) + TextFormat.WHITE + ": " + EngineInfo.COLOR + cTeamB + "/" + parameters.maxPlayers;

                    lines.add(player.processTranslation(TranslationKeys.PLAYER_GAME_TOWER_SCOREBOARD_INFOS));
                    lines.add(EngineInfo.VBAR_DEFAULT + player.processTranslation(TranslationKeys.PLAYER_GAME_TOWER_SCOREBOARD_HOSTER, getHoster()));
                    lines.add(EngineInfo.VBAR_DEFAULT + player.processTranslation(TranslationKeys.PLAYER_GAME_TOWER_SCOREBOARD_MODE, getName()));
                    lines.add(TextFormat.MINECOIN_GOLD.toString());
                    lines.add(player.processTranslation(TranslationKeys.PLAYER_GAME_TOWER_SCOREBOARD_TEAMS));
                    lines.add(EngineInfo.VBAR_DEFAULT + teamA);
                    lines.add(EngineInfo.VBAR_DEFAULT + teamB);
                }
                case RUNNING, PAUSE -> {
                    long seconds = getStartTick() / 20;
                    String mmss = String.format("%02d:%02d", seconds / 60, seconds % 60);

                    lines.add(player.processTranslation(TranslationKeys.PLAYER_GAME_TOWER_SCOREBOARD_INFOS));
                    lines.add(EngineInfo.VBAR_DEFAULT + player.processTranslation(TranslationKeys.PLAYER_GAME_TOWER_SCOREBOARD_TIME, mmss));
                    lines.add(EngineInfo.VBAR_DEFAULT + player.processTranslation(TranslationKeys.PLAYER_GAME_TOWER_SCOREBOARD_MODE, getName()));
                    lines.add(TextFormat.MINECOIN_GOLD.toString());
                    lines.add(player.processTranslation(TranslationKeys.PLAYER_GAME_TOWER_SCOREBOARD_POINTS));

                    var you = player.processTranslation(TranslationKeys.PLAYER_GAME_TOWER_SCOREBOARD_YOU);
                    var teamA = getTeamA().color() + player.processTranslation(getTeamA().name()) + TextFormat.WHITE + ": " + EngineInfo.COLOR + towerPoints.first;
                    var teamB = getTeamB().color() + player.processTranslation(getTeamB().name()) + TextFormat.WHITE + ": " + EngineInfo.COLOR + towerPoints.second;

                    boolean spectator = getSpectator(player.getName()) != null;
                    if(!spectator && gamePlayer != null) {
                        if(gamePlayer.getTeam().equals(getTeamA())) teamA += " " + you;
                        if(gamePlayer.getTeam().equals(getTeamB())) teamB += " " + you;

                        lines.add(EngineInfo.VBAR_DEFAULT + teamA);
                        lines.add(EngineInfo.VBAR_DEFAULT + teamB);
                    } else {
                        lines.add(EngineInfo.VBAR_DEFAULT + teamA);
                        lines.add(EngineInfo.VBAR_DEFAULT + teamB);
                        lines.add(TextFormat.MATERIAL_IRON.toString());
                        lines.add(EngineInfo.VBAR_DEFAULT + player.processTranslation(TranslationKeys.PLAYER_GAME_TOWER_SCOREBOARD_SPECTATOR));
                    }
                }
                case FINISHED -> {
                    lines.add(player.processTranslation(TranslationKeys.PLAYER_GAME_TOWER_SCOREBOARD_INFOS));
                    lines.add(EngineInfo.VBAR_DEFAULT + player.processTranslation(TranslationKeys.PLAYER_GAME_TOWER_SCOREBOARD_FINISH));
                    lines.add(EngineInfo.VBAR_DEFAULT + player.processTranslation(TranslationKeys.PLAYER_GAME_TOWER_SCOREBOARD_HOSTER, getHoster()));
                    lines.add(TextFormat.MINECOIN_GOLD.toString());

                    if(towerPoints.first == towerPoints.second) {
                        lines.add(player.processTranslation(TranslationKeys.PLAYER_GAME_TOWER_SCOREBOARD_EQUALITY));
                    } else {
                        Team winner = towerPoints.first > towerPoints.second ? getTeamA() : getTeamB();
                        lines.add(player.processTranslation(TranslationKeys.PLAYER_GAME_TOWER_SCOREBOARD_WINNER));
                        lines.add(EngineInfo.VBAR_DEFAULT + winner.color() + player.processTranslation(winner.name()));
                    }
                }
            }

            player.scoreboard.title("Tower");
            player.scoreboard.updates(lines);
        }, 20);
    }

    @Override
    public void disband(boolean force) {
        if(force) {
            boolean equality = towerPoints.first == towerPoints.second;
            Team winner = towerPoints.first > towerPoints.second ? getTeamA() : getTeamB();

            if(equality) {
                broadcast(TranslationKeys.PLAYER_GAME_TOWER_FINISHED_EQUALITY_BROADCAST, towerPoints.first);
            } else {
                global(player -> player.sendMessage(
                        player.processTranslation(TranslationKeys.PLAYER_GAME_TOWER_FINISHED_BROADCAST,
                                winner.color() + player.processTranslation(winner.name()),
                                winner.color() + Math.max(towerPoints.first, towerPoints.second))
                ));
            }

            broadcast(Glyph.hbarThick(TextFormat.DARK_GRAY, 1));
            broadcast(TranslationKeys.PLAYER_GAME_TOWER_SCORES);

            var sorted = getStartedPlayers().values().stream()
                    .map(gp -> (TowerPlayer) gp)
                    .sorted(Comparator.comparingDouble(TowerPlayer::getScore).reversed())
                    .toList();

            for (TowerPlayer player : sorted) {
                global(p -> {
                    p.sendMessage(Glyph.vbar(EngineInfo.COLOR, 1) + " " + p.processTranslation(
                            TranslationKeys.PLAYER_GAME_TOWER_SCORE,
                            player.getTeam().color() + player.getUsername(),
                            player.kills,
                            player.assists,
                            player.deaths,
                            player.hits
                    ));
                });
            }

            for (GamePlayer value : getStartedPlayers().values()) {
                TowerPlayer player = (TowerPlayer) value;
                DB.getPlayerDataInfo(player.getUsername()).then(info -> {
                    long shards = player.getShardsReward();
                    if(equality) {
                        shards = (long) (shards * 0.8f);
                    } else if(player.getTeam().equals(winner)) {
                        shards = (long) (shards * 1.2f);
                    }

                    var current = BoosterManager.getInstance().getCurrent();
                    if(current != null) {
                        var booster = current.booster();
                        if(booster.owner().equalsIgnoreCase(player.getUsername())) {
                            shards = (long) (shards * (booster.multiplier() / 100.0f));
                        } else {
                            shards = (long) (shards * (1.0f + (booster.multiplier() / 100.0f * 0.5f)));
                        }
                    }

                    shards = Math.max(shards, 2);

                    var p = player.getNukkitPlayer();
                    if(p != null) {
                        p.sendMessage(TranslationKeys.PLAYER_GAME_TOWER_SHARDS, shards);
                        System.out.println("Player " + player.getUsername() + " won " + shards + " shards");
                    }

                    info.increaseShards(shards);
                    syncStats(info, player);
                });
            }

            broadcast(Glyph.hbarThick(TextFormat.DARK_GRAY, 1));

            global(player -> player.addSound(Sound.BEACON_DEACTIVATE, 1.0f, 1.0f));
        }
        super.disband(force);
    }

    public void addPlayer(String name, TowerPlayer player) {
        super.addPlayer(name, player);
        if(getState().equals(State.RUNNING) || getState().equals(State.PAUSE)) preparePlayer(player.getNukkitPlayer());
        initTickerFor(player.getNukkitPlayer());
    }

    @Override
    public void addSpectator(EnginePlayer player) {
        super.addSpectator(player);
        initTickerFor(player);
    }

    @Override
    public void createPlayer(EnginePlayer player, Team team) {
        addPlayer(player.getName(), new TowerPlayer(player.getName(), this, team));
    }

    @Override
    public void preparePlayer(EnginePlayer player) {
        TowerPlayer p = (TowerPlayer) getPlayer(player.getName());
        if(p == null) return;

        TeamSpawnPoint spawnPoint = getSpawnPoint();
        spawn(p, Position.fromObject(Position.fromObject(p.getTeam().equals(getTeamA()) ? spawnPoint.first() : spawnPoint.second(), getCurrentLevel())));
    }

    @Override
    public void teleport(GamePlayer g, Position position) {
        EnginePlayer p = g.getNukkitPlayer();
        if(p == null) return;

        var gamePlayer = (TowerPlayer) g;

        Vector3 from = new Vector3(Math.floor(position.x) + 0.5, position.y, Math.floor(position.z) + 0.5);
        Vector3 target3D = getCurrentLevel().getSafeSpawn();

        float yaw = 90.0f;

        var spawns = getSpawnPoint();
        final Team teamA = getTeamA(), teamB = getTeamB();
        final Team myTeam = gamePlayer.getTeam();

        Float pitch = null;
        if (myTeam.equals(teamA)) {
            pitch = spawns.fpitch();
        } else if (myTeam.equals(teamB)) {
            pitch = spawns.spitch();
        }
        if (pitch == null) {
            pitch = facePitchTowards(from, target3D);
        }

        p.teleport(new Location(from.x, from.y, from.z, yaw, pitch, getCurrentLevel()));
        p.sendPosition(from, yaw, pitch, MovePlayerPacket.MODE_TELEPORT);
    }

    private void syncStats(PlayerDataInfo info, TowerPlayer player) {
        var stats = info.getStats();
        var prefix = getName().toLowerCase() + (isRanked() ? "_ranked" : "");
        stats.increment(prefix, "kills", player.kills);
        stats.increment(prefix, "deaths", player.deaths);
        stats.increment(prefix, "assists", player.assists);
        stats.increment(prefix, "hits", player.hits);
        stats.increment(prefix, "crits", player.crits);
        stats.increment(prefix, "blocks_placed", player.blockPlace);
        stats.increment(prefix, "blocks_broken", player.blockBreak);
        stats.increment(prefix, "apples_eaten", player.appleEaten);
        stats.increment(prefix, "apples_shared", player.appleShared);
        stats.increment(prefix, "apples_shared_enemies", player.appleSharedEnemies);
        stats.increment(prefix, "damage_inflicted", player.inflictedDamages);
        stats.increment(prefix, "damage_received", player.receivedDamages);
        stats.increment(prefix, "points", player.points);

        int bestStreaks = (int) stats.getOrDefault(getName().toLowerCase(), "best_kill_streaks", 0);
        if(player.bestKillStreaks > bestStreaks) {
            stats.set(prefix, "best_kill_streaks", player.bestKillStreaks);
        }

        info.setStats(stats);
    }
}
