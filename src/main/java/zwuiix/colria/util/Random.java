package zwuiix.colria.util;

import java.security.SecureRandom;

public class Random {
    private static final String ALPHABET = "abcdefghjkmnpqrstuvwxyz23456789";
    private static final SecureRandom RNG = new SecureRandom();

    public static String utf8(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(ALPHABET.charAt(RNG.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
