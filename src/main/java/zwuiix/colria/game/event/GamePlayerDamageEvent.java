package zwuiix.colria.game.event;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import cn.nukkit.event.entity.EntityDamageEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import zwuiix.colria.game.GamePlayer;

public class GamePlayerDamageEvent extends Event implements Cancellable {
    private GamePlayer victim;
    private GamePlayer attacker;
    private EntityDamageEvent.DamageCause cause;
    private EntityDamageEvent nukkitEvent;

    public GamePlayerDamageEvent(GamePlayer player, GamePlayer attacker, EntityDamageEvent.DamageCause cause, EntityDamageEvent nukkitEvent) {
        this.victim = player;
        this.attacker = attacker;
        this.cause = cause;
        this.nukkitEvent = nukkitEvent;
    }

    @NonNull
    public GamePlayer getVictim() {
        return victim;
    }

    public GamePlayer getAttacker() {
        return attacker;
    }

    public EntityDamageEvent.@NonNull DamageCause getCause() {
        return cause;
    }

    @NonNull
    public EntityDamageEvent getNukkitEvent() { return nukkitEvent; }
}
