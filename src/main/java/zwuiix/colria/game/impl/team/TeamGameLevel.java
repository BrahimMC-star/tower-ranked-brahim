package zwuiix.colria.game.impl.team;

import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;
import zwuiix.colria.game.GameLevel;

public class TeamGameLevel extends GameLevel {
    public TeamSpawnPoint spawnPoint;

    public TeamGameLevel(String name, Item reference, Vector3 defaultPosition, TeamSpawnPoint spawnPoint) {
        super(name, reference, defaultPosition);
        this.spawnPoint = spawnPoint;
    }
}
