package view.user;

import java.awt.BorderLayout;
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
import view.admin.CategoryManageFrm;
import view.admin.UserManageFrm;

public class AdminHomeFrm extends JPanel implements ActionListener {
    private final User admin;
    private final JLabel lblWelcome = new JLabel("", SwingConstants.CENTER);
    private final JButton btnCategories = new JButton("Quản lý danh mục");
    private final JButton btnUsers = new JButton("Quản lý tài khoản");
    private final JButton btnProfile = new JButton("Thông tin cá nhân");
    private final JButton btnLogout = new JButton("Đăng xuất");

    public AdminHomeFrm(User admin) {
        this.admin = admin;
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        lblWelcome.setText("Xin chào, " + admin.getFullName());
        JPanel buttons = new JPanel();
        buttons.add(btnUsers);
        buttons.add(btnCategories);
        buttons.add(btnProfile);
        buttons.add(btnLogout);
        btnProfile.addActionListener(this);
        btnCategories.addActionListener(this);
        btnUsers.addActionListener(this);
        btnLogout.addActionListener(this);

        JPanel content = new JPanel(new BorderLayout(10, 20));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.add(lblWelcome, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == btnUsers) {
            try {
                MainFrm.showScreen(this, new UserManageFrm(admin), "Quản lý tài khoản");
            } catch (IllegalArgumentException | SQLException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể mở quản lý tài khoản",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (event.getSource() == btnCategories) {
            try {
                MainFrm.showScreen(this, new CategoryManageFrm(admin), "Quản lý danh mục");
            } catch (IllegalArgumentException | SQLException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể mở quản lý danh mục",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (event.getSource() == btnProfile) {
            MainFrm.showScreen(this, new ProfileFrm(admin), "Thông tin cá nhân");
        } else if (event.getSource() == btnLogout) {
            int choice = JOptionPane.showConfirmDialog(this, "Bạn muốn đăng xuất?", "Đăng xuất",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                admin.setPasswordHash(null);
                MainFrm.showScreen(this, new LoginFrm(), "Đăng nhập");
            }
        }
    }
}
