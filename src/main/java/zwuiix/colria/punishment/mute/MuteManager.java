package zwuiix.colria.punishment.mute;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import zwuiix.colria.Loader;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MuteManager {
    @Getter
    private static MuteManager instance;

    private final Loader loader;

    private File file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Map<String, MuteEntry> cache = new HashMap<>();

    public MuteManager(Loader loader) {
        instance = this;

        this.loader = loader;
        load();
    }

    private void load() {
        file = new File(Path.of(loader.getDataFolder().toString(), "mutes.db").toString());

        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, MuteEntry>>() {}.getType();
            cache = gson.fromJson(reader, type);
            if (cache == null) cache = new HashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
        }

        purgeExpiredMutes();
    }

    public void save() {
        purgeExpiredMutes();
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(cache, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MuteEntry getMute(String playerName) {
        MuteEntry mute = cache.get(playerName.toLowerCase());

        if (mute != null && mute.isExpired()) {
            unmute(playerName);
            return null;
        }
        return mute;
    }

    public boolean isMuted(String playerName) {
        return getMute(playerName) != null;
    }

    public void mute(MuteEntry ban) {
        cache.put(ban.getUsername().toLowerCase(), ban);
    }

    public void unmute(String playerName) {
        cache.remove(playerName.toLowerCase());
    }

    public void purgeExpiredMutes() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}