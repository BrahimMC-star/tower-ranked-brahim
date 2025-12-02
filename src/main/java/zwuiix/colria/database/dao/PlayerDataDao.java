package zwuiix.colria.database.dao;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import zwuiix.colria.player.PlayerDataInfo;

import java.util.Optional;

@RegisterConstructorMapper(PlayerDataInfo.class)
public interface PlayerDataDao {
    @SqlUpdate("""
        CREATE TABLE IF NOT EXISTS players (
          xuid       TEXT    PRIMARY KEY,
          name       TEXT    NOT NULL,
          discordId  TEXT    DEFAULT '',
          shards     INTEGER NOT NULL DEFAULT 0,
          booster    INTEGER NOT NULL DEFAULT 0,
          last_login INTEGER NOT NULL DEFAULT (CAST(STRFTIME('%s','now') AS INTEGER)),
          playtime   INTEGER NOT NULL DEFAULT 0,
          json_data  TEXT    NOT NULL DEFAULT '{}',
          particle   TEXT    NOT NULL DEFAULT 'none'
        )""")
    void init();

    @SqlUpdate("CREATE INDEX IF NOT EXISTS idx_players_name_nocase ON players(name COLLATE NOCASE)")
    void initIndex();
    default void initAll() { init(); initIndex(); }

    @SqlQuery("""
    SELECT
      xuid,
      name,
      discordId,
      shards,
      booster,
      last_login AS lastLogin,
      playtime,
      json_data  AS jsonData,
      particle
    FROM players WHERE xuid = :xuid
    """)
    PlayerDataInfo _get(@Bind("xuid") String xuid);

    @SqlQuery("""
    SELECT
      xuid,
      name,
      discordId,
      shards,
      booster,
      last_login AS lastLogin,
      playtime,
      json_data  AS jsonData,
      particle
    FROM players WHERE name = :name
    """)
    PlayerDataInfo get(@Bind("name") String name);

    @SqlQuery("""
    SELECT
      xuid,
      name,
      discordId,
      shards,
      booster,
      last_login AS lastLogin,
      playtime,
      json_data  AS jsonData,
      particle
    FROM players WHERE discordId = :discordId
    """)
    PlayerDataInfo getFromDiscordId(String discordId);

    @SqlUpdate("INSERT INTO players(xuid, name) VALUES(:xuid, :name) ON CONFLICT(xuid) DO NOTHING")
    void insertIfAbsent(@Bind("xuid") String xuid, @Bind("name") String name);

    default PlayerDataInfo getOrCreate(String xuid, String name) {
        initAll();
        insertIfAbsent(xuid, name);
        return _get(xuid);
    }

    @SqlQuery("SELECT xuid FROM players WHERE name = :name COLLATE NOCASE LIMIT 1")
    Optional<String> getXuidByName(@Bind("name") String name);

    @SqlUpdate("UPDATE players SET shards = :shards WHERE xuid = :xuid")
    long setShards(@Bind("xuid") String xuid, @Bind("shards") long shards);

    @SqlUpdate("UPDATE players SET booster = :booster WHERE xuid = :xuid")
    long setBooster(@Bind("xuid") String xuid, @Bind("booster") long booster);

    @SqlUpdate("UPDATE players SET last_login = :lastLogin WHERE xuid = :xuid")
    long setLastLogin(@Bind("xuid") String xuid, @Bind("lastLogin") long lastLogin);

    @SqlUpdate("UPDATE players SET playtime = :playtime WHERE xuid = :xuid")
    long setPlaytime(@Bind("xuid") String xuid, @Bind("playtime") long playtime);

    @SqlUpdate("UPDATE players SET json_data = :jsonData WHERE xuid = :xuid")
    void setJsonData(@Bind("xuid") String xuid, @Bind("jsonData") String jsonData);

    @SqlUpdate("UPDATE players SET particle = :p WHERE xuid = :xuid")
    void setParticle(@Bind("xuid") String xuid, @Bind("p") String particle);

    @SqlUpdate("""
    UPDATE players SET
      name       = :info.name,
      discordId  = :info.discordId,
      shards     = :info.shards,
      booster    = :info.booster,
      last_login = :info.lastLogin,
      playtime   = :info.playtime,
      json_data  = :info.jsonData,
      particle   = :info.particle
    WHERE xuid = :info.xuid
    """)
    int setAll(@BindBean("info") PlayerDataInfo info);

    @SqlUpdate("""
    INSERT INTO players (xuid, name, discordId, shards, booster, last_login, playtime, json_data, particle)
    VALUES (:info.xuid, :info.name, :info.discordId,:info.shards, :info.booster,
            :info.lastLogin, :info.playtime, :info.jsonData, :info.particle)
    """)
    void insertAll(@BindBean("info") PlayerDataInfo info);

    default void setAllOrInsert(PlayerDataInfo info) {
        if (setAll(info) == 0) insertAll(info);
    }
}
