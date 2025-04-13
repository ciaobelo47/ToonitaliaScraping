package me.mailo.toonitaliascraping;

import me.mailo.log.LogLevel;
import me.mailo.log.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.StringTokenizer;

public class ScrapingCLI {
    static Logger logger = new Logger("JWebScraping");
    static Scanner sc = new Scanner(System.in);
    static FirefoxOptions opt = new FirefoxOptions();
    static WebDriver driver;
    static WebDriverWait wait;

    public static void scrape(String url) {
        logger.log(LogLevel.WARN, "Starting in headless mode...", true);
        opt.addArguments("--headless");
        driver = new FirefoxDriver(opt);
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        try {
            logger.log(LogLevel.INFO, "Trying to open initial page...", true);
            url = "https://toonitalia.green/teen-titans-go/";
            driver.get(url);

            WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("hostlinks")));
            ArrayList<WebElement> tableRows = (ArrayList<WebElement>) table.findElements(By.tagName("tr"));
            tableRows.removeIf(row -> !(row.getText().contains("Teen Titans Go 0")) || row.getText().isEmpty());

            logger.log(LogLevel.DEBUG, "TableRows size: " + tableRows.size(), true);

            for (int i = 0; i < tableRows.size(); i++) {
                logger.log(LogLevel.INFO, i + ": " + tableRows.get(i).findElement(By.tagName("td")).getText(), true);
            }

            logger.log(LogLevel.INFO, "Insert the number of the episode: ", false);
            int choice = sc.nextInt();
            System.out.println();

            logger.log(LogLevel.INFO, "Trying to get the url encrypter link...", true);
            WebElement ep = tableRows.get(choice);
            WebElement videoUrl = ep.findElements(By.tagName("td")).get(1).findElement(By.tagName("a"));

            logger.log(LogLevel.INFO, "Found link: " + videoUrl.getDomAttribute("href"), true);

            getfromUrlEncr(videoUrl.getDomAttribute("href"));

        } catch (NoSuchElementException | URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (driver != null) {
                logger.log(LogLevel.INFO, "Closing the client.", true);
                driver.quit();
            }
        }
    }

    public static void getfromUrlEncr(String url) throws IOException, URISyntaxException, InterruptedException {
        logger.log(LogLevel.INFO, "Trying to open url encrypter link...", true);
        driver.get(url);
        loadCookiesfromFile(driver, "cookies.txt");
        logger.log(LogLevel.INFO, "Refreshing page with loaded cookies...", true);
        driver.navigate().refresh();

        logger.log(LogLevel.INFO, "Identifying button and click on it...", true);
        Thread.sleep(3000);
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("buttok")));
        btn.click();
        logger.log(LogLevel.INFO, "Button Clicked!", true);
        getfromUrlVideo();
    }

    public static void getfromUrlVideo() throws URISyntaxException, IOException {
        logger.log(LogLevel.DEBUG, "Video URL: " + driver.getCurrentUrl(), true);

        WebElement div = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("iframe-container")));
        logger.log(LogLevel.DEBUG, "Video embed URL: " + div.findElement(By.tagName("iframe")).getDomAttribute("src"), true);
        openLink(div.findElement(By.tagName("iframe")).getDomAttribute("src"));
    }

    public static void openLink(String url) throws URISyntaxException, IOException {
        logger.log(LogLevel.INFO, "Opening Browser...", true);
        Desktop desktop = Desktop.getDesktop();
        desktop.browse(new URI(url));
    }

    public static void loadCookiesfromFile(WebDriver drv, String cookiesName) {
        logger.log(LogLevel.INFO, "Trying to load cookies...", true);
        int cookiesAdded = 0;
        int linesSkipped = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Launcher.class.getResourceAsStream(cookiesName), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
