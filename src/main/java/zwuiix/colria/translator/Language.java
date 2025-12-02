package zwuiix.colria.translator;

import cn.nukkit.lang.LangCode;

public enum Language {
    FR;

    public LanguageFile getFile() {
        return LanguageRegistry.getInstance().getLanguage(this);
    }

    public static Language fromLangCode(LangCode langCode) {
        switch (langCode) {
            default -> { return FR; }
        }
    }
}
