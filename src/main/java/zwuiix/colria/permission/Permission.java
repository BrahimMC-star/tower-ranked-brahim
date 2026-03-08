package zwuiix.colria.permission;

import java.util.Locale;

public enum Permission {
    WORLD_MANAGE,
    RANK_MANAGE,
    SHARD_MANAGE,
    PARTICLE_MANAGE,
    COSMETIC_MANAGE,
    CAPE_MANAGE,
    BOOSTER_MANAGE,
    LINK_MANAGE,

    GAME_HOSTER,
    GAME_ADMIN,

    TELL_ANYWAY,

    LOBBY_JOIN_SAYINGS,
    SUPPORTER,
    ;

    private final String node;

    Permission() {
        this.node = "colria." + name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return node;
    }

    public String node() {
        return node;
    }

    public String child(String suffix) {
        return node + "." + suffix;
    }

    public static java.util.List<String> allNodes() {
        return java.util.Arrays.stream(values())
                .map(Permission::toString)
                .toList();
    }
}
