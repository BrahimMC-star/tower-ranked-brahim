package zwuiix.colria.rank;

import lombok.Getter;
import zwuiix.colria.Loader;
import zwuiix.colria.permission.Permission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Getter
public class RankRegistry {
    @Getter
    private static RankRegistry instance = new RankRegistry();

    private final HashMap<Integer, Rank> ranks = new HashMap<>();
    private Rank defaultRank = null;

    RankRegistry() {
        instance = this;
    }

    public Optional<Rank> getRank(int id) {
        return Optional.ofNullable(ranks.get(id));
    }

    public Optional<Rank> getRank(String name) {
        for (Rank rank : ranks.values()) {
            if (rank.getName().equalsIgnoreCase(name)) {
                return Optional.of(rank);
            }
        }
        return Optional.empty();
    }

    public void register(Rank rank) {
        if(ranks.containsKey(rank.getId())) return;
        ranks.put(rank.getId(), rank);
        if(rank.isDefault()) {
            defaultRank = rank;
        }
    }

    public void unregisterRank(Rank rank) {
        if(!ranks.containsKey(rank.getId())) return;
        ranks.remove(rank.getId());
    }

    public void invoke(Loader loader) {
        register(new Rank(0,"&7Default", new ArrayList<>(), true, true));

        register(new Rank(50, "&bIce", List.of(
                Permission.SUPPORTER.toString()
        ), true));
        register(new Rank(51,"&3Frost", List.of(
                Permission.SUPPORTER.toString()
        ), true));
        register(new Rank(52,"&9Blizzard", List.of(
                Permission.SUPPORTER.toString(),
                Permission.GAME_HOSTER.toString()
        ), true));
        register(new Rank(53,"&uStorm", List.of(
                Permission.SUPPORTER.toString(),
                Permission.GAME_HOSTER.toString(),
                Permission.GAME_BYPASS_COOLDOWN.toString(),
                Permission.LOBBY_JOIN_SAYINGS.toString()
        ), true));
        register(new Rank(54,"&5Media", List.of(
                Permission.SUPPORTER.toString(),
                Permission.GAME_HOSTER.toString(),
                Permission.LOBBY_JOIN_SAYINGS.toString()
        ), true));
        register(new Rank(55,"&dFamous",  List.of(
                Permission.SUPPORTER.toString(),
                Permission.GAME_HOSTER.toString(),
                Permission.GAME_BYPASS_COOLDOWN.toString(),
                Permission.LOBBY_JOIN_SAYINGS.toString()
        ), true));
        register(new Rank(56,"&5Friend", List.of(
                Permission.GAME_HOSTER.toString(),
                Permission.GAME_BYPASS_COOLDOWN.toString(),
                Permission.LOBBY_JOIN_SAYINGS.toString()
        ), true));

        register(new Rank(97,"&2Helper", List.of(
                Permission.GAME_HOSTER.toString(),
                Permission.GAME_BYPASS_COOLDOWN.toString(),
                Permission.LOBBY_JOIN_SAYINGS.toString()
        ), true));
        register(new Rank(98,"&eMod", List.of(
                Permission.GAME_HOSTER.toString(),
                Permission.GAME_BYPASS_COOLDOWN.toString(),
                Permission.LOBBY_JOIN_SAYINGS.toString(),

                Permission.SHARD_MANAGE.toString(),
                Permission.PARTICLE_MANAGE.toString(),
                Permission.COSMETIC_MANAGE.toString(),
                Permission.CAPE_MANAGE.toString(),
                Permission.BOOSTER_MANAGE.toString(),
                Permission.LINK_MANAGE.toString()
        ), true));
        register(new Rank(99,"&6S.Mod", new ArrayList<>(), true)); //op
        register(new Rank(100,"&cAdmin", new ArrayList<>(), true)); //op
    }
}
