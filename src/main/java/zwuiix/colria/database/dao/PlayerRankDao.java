package zwuiix.colria.database.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public interface PlayerRankDao {
    @SqlUpdate("""
        CREATE TABLE IF NOT EXISTS player_ranks (
          xuid    TEXT    NOT NULL,
          rank_id INTEGER NOT NULL,
          PRIMARY KEY (xuid, rank_id)
        )""")
    void init();

    @SqlUpdate("""
        INSERT INTO player_ranks(xuid, rank_id) VALUES(:xuid, :rankId)
        ON CONFLICT(xuid, rank_id) DO NOTHING
        """)
    void add(@Bind("xuid") String xuid, @Bind("rankId") int rankId);

    @SqlBatch("""
        INSERT INTO player_ranks(xuid, rank_id) VALUES(:xuid, :rankId)
        ON CONFLICT(xuid, rank_id) DO NOTHING
        """)
    void addMany(@Bind("xuid") String xuid, @Bind("rankId") Iterator<Integer> rankIds);

    @SqlUpdate("DELETE FROM player_ranks WHERE xuid = :xuid AND rank_id = :rankId")
    void remove(@Bind("xuid") String xuid, @Bind("rankId") int rankId);

    @SqlUpdate("DELETE FROM player_ranks WHERE xuid = :xuid")
    void deleteAll(@Bind("xuid") String xuid);

    default void setAll(String xuid, Collection<Integer> rankIds) {
        deleteAll(xuid);
        if (rankIds != null && !rankIds.isEmpty()) addMany(xuid, rankIds.iterator());
    }

    default void toggle(String xuid, int rankId) {
        if (has(xuid, rankId)) remove(xuid, rankId); else add(xuid, rankId);
    }

    @SqlQuery("SELECT EXISTS(SELECT 1 FROM player_ranks WHERE xuid = :xuid AND rank_id = :rankId)")
    boolean has(@Bind("xuid") String xuid, @Bind("rankId") int rankId);

    @SqlQuery("SELECT rank_id FROM player_ranks WHERE xuid = :xuid ORDER BY rank_id")
    List<Integer> listByXuid(@Bind("xuid") String xuid);

    @SqlQuery("SELECT xuid FROM player_ranks WHERE rank_id = :rankId ORDER BY xuid")
    List<String> listXuidsByRank(@Bind("rankId") int rankId);

    @SqlQuery("SELECT COUNT(1) FROM player_ranks WHERE xuid = :xuid")
    long countForXuid(@Bind("xuid") String xuid);
}
