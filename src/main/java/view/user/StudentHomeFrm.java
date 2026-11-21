package view.user;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import model.User;
import view.MainFrm;
import view.course.CourseListFrm;
import view.course.MyCoursesFrm;

public class StudentHomeFrm extends JPanel implements ActionListener {
    private final User student;
    private final JLabel lblWelcome = new JLabel("", SwingConstants.CENTER);
    private final JButton btnCourses = new JButton("Danh sách khóa học");
    private final JButton btnMyCourses = new JButton("Khóa học đã tham gia");
    private final JButton btnProfile = new JButton("Thông tin cá nhân");
    private final JButton btnLogout = new JButton("Đăng xuất");

    public StudentHomeFrm(User student) {
        this.student = student;
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        lblWelcome.setText("Xin chào, " + student.getFullName());
        JPanel buttons = new JPanel();
        buttons.add(btnCourses);
        buttons.add(btnMyCourses);
        buttons.add(btnProfile);
        buttons.add(btnLogout);
        btnProfile.addActionListener(this);
        btnCourses.addActionListener(this);
        btnMyCourses.addActionListener(this);
        btnLogout.addActionListener(this);

        JPanel content = new JPanel(new BorderLayout(10, 20));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.add(lblWelcome, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == btnCourses) {
            try {
                MainFrm.showScreen(this, new CourseListFrm(student), "Danh sách khóa học");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Không thể tải danh sách khóa học. Vui lòng kiểm tra kết nối cơ sở dữ liệu.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else if (event.getSource() == btnMyCourses) {
            try {
                MainFrm.showScreen(this, new MyCoursesFrm(student), "Khóa học đã tham gia");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Không thể tải khóa học đã tham gia. Vui lòng kiểm tra kết nối cơ sở dữ liệu.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else if (event.getSource() == btnProfile) {
            MainFrm.showScreen(this, new ProfileFrm(student), "Thông tin cá nhân");
        } else if (event.getSource() == btnLogout) {
            int choice = JOptionPane.showConfirmDialog(this, "Bạn muốn đăng xuất?", "Đăng xuất",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                for (Window w : Window.getWindows()) {
                    if (w != null && w.getClass().getPackageName().equals("view.quiz")) {
                        w.dispose();
                    }
                }
                student.setPasswordHash(null);
                MainFrm.showScreen(this, new LoginFrm(), "Đăng nhập");
            }
        }
    }
}
