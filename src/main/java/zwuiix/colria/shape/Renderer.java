package zwuiix.colria.shape;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.Task;
import cn.nukkit.scheduler.TaskHandler;
import zwuiix.colria.shape.type.ShapeRenderer;
import zwuiix.colria.shape.type.TextRenderer;
import zwuiix.colria.shape.type.TextRgbRenderer;

import java.awt.*;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Renderer {
    private final Map<String, Object> shapes = new ConcurrentHashMap<>();
    private final Map<UUID, TaskHandler> tickers = new ConcurrentHashMap<>();

    public static Renderer create() {
        return new Renderer();
    }

    public TextRenderer drawText(String key, String text, Vector3 pos, Color color) {
        Objects.requireNonNull(key); Objects.requireNonNull(pos);
        return (TextRenderer) shapes.compute(key, (k, existing) -> {
            if (existing instanceof TextRenderer tr) {
                tr.update(text);
                tr.pos = pos;
                tr.color = color;
                return tr;
            }
            return new TextRenderer(ShapeIds.nextId(), text, pos, color);
        });
    }

    public TextRgbRenderer drawTextRgb(String key, String text, Vector3 pos, Color start, Color end, double speed) {
        Objects.requireNonNull(key); Objects.requireNonNull(pos);
        Objects.requireNonNull(start); Objects.requireNonNull(end);
        return (TextRgbRenderer) shapes.compute(key, (k, existing) -> {
            if (existing instanceof TextRgbRenderer trgb) {
                trgb.update(text);
                trgb.pos = pos;
                trgb.start = start;
                trgb.end = end;
                trgb.speed = speed;
                return trgb;
            }
            return new TextRgbRenderer(ShapeIds.nextId(), text, pos, start, end, speed);
        });
    }

    public ListTextRenderer drawTextList(String key, Vector3 pos, Color color, java.util.List<String> lines) {
        return drawListTextRgb(key, pos, color, color, 0.0, lines);
    }

    public ListTextRenderer drawListTextRgb(String key, Vector3 pos, Color start, Color end, double speed, java.util.List<String> lines) {
        Objects.requireNonNull(key); Objects.requireNonNull(pos);
        Objects.requireNonNull(start); Objects.requireNonNull(end); Objects.requireNonNull(lines);

        return (ListTextRenderer) shapes.compute(key, (k, existing) -> {
            if (existing instanceof ListTextRenderer list) {
                list.pos = pos;
                list.start = start;
                list.end = end;
                list.speed = speed;
                list.update(lines.toArray(new String[0]));
                return list;
            }
            return new ListTextRenderer(lines, pos, start, end, speed);
        });
    }

    public void remove(String key) {
        shapes.remove(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) shapes.get(key);
    }

    public void send(Player player) {
        if (player == null || !player.isOnline()) {
            removeTicker(player);
            return;
        }
        for (Object o : shapes.values()) {
            if (o instanceof ShapeRenderer sr) {
                sr.send(player);
            } else if (o instanceof ListTextRenderer lr) {
                lr.send(player);
            }
        }
    }

    public void removeAllFrom(Player player) {
        if (player == null) return;
        for (Object o : shapes.values()) {
            if (o instanceof ShapeRenderer sr) {
                sr.remove(player);
            } else if (o instanceof ListTextRenderer lr) {
                lr.remove(player);
            }
        }
    }

    public Renderer createTicker(Player player, int periodTicks) {
        if (player == null || !player.isOnline()) return this;
        UUID id = player.getUniqueId();
        removeTicker(player);

        TaskHandler handler = Server.getInstance().getScheduler()
                .scheduleRepeatingTask(new Task() {
                    @Override public void onRun(int currentTick) {
                        send(player);
                    }
                }, Math.max(1, periodTicks));
        tickers.put(id, handler);
        return this;
    }

    public void removeTicker(Player player) {
        if (player == null) return;
        TaskHandler h = tickers.remove(player.getUniqueId());
        if (h != null) h.cancel();
    }
}
