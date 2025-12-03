package zwuiix.colria;

import cn.nukkit.utils.TextFormat;
import zwuiix.colria.util.Glyph;

public class EngineInfo {
    public static String NAME = "§r§3Colria";
    public static String SUFFIX = "§r§8»§f";
    public static String PREFIX = NAME + " " + SUFFIX;
    public static TextFormat COLOR = TextFormat.DARK_AQUA;
    public static String DOMAIN = "colria.club";

    public static String VBAR_DEFAULT = Glyph.vbar(EngineInfo.COLOR, 1) + " ";

    public static String VERSION = "v1.0.2";
}
