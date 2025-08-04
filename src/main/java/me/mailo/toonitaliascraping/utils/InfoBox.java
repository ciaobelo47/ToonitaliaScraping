package me.mailo.toonitaliascraping.utils;

import me.mailo.toonitaliascraping.HistoryManager;

import javax.swing.*;

public class InfoBox {
    public static void remindShow() {
        String lastShow = HistoryManager.getLastShow();

        if (Integer.parseInt(lastShow) != -1) {
            JOptionPane.showMessageDialog(null, "The last show you watched was\n" + lastShow, "Last Show", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
