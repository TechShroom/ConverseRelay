package me.kenzierocks.converse.util;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;

public class LoggingEvents {

    private static final Logger LOGGER =
            (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    public static Logger getDefaultLogger() {
        return LOGGER;
    }

    public static ILoggingEvent create(String fqcn, Level level, String message,
            Throwable throwable, Object... argArray) {
        return create(fqcn, LOGGER, level, message, throwable, argArray);
    }

    public static ILoggingEvent create(String fqcn, Logger logger, Level level,
            String message, Throwable throwable, Object... argArray) {
        return new LoggingEvent(fqcn, logger, level, message, throwable,
                argArray);
    }

    private LoggingEvents() {
        throw new AssertionError();
    }

}
