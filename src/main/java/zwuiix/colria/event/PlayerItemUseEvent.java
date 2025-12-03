package zwuiix.colria.event;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.player.PlayerEvent;
import cn.nukkit.item.Item;
import lombok.Getter;

@Getter
public class PlayerItemUseEvent extends PlayerEvent implements Cancellable {
    @Getter
    private static final HandlerList handlers = new HandlerList();

    private final Item item;

    public  PlayerItemUseEvent(Player who, Item item) {
        this.player = who;
        this.item = item;
    }

}
