package zwuiix.colria.database.dao;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import zwuiix.colria.player.cooldown.Cooldown;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface PlayerCooldownDao {

    @SqlUpdate("""
            CREATE TABLE IF NOT EXISTS cooldowns (
                xuid       VARCHAR(64) NOT NULL,
                action     VARCHAR(64) NOT NULL,
                expires_at INT         NOT NULL, -- timestamp UNIX (secondes)
                PRIMARY KEY (xuid, action)
            )
            """)
    void init();

    @SqlUpdate("""
        INSERT INTO cooldowns(xuid, action, expires_at)
        VALUES (:xuid, :action, :expiresAt)
        ON CONFLICT(xuid, action) DO UPDATE SET expires_at = :expiresAt
        """)
    Integer set(
            @Bind("xuid") String xuid,
            @Bind("action") String action,
            @Bind("expiresAt") long expiresAt
    );

    @SqlBatch("""
        INSERT INTO cooldowns(xuid, action, expires_at)
        VALUES (:xuid, :action, :expiresAt)
        ON CONFLICT(xuid, action) DO UPDATE SET expires_at = :expiresAt
        """)
    void setMany(
            @Bind("xuid") String xuid,
            @Bind("action") Iterator<String> actions,
            @Bind("expiresAt") Iterator<Long> expiresAt
    );

    @SqlUpdate("DELETE FROM cooldowns WHERE xuid = :xuid AND action = :action")
    Integer delete(@Bind("xuid") String xuid, @Bind("action") String action);

    @SqlUpdate("DELETE FROM cooldowns WHERE xuid = :xuid")
    void deleteAll(@Bind("xuid") String xuid);

    @SqlQuery("SELECT expires_at FROM cooldowns WHERE xuid = :xuid AND action = :action")
    Long getExpiresAt(@Bind("xuid") String xuid, @Bind("action") String action);

    @SqlQuery("""
        SELECT EXISTS(
            SELECT 1 FROM cooldowns
            WHERE xuid = :xuid
              AND action = :action
              AND expires_at > :now
        )
        """)
    boolean isOnCooldown(
            @Bind("xuid") String xuid,
            @Bind("action") String action,
            @Bind("now") long now
    );

    @SqlQuery("""
    SELECT xuid, action, expires_at
    FROM cooldowns
    WHERE xuid = :xuid
      AND expires_at > :now
    ORDER BY action
    """)
    @RegisterConstructorMapper(Cooldown.class)
    List<Cooldown> listActive(
            @Bind("xuid") String xuid,
            @Bind("now") long now
    );

    @SqlQuery("SELECT COUNT(1) FROM cooldowns WHERE xuid = :xuid")
    long countForXuid(@Bind("xuid") String xuid);

    @SqlUpdate("DELETE FROM cooldowns WHERE expires_at <= :now")
    void purgeExpired(@Bind("now") long now);

    default long remaining(String xuid, String action, long now) {
        Long expiresAt = getExpiresAt(xuid, action);
        if (expiresAt == null) return 0L;
        long diff = expiresAt - now;
        return Math.max(diff, 0L);
    }

    default Map<String, Cooldown> listByXuid(String xuid) {
        Map<String, Cooldown> map = new HashMap<>();
        for (Cooldown cd : listActive(xuid, System.currentTimeMillis())) {
            if(cd.isExpired()) continue;
            map.put(cd.getAction(), cd);
        }
        return map;
    }
}
