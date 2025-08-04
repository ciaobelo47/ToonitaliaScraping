package me.mailo.toonitaliascraping;

import me.mailo.log.LogLevel;
import me.mailo.log.Logger;
import me.mailo.toonitaliascraping.utils.ConfirmBox;
import me.mailo.toonitaliascraping.utils.InfoBox;

public class Launcher {
    private static final Logger logger = new Logger("ToonitaliaScraping");
    private static String url;

    public static void main(String[] args) {
        InfoBox.remindShow();
        url = ConfirmBox.collectUrl();

        if (args.length == 1) {
            switchArgs(args[0], true);
        } else if (args.length == 2) {
            switchArgs(args[0], false);
            switchArgs(args[1], true);
        } else {
            ScrapingCLI.scrape(url);
        }
    }

    public static void switchArgs(String arg, boolean run) {
        switch (arg) {
            case "no-headless":
                ScrapingCLI.headless = false;
                break;
            case "enable-debug":
                logger.enableDebug = true;
                logger.log(LogLevel.DEBUG, "Starting with debug log enabled!", true);
                break;
            default:
                logger.log(LogLevel.ERROR, "Invalid Argument!", true);
                System.exit(404);
        }

        if (run) {
            ScrapingCLI.scrape(url);
        }
    }

    public static Logger getLogger() {
        return logger;
    }
}
