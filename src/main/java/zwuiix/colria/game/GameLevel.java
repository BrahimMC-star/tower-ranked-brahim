package zwuiix.colria.game;

import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;

public class GameLevel {
    public String name;
    public Item reference;
    public Vector3 defaultSpawn;

    public GameLevel(String name, Item reference, Vector3 defaultSpawn) {
        this.name = name;
        this.reference = reference;
        this.defaultSpawn = defaultSpawn;
    }
}
