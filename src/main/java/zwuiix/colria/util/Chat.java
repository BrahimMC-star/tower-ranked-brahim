package zwuiix.colria.util;

import cn.nukkit.utils.TextFormat;

import java.util.Locale;
import java.util.regex.Pattern;

public class Chat {
    private static final Pattern REPEAT_CHAR_TO_ONE  = Pattern.compile("(.)\\1{2,}", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern REPEAT_CHAR_TO_TWO  = Pattern.compile("(.)\\1{2,}", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern REPEAT_PAIR_3PLUS   = Pattern.compile("((..))\\1{2,}", Pattern.UNICODE_CHARACTER_CLASS);

    public static String clean(String message) {
        String stripped;
        try {
            stripped = TextFormat.clean(message);
        } catch (Throwable ignored) {
            stripped = message.replaceAll("(?i)[§&][0-9A-FK-ORX]", "");
        }

        String[] words = stripped.split(" ", -1);

        for (int i = 0; i < words.length; i++) {
            String w = words[i];

            w = REPEAT_CHAR_TO_ONE.matcher(w).replaceAll("$1");
            w = REPEAT_PAIR_3PLUS.matcher(w).replaceAll("$1");

            while (REPEAT_CHAR_TO_TWO.matcher(w).find()) {
                w = REPEAT_CHAR_TO_TWO.matcher(w).replaceAll("$1$1");
            }

            int lenCp = w.isEmpty() ? 0 : w.codePointCount(0, w.length());
            if (lenCp > 0) {
                long upper = w.codePoints().filter(Character::isUpperCase).count();
                double percent = upper / Math.max(1.0, (double) lenCp);

                if (percent >= 0.8) {
                    w = capitalizeFirst(w);
                }
            }

            words[i] = w;
        }

        return String.join(" ", words);
    }

    public static String capitalizeFirst(String s) {
        String lower = s.toLowerCase(Locale.ROOT);
        if (lower.isEmpty()) return lower;
        int firstCp = lower.codePointAt(0);
        int firstLen = Character.charCount(firstCp);
        String first = new String(Character.toChars(Character.toUpperCase(firstCp)));
        return first + lower.substring(firstLen);
    }

    public static String sectionize(String s) {
        StringBuilder sb = new StringBuilder(s.length() * 2 + 2);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c)) sb.append(c);
            else sb.append('§').append(c);
        }
        return sb.append('§').append('r').toString();
    }
}
