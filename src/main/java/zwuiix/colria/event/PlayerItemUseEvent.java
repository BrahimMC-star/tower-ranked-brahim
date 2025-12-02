package zwuiix.colria.event;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.player.PlayerEvent;
import cn.nukkit.item.Item;

public class PlayerItemUseEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlers() {
        return handlers;
    }

    private Item item;

    public  PlayerItemUseEvent(Player who, Item item) {
        this.player = who;
        this.item = item;
    }

    public Item getItem() {
        return item;
    }
}
