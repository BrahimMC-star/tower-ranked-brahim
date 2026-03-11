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

    public Object getOrDefault(String key, String field, Object defaultValue) {
        JsonObject stat = json.getOrDefault(key.toLowerCase(), new JsonObject());
        if (!stat.has(field)) return defaultValue;
        return switch (defaultValue) {
            case Float f -> stat.get(field).getAsFloat();
            case Long l -> stat.get(field).getAsLong();
            case Double d -> stat.get(field).getAsDouble();
            case Short s -> stat.get(field).getAsShort();
            case Byte b -> stat.get(field).getAsByte();
            case Character c -> stat.get(field).getAsCharacter();
            case Integer integer -> stat.get(field).getAsInt();
            case Boolean b -> stat.get(field).getAsBoolean();
            case String s -> stat.get(field).getAsString();
            case null, default -> stat.get(field);
        };
    }

    public void set(String key, String field, Object value) {
        JsonObject stat = json.getOrDefault(key.toLowerCase(), new JsonObject());
        switch (value) {
            case Float f -> stat.addProperty(field, f);
            case Long l -> stat.addProperty(field, l);
            case Double d -> stat.addProperty(field, d);
            case Short s -> stat.addProperty(field, s);
            case Byte b -> stat.addProperty(field, b);
            case Character c -> stat.addProperty(field, c);
            case Integer integer -> stat.addProperty(field, integer);
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
