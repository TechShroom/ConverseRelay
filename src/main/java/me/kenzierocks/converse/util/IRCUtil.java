package me.kenzierocks.converse.util;

import java.util.regex.Pattern;

public final class IRCUtil {

    // a to z, digits, and '`^-\[]{}|'
    private static final Pattern NICK_PATTERN =
            Pattern.compile("^[a-zA-Z0-9`^\\-\\\\\\[\\]{}|]+$");
    // Anything that's no spaces is probably good.
    private static final Pattern REAL_NAME_PATTERN =
            Pattern.compile("^[^\\s]+$");

    public static boolean isValidNickName(String val) {
        return NICK_PATTERN.matcher(val).matches();
    }

    public static boolean isValidRealName(String val) {
        return REAL_NAME_PATTERN.matcher(val).matches();
    }

    private IRCUtil() {
    }
}
