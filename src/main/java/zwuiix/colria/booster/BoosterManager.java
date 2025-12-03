package zwuiix.colria.booster;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.bossbar.BossBarColor;
import cn.nukkit.level.Sound;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.scheduler.Task;
import com.google.gson.*;
import zwuiix.colria.player.EnginePlayer;
import zwuiix.colria.translator.TranslationKeys;
import zwuiix.colria.util.BossBar;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoosterManager {

    public static final long START_TIME = 60;

    private static BoosterManager INSTANCE;
    public static BoosterManager getInstance() {
        return INSTANCE;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Plugin plugin;
    private final File file;

    private long lastReminder = 0L;
    private Long nextStartTime = null;
    private Booster.Current current = null;
    private final List<Booster> queue = new ArrayList<>();
    private BossBar bossBar = null;

    public BoosterManager(Plugin plugin) {
        INSTANCE = this;
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "booster.json");
        load();

        Server.getInstance().getScheduler().scheduleRepeatingTask(plugin, new Task() {
            @Override
            public void onRun(int currentTick) {
                tick();
            }
        }, 20);
    }

    private void load() {
        if (!file.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null || !root.isJsonObject()) {
                return;
            }

            if (root.has("current") && root.get("current").isJsonObject()) {
                JsonObject currentObj = root.getAsJsonObject("current");
                Booster.Current current = Booster.Current.fromJson(currentObj);

                long now = System.currentTimeMillis() / 1000L;
                if (current.end() > now) {
                    this.current = current;
                    this.bossBar = new BossBar();
                    this.bossBar.updateColor(BossBarColor.BLUE);
                }
            }

            this.queue.clear();
            if (root.has("queue") && root.get("queue").isJsonArray()) {
                JsonArray q = root.getAsJsonArray("queue");
                for (JsonElement el : q) {
                    if (!el.isJsonObject()) continue;
                    Booster booster = Booster.fromJson(el.getAsJsonObject());
                    this.queue.add(booster);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().error("Error loading booster.json: " + e.getMessage(), e);
        }
    }

    public void close() {
        try (FileWriter writer = new FileWriter(file)) {
            JsonObject root = new JsonObject();

            if (this.current != null) {
                root.add("current", this.current.toJson());
            } else {
                root.add("current", JsonNull.INSTANCE);
            }

            JsonArray q = new JsonArray();
            for (Booster booster : this.queue) {
                q.add(booster.toJson());
            }
            root.add("queue", q);

            GSON.toJson(root, writer);
        } catch (Exception e) {
            plugin.getLogger().error("Error saving booster.json: " + e.getMessage(), e);
        }
    }

    public void tick() {
        long now = System.currentTimeMillis() / 1000L;

        if (this.bossBar != null) {
            if (this.current != null) {
                long remaining = this.current.end() - now;
                long duration  = this.current.booster().duration();

                double percent = duration > 0
                        ? Math.max(0.0, Math.min(1.0, (double) remaining / (double) duration))
                        : 0.0;

                long clampedRemaining = Math.max(0L, remaining);
                long minutes = clampedRemaining / 60;
                long seconds = clampedRemaining % 60;
                String timeStr = String.format("%02d:%02d", minutes, seconds);

                Booster booster = this.current.booster();
                String owner = booster.owner();
                float multiplier = booster.multiplier();

                for (EnginePlayer viewer : this.bossBar.getViewers().values()) {
                    this.bossBar.show(viewer);
                    this.bossBar.syncText(viewer, viewer.processTranslation(TranslationKeys.BOOSTER_TITLE, owner, (long)multiplier, timeStr));
                    BossBarColor[] colors = BossBarColor.values();
                    BossBarColor randomColor = colors[(int) (Math.random() * colors.length)];
                    this.bossBar.updateColor(randomColor);
                }
                this.bossBar.setPercentage((float) percent);
            } else {
                this.bossBar.hide();
                this.bossBar = null;
            }
        }

        if (this.current != null) {
            long duration = this.current.booster().duration();
            long interval = Math.max(1L, duration / 12L);

            if (now - this.lastReminder >= interval) {
                Booster booster = this.current.booster();
                for (Player p : Server.getInstance().getOnlinePlayers().values()) {
                    EnginePlayer player = (EnginePlayer) p;
                    player.sendMessage(TranslationKeys.BOOSTER_BROADCAST, booster.type(), booster.owner());
                }
                this.lastReminder = now;
            }
        }

        if (this.current != null && now >= this.current.end()) {
            Booster finished = this.current.booster();

            if (this.bossBar != null) {
                this.bossBar.hide();
                this.bossBar = null;
            }

            for (Player p : Server.getInstance().getOnlinePlayers().values()) {
                EnginePlayer player = (EnginePlayer) p;
                player.sendMessage(TranslationKeys.BOOSTER_FINISH, finished.type(), finished.owner());
            }

            this.current = null;
            this.nextStartTime = now + START_TIME;
        }

        if (this.current == null && this.nextStartTime != null && now < this.nextStartTime) {
            return;
        }

        if (this.current == null && !this.queue.isEmpty()) {
            Booster next = this.queue.removeFirst();
            long end = now + next.duration();

            this.current = new Booster.Current(next, now, end);

            this.bossBar = new BossBar();
            this.bossBar.updateColor(BossBarColor.BLUE);
            for (Player p : Server.getInstance().getOnlinePlayers().values()) {
                EnginePlayer player = (EnginePlayer) p;
                player.sendMessage(TranslationKeys.BOOSTER_START, next.owner(), next.type());

                player.addSound(Sound.MOB_ENDERDRAGON_DEATH, 0.5f, 1.0f);
                this.bossBar.addViewer(player);
            }

            this.nextStartTime = null;
            this.lastReminder = now;
        }
    }

    public void addBooster(Booster booster) {
        this.queue.add(booster);
    }

    public Booster.Current getCurrent() {
        return this.current;
    }

    public List<Booster> getQueue() {
        return Collections.unmodifiableList(this.queue);
    }

    public int getRemaining() {
        if (this.current == null) {
            return 0;
        }
        long now = System.currentTimeMillis() / 1000L;
        return (int) Math.max(0L, this.current.end() - now);
    }

    public float getMultiplier() {
        return this.current != null ? this.current.booster().multiplier() : 1.0f;
    }

    public BossBar getBossBar() {
        return this.bossBar;
    }

    public boolean hasBossBar() {
        return this.bossBar != null;
    }
}
