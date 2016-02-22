appender("syserr", ConsoleAppender) {
  target = "System.err"
  encoder(PatternLayoutEncoder) {
    pattern = "[%d{dd_MMM_yy-HH:mm:ss}][%logger{1}@%level] %msg%n"
  }
}
root(DEBUG, ["syserr"])