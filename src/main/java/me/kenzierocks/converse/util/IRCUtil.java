package me.kenzierocks.converse.util;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class IRCUtil {

    // a to z, digits, and '`^-\[]{}|'
    private static final Predicate<String> NICK_PATTERN =
            Pattern.compile("^[a-zA-Z0-9`^\\-\\\\\\[\\]{}|]+$").asPredicate();
    // Anything is probably good.
    private static final Predicate<String> REAL_NAME_PATTERN = s -> true;

    public static boolean isValidNickName(String val) {
        return NICK_PATTERN.test(val);
    }

    public static boolean isValidRealName(String val) {
        return REAL_NAME_PATTERN.test(val);
    }

    private IRCUtil() {
    }
}
