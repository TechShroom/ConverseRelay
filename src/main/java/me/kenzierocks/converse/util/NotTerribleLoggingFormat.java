package me.kenzierocks.converse.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;

import ch.qos.logback.classic.pattern.Abbreviator;
import ch.qos.logback.classic.pattern.ClassOfCallerConverter;
import ch.qos.logback.classic.pattern.TargetLengthBasedClassNameAbbreviator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;

public class NotTerribleLoggingFormat extends LayoutBase<ILoggingEvent> {

    private static final String DATE_FORMAT = "dd_MMM_yy-HH:mm:ss";
    @VisibleForTesting
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final Abbreviator LOGGER_ABBREVIATOR = new TargetLengthBasedClassNameAbbreviator(1);
    private static final ClassOfCallerConverter CLASS_CONVERTER = new ClassOfCallerConverter();

    /**
     * Modeled after {@link Exception#printStackTrace()}. Doesn't use prefix for
     * enclosed stack traces, instead just add to index.
     */
    private static final class PrintException {

        /** Caption for labeling causative exception stack traces */
        private static final String CAUSE_CAPTION = "Caused by: ";

        /** Caption for labeling suppressed exception stack traces */
        private static final String SUPPRESSED_CAPTION = "Suppressed: ";
        private static final int INDENT_INCREASE = 4;
        private static final String STACK_INDENT = Strings.repeat(" ", 4);

        private final StringBuilder exceptionBuilder = new StringBuilder();
        private final StringBuilder targetBuilder;
        private final IThrowableProxy throwable;
        private final String indentString;
        private final Set<IThrowableProxy> dejaVu = Collections.newSetFromMap(new IdentityHashMap<>());
        private final String caption;

        /**
         * Outward-facing constructor, for building conveniently.
         */
        private PrintException(StringBuilder builder, IThrowableProxy throwable, int indent) {
            this(builder, throwable, indent, "", Collections.emptySet());
        }

        /**
         * Internal state constructor.
         */
        private PrintException(StringBuilder builder, IThrowableProxy throwable, int currentIndent, String caption,
                Set<IThrowableProxy> dejaVu) {
            this.targetBuilder = builder;
            this.throwable = throwable;
            this.indentString = Strings.repeat(" ", currentIndent);
            this.dejaVu.addAll(dejaVu);
            this.caption = caption;
        }

        private void emit() {
            this.exceptionBuilder.setLength(0);
            if (this.dejaVu.contains(this.throwable)) {
                emitCircularReference();
            } else {
                this.dejaVu.add(this.throwable);
                // This code is all similar to printEnclosedStackTrace
                // I've opted to merge the two very similar paths rather than
                // deal with two states
                emitHeader();
                emitMessage();
                emitNewline();

                StackTraceElementProxy[] stack = this.throwable.getStackTraceElementProxyArray();
                int framesInCommon = this.throwable.getCommonFrames();
                int framesToPrint = stack.length - framesInCommon;
                for (int i = 0; i < framesToPrint; i++) {
                    emitStackTrace(stack[i]);
                    emitNewline();
                }
                if (framesInCommon > 0) {
                    emitFramesInCommon(framesInCommon);
                    emitNewline();
                }

                for (IThrowableProxy se : this.throwable.getSuppressed()) {
                    emitSuppressed(se);
                    emitNewline();
                }

                IThrowableProxy cause = this.throwable.getCause();
                if (cause != null) {
                    emitCause(cause);
                    emitNewline();
                }
            }

            emitToReal();
        }

        private void emitHeader() {
            this.exceptionBuilder.append(this.caption);
        }

        private void emitFramesInCommon(int framesInCommon) {
            emitStackIndent();
            this.exceptionBuilder.append("... ").append(framesInCommon).append(" more");
        }

        private void emitCircularReference() {
            this.exceptionBuilder.append("[CIRCULAR REFERENCE: ");
            emitMessage();
            this.exceptionBuilder.append(']');
        }

        private void emitSuppressed(IThrowableProxy se) {
            new PrintException(this.exceptionBuilder, se, INDENT_INCREASE, SUPPRESSED_CAPTION, this.dejaVu).emit();
        }

        private void emitCause(IThrowableProxy se) {
            new PrintException(this.exceptionBuilder, se, 0, CAUSE_CAPTION, this.dejaVu).emit();
        }

        private void emitToReal() {
            String stuff = this.exceptionBuilder.toString();
            List<String> lines = FluentIterable.from(Splitter.on('\n').split(stuff)).transform(line -> {
                if (line.trim().isEmpty()) {
                    return "";
                } else {
                    return this.indentString.concat(line);
                }
            }).toList();
            for (Iterator<String> iterator = lines.iterator(); iterator.hasNext();) {
                String next = iterator.next();
                this.targetBuilder.append(next);
                if (!next.isEmpty() && iterator.hasNext()) {
                    this.targetBuilder.append(CoreConstants.LINE_SEPARATOR);
                }
            }
        }

        private void emitMessage() {
            this.exceptionBuilder.append(this.throwable.getClassName());
            if (this.throwable.getMessage() != null) {
                this.exceptionBuilder.append(": ").append(this.throwable.getMessage());
            }
        }

        private void emitStackTrace(StackTraceElementProxy trace) {
            emitStackIndent();
            this.exceptionBuilder.append(trace);
        }

        private void emitStackIndent() {
            this.exceptionBuilder.append(STACK_INDENT);
        }

        private void emitNewline() {
            this.exceptionBuilder.append(CoreConstants.LINE_SEPARATOR);
        }

    }

    private final boolean verbose;

    public NotTerribleLoggingFormat(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        StringBuilder buf = new StringBuilder();
        // Date
        buf.append('[').append(DATE_FORMATTER.format(getUtcTime(event))).append(']');
        buf.append(' ');
        // Logger
        buf.append('[').append(LOGGER_ABBREVIATOR.abbreviate(event.getLoggerName()));
        // Level
        buf.append('@').append(event.getLevel()).append(']');
        buf.append(' ');
        // Verbosity
        if (this.verbose) {
            // Class + method + line number data
            buf.append('[').append(CLASS_CONVERTER.convert(event)).append(']');
            buf.append(' ');
        }
        // Check that there's no weird stuff going on.
        assert buf.indexOf("\n") == -1;
        // Capture the indent at which the message is printed, for exceptions
        int messageIndent = buf.length();
        // Message
        buf.append(event.getFormattedMessage()).append(CoreConstants.LINE_SEPARATOR);
        IThrowableProxy throwable = event.getThrowableProxy();
        if (throwable != null) {
            new PrintException(buf, throwable, messageIndent).emit();
        }
        return buf.toString();
    }

    private TemporalAccessor getUtcTime(ILoggingEvent event) {
        Instant timestamp = Instant.ofEpochMilli(event.getTimeStamp());
        ZonedDateTime utc = ZonedDateTime.ofInstant(timestamp, ZoneOffset.UTC);
        return utc;
    }

}
