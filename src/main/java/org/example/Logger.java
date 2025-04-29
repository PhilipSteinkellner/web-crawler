package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Logger() {
    }

    public static Logger getInstance() {
        return InstanceHolder.instance;
    }

    public void log(Level level, String message, Object... args) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String formattedMessage = String.format(message, args);
        System.out.printf("[%s] [%s]: %s%n", timestamp, level.name(), formattedMessage);
    }

    public void debug(String message, Object... args) {
        log(Level.DEBUG, message, args);
    }

    public void info(String message, Object... args) {
        log(Level.INFO, message, args);
    }

    public void warn(String message, Object... args) {
        log(Level.WARN, message, args);
    }

    public void error(String message, Object... args) {
        log(Level.ERROR, message, args);
    }

    public enum Level {
        DEBUG, INFO, WARN, ERROR
    }

    private static final class InstanceHolder {
        private static final Logger instance = new Logger();
    }
}
