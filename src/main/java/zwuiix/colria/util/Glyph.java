package zwuiix.colria.util;

import cn.nukkit.utils.TextFormat;

import java.util.HashMap;
import java.util.Map;

public final class Glyph {
    private Glyph() {}

    public enum Code {
        E0(0xE000),
        E1(0xE100),
        E2(0xE200),
        E3(0xE300);
        final int base;

        Code(int base) {
            this.base = base;
        }
    }

    public enum Emoji {
        VBAR(Code.E0, 0),
        HBAR_S(Code.E0, 16),
        HBAR_UNDER_S(Code.E0, 32),
        BAR(Code.E0, 0),

        HBAR_L(Code.E3, 0),
        HBAR_UNDER_L(Code.E3, 16);

        final Code page;
        final int baseOffset;

        Emoji(Code page, int baseOffset) {
            this.page = page;
            this.baseOffset = baseOffset;
        }
    }

    private static final char SECTION = TextFormat.ESCAPE; // '§'
    private static final int LETTERS_PER_COLOR = 26;       // a..z
    private static final int PAGE_SIZE = 256;

    private static final char[] COLOR_ORDER = {
            'f', 'e', '6', 'c', '4', 'd', '5', '9', '1', 'b', '3', 'a', '2', '7', '8', '0'
    };
    private static final Map<Character, Integer> COLOR_INDEX = new HashMap<>();

    static {
        for (int i = 0; i < COLOR_ORDER.length; i++) COLOR_INDEX.put(COLOR_ORDER[i], i);
    }

    private static final int[][] VANILLA_RGB = new int[16][3];

    static {
        putRGB('0', 0x00, 0x00, 0x00);
        putRGB('1', 0x00, 0x00, 0xAA);
        putRGB('2', 0x00, 0xAA, 0x00);
        putRGB('3', 0x00, 0xAA, 0xAA);
        putRGB('4', 0xAA, 0x00, 0x00);
        putRGB('5', 0xAA, 0x00, 0xAA);
        putRGB('6', 0xFF, 0xAA, 0x00);
        putRGB('7', 0xAA, 0xAA, 0xAA);
        putRGB('8', 0x55, 0x55, 0x55);
        putRGB('9', 0x55, 0x55, 0xFF);
        putRGB('a', 0x55, 0xFF, 0x55);
        putRGB('b', 0x55, 0xFF, 0xFF);
        putRGB('c', 0xFF, 0x55, 0x55);
        putRGB('d', 0xFF, 0x55, 0xFF);
        putRGB('e', 0xFF, 0xFF, 0x55);
        putRGB('f', 0xFF, 0xFF, 0xFF);
    }

    private static void putRGB(char code, int r, int g, int b) {
        Integer idx = COLOR_INDEX.get(code);
        if (idx != null) VANILLA_RGB[idx] = new int[]{r, g, b};
    }

    public static String translate(String text) {
        return translate(Code.E1, text, TextFormat.WHITE);
    }

    public static String translate(String text, TextFormat color) {
        return translate(Code.E1, text, color);
    }

