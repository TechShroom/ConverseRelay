appender("syserr", ConsoleAppender) {
    target = "System.err"
    encoder(LayoutWrappingEncoder) {
        charset = java.nio.charset.StandardCharsets.UTF_8
        layout = new me.kenzierocks.converse.util.NotTerribleLoggingFormat(false)
    }
}
appender("syserr.verbose", ConsoleAppender) {
    target = "System.err"
    encoder(LayoutWrappingEncoder) {
        charset = java.nio.charset.StandardCharsets.UTF_8
        layout = new me.kenzierocks.converse.util.NotTerribleLoggingFormat(true)
    }
}
root(DEBUG, ["syserr"])
logger("overrides.std.err", null, ["syserr.verbose"])
