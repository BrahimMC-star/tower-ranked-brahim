package zwuiix.colria.game.event;

import cn.nukkit.event.Event;
import cn.nukkit.item.Item;
import lombok.Getter;
import zwuiix.colria.game.GamePlayer;

@Getter
public class GamePlayerItemDropDeathEvent extends Event {
    private final GamePlayer who;
    private final Item item;

    public GamePlayerItemDropDeathEvent(GamePlayer who, Item item) {
        this.who = who;
        this.item = item;
    }

    public GamePlayer getPlayer() {
        return who;
    }

}
