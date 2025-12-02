package zwuiix.colria.shape.type;

import cn.nukkit.Player;
import cn.nukkit.debugshape.DebugShape;

public abstract class ShapeRenderer {
    private final int id;
    protected ShapeRenderer(int id) { this.id = id; }
    public int getId() { return id; }

    abstract public DebugShape toNetwork();

    public void send(Player player) {
        player.sendDebugShape(toNetwork());
    }

    public void remove(Player player) {
        player.removeDebugShape(id);
    }
}