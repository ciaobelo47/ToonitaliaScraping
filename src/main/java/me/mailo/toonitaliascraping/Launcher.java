package me.mailo.toonitaliascraping;

import me.mailo.log.LogLevel;
import me.mailo.log.Logger;

public class Launcher {

    private static final Logger logger = new Logger("ToonitaliaScraping");

    public static void main(String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "no-headless":
                    ScrapingCLI.headless = false;
                    ScrapingCLI.scrape("https://toonitalia.green/teen-titans-go/");
                    break;
                default:
                    logger.log(LogLevel.ERROR, "Invalid Argument!", true);
                    System.exit(-1);
            }
        } else {
            ScrapingCLI.scrape("https://toonitalia.green/teen-titans-go/");
        }
    }

    public static Logger getLogger() {
        return logger;
    }
}
