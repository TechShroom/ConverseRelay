package me.kenzierocks.converse.util;

import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PrimitiveIterator;
import java.util.function.Function;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

/**
 * Converts a string to another string using substitutions defined in a map.
 */
public final class StringSubstitution {

    /**
     * Cleans up generic hell.
     */
    private interface FormatFunction
            extends Function<Map<String, String>, String> {
    }

    public static StringSubstitution formatting(String format) {
        List<FormatFunction> parts = breakIntoParts(format);
        return new StringSubstitution(parts);
    }

    private static List<FormatFunction> breakIntoParts(String format) {
        ImmutableList.Builder<FormatFunction> parts = ImmutableList.builder();
        boolean seenDollar = false;
        boolean seenBackslash = false;
        StringBuilder notKeySoFar = new StringBuilder();
        StringBuilder keySoFar = null;
        for (PrimitiveIterator.OfInt iter = format.codePoints().iterator(); iter
                .hasNext();) {
            int point = iter.nextInt();
            StringBuilder target =
                    MoreObjects.firstNonNull(keySoFar, notKeySoFar);
            if (point == '\\') {
                if (seenBackslash) {
                    target.append('\\');
                }
                seenBackslash = !seenBackslash;
                continue;
            }
            try {
                if (!seenBackslash && point == '$' && keySoFar == null) {
                    seenDollar = true;
                    continue;
                }
                try {
                    if (!seenBackslash && point == '{' && seenDollar
                            && keySoFar == null) {
                        // NEW PART HERE
                        createTextPart(notKeySoFar).ifPresent(parts::add);
                        notKeySoFar.setLength(0);
                        keySoFar = new StringBuilder();
                        continue;
                    }
                    if (!seenBackslash && point == '}' && keySoFar != null) {
                        // NEW PART HERE
                        parts.add(createKeyPart(keySoFar));
                        keySoFar = null;
                        continue;
                    }
                    if (seenBackslash) {
                        if (seenDollar) {
                            target.append('$');
                        }
                        switch (point) {
                            case '$':
                            case '{':
                            case '}':
                                break;
                            default:
                                target.append('\\');
                        }
                    }
                    if (Character.isBmpCodePoint(point)) {
                        target.append((char) point);
                    } else {
                        target.append(Character.toChars(point));
                    }
                } finally {
                    seenDollar = false;
                }
            } finally {
                seenBackslash = false;
            }
        }
        if (notKeySoFar.length() > 0) {
            assert keySoFar == null;
            // NEW PART HERE
            createTextPart(notKeySoFar).ifPresent(parts::add);
        }
        if (keySoFar != null) {
            // Missing end '}' -> not a key
            // NEW PART HERE
            createTextPart(new StringBuilder("${").append(keySoFar))
                    .ifPresent(parts::add);
        }
        return parts.build();
    }

    private static Optional<FormatFunction>
            createTextPart(StringBuilder notKeySoFar) {
        if (notKeySoFar.length() == 0) {
            return Optional.empty();
        }
        String text = notKeySoFar.toString();
        return Optional.of(map -> text);
    }

    private static FormatFunction createKeyPart(StringBuilder keySoFar) {
        checkState(keySoFar.length() > 0, "must have a key present");
        String key = keySoFar.toString();
        return map -> {
            String val = map.get(key);
            checkState(val != null, "Missing required key " + key);
            return val;
        };
    }

    private final List<FormatFunction> parts;

    private StringSubstitution(Collection<FormatFunction> parts) {
        this.parts = ImmutableList.copyOf(parts);
    }

    public String substitute(Map<String, ?> replacements) {
        if (this.parts.isEmpty()) {
            return "";
        }
        Map<String, String> replString =
                Maps.transformValues(replacements, String::valueOf);
        StringBuilder builder = new StringBuilder();
        for (FormatFunction formatFunction : this.parts) {
            builder.append(formatFunction.apply(replString));
        }
        return builder.toString();
    }

}
