package zwuiix.colria.rank;

import zwuiix.colria.Loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class RankRegistry {
    private static RankRegistry INSTANCE = new RankRegistry();

    public static RankRegistry getInstance() {
        return INSTANCE;
    }

    private HashMap<Integer, Rank> ranks = new HashMap<>();
    private Rank defaultRank = null;

    RankRegistry() {
        INSTANCE = this;
    }

    public HashMap<Integer, Rank> getRanks() {
        return ranks;
    }

    public Rank getDefaultRank() {
        return defaultRank;
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

        register(new Rank(50,"&bIce", new ArrayList<>(), true));
        register(new Rank(51,"&3Frost", new ArrayList<>(), true));
        register(new Rank(52,"&9Blizzard", new ArrayList<>(), true));
        register(new Rank(53,"&uStorm", new ArrayList<>(), true));
        register(new Rank(54,"&5Media", new ArrayList<>(), true));
        register(new Rank(55,"&dFamous", new ArrayList<>(), true));
        register(new Rank(56,"&5Friend", new ArrayList<>(), true));

        register(new Rank(97,"&2Helper", new ArrayList<>(), true));
        register(new Rank(98,"&eMod", new ArrayList<>(), true));
        register(new Rank(99,"&6S.Mod", new ArrayList<>(), true));
        register(new Rank(100,"&cAdmin", new ArrayList<>(), true));
    }
}
