package me.kenzierocks.converse.util;

/**
 * Fakes having a single frame.
 */
public class SingleStackFrameThrowable extends Throwable {

    private static final long serialVersionUID = 7161378420885771825L;

    public SingleStackFrameThrowable(String message, StackTraceElement singleFrame) {
        super(message);
        setStackTrace(new StackTraceElement[] { singleFrame });
    }

}
