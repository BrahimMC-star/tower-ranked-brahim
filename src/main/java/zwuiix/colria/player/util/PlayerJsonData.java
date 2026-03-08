package zwuiix.colria.player.util;

import com.google.gson.JsonObject;

import java.util.LinkedHashMap;

public class PlayerJsonData {
    private final LinkedHashMap<String, JsonObject> json;

    public PlayerJsonData(LinkedHashMap<String, JsonObject> json) {
        this.json = json;
    }

    public LinkedHashMap<String, JsonObject> getJson() {
        return json;
    }

    public JsonObject get(String key) {
        return json.getOrDefault(key.toLowerCase(), new JsonObject());
    }

    public Object getOrDefault(String key, Object defaultValue) {
        JsonObject stat = json.getOrDefault(key.toLowerCase(), new JsonObject());
        return switch (defaultValue) {
            case Number number -> stat.getAsNumber();
            case Boolean b -> stat.getAsBoolean();
            case String s -> stat.getAsString();
            case null, default -> stat;
        };
    }

    public Object getOrDefault(String key, String field, Object defaultValue) {
        JsonObject stat = json.getOrDefault(key.toLowerCase(), new JsonObject());
        if (!stat.has(field)) return defaultValue;
        return switch (defaultValue) {
            case Number number -> stat.get(field).getAsNumber();
            case Boolean b -> stat.get(field).getAsBoolean();
            case String s -> stat.get(field).getAsString();
            case null, default -> stat.get(field);
        };
    }

    public void set(String key, JsonObject value) { json.put(key.toLowerCase(), value); }

    public void set(String key, String field, Object value) {
        JsonObject stat = json.getOrDefault(key.toLowerCase(), new JsonObject());
        switch (value) {
            case Number number -> stat.addProperty(field, number);
            case Boolean b ->  stat.addProperty(field, b);
            case String s -> stat.addProperty(field, s);
            default -> throw new IllegalArgumentException("Unsupported value type");
        }
        json.put(key.toLowerCase(), stat);
    }

    public void increment(String key, String field, long amount) {
        JsonObject stat = json.getOrDefault(key.toLowerCase(), new JsonObject());
        long current = stat.has(field) ? stat.get(field).getAsLong() : 0;
        stat.addProperty(field, current + amount);
        json.put(key.toLowerCase(), stat);
    }

    public void increment(String key, String field, float amount) {
        JsonObject stat = json.getOrDefault(key.toLowerCase(), new JsonObject());
        float current = stat.has(field) ? stat.get(field).getAsFloat() : 0;
        stat.addProperty(field, current + amount);
        json.put(key.toLowerCase(), stat);
    }
}
