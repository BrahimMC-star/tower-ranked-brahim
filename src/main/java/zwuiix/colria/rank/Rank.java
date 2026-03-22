package zwuiix.colria.rank;

import cn.nukkit.utils.TextFormat;
import lombok.Getter;

import java.util.List;

public class Rank {
    private static final char COLOR = '§';

    @Getter
    protected int id;
    protected String name;
    @Getter
    protected List<String> permissions;
    @Getter
    protected boolean visual;
    protected boolean _default;

    Rank(int id, String name, List<String> permissions, boolean visual, boolean _default) {
        this.id = id;

        this.name = name;
        this.permissions = permissions;
        this.visual = visual;
        this._default = _default;
    }

    Rank(int id, String name, List<String> permissions, boolean visual) {
        this(id, name, permissions, visual, false);
    }

    public String getName() {
        return TextFormat.clean(getColoredName());
    }

    public String getColoredName() {
        return TextFormat.colorize(name);
    }

    public String getColor() {
        if (name == null) return "";
        String s = TextFormat.colorize(name);
        StringBuilder sb = new StringBuilder();
        int i = 0;

        while (i + 1 < s.length() && s.charAt(i) == COLOR) {
            char code = s.charAt(i + 1);
            sb.append(COLOR).append(code);
            i += 2;

            if (code == 'x') {
                for (int j = 0; j < 6 && i + 1 < s.length(); j++) {
                    if (s.charAt(i) == COLOR) {
                        sb.append(s, i, i + 2);
                        i += 2;
                    }
                }
            }
        }
        return sb.toString();
    }

    public boolean isDefault() {
        return _default;
    }
}
