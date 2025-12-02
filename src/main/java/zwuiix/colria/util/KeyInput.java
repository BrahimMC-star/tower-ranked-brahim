package zwuiix.colria.util;

public enum KeyInput {
    W(0, "W"),
    S(1, "S"),
    A(2, "A"),
    D(3, "D");

    final String name;
    public final int id;

    KeyInput(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
