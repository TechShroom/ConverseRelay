package me.kenzierocks.converse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Test;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import me.kenzierocks.converse.util.LoggingEvents;
import me.kenzierocks.converse.util.NotTerribleLoggingFormat;
import me.kenzierocks.converse.util.SingleStackFrameThrowable;
import me.kenzierocks.converse.util.StringSubstitution;

public class NTLoggingFormatTest {

    private static final String MESSAGE = "message";
    private static final String EX_MESSAGE = "exception-message";
    private static final String FQCN = "class";
    private static final Level LEVEL = Level.INFO;
    private static final String LOGGER_NAME =
            LoggingEvents.getDefaultLogger().getName();
    private static final StackTraceElement DEFAULT_FRAME =
            new StackTraceElement(FQCN, "method", "file", 0);

    private static Throwable newDefaultThrowable() {
        return new SingleStackFrameThrowable(EX_MESSAGE, DEFAULT_FRAME);
    }

    private static String formatTimestamp(long timestamp) {
        return NotTerribleLoggingFormat.DATE_FORMATTER.format(ZonedDateTime
                .ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC));
    }

    private final NotTerribleLoggingFormat loggingFormat =
            new NotTerribleLoggingFormat();

    @Test
    public void normalLog() {
        final String expectedFormat =
                "[${date}] [${logger}@${level}] ${message}\n";
        ILoggingEvent event = LoggingEvents.create(FQCN, LEVEL, MESSAGE, null);
        ImmutableMap.Builder<String, Object> repl = ImmutableMap.builder();
        repl.put("date", formatTimestamp(event.getTimeStamp()));
        repl.put("logger", LOGGER_NAME);
        repl.put("level", LEVEL);
        repl.put("message", MESSAGE);

        String expected = StringSubstitution.formatting(expectedFormat)
                .substitute(repl.build());

        String wouldLog = this.loggingFormat.doLayout(event);
        assertEquals(expected, wouldLog);
    }

    @Test
    public void commonExceptionPrinting() {
        final String loggerPreMessageFormat = "[${date}] [${logger}@${level}] ";
        final String expectedFormat = loggerPreMessageFormat + "${message}\n"
                + "${wspace}${exclass}: ${exmessage}\n"
                + "${wspace}    at ${frame}\n";
        Throwable ex = newDefaultThrowable();
        ILoggingEvent event = LoggingEvents.create(FQCN, LEVEL, MESSAGE, ex);
        ImmutableMap.Builder<String, Object> repl = ImmutableMap.builder();
        repl.put("date", formatTimestamp(event.getTimeStamp()));
        repl.put("logger", LOGGER_NAME);
        repl.put("level", LEVEL);

        int wspace = StringSubstitution.formatting(loggerPreMessageFormat)
                .substitute(repl.build()).length();
        String whitespace = Strings.repeat(" ", wspace);
        repl.put("wspace", whitespace);
        repl.put("message", MESSAGE);
        repl.put("exclass", ex.getClass().getName());
        repl.put("exmessage", EX_MESSAGE);
        repl.put("frame", DEFAULT_FRAME);

        String expected = StringSubstitution.formatting(expectedFormat)
                .substitute(repl.build());

        String wouldLog = this.loggingFormat.doLayout(event);
        assertEquals(expected, wouldLog);
    }

    @Test
    public void supressedExceptionPrinting() {
        final String loggerPreMessageFormat = "[${date}] [${logger}@${level}] ";
        final String expectedFormat = loggerPreMessageFormat + "${message}\n"
                + "${wspace}${exclass}: ${exmessage}\n"
                + "${wspace}    at ${frame}\n"
                + "${wspace}    Suppressed: ${exclass}: ${exmessage}\n"
                + "${wspace}        ... 1 more\n";
        Throwable ex = newDefaultThrowable();
        ex.addSuppressed(newDefaultThrowable());
        ILoggingEvent event = LoggingEvents.create(FQCN, LEVEL, MESSAGE, ex);
        ImmutableMap.Builder<String, Object> repl = ImmutableMap.builder();
        repl.put("date", formatTimestamp(event.getTimeStamp()));
        repl.put("logger", LOGGER_NAME);
        repl.put("level", LEVEL);

        int wspace = StringSubstitution.formatting(loggerPreMessageFormat)
                .substitute(repl.build()).length();
        String whitespace = Strings.repeat(" ", wspace);
        repl.put("wspace", whitespace);
        repl.put("message", MESSAGE);
        repl.put("exclass", ex.getClass().getName());
        repl.put("exmessage", EX_MESSAGE);
        repl.put("frame", DEFAULT_FRAME);

        String expected = StringSubstitution.formatting(expectedFormat)
                .substitute(repl.build());

        String wouldLog = this.loggingFormat.doLayout(event);
        assertEquals(expected, wouldLog);
    }

    @Test
    public void causedExceptionPrinting() {
        final String loggerPreMessageFormat = "[${date}] [${logger}@${level}] ";
        final String expectedFormat = loggerPreMessageFormat + "${message}\n"
                + "${wspace}${exclass}: ${exmessage}\n"
                + "${wspace}    at ${frame}\n"
                + "${wspace}Caused by: ${exclass}: ${exmessage}\n"
                + "${wspace}    ... 1 more\n";
        Throwable ex = newDefaultThrowable();
        ex.initCause(newDefaultThrowable());
        ILoggingEvent event = LoggingEvents.create(FQCN, LEVEL, MESSAGE, ex);
        ImmutableMap.Builder<String, Object> repl = ImmutableMap.builder();
        repl.put("date", formatTimestamp(event.getTimeStamp()));
        repl.put("logger", LOGGER_NAME);
        repl.put("level", LEVEL);

        int wspace = StringSubstitution.formatting(loggerPreMessageFormat)
                .substitute(repl.build()).length();
        String whitespace = Strings.repeat(" ", wspace);
        repl.put("wspace", whitespace);
        repl.put("message", MESSAGE);
        repl.put("exclass", ex.getClass().getName());
        repl.put("exmessage", EX_MESSAGE);
        repl.put("frame", DEFAULT_FRAME);

        String expected = StringSubstitution.formatting(expectedFormat)
                .substitute(repl.build());

        String wouldLog = this.loggingFormat.doLayout(event);
        assertEquals(expected, wouldLog);
    }

    @Test
    public void causedAndSuppressedExceptionPrinting() {
        final String loggerPreMessageFormat = "[${date}] [${logger}@${level}] ";
        final String expectedFormat = loggerPreMessageFormat + "${message}\n"
                + "${wspace}${exclass}: ${exmessage}\n"
                + "${wspace}    at ${frame}\n"
                + "${wspace}    Suppressed: ${exclass}: ${exmessage}\n"
                + "${wspace}        ... 1 more\n"
                + "${wspace}Caused by: ${exclass}: ${exmessage}\n"
                + "${wspace}    ... 1 more\n";
        Throwable ex = newDefaultThrowable();
        ex.addSuppressed(newDefaultThrowable());
        ex.initCause(newDefaultThrowable());
        ILoggingEvent event = LoggingEvents.create(FQCN, LEVEL, MESSAGE, ex);
        ImmutableMap.Builder<String, Object> repl = ImmutableMap.builder();
        repl.put("date", formatTimestamp(event.getTimeStamp()));
        repl.put("logger", LOGGER_NAME);
        repl.put("level", LEVEL);

        int wspace = StringSubstitution.formatting(loggerPreMessageFormat)
                .substitute(repl.build()).length();
        String whitespace = Strings.repeat(" ", wspace);
        repl.put("wspace", whitespace);
        repl.put("message", MESSAGE);
        repl.put("exclass", ex.getClass().getName());
        repl.put("exmessage", EX_MESSAGE);
        repl.put("frame", DEFAULT_FRAME);

        String expected = StringSubstitution.formatting(expectedFormat)
                .substitute(repl.build());

        String wouldLog = this.loggingFormat.doLayout(event);
        assertEquals(expected, wouldLog);
    }

    @Test
    public void causedAndSuppressedWithCauseExceptionPrinting() {
        final String loggerPreMessageFormat = "[${date}] [${logger}@${level}] ";
        final String expectedFormat = loggerPreMessageFormat + "${message}\n"
                + "${wspace}${exclass}: ${exmessage}\n"
                + "${wspace}    at ${frame}\n"
                + "${wspace}    Suppressed: ${exclass}: ${exmessage}\n"
                + "${wspace}        ... 1 more\n"
                + "${wspace}    Caused by: ${exclass}: ${exmessage}\n"
                + "${wspace}        ... 1 more\n"
                + "${wspace}Caused by: ${exclass}: ${exmessage}\n"
                + "${wspace}    ... 1 more\n";
        Throwable ex = newDefaultThrowable();
        Throwable sup = newDefaultThrowable();
        sup.initCause(newDefaultThrowable());
        ex.addSuppressed(sup);
        ex.initCause(newDefaultThrowable());
        ILoggingEvent event = LoggingEvents.create(FQCN, LEVEL, MESSAGE, ex);
        ImmutableMap.Builder<String, Object> repl = ImmutableMap.builder();
        repl.put("date", formatTimestamp(event.getTimeStamp()));
        repl.put("logger", LOGGER_NAME);
        repl.put("level", LEVEL);

        int wspace = StringSubstitution.formatting(loggerPreMessageFormat)
                .substitute(repl.build()).length();
        String whitespace = Strings.repeat(" ", wspace);
        repl.put("wspace", whitespace);
        repl.put("message", MESSAGE);
        repl.put("exclass", ex.getClass().getName());
        repl.put("exmessage", EX_MESSAGE);
        repl.put("frame", DEFAULT_FRAME);

        String expected = StringSubstitution.formatting(expectedFormat)
                .substitute(repl.build());

        String wouldLog = this.loggingFormat.doLayout(event);
        assertEquals(expected, wouldLog);
    }

    @Test
    public void circularExceptionPrinting() {
        // Logback doesn't handle this.
        // final String loggerPreMessageFormat = "[${date}] [${logger}@${level}]
        // ";
        // final String expectedFormat = loggerPreMessageFormat + "${message}\n"
        // + "${wspace}${exclass}: ${exmessage}\n"
        // + "${wspace}Caused by:${exclass}: ${exmessage}\n"
        // + "${wspace} ... 1 more\n"
        // + "${wspace}Caused by: [CIRCULAR REFERENCE: ${exclass}:
        // ${exmessage}]\n";
        Throwable ex = newDefaultThrowable();
        Throwable cause = newDefaultThrowable();
        ex.initCause(cause);
        cause.initCause(ex);
        try {
            ILoggingEvent event =
                    LoggingEvents.create(FQCN, LEVEL, MESSAGE, ex);
            // ImmutableMap.Builder<String, Object> repl =
            // ImmutableMap.builder();
            // repl.put("date", formatTimestamp(event.getTimeStamp()));
            // repl.put("logger", LOGGER_NAME);
            // repl.put("level", LEVEL);
            //
            // int wspace =
            // StringSubstitution.formatting(loggerPreMessageFormat)
            // .substitute(repl.build()).length();
            // String whitespace = Strings.repeat(" ", wspace);
            // repl.put("wspace", whitespace);
            // repl.put("message", MESSAGE);
            // repl.put("exclass", ex.getClass().getName());
            // repl.put("exmessage", EX_MESSAGE);
            // repl.put("frame", DEFAULT_FRAME);
            //
            // String expected = StringSubstitution.formatting(expectedFormat)
            // .substitute(repl.build());
            //
            @SuppressWarnings("unused")
            String wouldLog = this.loggingFormat.doLayout(event);
            // assertEquals(expected, wouldLog);
        } catch (StackOverflowError noStack) {
            assertTrue("stack not in logback",
                    String.valueOf(noStack.getStackTrace()[5])
                            .contains("ThrowableProxy"));
        }
    }

}
