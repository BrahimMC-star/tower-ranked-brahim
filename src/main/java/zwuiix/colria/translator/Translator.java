package zwuiix.colria.translator;

import cn.nukkit.command.CommandSender;
import cn.nukkit.lang.TextContainer;
import cn.nukkit.utils.TextFormat;
import zwuiix.colria.EngineInfo;
import zwuiix.colria.player.EnginePlayer;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

public class Translator {
    private static Translator instance = new Translator();

    public static Translator getInstance() {
        if (instance == null) {
            instance = new Translator();
        }

        return instance;
    }

    public String autoProcess(CommandSender sender, TranslationKeys key, Object... args) {
        if(sender instanceof EnginePlayer player) return player.processTranslation(key, args);

        Optional<Map.Entry<Language, LanguageFile>> language = LanguageRegistry.getInstance().getLanguages().entrySet().stream().findFirst();
        if(language.isPresent()) {
            return process(language.get().getKey(), key, args);
        }

        throw new IllegalArgumentException("Impossible to find a language for this translation");
    }

    public String process(Language language, TranslationKeys key, Object... args) {
        return process(language, key.toString(), args);
    }

    public String process(Language language, String key, Object ...params) {
        LanguageFile file = language.getFile();
        var value = file.get(key)
                .replace("{PREFIX}", EngineInfo.PREFIX)
                .replace("{SUFFIX}", EngineInfo.SUFFIX)
                .replace("{LINE}", "\n");

        if (params != null && params.length > 0) {
            final Locale locale = safeLocale(file);

            final Object[] formatted = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                Object arg = params[i];

                if (arg instanceof TextContainer tc) {
                    formatted[i] = tc.getText();
                } else if (arg instanceof Number n) {
                    formatted[i] = formatNumberCompact(n);
                } else {
                    formatted[i] = arg;
                }
            }

            try {
                value = String.format(locale, value, formatted);
            } catch (IllegalFormatException ex) {
                value = fallbackPercentReplacement(value, formatted);
            }
        }

        return TextFormat.colorize(value.replace("{PERC}", "%"));
    }

    private static Locale safeLocale(LanguageFile file) {
        try {
            Locale l = file.getLocale();
            return (l != null) ? l : Locale.getDefault();
        } catch (Throwable ignored) {
            return Locale.getDefault();
        }
    }

    private static String formatNumberCompact(Number number) {
        if (number == null) return "0";
        double d = number.doubleValue();
        if (d == 0d) return "0";

        boolean negative = d < 0;
        d = Math.abs(d);

        if (d >= 1_000_000d) {
            String s = formatWithComma(d / 1_000_000d, 2);
            return (negative ? "-" : "") + s + "M";
        }

        if (d >= 1_000d) {
            String s = formatWithComma(d / 1_000d, 2);
            return (negative ? "-" : "") + s + "K";
        }

        int maxFrac = (number instanceof Float || number instanceof Double) ? 3 : 0;
        String s = formatWithComma(d, maxFrac);
        return (negative ? "-" : "") + s;
    }

    private static String formatWithComma(double value, int maxFrac) {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.US);
        sym.setDecimalSeparator('.');

        String pattern = "0";
        if (maxFrac > 0) pattern += "." + "#".repeat(maxFrac);

        DecimalFormat df = new DecimalFormat(pattern, sym);
        df.setGroupingUsed(false);

        String out = df.format(value);
        if (maxFrac > 0 && out.indexOf('.') >= 0) {
            out = out.replaceAll("(\\.\\d*?)0+$", "$1");
            out = out.replaceAll("\\.$", "");
        }
        return out;
    }

    private static String fallbackPercentReplacement(String fmt, Object[] args) {
        String out = fmt;
        for (Object a : args) {
            out = out.replaceFirst("%[\\w.#+-]*[a-zA-Z]", Matcher.quoteReplacement(String.valueOf(a)));
        }
        return out;
    }
}
