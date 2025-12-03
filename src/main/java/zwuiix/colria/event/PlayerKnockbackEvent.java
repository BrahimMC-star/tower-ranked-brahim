package zwuiix.colria.event;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.player.PlayerEvent;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PlayerKnockbackEvent extends PlayerEvent implements Cancellable {
    @Getter
    private static final HandlerList handlers = new HandlerList();

    private double strengthXZ;
    private double strengthY;

    public PlayerKnockbackEvent(Player who, double strengthXZ, double strengthY) {
        this.player = who;
        this.strengthXZ = strengthXZ;
        this.strengthY = strengthY;
    }

}
