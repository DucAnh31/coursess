package app;

import java.awt.Color;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import view.MainFrm;

public final class Main {
    private Main() {
    }

    public static void applyDarkTheme() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            Color bg = new Color(43, 45, 48);
            Color bgDark = new Color(30, 31, 34);
            Color bgPanel = new Color(49, 51, 56);
            Color fg = new Color(223, 225, 229);
            Color muted = new Color(160, 165, 175);
            Color accent = new Color(53, 116, 240);
            Color select = new Color(46, 67, 110);

            UIManager.put("control", bg);
            UIManager.put("info", bgPanel);
            UIManager.put("nimbusBase", new Color(60, 63, 65));
            UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
            UIManager.put("nimbusDisabledText", new Color(120, 125, 135));
            UIManager.put("nimbusFocus", accent);
            UIManager.put("nimbusGreen", new Color(87, 166, 74));
            UIManager.put("nimbusInfoBlue", accent);
            UIManager.put("nimbusLightBackground", bgDark);
            UIManager.put("nimbusOrange", new Color(207, 130, 50));
            UIManager.put("nimbusRed", new Color(215, 90, 90));
            UIManager.put("nimbusSelectedText", Color.WHITE);
            UIManager.put("nimbusSelectionBackground", select);
            UIManager.put("text", fg);

            UIManager.put("Panel.background", bg);
            UIManager.put("ScrollPane.background", bgDark);
            UIManager.put("Viewport.background", bgDark);
            UIManager.put("Label.foreground", fg);
            UIManager.put("RadioButton.foreground", fg);
            UIManager.put("RadioButton.background", bg);
            UIManager.put("CheckBox.foreground", fg);
            UIManager.put("TitledBorder.titleColor", muted);
            UIManager.put("Table.background", bgDark);
            UIManager.put("Table.foreground", fg);
            UIManager.put("Table.alternateRowColor", new Color(37, 39, 43));
            UIManager.put("Table.gridColor", new Color(65, 68, 74));
            UIManager.put("Table.selectionBackground", select);
            UIManager.put("Table.selectionForeground", Color.WHITE);
            UIManager.put("TableHeader.background", bgPanel);
            UIManager.put("TableHeader.foreground", fg);
            UIManager.put("TextField.background", bgDark);
            UIManager.put("TextField.foreground", fg);
            UIManager.put("TextField.caretForeground", fg);
            UIManager.put("PasswordField.background", bgDark);
            UIManager.put("PasswordField.foreground", fg);
            UIManager.put("TextArea.background", bgDark);
            UIManager.put("TextArea.foreground", fg);
            UIManager.put("ComboBox.background", bgPanel);
            UIManager.put("ComboBox.foreground", fg);
            UIManager.put("List.background", bgDark);
            UIManager.put("List.foreground", fg);
            UIManager.put("OptionPane.background", bg);
            UIManager.put("OptionPane.messageForeground", fg);
        } catch (Exception e) {
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            applyDarkTheme();
            new MainFrm().setVisible(true);
        });
    }
}
