package me.kenzierocks.converse.util;

import java.util.regex.Pattern;

public final class IRCUtil {

    // a to z, digits, and '`^-\[]{}|'
    private static final Pattern NICK_PATTERN =
            Pattern.compile("^[a-zA-Z0-9`^\\-\\\\\\[\\]{}|]+$");

    public static boolean isValidNickName(String val) {
        return NICK_PATTERN.matcher(val).matches();
    }

    private IRCUtil() {
    }
}
