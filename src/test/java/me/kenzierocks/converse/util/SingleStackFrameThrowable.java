package me.kenzierocks.converse.util;

/**
 * Fakes having a single frame.
 */
public class SingleStackFrameThrowable extends Throwable {

    public SingleStackFrameThrowable(String message,
            StackTraceElement singleFrame) {
        super(message);
        setStackTrace(new StackTraceElement[] { singleFrame });
    }

}
