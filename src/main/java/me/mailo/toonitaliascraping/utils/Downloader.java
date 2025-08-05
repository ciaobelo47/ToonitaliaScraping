package me.mailo.toonitaliascraping.utils;

import me.mailo.log.LogLevel;
import me.mailo.toonitaliascraping.Launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class Downloader {
    private static int updateTries = 0;

    public static void update(String newVersion) throws InterruptedException {
        try {
            File downloadedFile = new File("ToonitaliaScraping-" + newVersion + "-jar-with-dependencies.jar");
            URL downloadUrl = new URL("https://github.com/ciaobelo47/ToonitaliaScraping/releases/latest/download/ToonitaliaScraping-" + newVersion + "-jar-with-dependencies.jar");

            if (!downloadedFile.exists()) {
                Launcher.getLogger().log(LogLevel.WARN, "Downloading new version...", true);

                ReadableByteChannel rbc = Channels.newChannel(downloadUrl.openStream());
                FileOutputStream fos = new FileOutputStream(downloadedFile);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

                Launcher.getLogger().log(LogLevel.INFO, "Succesfully downloaded new version!", true);
            }

            String[] lines = {
                    "@echo off",
                    "java -jar " + downloadedFile.getAbsoluteFile()
            };
            File bat = new File("start.bat");
            bat.delete();
            PrintWriter pw = new PrintWriter(bat);
            pw.println(lines[0]);
            pw.println(lines[1]);
            pw.close();

            InfoBox.restartProgramMsg();
            System.exit(0);
        } catch (ConnectException ce) {
            updateTries++;
            if (updateTries > 4) {
                Launcher.getLogger().log(LogLevel.ERROR, "Updating Failed. Check Internet Connection or report a bug. Error message: ", true);
                ce.printStackTrace();
                Thread.sleep(500);
                Launcher.getLogger().log(LogLevel.WARN, "The program will resume...", true);

                return;
            }

            Launcher.getLogger().log(LogLevel.WARN, "Update Attempt n." + updateTries + "failed. Retrying...", true);
            update(newVersion);
        } catch (FileNotFoundException fnfe) {
            Launcher.getLogger().log(LogLevel.ERROR, "Updating Failed. Check Internet Connection or report a bug. Error message: ", true);
            fnfe.printStackTrace();
            Thread.sleep(500);
            Launcher.getLogger().log(LogLevel.WARN, "The program will resume...", true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
