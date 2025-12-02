package zwuiix.colria.game.event;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.Position;
import zwuiix.colria.game.GamePlayer;

public class GamePlayerDeathEvent extends Event implements Cancellable {
    private GamePlayer victim;
    private GamePlayer attacker;
    private Position position;
    private EntityDamageEvent.DamageCause cause;

    public GamePlayerDeathEvent(GamePlayer player, GamePlayer attacker, Position position, EntityDamageEvent.DamageCause cause) {
        this.victim = player;
        this.attacker = attacker;
        this.position = position;
        this.cause = cause;
    }

    public GamePlayer getVictim() {
        return victim;
    }

    public GamePlayer getAttacker() {
        return attacker;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public EntityDamageEvent.DamageCause getCause() {
        return cause;
    }
}
