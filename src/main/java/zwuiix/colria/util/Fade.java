package zwuiix.colria.util;

import cn.nukkit.math.Vector3;

public class Fade {
    public static int alpha(Vector3 viewer, Vector3 target,
                            double near, double far,
                            int minA, int maxA) {
        double n2 = near * near;
        double f2 = far * far;
        double d2 = viewer.distanceSquared(target);

        if (f2 <= n2) return minA;

        double t = (d2 - n2) / (f2 - n2);
        if (t < 0) t = 0;
        if (t > 1) t = 1;

        return (int) Math.round(minA + t * (maxA - minA));
    }
}
