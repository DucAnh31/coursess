package view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import model.User;
import view.user.AdminHomeFrm;
import view.user.LoginFrm;
import view.user.StudentHomeFrm;
import view.user.TeacherHomeFrm;

public class MainFrm extends JFrame {
    public MainFrm() {
        super("COURSESS");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(900, 600));
        setResizable(true);
        setLocationRelativeTo(null);
        setScreen(new LoginFrm(), "Đăng nhập");
    }

    private void setScreen(JPanel screen, String title) {
        getRootPane().setDefaultButton(null);
        setTitle("COURSESS - " + title);
        setContentPane(screen);
        getRootPane().setDefaultButton((JButton) screen.getClientProperty("defaultButton"));
        revalidate();
        repaint();
    }

    public static void showScreen(Component current, JPanel screen, String title) {
        Window window = SwingUtilities.getWindowAncestor(current);
        if (!(window instanceof MainFrm mainFrame)) {
            throw new IllegalStateException("Màn hình phải được mở trong MainFrm.");
        }
        mainFrame.setScreen(screen, title);
    }

    public static void showHome(Component current, User user) {
        switch (user.getRole()) {
            case ADMIN -> showScreen(current, new AdminHomeFrm(user), "Trang chính quản trị viên");
            case TEACHER -> showScreen(current, new TeacherHomeFrm(user), "Trang chính giáo viên");
            default -> showScreen(current, new StudentHomeFrm(user), "Trang chính học sinh");
        }
    }
}
