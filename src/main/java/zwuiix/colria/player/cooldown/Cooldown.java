package zwuiix.colria.player.cooldown;

import cn.nukkit.Server;
import lombok.Getter;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import zwuiix.colria.database.DataBase;
import zwuiix.colria.database.dao.PlayerCooldownDao;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.util.DB;

import java.util.function.Function;

public class Cooldown{
    @Getter
    private String xuid;
    @Getter
    private final String action;
    private long expiresAt;

    @JdbiConstructor
    public Cooldown(
            @ColumnName("xuid") String xuid,
            @ColumnName("action") String action,
            @ColumnName("expires_at") long expiresAt
    ) {
        this.xuid = xuid;
        this.action = action;
        this.expiresAt = expiresAt;
    }

    public long getRemainingTime() {
        long remaining = expiresAt - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiresAt;
    }

    public void refresh(long durationMillis) {
        expiresAt = System.currentTimeMillis() + durationMillis;
        update();
    }

    public void refresh(int time) {
        expiresAt = System.currentTimeMillis() + time * 1000L;
        update();
    }

    public void clear() {
        expiresAt = 0;
        update();
    }

    public void update() {
        DB.getPlayerDataInfoXuid(xuid).then(info -> {
            if(info == null) return;
            this.xuid = info.getXuid();

            if(isExpired()) {
                DataBase.getInstance().write(PlayerCooldownDao.class, (Function<PlayerCooldownDao, Integer>) dao -> dao.delete(xuid, action));
            } else {
                DataBase.getInstance().write(PlayerCooldownDao.class, (Function<PlayerCooldownDao, Integer>) dao -> dao.set(xuid, action, expiresAt));
            }

            EnginePlayer p = (EnginePlayer) Server.getInstance().getPlayerExact(info.getName());
            if(p != null) {
                if(isExpired()) p.getCooldowns().remove(action);
                else p.getCooldowns().put(action, this);
            }
        });
    }
}
