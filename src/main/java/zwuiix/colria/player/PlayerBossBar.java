package zwuiix.colria.player;

import org.apache.commons.collections4.Closure;
import zwuiix.colria.util.BossBar;

public class PlayerBossBar extends BossBar {
    private int remainingTime = 0;
    private int duration = 0;
    private Closure<PlayerBossBar> onTick = null;

    public PlayerBossBar(int duration) {
        super();
        this.duration = duration;
        this.remainingTime = duration;
    }

    public void tick() {
        remainingTime--;
        if (remainingTime < 0) {
            for (EnginePlayer player : getViewers().values()) {
                removeViewer(player);
            }
            return;
        } else if (onTick != null) {
            onTick.execute(this);
        }

        setPercentage(duration > 0 ? Math.max(0.0f, Math.min(1.0f, (float) remainingTime / (float) duration)) : 0.0f);
    }

    public void onTick(Closure<PlayerBossBar> onTick) {
        this.onTick = onTick;
    }

    public int getRemainingTime() {
        return remainingTime;
    }
}