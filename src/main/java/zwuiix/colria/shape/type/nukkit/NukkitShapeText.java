package zwuiix.colria.shape.type.nukkit;

import cn.nukkit.debugshape.DebugShape;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.types.ScriptDebugShape;
import cn.nukkit.network.protocol.types.ScriptDebugShapeType;
import lombok.Setter;

import java.awt.*;

public class NukkitShapeText extends DebugShape {
    @Setter
    public String text;
    public long id;

    public NukkitShapeText(Vector3f position, Color color, String text, int id) {
        super(position, color);
        this.text = text;
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public ScriptDebugShapeType getType() {
        return ScriptDebugShapeType.TEXT;
    }

    @Override
    public ScriptDebugShape toNetworkData() {
        return new ScriptDebugShape(
                id, getType(), position, null,
                null, null, color, 0L, 0, text, null, null,
                null, null, null
        );
    }
}