package me.mailo.toonitaliascraping.utils;

import me.mailo.toonitaliascraping.HistoryManager;

import javax.swing.*;

public class InfoBox {
    public static String remindShow() {
        String lastShow = HistoryManager.getLastShow();

        if (!lastShow.equals("-1")) {
            JOptionPane.showMessageDialog(null, "The last show you watched was\n" + lastShow, "Last Show", JOptionPane.INFORMATION_MESSAGE);
            switch (JOptionPane.showConfirmDialog(null, "Want to Resume?", "Resume", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                case 0:
                    return HistoryManager.getShowUrl(lastShow);
                case 1:
                    return "-1";
                case -1:
                    System.exit(0);
                default:
                    throw new RuntimeException("Bo?");
            }
        } else {
            return "-1";
        }
    }
}
