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
                id,                        // long
                getType(),                 // ScriptDebugShapeType
                position,                  // Vector3f
                null,                      // rotationX (Float)
                null,                      // rotationVec (Vector3f)
                null,                      // scale (Float)
                color,                     // Color
                text,                      // String
                null,                      // size (Vector3f)
                null,                      // offset (Vector3f)
                null,                      // something1 (Float)
                null,                      // something2 (Float)
                null                       // something3 (Integer)
        );
    }
}