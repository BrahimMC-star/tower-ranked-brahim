package zwuiix.colria.shape.type;

import cn.nukkit.debugshape.DebugShape;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;
import zwuiix.colria.shape.type.nukkit.NukkitShapeText;

import java.awt.*;
import java.util.Objects;

public class TextRgbRenderer extends ShapeRenderer {
    @Getter
    private String text;
    public Vector3 pos;
    public Color start;
    public Color end;
    public double speed = 0.0005;

    public TextRgbRenderer(int id, String text, Vector3 pos, Color start, Color end, double speed) {
        super(id);
        this.text = Objects.requireNonNullElse(text, "");
        this.pos = Objects.requireNonNull(pos, "pos");
        this.start = Objects.requireNonNull(start, "start");
        this.end = Objects.requireNonNull(end, "end");
        this.speed = speed;
    }

    public TextRgbRenderer update(String text) {
        this.text = Objects.requireNonNullElse(text, "");
        return this;
    }

    public Color getGradientColor(Color a, Color b, double speed) {
        long nowMs = System.nanoTime() / 1_000_000L;
        double phase = ((nowMs * speed) % 2.0);
        double t = (phase <= 1.0) ? phase : (2.0 - phase);
        return lerp(a, b, t);
    }

    public Color currentColor() {
        return getGradientColor(this.start, this.end, this.speed);
    }

    public String toColoredText() {
        return TextFormat.colorize(this.text);
    }

    private static Color lerp(Color a, Color b, double t) {
        t = clamp01(t);
        int r = (int)Math.round(a.getRed()   + (b.getRed()   - a.getRed())   * t);
        int g = (int)Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl= (int)Math.round(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t);
        int al= (int)Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t);
        return new Color(r, g, bl, al);
    }

    private static double clamp01(double x) {
        return x < 0 ? 0 : (x > 1 ? 1 : x);
    }

    @Override
    public DebugShape toNetwork() {
        return new NukkitShapeText(pos.asVector3f(), currentColor(), text, getId());
    }
}
