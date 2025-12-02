package zwuiix.colria.booster;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class Booster {
    private static Gson gson = new GsonBuilder().create();

    private String type;
    private String owner;
    private float multiplier;
    private long duration;
    private ArrayList<String> claims;

    public Booster(
            String type,
            String owner,
            float multiplier,
            long duration,
            ArrayList<String> claims
    ) {
        this.type = type;
        this.owner = owner;
        this.multiplier = multiplier;
        this.duration = duration;
        this.claims = claims;
    }

    public String getType() {
        return type;
    }

    public String getOwner() {
        return owner;
    }

    public float getMultiplier() {
        return multiplier;
    }

    public long getDuration() {
        return duration;
    }

    public ArrayList<String> getClaims() {
        return claims;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.addProperty("owner", owner);
        json.addProperty("multiplier", multiplier);
        json.addProperty("duration", duration);
        json.add("claims", gson.toJsonTree(claims));
        return json;
    }

    public static Booster fromJson(JsonObject json) {
        String type = json.get("type").getAsString();
        String owner = json.get("owner").getAsString();
        float multiplier = json.get("multiplier").getAsFloat();
        long duration = json.get("duration").getAsLong();
        ArrayList<String> claims = new ArrayList<>();
        json.getAsJsonArray("claims").forEach(element -> claims.add(element.getAsString()));
        return new Booster(type, owner, multiplier, duration, claims);
    }

    public record Current(Booster booster, long start, long end) {
        public JsonObject toJson() {
            JsonObject json = booster.toJson();
            json.addProperty("start", start);
            json.addProperty("end", end);
            return json;
        }

        public static Current fromJson(JsonObject json) {
            Booster booster = Booster.fromJson(json);
            long start = json.get("start").getAsLong();
            long end = json.get("end").getAsLong();
            return new Current(booster, start, end);
        }
    }
}
