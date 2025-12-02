package zwuiix.colria.shape;

import java.util.concurrent.atomic.AtomicInteger;

public final class ShapeIds {
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private ShapeIds() {}

    public static int nextId() {
        return COUNTER.incrementAndGet();
    }

    public static int current() {
        return COUNTER.get();
    }
}