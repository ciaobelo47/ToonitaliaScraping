package me.mailo.toonitaliascraping;

import me.mailo.log.LogLevel;
import me.mailo.log.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;

public class ScrapingCLI {
    static Logger logger = Launcher.getLogger();
    static Scanner sc = new Scanner(System.in);
    static FirefoxOptions opt = new FirefoxOptions();
    static WebDriver driver;
    static WebDriverWait wait;
    static boolean headless = true;

    public static void scrape(String url) {
        if (headless) {
            logger.log(LogLevel.WARN, "Starting in headless mode...", true);
            opt.addArguments("--headless");
        }

        driver = new FirefoxDriver(opt);
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        try {
            logger.log(LogLevel.INFO, "Trying to open initial page...", true);
            driver.get(url);

            WebElement titleWE = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("no-border")));
            final String finalSb = getCleanTitle(titleWE);

            WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("hostslinks")));
            ArrayList<WebElement> tableRows = (ArrayList<WebElement>) table.findElements(By.tagName("tr"));
            tableRows.removeIf(row -> !(row.getText().toLowerCase().contains(finalSb.toLowerCase())) || row.getText().isEmpty());

            logger.log(LogLevel.DEBUG, "TableRows size: " + tableRows.size(), true);

            for (int i = 0; i < tableRows.size(); i++) {
                logger.log(LogLevel.INFO, i + ": " + tableRows.get(i).findElement(By.tagName("td")).getText(), true);
            }

            if (HistoryManager.getShowIndex(finalSb) != -1) {
                logger.log(LogLevel.INFO, "The last watched episode of " + finalSb + " was at index: " + HistoryManager.getShowIndex(finalSb), true);
            }

            logger.log(LogLevel.INFO, "Insert the number of the episode: ", false);
            int choice = sc.nextInt();
            System.out.print('\n'); // UwU

            logger.log(LogLevel.INFO, "Trying to get the url encrypter link...", true);
            WebElement ep = tableRows.get(choice);
            HistoryManager.saveLastChoice(finalSb, choice);
            WebElement videoUrl = ep.findElements(By.tagName("td")).get(1).findElement(By.tagName("a"));

            logger.log(LogLevel.INFO, "Found link: " + videoUrl.getDomAttribute("href"), true);

            getfromUrlEncr(videoUrl.getDomAttribute("href"));

        } catch (NoSuchElementException e) {
            throw new RuntimeException(e);
        } finally {
            if (driver != null) {
                logger.log(LogLevel.INFO, "Closing the client.", true);
                driver.quit();
            }
        }
    }

    private static String getCleanTitle(WebElement titleWE) {
        String rawTitle = titleWE.findElement(By.xpath("tbody/tr/td")).getText().replaceAll("-", " ").replaceAll("[^\\w\\s]", "");
        String[] words = rawTitle.split("\\s+");
        String sb = "";
        for (int i = 1; i < words.length; i++) {
            sb += words[i];
            sb += " ";
        }
        sb = sb.trim();

        logger.log(LogLevel.DEBUG, sb, true);
        return sb;
    }

    private static void getfromUrlEncr(String url) {
        try {
            logger.log(LogLevel.INFO, "Trying to open url encrypter link...", true);
            driver.get(url);
            loadCookiesfromFile(driver);
            logger.log(LogLevel.INFO, "Refreshing page with loaded cookies...", true);
            driver.navigate().refresh();

            logger.log(LogLevel.INFO, "Identifying button and click on it...", true);
            WebElement a = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("butok"))).findElement(By.xpath("./.."));
            if (!a.getTagName().equals("a")) {
                throw new RuntimeException("ah bo");
            }

            String videoUrl = a.getDomAttribute("href");
            logger.log(LogLevel.DEBUG, "HREF: " + a.getDomAttribute("href"), true);

            getfromUrlVideo(videoUrl);
        } catch (TimeoutException | IOException e) {
            renewCookies(driver.getCurrentUrl());
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }

    private static void getfromUrlVideo(String url) throws URISyntaxException, IOException {
        logger.log(LogLevel.DEBUG, "Video URL: " + url, true);

        driver.get(url);

        WebElement div = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("iframes-container")));
        logger.log(LogLevel.DEBUG, "Video embed URL: " + div.findElement(By.tagName("iframe")).getDomAttribute("src"), true);
        openLink(div.findElement(By.tagName("iframe")).getDomAttribute("src"));
    }

    private static void openLink(String url) throws URISyntaxException, IOException {
        logger.log(LogLevel.INFO, "Opening Browser...", true);
        Desktop desktop = Desktop.getDesktop();
        desktop.browse(new URI(url));
    }

    private static void loadCookiesfromFile(WebDriver drv) throws IOException {
        logger.log(LogLevel.INFO, "Trying to load cookies...", true);
        int cookiesAdded = 0;
        int linesSkipped = 0;

        BufferedReader ps = new BufferedReader(new FileReader("cookies.txt"));
        String line;
        while ((line = ps.readLine()) != null) {
            if (line.trim().isEmpty() || line.startsWith("#")) {
                linesSkipped++;
                continue;
            }

            StringTokenizer tokenizer = new StringTokenizer(line, "\t");
            if (tokenizer.countTokens() < 7) {
                linesSkipped++;
                continue;
            }

            try {
                String domain = tokenizer.nextToken();
                String flag = tokenizer.nextToken();
                String path = tokenizer.nextToken();
                boolean isSecure = Boolean.parseBoolean(tokenizer.nextToken());
                long expiryTimestampSeconds = Long.parseLong(tokenizer.nextToken());
                String name = tokenizer.nextToken();
                String value = tokenizer.nextToken();

                boolean isHttpOnly = false;
                if (domain.startsWith("#HttpOnly_")) {
                    isHttpOnly = true;
                    domain = domain.substring("#HttpOnly_".length());
                }

                Date expiry = null;
                if (expiryTimestampSeconds != 0) {
                    expiry = new Date(expiryTimestampSeconds * 1000);
                }

                Cookie.Builder cookieBuilder = new Cookie.Builder(name, value)
                        .domain(domain)
                        .path(path)
                        .expiresOn(expiry)
                        .isSecure(isSecure)
                        .isHttpOnly(isHttpOnly);

                Cookie cookie = cookieBuilder.build();

                drv.manage().addCookie(cookie);
                cookiesAdded++;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        logger.log(LogLevel.DEBUG, "Loading completed! Cookies loaded: " + cookiesAdded + " ; Lines Skipped/Wrong: " + linesSkipped, true);
    }

    private static void renewCookies(String urltorenew) {
        logger.log(LogLevel.WARN, "Your cookies have expired. Trying to renew them...", true);

        FirefoxDriver tmpDrv = new FirefoxDriver();
        WebDriverWait tmpWait = new WebDriverWait(tmpDrv, Duration.ofSeconds(30));

        try {
            tmpDrv.manage().window().minimize();
            logger.log(LogLevel.WARN, "A page will open up in 5 seconds, you have to do the verification under 30 seconds", true);
            Thread.sleep(5000);

            tmpDrv.manage().window().maximize();

            tmpDrv.get(urltorenew);
            WebElement tmpA = tmpWait.until(ExpectedConditions.presenceOfElementLocated(By.id("butok")));
            if (tmpA.isDisplayed()) {
                try {
                    logger.log(LogLevel.INFO, "Collected new cookies! Formatting and saving them...", true);

                    formatCookies(tmpDrv.manage().getCookies(), "cookies.txt");

                    Thread.sleep(1000);

                    tmpDrv.close();
                    getfromUrlEncr(urltorenew);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (TimeoutException e) {
            logger.log(LogLevel.ERROR, "Failed to renew cookies, the app will close.", true);
            tmpDrv.close();
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }

    private static void formatCookies(Set<Cookie> cookies, String filepath) {
        try {
            PrintWriter pw = new PrintWriter(filepath);
            pw.write("# Netscape HTTP Cookie File\n");
            pw.write("# This is a generated file! Do not edit.\n");
            pw.write("\n");

            for (Cookie c : cookies) {
                String domain = c.getDomain();
                boolean includeSubdomains = domain.startsWith(".");
                String path = c.getPath();
                boolean isSecure = c.isSecure();
                long expiryTimestampSeconds = (c.getExpiry() != null) ? c.getExpiry().getTime() / 1000 : 0;
                String name = c.getName();
                String value = c.getValue();
                boolean isHttpOnly = c.isHttpOnly();

                // Formato Netscape: domain flag path secure expiry name value
                pw.write(domain);
                pw.write("\t");
                pw.write(includeSubdomains ? "TRUE" : "FALSE");
                pw.write("\t");
                pw.write(path);
                pw.write("\t");
                pw.write(isSecure ? "TRUE" : "FALSE");
                pw.write("\t");
                pw.write(String.valueOf(expiryTimestampSeconds));
                pw.write("\t");
                pw.write(name);
                pw.write("\t");
                pw.write(value);
                pw.write("\n");
            }

            pw.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
