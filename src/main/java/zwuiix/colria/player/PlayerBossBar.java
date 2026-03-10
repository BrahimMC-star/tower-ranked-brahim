package zwuiix.colria.player;

import lombok.Getter;
import org.apache.commons.collections4.Closure;
import zwuiix.colria.util.BossBar;

public class PlayerBossBar extends BossBar {
    @Getter
    private int remainingTime = 0;
    private int duration = 0;
    private Closure<PlayerBossBar> onTick = null;

    public PlayerBossBar(int duration) {
        super();
        this.duration = duration;
        this.remainingTime = duration;
    }

    public void tick() {
        if (remainingTime > 0) {
            if (onTick != null) {
                onTick.execute(this);
            }
        }

        setPercentage((float) remainingTime / duration);
        remainingTime--;
    }

    public void onTick(Closure<PlayerBossBar> onTick) {
        this.onTick = onTick;
    }
}