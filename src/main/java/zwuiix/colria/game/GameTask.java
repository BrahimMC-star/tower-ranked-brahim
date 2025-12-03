package zwuiix.colria.game;

import cn.nukkit.network.protocol.LevelEventPacket;
import cn.nukkit.scheduler.Task;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;

import java.util.*;
import java.util.function.Consumer;

public class GameTask extends Task {
    private final Game game;
    private final Map<String, Consumer<Integer>> stringRunnableMap = new LinkedHashMap<>();
    private final Map<String, Consumer<Integer>> pendingAdd = new LinkedHashMap<>();
    private final Set<String> pendingRemove = new HashSet<>();

    private final ArrayList<Consumer<Integer>> tickers = new ArrayList<>();

    public GameTask(Game game) {
        this.game = game;
    }

    public void addTicker(Consumer<Integer> r) {
        tickers.add(r);
    }

    public synchronized void addTicker(String key, Consumer<Integer> r) {
        pendingRemove.remove(key);
        pendingAdd.put(key, r);
    }

    public synchronized void removeTicker(String key) {
        pendingAdd.remove(key);
        pendingRemove.add(key);
    }
    
    @Override
    public void onRun(int i) {
        synchronized (this) {
            if (!pendingAdd.isEmpty()) {
                stringRunnableMap.putAll(pendingAdd);
                pendingAdd.clear();
            }
        }

        if(game.getStartTick() >= ((game.getParameters().timeLimit * 60L) * 20L) || game.getState().equals(Game.State.FINISHED)) {
            game.stop();
            this.cancel();
            return;
        }

        tickSpeed();
        if(!game.getState().equals(Game.State.RUNNING)) {
            return;
        }

        game.incrementTick();
        tickRespawn();
        tickers.forEach(consumer -> consumer.accept(i));
        stringRunnableMap.forEach((key, consumer) -> {
            try {
                consumer.accept(i);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        synchronized (this) {
            if (!pendingRemove.isEmpty()) {
                for (String k : pendingRemove) stringRunnableMap.remove(k);
                pendingRemove.clear();
            }
        }
    }

    public void tickRespawn() {
        for(GamePlayer gamePlayer : game.getPlayers().values()) {
            EnginePlayer p = gamePlayer.getNukkitPlayer();
            if (p == null) continue;

            if(gamePlayer.respawnTicks > 0) {
                gamePlayer.respawnTicks--;
                if(gamePlayer.respawnTicks <= 0) {
                    p.setImmobile(false);
                    p.setGamemode(0);
                    p.sendActionBar(TranslationKeys.PLAYER_GAME_RUNNING_SPAWNED);
                    return;
                }

                p.setGamemode(3);
                p.setImmobile(true);

                p.sendActionBar(TranslationKeys.PLAYER_GAME_RUNNING_SPAWN, gamePlayer.respawnTicks / 20.0);
            }
        }
    }

    public void tickSpeed() {
        LevelEventPacket pk = new LevelEventPacket();
        pk.evid = LevelEventPacket.EVENT_SET_GAME_SPEED;
        pk.data = 0;
        pk.x = game.getState().equals(Game.State.PAUSE) ? 0 : 1;
        pk.y = 0;
        pk.z = 0;

        game.broadcast(pk);
    }
}
