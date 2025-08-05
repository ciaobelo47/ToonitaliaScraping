package me.mailo.toonitaliascraping;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.*;
import java.util.ArrayList;

public class HistoryManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static class HistoryData {
        public HistoryData(ArrayList<ShowData> showsToAss) {
            shows = showsToAss;
        }

        @SerializedName("lastShow")
        private String lastShow;

        @SerializedName("shows")
        private ArrayList<ShowData> shows;
    }

    private static class ShowData {
        public ShowData(String urlToAss, String titleToAss, Double epIndexToAss) {
            url = urlToAss;
            title = titleToAss;
            epIndex = epIndexToAss;
        }

        @SerializedName("url")
        private String url;

        @SerializedName("title")
        private String title;

        @SerializedName("epIndex")
        private Double epIndex;
    }

    public static String getLastShow() {
        try {
            FileReader fr = new FileReader("history.json");
            HistoryData hd = gson.fromJson(fr, HistoryData.class);

            fr.close();
            if (hd != null && hd.lastShow != null) {
                return hd.lastShow;
            } else {
                return "-1";
            }
        } catch (IOException e) {
            return "-1";
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public static String getShowUrl(String showTitle) {
        try {
            FileReader fr = new FileReader("history.json");

            HistoryData history = gson.fromJson(fr, HistoryData.class);

            if (history != null && history.shows != null) {
                for (ShowData showData : history.shows) {
                    if (showData.title != null && showData.title.equals(showTitle)) {
                        if (showData.url != null) {
                            return showData.url;
                        } else {
                            return "-1";
                        }
                    }
                }
            }

        } catch (IOException e) {
            return "-1";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return "-1";
    }

    public static int getShowIndex(String showTitle) {
        try {
            FileReader fr = new FileReader("history.json");

            HistoryData history = gson.fromJson(fr, HistoryData.class);

            if (history != null && history.shows != null) {
                for (ShowData showData : history.shows) {
                    if (showData.title != null && showData.title.equals(showTitle)) {
                        if (showData.epIndex != null) {
                            return showData.epIndex.intValue();
                        } else {
                            return -1;
                        }
                    }
                }
            }

        } catch (IOException e) {
            return -1;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return -1;
    }

    public static void saveLastChoice(String url, String showTitle, int epIndex) {
        try {
            File file = new File("history.json");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileReader fr = new FileReader("history.json");

            HistoryData history = gson.fromJson(fr, HistoryData.class);
            fr.close();

            FileWriter fw = new FileWriter("history.json");
            PrintWriter pw = new PrintWriter(fw);

            if (history != null && history.shows != null) {
                boolean found = false;

                for (ShowData s : history.shows) {
                    if (s.title.equals(showTitle)) {
                        found = true;
                        s.epIndex = (double) epIndex;
                    }
                }

                if (!found) {
                    history.shows.add(new ShowData(url, showTitle, (double) epIndex));
                }

                history.lastShow = showTitle;
                pw.print(gson.toJson(history));
            } else {
                ArrayList<ShowData> toSave = new ArrayList<>();
                toSave.add(new ShowData(url, showTitle, (double) epIndex));
                HistoryData newHistory = new HistoryData(toSave);

                newHistory.lastShow = showTitle;
                pw.print(gson.toJson(newHistory));
            }

            pw.close();
            fw.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
