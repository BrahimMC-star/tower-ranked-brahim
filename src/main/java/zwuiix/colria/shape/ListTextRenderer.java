package zwuiix.colria.shape;

import cn.nukkit.Player;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import zwuiix.colria.shape.type.TextRgbRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListTextRenderer {

    private final List<TextRgbRenderer> shapes = new ArrayList<>();
    private final List<TextRgbRenderer> pendingRemoval = new ArrayList<>();

    public Vector3 pos;
    public Color start;
    public Color end;
    public double speed = 0.0005;

    public ListTextRenderer(
            List<String> texts,
            Vector3 pos,
            Color start,
            Color end,
            double speed
    ) {
        this.pos = Objects.requireNonNull(pos, "pos");
        this.start = Objects.requireNonNull(start, "start");
        this.end = Objects.requireNonNull(end, "end");
        this.speed = speed;
        update(texts.toArray(new String[0]));
    }

    public ListTextRenderer update(String... texts) {
        for (int k = 0; k < texts.length; k++) {
            String text = texts[k] == null ? "" : texts[k];
            if (k < shapes.size()) {
                shapes.get(k).update(text);
            } else {
                TextRgbRenderer s = new TextRgbRenderer(
                        ShapeIds.nextId(),
                        text,
                        new Vector3(0, 0, 0),
                        start,
                        end,
                        speed
                );
                shapes.add(s);
            }
        }

        for (int i = shapes.size() - 1; i >= texts.length; i--) {
            pendingRemoval.add(shapes.remove(i));
        }

        return this;
    }

    public void send(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        if (!pendingRemoval.isEmpty()) {
            for (TextRgbRenderer s : pendingRemoval) {
                s.remove(player);
            }
            pendingRemoval.clear();
        }

        for (int k = 0; k < shapes.size(); k++) {
            TextRgbRenderer s = shapes.get(k);

            s.speed = speed;
            s.start = start;
            s.end = end;

            s.pos = new Vector3(pos.x, pos.y + 0.3 * k, pos.z);

            String clean = TextFormat.clean(s.getText());
            if (clean == null || clean.isEmpty()) continue;

            s.send(player);
        }
    }

    public void remove(Player player) {
        if (player == null) return;

        for (TextRgbRenderer s : pendingRemoval) {
            s.remove(player);
        }
        pendingRemoval.clear();

        for (TextRgbRenderer s : shapes) {
            s.remove(player);
        }
    }
}
