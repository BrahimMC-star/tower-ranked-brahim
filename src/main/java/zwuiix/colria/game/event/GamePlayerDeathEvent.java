package zwuiix.colria.game.event;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.Position;
import lombok.Getter;
import lombok.Setter;
import zwuiix.colria.game.GamePlayer;

@Getter
public class GamePlayerDeathEvent extends Event implements Cancellable {
    private final GamePlayer victim;
    private final GamePlayer attacker;
    @Setter
    private Position position;
    private final EntityDamageEvent.DamageCause cause;

    public GamePlayerDeathEvent(GamePlayer player, GamePlayer attacker, Position position, EntityDamageEvent.DamageCause cause) {
        this.victim = player;
        this.attacker = attacker;
        this.position = position;
        this.cause = cause;
    }

}