    public static String translate(Code code, String text, TextFormat defaultColor) {
        if (text == null || text.isEmpty()) return "";

        text = normalizeAlternateColors(text); // &a / &#RRGGBB -> §…

        char def = colorCodeOf(defaultColor);
        int currentColorIdx = COLOR_INDEX.getOrDefault(def, COLOR_INDEX.get('f'));

        StringBuilder out = new StringBuilder(text.length() + 8);
        out.append(SECTION).append(def);

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            if (ch == SECTION) {
                if (i + 1 < text.length()) {
                    char n = Character.toLowerCase(text.charAt(i + 1));

                    // HEX §x§R§R§G§G§B§B
                    if (n == 'x' && i + 13 < text.length() && isValidSectionHexChain(text, i)) {
                        out.append(text, i, i + 14);
                        int r = hexNibble(text.charAt(i + 3)) * 16 + hexNibble(text.charAt(i + 5));
                        int g = hexNibble(text.charAt(i + 7)) * 16 + hexNibble(text.charAt(i + 9));
                        int b = hexNibble(text.charAt(i + 11)) * 16 + hexNibble(text.charAt(i + 13));
                        currentColorIdx = nearestVanilla(r, g, b);
                        i += 13;
                        continue;
                    }

                    if (isColorChar(n)) {
                        out.append(SECTION).append(n);
                        currentColorIdx = COLOR_INDEX.getOrDefault(n, currentColorIdx);
                        i++;
                        continue;
                    }
                }

                out.append(ch);
                continue;
            }

            char lower = Character.toLowerCase(ch);
            if (lower >= 'a' && lower <= 'z') {
                int letterIdx = lower - 'a'; // 0..25
                int total = currentColorIdx * LETTERS_PER_COLOR + letterIdx;
                int pageShift = total / PAGE_SIZE; // spill E1→E2…
                int inPage = total % PAGE_SIZE;
                int cp = code.base + pageShift * PAGE_SIZE + inPage;
                if (cp >= 0xE000 && cp <= 0xF8FF) out.append((char) cp);
                else out.append(ch);
            } else {
                out.append(ch);
            }
        }
        return out.toString();
    }

    public static String emoji(Emoji emoji, char colorCode) {
        int colorIdx = COLOR_INDEX.getOrDefault(Character.toLowerCase(colorCode), COLOR_INDEX.get('f'));
        int cp = emoji.page.base + emoji.baseOffset + colorIdx;
        if (cp < 0xE000 || cp > 0xF8FF) return "";
        return String.valueOf((char) cp);
    }

    public static String emoji(Emoji emoji, TextFormat color) {
        return emoji(emoji, colorCodeOf(color));
    }

    public static String emojiHex(Emoji emoji, String hexRRGGBB) {
        int[] rgb = parseHexRGB(hexRRGGBB);
        int idx = nearestVanilla(rgb[0], rgb[1], rgb[2]);
        char code = COLOR_ORDER[idx];
        return emoji(emoji, code);
    }

    public static String hbar(char color, int length) {
        return hbarFor(Emoji.HBAR_S, color, length);
    }

    public static String hbar(TextFormat color, int length) {
        return hbarFor(Emoji.HBAR_S, colorCodeOf(color), length);
    }

    public static String hbarUnder(char color, int length) {
        return hbarFor(Emoji.HBAR_UNDER_S, color, length);
    }

    public static String hbarUnder(TextFormat c, int length) {
        return hbarFor(Emoji.HBAR_UNDER_S, colorCodeOf(c), length);
    }

    public static String hbarThick(char color, int length) {
        return hbarFor(Emoji.HBAR_L, color, length);
    }

    public static String hbarThick(TextFormat c, int length) {
        return hbarFor(Emoji.HBAR_L, colorCodeOf(c), length);
    }

    public static String hbarUnderThick(char color, int length) {
        return hbarFor(Emoji.HBAR_UNDER_L, color, length);
    }

    public static String hbarUnderThick(TextFormat c, int length) {
        return hbarFor(Emoji.HBAR_UNDER_L, colorCodeOf(c), length);
    }

    private static String hbarFor(Emoji e, char color, int length) {
        if (length <= 0) return "";
        String g = emoji(e, color);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append(g);
        return sb.toString();
    }

    public static String vbar(char color, int height) {
        return vbarFor(Emoji.VBAR, color, height);
    }

    public static String vbar(TextFormat color, int height) {
        return vbarFor(Emoji.VBAR, colorCodeOf(color), height);
    }

    private static String vbarFor(Emoji e, char color, int height) {
        if (height <= 0) return "";
        String g = emoji(e, color);
        StringBuilder sb = new StringBuilder(height * 2);
        for (int i = 0; i < height; i++) {
            if (i > 0) sb.append('\n');
            sb.append(g);
        }
        return sb.toString();
    }

    private static boolean isColorChar(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f');
    }

    private static String normalizeAlternateColors(String s) {
        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            // &#RRGGBB -> §x§R§R§G§G§B§B
            if (c == '&' && i + 7 < s.length() && s.charAt(i + 1) == '#') {
                String hex = s.substring(i + 2, i + 8);
                if (isHexRGB(hex)) {
                    b.append(SECTION).append('x');
                    for (int k = 0; k < 6; k++) b.append(SECTION).append(Character.toLowerCase(hex.charAt(k)));
                    i += 7;
                    continue;
                }
            }
            // &a -> §a
            if (c == '&' && i + 1 < s.length()) {
                char n = Character.toLowerCase(s.charAt(i + 1));
                if (isColorChar(n)) {
                    b.append(SECTION).append(n);
                    i++;
                    continue;
                }
            }
            b.append(c);
        }
        return b.toString();
    }

    private static boolean isHexRGB(String s) {
        if (s.length() != 6) return false;
        for (int i = 0; i < 6; i++) {
            char c = Character.toLowerCase(s.charAt(i));
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f'))) return false;
        }
        return true;
    }

    private static boolean isValidSectionHexChain(String text, int i) {
        if (i + 13 >= text.length()) return false;
        if (text.charAt(i) != SECTION || Character.toLowerCase(text.charAt(i + 1)) != 'x') return false;
        for (int off = 2; off <= 12; off += 2) {
            if (text.charAt(i + off) != SECTION) return false;
            char h = Character.toLowerCase(text.charAt(i + off + 1));
            if (!((h >= '0' && h <= '9') || (h >= 'a' && h <= 'f'))) return false;
        }
        return true;
    }

    private static int hexNibble(char c) {
        c = Character.toLowerCase(c);
        if (c >= '0' && c <= '9') return c - '0';
        if (c >= 'a' && c <= 'f') return 10 + (c - 'a');
        return 0;
    }

    private static int nearestVanilla(int r, int g, int b) {
        int best = 0;
        long bestD = Long.MAX_VALUE;
        for (int i = 0; i < COLOR_ORDER.length; i++) {
            int[] v = VANILLA_RGB[i];
            long dr = r - v[0], dg = g - v[1], db = b - v[2];
            long d = dr * dr + dg * dg + db * db;
            if (d < bestD) {
                bestD = d;
                best = i;
            }
        }
        return best;
    }

    private static char colorCodeOf(TextFormat tf) {
        String s = tf == null ? "" : tf.toString(); // "§f"
        if (s.length() >= 2 && s.charAt(0) == SECTION) {
            char c = Character.toLowerCase(s.charAt(1));
            if (isColorChar(c)) return c;
        }
        return 'f';
    }

    private static int[] parseHexRGB(String hex) {
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        if (!isHexRGB(h)) return new int[]{255, 255, 255};
        int r = Integer.parseInt(h.substring(0, 2), 16);
        int g = Integer.parseInt(h.substring(2, 4), 16);
        int b = Integer.parseInt(h.substring(4, 6), 16);
        return new int[]{r, g, b};
    }
}