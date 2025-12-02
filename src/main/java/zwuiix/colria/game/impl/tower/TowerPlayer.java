package zwuiix.colria.game.impl.tower;

import zwuiix.colria.game.Game;
import zwuiix.colria.game.impl.team.Team;
import zwuiix.colria.game.impl.team.TeamPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TowerPlayer extends TeamPlayer {
    private static final long ASSIST_WINDOW_MS = 8000L;
    private static final double MIN_DAMAGE = 2.0;
    private static final double MIN_DAMAGE_SHARE = 0.10;
    private static final int MAX_ASSISTS = 3;

    public Integer kills = 0;
    public Integer deaths = 0;
    public Integer assists = 0;
    public Integer killStreaks = 0;
    public Integer bestKillStreaks = 0;
    public Integer hits = 0;
    public Integer crits = 0;
    public Integer blockPlace = 0;
    public Integer blockBreak = 0;
    public Integer points = 0;
    public Float inflictedDamages = 0.f;
    public Float receivedDamages = 0.f;
    public Integer appleEaten = 0;
    public Integer appleShared = 0;
    public Integer appleSharedEnemies = 0;

    private final Map<TowerPlayer, DamageRecord> assistLedger = new HashMap<>();

    public TowerPlayer(String username, Game game, Team team) {
        super(username, game, team);
    }

    public void hit(TowerPlayer attacker, double damage) {
        if (attacker == null || attacker == this || damage <= 0) return;

        DamageRecord rec = assistLedger.computeIfAbsent(attacker, k -> new DamageRecord());
        rec.damage += damage;
        rec.lastHitAtMs = System.currentTimeMillis();
    }

    public List<TowerPlayer> resolveAssists(TowerPlayer killer) {
        long nowMs = System.currentTimeMillis();
        assistLedger.values().removeIf(r -> nowMs - r.lastHitAtMs > ASSIST_WINDOW_MS);

        double totalDamage = assistLedger.entrySet().stream()
                .filter(e -> !e.getKey().equals(killer))
                .mapToDouble(e -> e.getValue().damage)
                .sum();
        if (totalDamage <= 0) {
            assistLedger.clear();
            return List.of();
        }

        var contributions = assistLedger.entrySet().stream()
                .filter(e -> e.getKey() != null && (!e.getKey().equals(killer)))
                .sorted((a, b) -> Double.compare(b.getValue().damage, a.getValue().damage))
                .toList();

        List<TowerPlayer> assistants = new ArrayList<>(Math.min(MAX_ASSISTS, contributions.size()));
        for (var en : contributions) {
            double dmg = en.getValue().damage, share = dmg / totalDamage;
            if (dmg >= MIN_DAMAGE && share >= MIN_DAMAGE_SHARE) {
                assistants.add(en.getKey());
                if (assistants.size() >= MAX_ASSISTS) break;
            }
        }

        if (killer != null) assistants.removeIf(p -> p.equals(killer));

        assistants.forEach(p -> p.assists++);
        assistLedger.clear();
        return assistants;
    }

    public double getScore() {
        int k = kills != null ? kills : 0;
        int d = deaths != null ? deaths : 0;
        int a = assists != null ? assists : 0;
        int bks = bestKillStreaks != null ? bestKillStreaks : 0;
        int h = hits != null ? hits : 0;
        int c = crits != null ? crits : 0;
        int bp = blockPlace != null ? blockPlace : 0;
        int bb = blockBreak != null ? blockBreak : 0;
        int ae = appleEaten != null ? appleEaten : 0;
        int as_ = appleShared != null ? appleShared : 0;
        int ase_ = appleSharedEnemies != null ? appleSharedEnemies : 0;
        int pts = points != null ? points : 0;
        float dmgInf = inflictedDamages != null ? inflictedDamages : 0f;
        float dmgRec = receivedDamages != null ? receivedDamages : 0f;

        double score = 0;

        score += pts * 200;

        score += k * 100;
        score += a * 40;
        score -= d * 60;
        score -= bks * 10;
        score += h * 1;
        score += c * 3;
        score += dmgInf * 5;
        score -= dmgRec * 2;

        score += bp * 0.5;
        score += bb * 0.5;
        score += ae * 10;
        score += as_ * 15;
        score -= ase_ * 5;

        return score;
    }

    public int getShardsReward() {
        double score = Math.max(0, getScore());
        double scaled = Math.sqrt(score / 50.0) * 10.0;

        int shards = (int) Math.round(scaled);
        int maxShards = 150;
        return Math.min(shards, maxShards);
    }

    private static final class DamageRecord {
        double damage;
        long lastHitAtMs;
    }
}