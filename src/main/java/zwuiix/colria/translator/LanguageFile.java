package zwuiix.colria.translator;

import cn.nukkit.utils.Config;

import java.io.InputStream;
import java.util.Locale;

public class LanguageFile {
    private final Config config;
    private final Locale locale;

    public LanguageFile(InputStream stream) {
        Config cfg = new Config(Config.PROPERTIES);
        try {
            cfg.load(stream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.config = cfg;
        this.locale = parseLocaleFromConfig(cfg);
    }

    public String get(String key) {
        var k = key.replaceAll("_", ".").toLowerCase();
        return config.getString(k, k);
    }

    public Locale getLocale() {
        return locale != null ? locale : Locale.getDefault();
    }

    private static Locale parseLocaleFromConfig(Config cfg) {
        try {
            String code = cfg.getString("code", "").trim();
            if (!code.isEmpty()) {
                return Locale.forLanguageTag(code.replace('_', '-'));
            }
        } catch (Throwable ignored) {}
        return Locale.getDefault();
    }
}
