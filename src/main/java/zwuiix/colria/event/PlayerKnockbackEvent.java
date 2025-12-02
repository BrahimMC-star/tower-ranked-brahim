package zwuiix.colria.event;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.player.PlayerEvent;

public class PlayerKnockbackEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlers() {
        return handlers;
    }

    private double strengthXZ;
    private double strengthY;

    public PlayerKnockbackEvent(Player who, double strengthXZ, double strengthY) {
        this.player = who;
        this.strengthXZ = strengthXZ;
        this.strengthY = strengthY;
    }

    public double getStrengthXZ() {
        return strengthXZ;
    }

    public void setStrengthXZ(double strengthXZ) {
        this.strengthXZ = strengthXZ;
    }

    public double getStrengthY() {
        return strengthY;
    }

    public void setStrengthY(double strengthY) {
        this.strengthY = strengthY;
    }
}
