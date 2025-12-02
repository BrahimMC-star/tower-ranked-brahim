package zwuiix.colria.game.event;

import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import org.checkerframework.checker.nullness.qual.NonNull;
import zwuiix.colria.game.GamePlayer;

public class GamePlayerPickupEvent extends Event implements Cancellable {
    private GamePlayer player;
    private EntityItem entityItem;

    public GamePlayerPickupEvent(GamePlayer player, EntityItem entityItem) {
        this.player = player;
        this.entityItem = entityItem;
    }

    @NonNull
    public GamePlayer getPlayer() {
        return player;
    }

    @NonNull
    public EntityItem getEntityItem() {
        return entityItem;
    }
}
