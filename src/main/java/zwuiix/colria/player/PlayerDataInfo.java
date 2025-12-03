package zwuiix.colria.player;

import cn.nukkit.Server;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import zwuiix.colria.database.DataBase;
import zwuiix.colria.database.dao.PlayerDataDao;
import zwuiix.colria.player.stats.StatsInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.function.Function;

@Getter
public class PlayerDataInfo {
    private static final Gson gson = new Gson();

    private final String xuid;
    private final String name;
    private String discordId;
    private long shards;
    private long booster;
    private long lastLogin;
    private long playtime;
    private String jsonData;
    private String particle;

    public PlayerDataInfo(String xuid, String name, String discordId,long shards, long booster,
                          long lastLogin, long playtime, String jsonData, String particle) {
        this.xuid = xuid;
        this.name = name;
        this.discordId = discordId;
        this.shards = shards;
        this.booster = booster;
        this.lastLogin = lastLogin;
        this.playtime = playtime;
        this.jsonData = jsonData;
        this.particle = particle;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
        this.update();
    }

    public void setShards(long shards) {
        this.shards = shards;
        this.update();
    }
    
    public void increaseShards(long amount) {
        this.shards += amount;
        this.update();
    }
    
    public void decreaseShards(long amount) {
        this.shards -= amount;
        if(this.shards < 0) {
            this.shards = 0;
        }
        this.update();
    }

    public void setBooster(long booster) {
        this.booster = booster;
        this.update();
    }
    
    public void increaseBooster(long amount) {
        this.booster += amount;
        this.update();
    }
    
    public void decreaseBooster(long amount) {
        this.booster -= amount;
        if(this.booster < 0) {
            this.booster = 0;
        }
        this.update();
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
        this.update();
    }

    public void setPlaytime(long playtime) {
        this.playtime = playtime;
        this.update();
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
        this.update();
    }

    public void setParticle(String particle) {
        this.particle = particle;
        this.update();
    }

    public JsonObject getJson() {
        return jsonData == null ? new JsonObject() : gson.fromJson(jsonData, JsonObject.class);
    }

    public void setJson(JsonObject json) {
        this.jsonData = gson.toJson(json);
        this.update();
    }

    public String getCape() {
        JsonObject json = this.getJson();
        if(json.has("cape")) {
            return json.get("cape").getAsString();
        }
        return "none";
    }

    public void setCape(String cape) {
        JsonObject json = this.getJson();
        json.addProperty("cape", cape);
        this.setJson(json);
    }

    public ArrayList<String> getCosmetics() {
        JsonObject json = this.getJson();
        ArrayList<String> cosmetics = new ArrayList<>();
        if(json.has("cosmetics")) {
            for(var el : json.getAsJsonArray("cosmetics")) {
                cosmetics.add(el.getAsString());
            }
        }
        return cosmetics;
    }

    public void setCosmetics(ArrayList<String> cosmetics) {
        JsonObject json = this.getJson();
        json.remove("cosmetics");
        json.add("cosmetics", gson.toJsonTree(cosmetics));
        this.setJson(json);
    }

    public void addCosmetic(String cosmetic) {
        ArrayList<String> cosmetics = this.getCosmetics();
        if(!cosmetics.contains(cosmetic)) {
            cosmetics.add(cosmetic);
            this.setCosmetics(cosmetics);
        }
    }

    public void removeCosmetic(String cosmetic) {
        ArrayList<String> cosmetics = this.getCosmetics();
        if(cosmetics.contains(cosmetic)) {
            cosmetics.remove(cosmetic);
            this.setCosmetics(cosmetics);
        }
    }

    public ArrayList<String> getPets() {
        JsonObject json = this.getJson();
        ArrayList<String> pets = new ArrayList<>();
        if(json.has("pets")) {
            for(var el : json.getAsJsonArray("pets")) {
                pets.add(el.getAsString());
            }
        }
        return pets;
    }

    public void setPets(ArrayList<String> pets) {
        JsonObject json = this.getJson();
        json.remove("pets");
        json.add("pets", gson.toJsonTree(pets));
        this.setJson(json);
    }

    public void addPet(String pet) {
        ArrayList<String> pets = this.getPets();
        if(!pets.contains(pet)) {
            pets.add(pet);
            this.setPets(pets);
        }
    }

    public void removePet(String pet) {
        ArrayList<String> pets = this.getPets();
        if(pets.contains(pet)) {
            pets.remove(pet);
            this.setPets(pets);
        }
    }

    public String getPet() {
        JsonObject json = this.getJson();
        if(json.has("pet")) {
            return json.get("pet").getAsString();
        }
        return "none";
    }

    public void setPet(String pet) {
        JsonObject json = this.getJson();
        json.addProperty("pet", pet);
        this.setJson(json);
    }

    public StatsInfo getStats() {
        JsonObject json = this.getJson();
        if(json.has("stats")) {
            return gson.fromJson(json.getAsJsonObject("stats"), StatsInfo.class);
        }
        return new StatsInfo(new LinkedHashMap<>());
    }

    public void setStats(StatsInfo stats) {
        JsonObject json = this.getJson();
        json.add("stats", gson.toJsonTree(stats));
        this.setJson(json);
    }
    
    public void update() {
        DataBase.getInstance().write(PlayerDataDao.class, (Function<PlayerDataDao, Integer>) dao -> dao.setAll(this));

        EnginePlayer p = (EnginePlayer) Server.getInstance().getPlayerExact(this.getName());
        if(p != null) p.setPlayerDataInfo(this);
    }
}
