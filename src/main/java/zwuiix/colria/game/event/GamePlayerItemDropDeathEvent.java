package zwuiix.colria.game.event;

import cn.nukkit.event.Event;
import cn.nukkit.item.Item;
import zwuiix.colria.game.GamePlayer;

public class GamePlayerItemDropDeathEvent extends Event {
    private GamePlayer who;
    private Item item;

    public GamePlayerItemDropDeathEvent(GamePlayer who, Item item) {
        this.who = who;
        this.item = item;
    }

    public GamePlayer getPlayer() {
        return who;
    }

    public GamePlayer getWho() {
        return who;
    }

    public Item getItem() {
        return item;
    }
}
