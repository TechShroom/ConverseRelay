appender("syserr", ConsoleAppender) {
    target = "System.err"
    encoder(LayoutWrappingEncoder) {
        charset = java.nio.charset.StandardCharsets.UTF_8
        layout = new me.kenzierocks.converse.util.NotTerribleLoggingFormat()
    }
}
root(DEBUG, ["syserr"])