package zwuiix.colria.punishment.mute;

import com.google.gson.*;
import lombok.Getter;

import java.lang.reflect.Type;

@Getter
public class MuteEntry {
    private final String username;

    private final String reason;
    private final String staff;

    private final long createdAt;
    private final long expiresAt; // -1 = permanent

    public MuteEntry(String username, String reason, String staff, long createdAt, long expiresAt) {
        this.username = username;
        this.reason = reason;
        this.staff = staff;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public boolean isPermanent() {
        return expiresAt == -1;
    }

    public boolean isExpired() {
        return !isPermanent() && System.currentTimeMillis() > expiresAt;
    }

    public long getRemainingTime() {
        if (isPermanent()) return -1;
        return Math.max(0, expiresAt - System.currentTimeMillis());
    }

    public static class Adapter implements JsonSerializer<MuteEntry>, JsonDeserializer<MuteEntry> {

        @Override
        public JsonElement serialize(MuteEntry ban, Type type, JsonSerializationContext ctx) {
            JsonObject obj = new JsonObject();
            obj.addProperty("username", ban.username);

            obj.addProperty("reason", ban.reason);
            obj.addProperty("staff", ban.staff);

            obj.addProperty("createdAt", ban.createdAt);
            obj.addProperty("expiresAt", ban.expiresAt);

            return obj;
        }

        @Override
        public MuteEntry deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();

            return new MuteEntry(
                    obj.get("username").getAsString(),
                    obj.get("reason").getAsString(),
                    obj.get("staff").getAsString(),
                    obj.get("createdAt").getAsLong(),
                    obj.get("expiresAt").getAsLong()
            );
        }
    }
}