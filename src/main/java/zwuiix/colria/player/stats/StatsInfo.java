package zwuiix.colria.player.stats;

import com.google.gson.JsonObject;

import java.util.LinkedHashMap;

public class StatsInfo {
    private final LinkedHashMap<String, JsonObject> stats;

    public StatsInfo(LinkedHashMap<String, JsonObject> stats) {
        this.stats = stats;
    }

    public LinkedHashMap<String, JsonObject> getStats() {
        return stats;
    }

    public JsonObject get(String key) {
        return stats.getOrDefault(key.toLowerCase(), new JsonObject());
    }

    public Object getOrDefault(String key, String field, Object defaultValue) {
        JsonObject stat = stats.getOrDefault(key.toLowerCase(), new JsonObject());
        if (!stat.has(field)) return defaultValue;
        return switch (defaultValue) {
            case Number number -> stat.get(field).getAsNumber();
            case Boolean b -> stat.get(field).getAsBoolean();
            case String s -> stat.get(field).getAsString();
            case null, default -> stat.get(field);
        };
    }

    public void set(String key, JsonObject value) {
        stats.put(key.toLowerCase(), value);
    }

    public void set(String key, String field, Object value) {
        JsonObject stat = stats.getOrDefault(key.toLowerCase(), new JsonObject());
        switch (value) {
            case Number number -> stat.addProperty(field, number);
            case Boolean b ->  stat.addProperty(field, b);
            case String s -> stat.addProperty(field, s);
            default -> throw new IllegalArgumentException("Unsupported value type");
        }
        stats.put(key.toLowerCase(), stat);
    }

    public void increment(String key, String field, long amount) {
        JsonObject stat = stats.getOrDefault(key.toLowerCase(), new JsonObject());
        long current = stat.has(field) ? stat.get(field).getAsLong() : 0;
        stat.addProperty(field, current + amount);
        stats.put(key.toLowerCase(), stat);
    }

    public void increment(String key, String field, float amount) {
        JsonObject stat = stats.getOrDefault(key.toLowerCase(), new JsonObject());
        float current = stat.has(field) ? stat.get(field).getAsFloat() : 0;
        stat.addProperty(field, current + amount);
        stats.put(key.toLowerCase(), stat);
    }
}
