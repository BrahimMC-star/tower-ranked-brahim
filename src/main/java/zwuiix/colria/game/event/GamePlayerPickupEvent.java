package zwuiix.colria.game.event;

import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import zwuiix.colria.game.GamePlayer;

@Getter
public class GamePlayerPickupEvent extends Event implements Cancellable {
    private final GamePlayer player;
    private final EntityItem entityItem;

    public GamePlayerPickupEvent(GamePlayer player, EntityItem entityItem) {
        this.player = player;
        this.entityItem = entityItem;
    }

}
