package zwuiix.colria.game;

import cn.nukkit.Server;
import zwuiix.colria.player.EnginePlayer;

public class GamePlayer {
    private final String username;
    private final Game game;

    public Long respawnTicks = 0L;

    public GamePlayer(String username, Game game) {
        this.username = username;
        this.game = game;
    }

    public String getUsername() {
        return username;
    }

    public Game getGame() {
        return game;
    }

    public EnginePlayer getNukkitPlayer() {
        return (EnginePlayer) Server.getInstance().getPlayerExact(getUsername());
    }
}
