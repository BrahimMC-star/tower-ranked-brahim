package zwuiix.colria.game;

import cn.nukkit.Server;
import lombok.Getter;
import zwuiix.colria.player.EnginePlayer;

public class GamePlayer {
    @Getter
    private final String username;
    @Getter
    private final Game game;

    public Long respawnTicks = 0L;

    public GamePlayer(String username, Game game) {
        this.username = username;
        this.game = game;
    }

    public EnginePlayer getNukkitPlayer() {
        return (EnginePlayer) Server.getInstance().getPlayerExact(getUsername());
    }
}
