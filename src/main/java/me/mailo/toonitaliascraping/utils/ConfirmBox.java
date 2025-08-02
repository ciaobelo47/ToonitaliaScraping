package me.mailo.toonitaliascraping.utils;

import javax.swing.*;

public class ConfirmBox {
    public static String collectUrl() {
        String rawUrl = JOptionPane.showInputDialog("Insert url:");
        if (rawUrl == null) {
            System.exit(104);
        }

        return rawUrl;
    }
}
