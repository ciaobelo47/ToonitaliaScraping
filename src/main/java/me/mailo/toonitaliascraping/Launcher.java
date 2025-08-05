package me.mailo.toonitaliascraping;

import me.mailo.log.LogLevel;
import me.mailo.log.Logger;
import me.mailo.toonitaliascraping.utils.ConfirmBox;
import me.mailo.toonitaliascraping.utils.Downloader;
import me.mailo.toonitaliascraping.utils.InfoBox;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;

public class Launcher {
    private static String version = "1.4-BETA";
    private static final Logger logger = new Logger("ToonitaliaScraping");
    private static String url;

    public static void main(String[] args) {
        if (args.length == 1) {
            switchArgs(args[0]);
        } else if (args.length == 2) {
            switchArgs(args[0]);
            switchArgs(args[1]);
        }

        checkVersion();

        String tmp = InfoBox.remindShow();
        if (!tmp.equals("-1")) {
            url = tmp;
        } else {
            url = ConfirmBox.collectUrl();
        }

        ScrapingCLI.scrape(url);
    }

    public static Logger getLogger() {
        return logger;
    }

    private static void checkVersion() {
        try {
            String gitVersion = "";

            InputStream pom = new URL("https://raw.githubusercontent.com/ciaobelo47/ToonitaliaScraping/refs/heads/main/pom.xml").openStream();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(pom);
            doc.getDocumentElement().normalize();

            NodeList projects = doc.getElementsByTagName("project");
            if (projects.getLength() > 0) {
                Element project = (Element) projects.item(0);

                NodeList versions = project.getElementsByTagName("version");

                if (versions.getLength() > 0) {
                    gitVersion = versions.item(0).getTextContent();
                } else {
                    logger.log(LogLevel.ERROR, "Tag version NOT found. Please report this error", true);
                    logger.log(LogLevel.WARN, "Check Version Failed", true);
                    return;
                }
            } else {
                logger.log(LogLevel.ERROR, "Tag project NOT found. Please report this error", true);
                logger.log(LogLevel.WARN, "Check Version Failed", true);
                return;
            }

            if (!gitVersion.equals(version)) {
                logger.log(LogLevel.INFO, "Found new version on github: " + gitVersion + ". Updating...", true);
                Downloader.update(gitVersion);
            } else {
                return;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void switchArgs(String arg) {
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
    }
}
