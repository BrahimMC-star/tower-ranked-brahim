package zwuiix.colria.util;

import cn.nukkit.math.Vector3;

public class Rotation {
    public static float faceYawTowards(Vector3 from, Vector3 to) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        return wrapYaw(yaw);
    }

    public static float facePitchTowards(Vector3 from, Vector3 to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        return (float) Math.toDegrees(Math.atan2(dy, horiz)); // MC: up = -, down = +
    }

    public static float wrapYaw(float yaw) {
        yaw %= 360f;
        if (yaw >= 180f) yaw -= 360f;
        if (yaw < -180f) yaw += 360f;
        return yaw;
    }
}
