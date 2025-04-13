package me.mailo.log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    public String name;

    public Logger(String name) {
        this.name = name;
    }

    public void log(int level, String msg, boolean newLine) {
        String datePattern = "dd/MM/yyyy - HH:mm";
        SimpleDateFormat format = new SimpleDateFormat(datePattern);
        String time = format.format(new Date());

        if (newLine) {
            switch (level) {
                case 0 -> System.out.printf("%s[%s] [%s] %s\n", ConsoleColors.GREEN_BRIGHT, time, name, msg);
                case 1 -> System.out.printf("%s[%s] [%s] %s\n", ConsoleColors.CYAN, time, name, msg);
                case 2 -> System.out.printf("%s[%s] [%s] %s\n", ConsoleColors.YELLOW, time, name, msg);
                case 3 -> System.out.printf("%s[%s] [%s] %s\n", ConsoleColors.RED, time, name, msg);
            }
        } else {
            switch (level) {
                case 0 -> System.out.printf("%s[%s] [%s] %s", ConsoleColors.GREEN_BRIGHT, time, name, msg);
                case 1 -> System.out.printf("%s[%s] [%s] %s", ConsoleColors.CYAN, time, name, msg);
                case 2 -> System.out.printf("%s[%s] [%s] %s", ConsoleColors.YELLOW, time, name, msg);
                case 3 -> System.out.printf("%s[%s] [%s] %s", ConsoleColors.RED, time, name, msg);
            }
        }
    }

    public void log(LogLevel level, String msg, boolean newLine) {
        switch (level) {
            case INFO -> log(0, msg, newLine);
            case DEBUG -> log(1, msg, newLine);
            case WARN -> log(2, msg, newLine);
            case ERROR -> log(3, msg, newLine);
        }
    }

    public void log(int level, int msg, boolean newLine) {
        log(level, String.valueOf(msg), newLine);
    }

    public void log(LogLevel level, int msg, boolean newLine) {
        log(level, String.valueOf(msg), newLine);
    }

    public void log(int level, double msg, boolean newLine) {
        log(level, String.valueOf(msg), newLine);
    }

    public void log(LogLevel level, double msg, boolean newLine) {
        log(level, String.valueOf(msg), newLine);
    }

    public void log(int level, float msg, boolean newLine) {
        log(level, String.valueOf(msg), newLine);
    }

    public void log(LogLevel level, float msg, boolean newLine) {
        log(level, String.valueOf(msg), newLine);
    }

    public void log(int level, long msg, boolean newLine) {
        log(level, String.valueOf(msg), newLine);
    }

    public void log(LogLevel level, long msg, boolean newLine) {
        log(level, String.valueOf(msg), newLine);
    }
}
