package zwuiix.colria.util;

import java.util.regex.Pattern;

public final class Regex {
    private static boolean m(Pattern p, String s) {
        if (s == null) return false;
        String x = s.trim();
        if (x.isEmpty()) return false;
        return p.matcher(x).matches();
    }

    public static final Pattern UINT = Pattern.compile("^\\d+$");
    public static final Pattern INT  = Pattern.compile("^[+-]?\\d+$");

    public static final Pattern DECIMAL = Pattern.compile(
            "^[+-]?(?:\\d+\\.\\d*|\\d*\\.\\d+|\\d+)(?:[eE][+-]?\\d+)?$"
    );

    public static final Pattern FLOAT_STRICT = Pattern.compile(
            "^[+-]?(?:(?:\\d+\\.\\d*|\\d*\\.\\d+)(?:[eE][+-]?\\d+)?|\\d+(?:[eE][+-]?\\d+))$"
    );

    public static final Pattern NON_EMPTY = Pattern.compile(".*\\S.*", Pattern.DOTALL);

    public static final Pattern EMAIL = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,63}$",
            Pattern.CASE_INSENSITIVE
    );

    public static final Pattern DOMAIN = Pattern.compile(
            "^(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,63}$"
    );

    public static final Pattern URL = Pattern.compile(
            "^(?:(?:https?|ftp)://)" +                                  // scheme
                    "(?:\\S+(?::\\S*)?@)?" +                                     // user:pass@
                    "(?:" +
                    "(?:[A-Za-z0-9-]+\\.)+[A-Za-z]{2,63}" +                  // domain
                    "|\\d{1,3}(?:\\.\\d{1,3}){3}" +                          // IPv4
                    "|\\[[0-9A-Fa-f:]+\\]" +                                 // [IPv6]
                    ")" +
                    "(?::\\d{2,5})?" +                                           // :port
                    "(?:/[^\\s]*)?$"                                             // /path...
    );

    public static final Pattern UUID = Pattern.compile(
            "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$"
    );
    public static final Pattern UUID_V4 = Pattern.compile(
            "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-4[0-9A-Fa-f]{3}-[89ABab][0-9A-Fa-f]{3}-[0-9A-Fa-f]{12}$"
    );

    public static final Pattern IPV4 = Pattern.compile(
            "^(?:(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}" +
                    "(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$"
    );

    public static final Pattern IPV6 = Pattern.compile(
            "^(?:" +
                    "(?:[0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4}|" +          // 1:2:3:4:5:6:7:8
                    "(?:[0-9A-Fa-f]{1,4}:){1,7}:|" +                       // 1::    à    1:2:3:4:5:6:7::
                    "(?:[0-9A-Fa-f]{1,4}:){1,6}:[0-9A-Fa-f]{1,4}|" +
                    "(?:[0-9A-Fa-f]{1,4}:){1,5}(?::[0-9A-Fa-f]{1,4}){1,2}|" +
                    "(?:[0-9A-Fa-f]{1,4}:){1,4}(?::[0-9A-Fa-f]{1,4}){1,3}|" +
                    "(?:[0-9A-Fa-f]{1,4}:){1,3}(?::[0-9A-Fa-f]{1,4}){1,4}|" +
                    "(?:[0-9A-Fa-f]{1,4}:){1,2}(?::[0-9A-Fa-f]{1,4}){1,5}|" +
                    "[0-9A-Fa-f]{1,4}:(?:(?::[0-9A-Fa-f]{1,4}){1,6})|" +
                    ":(?:(?::[0-9A-Fa-f]{1,4}){1,7}|:)" +
                    ")$"
    );

    public static final Pattern HEX_COLOR = Pattern.compile(
            "^#?(?:[A-Fa-f0-9]{3}|[A-Fa-f0-9]{4}|[A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$"
    );

    public static final Pattern BASE64 = Pattern.compile(
            "^(?:[A-Za-z0-9+/]{4})*" +
                    "(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$"
    );

    public static final Pattern ALPHA = Pattern.compile("^[\\p{L}]+$");
    public static final Pattern ALNUM = Pattern.compile("^[\\p{L}\\p{N}]+$");
    public static final Pattern SLUG  = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    public static final Pattern BOOLEAN = Pattern.compile(
            "^(?:true|false|1|0|yes|no|on|off)$", Pattern.CASE_INSENSITIVE
    );

    public static final Pattern ISO_DATE = Pattern.compile(
            "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$"
    );
    public static final Pattern ISO_TIME = Pattern.compile(
            "^(?:[01]\\d|2[0-3]):[0-5]\\d(?::[0-5]\\d(?:\\.\\d{1,9})?)?(?:Z|[+-](?:[01]\\d|2[0-3]):?[0-5]\\d)?$"
    );
    public static final Pattern ISO_DATETIME = Pattern.compile(
            "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])T" +
                    "(?:[01]\\d|2[0-3]):[0-5]\\d(?::[0-5]\\d(?:\\.\\d{1,9})?)?" +
                    "(?:Z|[+-](?:[01]\\d|2[0-3]):?[0-5]\\d)?$"
    );

    public static final Pattern MC_BEDROCK_USERNAME = Pattern.compile(
            "^(?! )(?!.* {2})[A-Za-z0-9_ ]{1,16}(?<! )$"
    );

    public static boolean isUint(String s)          { return m(UINT, s); }
    public static boolean isInt(String s)           { return m(INT, s); }
    public static boolean isDecimal(String s)       { return m(DECIMAL, s); }
    public static boolean isFloatStrict(String s)   { return m(FLOAT_STRICT, s); }
    public static boolean isNonEmpty(String s)      { return m(NON_EMPTY, s); }
    public static boolean isEmail(String s)         { return m(EMAIL, s); }
    public static boolean isDomain(String s)        { return m(DOMAIN, s); }
    public static boolean isUrl(String s)           { return m(URL, s); }
    public static boolean isUUID(String s)          { return m(UUID, s); }
    public static boolean isUUIDv4(String s)        { return m(UUID_V4, s); }
    public static boolean isIPv4(String s)          { return m(IPV4, s); }
    public static boolean isIPv6(String s)          { return m(IPV6, s); }
    public static boolean isIP(String s)            { return isIPv4(s) || isIPv6(s); }
    public static boolean isHexColor(String s)      { return m(HEX_COLOR, s); }
    public static boolean isBase64(String s)        { return m(BASE64, s); }
    public static boolean isAlpha(String s)         { return m(ALPHA, s); }
    public static boolean isAlnum(String s)         { return m(ALNUM, s); }
    public static boolean isSlug(String s)          { return m(SLUG, s); }
    public static boolean isBooleanWord(String s)   { return m(BOOLEAN, s); }
    public static boolean isIsoDate(String s)       { return m(ISO_DATE, s); }
    public static boolean isIsoTime(String s)       { return m(ISO_TIME, s); }
    public static boolean isIsoDateTime(String s)   { return m(ISO_DATETIME, s); }
    public static boolean isBedrockUsername(String s) { return m(MC_BEDROCK_USERNAME, s); }
}
