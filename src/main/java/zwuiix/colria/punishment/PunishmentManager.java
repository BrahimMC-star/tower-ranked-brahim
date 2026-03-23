package zwuiix.colria.punishment;

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
import java.util.*;

public class PunishmentManager {

    @Getter
    private static PunishmentManager instance;

    private final Loader loader;

    private File file;
    private final Gson gson;

    private Map<String, List<PunishmentEntry>> cache = new HashMap<>();

    public PunishmentManager(Loader loader) {
        instance = this;
        this.loader = loader;

        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(PunishmentEntry.class, new PunishmentEntry.Adapter())
                .create();

        load();
    }

    private void load() {
        file = new File(Path.of(loader.getDataFolder().toString(), "punishments.db").toString());

        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, List<PunishmentEntry>>>() {}.getType();
            cache = gson.fromJson(reader, type);
            if (cache == null) cache = new HashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
        }

        purgeExpired();
    }

    public void save() {
        purgeExpired();
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(cache, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(PunishmentEntry entry) {
        cache.computeIfAbsent(entry.getUsername().toLowerCase(), k -> new ArrayList<>())
                .add(entry);
    }

    public void remove(String playerName, PunishmentType type) {
        List<PunishmentEntry> list = cache.get(playerName.toLowerCase());
        if (list == null) return;

        list.removeIf(p -> p.getType() == type);

        if (list.isEmpty()) {
            cache.remove(playerName.toLowerCase());
        }
    }

    public List<PunishmentEntry> getAll(String playerName) {
        return cache.getOrDefault(playerName.toLowerCase(), Collections.emptyList());
    }

    public void purgeExpired() {
        cache.values().forEach(list ->
                list.removeIf(PunishmentEntry::isExpired)
        );
        cache.entrySet().removeIf(e -> e.getValue().isEmpty());
    }

    public boolean isBanned(String playerName) {
        return getActive(playerName, PunishmentType.BAN) != null;
    }

    public PunishmentEntry getBan(String playerName) {
        return getActive(playerName, PunishmentType.BAN);
    }

    public void ban(PunishmentEntry entry) {
        add(entry);
    }

    public void unban(String playerName) {
        remove(playerName, PunishmentType.BAN);
    }

    public boolean kickIfBanned(EnginePlayer target) {
        PunishmentEntry ban = getBan(target.getName());
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

    public boolean isMuted(String playerName) {
        return getActive(playerName, PunishmentType.MUTE) != null;
    }

    public PunishmentEntry getMute(String playerName) {
        return getActive(playerName, PunishmentType.MUTE);
    }

    public void mute(PunishmentEntry entry) {
        add(entry);
    }

    public void unmute(String playerName) {
        remove(playerName, PunishmentType.MUTE);
    }

    private PunishmentEntry getActive(String playerName, PunishmentType type) {
        List<PunishmentEntry> list = cache.get(playerName.toLowerCase());
        if (list == null) return null;

        for (PunishmentEntry p : list) {
            if (p.getType() == type) {
                if (p.isExpired()) continue;
                return p;
            }
        }
        return null;
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