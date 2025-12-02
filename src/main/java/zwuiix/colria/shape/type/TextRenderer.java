package zwuiix.colria.shape.type;

import cn.nukkit.debugshape.DebugShape;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import zwuiix.colria.shape.type.nukkit.NukkitShapeText;

import java.awt.*;
import java.util.Objects;

public class TextRenderer extends ShapeRenderer {
    private String text;
    public Vector3 pos;
    public Color color;

    public TextRenderer(int id, String text, Vector3 pos, Color color) {
        super(id);
        this.text = Objects.requireNonNullElse(text, "");
        this.pos = Objects.requireNonNull(pos, "pos");
        this.color = color;
    }

    public String getText() { return text; }

    public TextRenderer update(String text) {
        this.text = Objects.requireNonNullElse(text, "");
        return this;
    }

    public String toColoredText() {
        return TextFormat.colorize(this.text);
    }

    @Override
    public DebugShape toNetwork() {
        return new NukkitShapeText(pos.asVector3f(), color, text, getId());
    }
}
