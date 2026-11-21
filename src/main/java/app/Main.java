package app;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import view.MainFrm;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
            }
            new MainFrm().setVisible(true);
        });
    }
}
