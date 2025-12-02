package zwuiix.colria.database.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public interface PlayerCosmeticDao {

    @SqlUpdate("""
        CREATE TABLE IF NOT EXISTS player_cosmetics (
          xuid       TEXT    NOT NULL,
          identifier TEXT    NOT NULL,
          PRIMARY KEY (xuid, identifier)
        )""")
    void init();

    @SqlUpdate("CREATE INDEX IF NOT EXISTS idx_player_cosmetics_identifier ON player_cosmetics(identifier)")
    void initIndex();

    default void initAll() { init(); initIndex(); }

    @SqlUpdate("""
        INSERT INTO player_cosmetics(xuid, identifier) VALUES(:xuid, :identifier)
        ON CONFLICT(xuid, identifier) DO NOTHING
        """)
    void add(@Bind("xuid") String xuid, @Bind("identifier") String identifier);

    @SqlBatch("""
        INSERT INTO player_cosmetics(xuid, identifier) VALUES(:xuid, :identifier)
        ON CONFLICT(xuid, identifier) DO NOTHING
        """)
    void addMany(@Bind("xuid") String xuid, @Bind("identifier") Iterator<String> identifiers);

    @SqlUpdate("DELETE FROM player_cosmetics WHERE xuid = :xuid AND identifier = :identifier")
    void remove(@Bind("xuid") String xuid, @Bind("identifier") String identifier);

    @SqlUpdate("DELETE FROM player_cosmetics WHERE xuid = :xuid")
    void deleteAll(@Bind("xuid") String xuid);

    default void setAll(String xuid, Collection<String> identifiers) {
        deleteAll(xuid);
        if (identifiers != null && !identifiers.isEmpty()) {
            addMany(xuid, identifiers.iterator());
        }
    }

    default void toggle(String xuid, String identifier) {
        if (has(xuid, identifier)) remove(xuid, identifier);
        else add(xuid, identifier);
    }

    @SqlQuery("SELECT EXISTS(SELECT 1 FROM player_cosmetics WHERE xuid = :xuid AND identifier = :identifier)")
    boolean has(@Bind("xuid") String xuid, @Bind("identifier") String identifier);

    @SqlQuery("SELECT identifier FROM player_cosmetics WHERE xuid = :xuid ORDER BY identifier")
    List<String> listByXuid(@Bind("xuid") String xuid);

    @SqlQuery("SELECT xuid FROM player_cosmetics WHERE identifier = :identifier ORDER BY xuid")
    List<String> listXuidsByIdentifier(@Bind("identifier") String identifier);

    @SqlQuery("SELECT COUNT(1) FROM player_cosmetics WHERE xuid = :xuid")
    long countForXuid(@Bind("xuid") String xuid);
}
