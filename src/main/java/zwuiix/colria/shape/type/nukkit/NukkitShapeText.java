package zwuiix.colria.shape.type.nukkit;

import cn.nukkit.debugshape.DebugShape;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.types.ScriptDebugShape;
import cn.nukkit.network.protocol.types.ScriptDebugShapeType;

import java.awt.*;

public class NukkitShapeText extends DebugShape {
    public String text;
    public long id;

    /**
     * Creates a DebugShapeText with the specified position, color, and text.
     *
     * @param position The position of the text in the world.
     * @param color    The color of the text.
     * @param text     The text to display.
     */
    public NukkitShapeText(Vector3f position, Color color, String text, int id) {
        super(position, color);
        this.text = text;
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    /**
     * Sets the text to display.
     *
     * @param text The text to display.
     */
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public ScriptDebugShapeType getType() {
        return ScriptDebugShapeType.TEXT;
    }

    @Override
    public ScriptDebugShape toNetworkData() {
        return new ScriptDebugShape(
                id, getType(), position, null,
                null, null, color,
                text, null, null,
                null, null, null
        );
    }
}