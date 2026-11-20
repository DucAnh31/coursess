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
                // Giữ giao diện Swing mặc định nếu hệ điều hành không có look and feel.
            }
            new MainFrm().setVisible(true);
        });
    }
}
