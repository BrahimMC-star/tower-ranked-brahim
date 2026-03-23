package zwuiix.colria.punishment;

import com.google.gson.*;
import lombok.Getter;

import java.lang.reflect.Type;

@Getter
public class PunishmentEntry {
    private final String username;

    private final String reason;
    private final String staff;

    private final long createdAt;
    private final long expiresAt; // -1 = permanent

    private final PunishmentType type;

    public PunishmentEntry(String username, String reason, String staff,
                           long createdAt, long expiresAt, PunishmentType type) {
        this.username = username;
        this.reason = reason;
        this.staff = staff;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.type = type;
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

    public static class Adapter implements JsonSerializer<PunishmentEntry>, JsonDeserializer<PunishmentEntry> {

        @Override
        public JsonElement serialize(PunishmentEntry p, Type type, JsonSerializationContext ctx) {
            JsonObject obj = new JsonObject();

            obj.addProperty("username", p.username);
            obj.addProperty("reason", p.reason);
            obj.addProperty("staff", p.staff);

            obj.addProperty("createdAt", p.createdAt);
            obj.addProperty("expiresAt", p.expiresAt);

            obj.addProperty("type", p.type.name());

            return obj;
        }

        @Override
        public PunishmentEntry deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();

            return new PunishmentEntry(
                    obj.get("username").getAsString(),
                    obj.get("reason").getAsString(),
                    obj.get("staff").getAsString(),
                    obj.get("createdAt").getAsLong(),
                    obj.get("expiresAt").getAsLong(),
                    PunishmentType.valueOf(obj.get("type").getAsString())
            );
        }
    }
}