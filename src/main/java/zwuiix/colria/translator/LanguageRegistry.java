package zwuiix.colria.translator;

import zwuiix.colria.Loader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class LanguageRegistry {
    private static LanguageRegistry instance = new LanguageRegistry();

    public static LanguageRegistry getInstance() {
        return instance;
    }

    private final LinkedHashMap<Language, LanguageFile> languages = new LinkedHashMap<>();

    public LinkedHashMap<Language, LanguageFile> getLanguages() {
        return languages;
    }

    public LanguageFile getLanguage(Language language) {
        return languages.get(language);
    }

    public void add(Language language, InputStream stream) {
        languages.put(language, new LanguageFile(stream));
    }

    public void invoke(Loader loader) {
        add(Language.FR, loader.getResource("languages/fr.ini"));
    }
}
