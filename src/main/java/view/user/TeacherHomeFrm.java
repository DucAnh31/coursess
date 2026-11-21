package view.user;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import model.User;
import view.MainFrm;
import view.course.TeacherCourseListFrm;

public class TeacherHomeFrm extends JPanel implements ActionListener {
    private final User teacher;
    private final JLabel lblWelcome = new JLabel("", SwingConstants.CENTER);
    private final JButton btnManageCourses = new JButton("Quản lý khóa học");
    private final JButton btnProfile = new JButton("Thông tin cá nhân");
    private final JButton btnLogout = new JButton("Đăng xuất");

    public TeacherHomeFrm(User teacher) {
        this.teacher = teacher;
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        lblWelcome.setText("Xin chào, " + teacher.getFullName());
        JPanel buttons = new JPanel();
        buttons.add(btnManageCourses);
        buttons.add(btnProfile);
        buttons.add(btnLogout);
        btnManageCourses.addActionListener(this);
        btnProfile.addActionListener(this);
        btnLogout.addActionListener(this);

        JPanel content = new JPanel(new BorderLayout(10, 20));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.add(lblWelcome, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == btnManageCourses) {
            new TeacherCourseListFrm(teacher).setVisible(true);
        } else if (event.getSource() == btnProfile) {
            MainFrm.showScreen(this, new ProfileFrm(teacher), "Thông tin cá nhân");
        } else if (event.getSource() == btnLogout) {
            int choice = JOptionPane.showConfirmDialog(this, "Bạn muốn đăng xuất?", "Đăng xuất",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                teacher.setPasswordHash(null);
                MainFrm.showScreen(this, new LoginFrm(), "Đăng nhập");
            }
        }
    }
}
