package zwuiix.colria.punishment.ban;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import zwuiix.colria.Loader;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.Glyph;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class BanManager {
    @Getter
    private static BanManager instance;

    private final Loader loader;

    private File file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Map<String, BanEntry> cache = new HashMap<>();

    public BanManager(Loader loader) {
        instance = this;

        this.loader = loader;
        load();
    }

    private void load() {
        file = new File(Path.of(loader.getDataFolder().toString(), "bans.db").toString());

        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, BanEntry>>() {}.getType();
            cache = gson.fromJson(reader, type);
            if (cache == null) cache = new HashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
        }

        purgeExpiredBans();
    }

    public void save() {
        purgeExpiredBans();
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(cache, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BanEntry getBan(String playerName) {
        BanEntry ban = cache.get(playerName.toLowerCase());

        if (ban != null && ban.isExpired()) {
            unban(playerName);
            return null;
        }
        return ban;
    }

    public boolean isBanned(String playerName) {
        return getBan(playerName) != null;
    }

    public void ban(BanEntry ban) {
        cache.put(ban.getUsername().toLowerCase(), ban);
    }

    public void unban(String playerName) {
        cache.remove(playerName.toLowerCase());
    }

    public void purgeExpiredBans() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    public boolean kickIfBanned(EnginePlayer target) {
        BanEntry ban = getBan(target.getName());
        if (ban == null) return false;

        String reason = ban.getReason();
        String duration = ban.isPermanent() ? "Permanent" : formatDuration(ban.getRemainingTime());

        String message = target.processTranslation(
                TranslationKeys.BAN_KICK,
                reason,
                duration,
                ban.getStaff()
        );

        String[] lines = message.split("\\r?\\n");
        if (lines.length > 0) {
            lines[0] = Glyph.translate(lines[0]);
        }

        String finalMessage = String.join("\n", lines);
        target.close(finalMessage, finalMessage);
        return true;
    }

    public static String formatDuration(long ms) {
        long seconds = ms / 1000 % 60;
        long minutes = ms / (1000 * 60) % 60;
        long hours = ms / (1000 * 60 * 60) % 24;
        long days = ms / (1000 * 60 * 60 * 24);

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0) sb.append(seconds).append("s");

        return sb.toString().trim();
    }
}