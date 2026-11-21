package view.user;

import dao.UserDAO;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import model.User;
import view.MainFrm;

public class ProfileFrm extends JPanel implements ActionListener {
    private final User user;
    private final JTextField txtFullName = new JTextField(20);
    private final JButton btnSave = new JButton("Lưu họ tên");
    private final JButton btnChangePassword = new JButton("Đổi mật khẩu");
    private final JButton btnClose = new JButton("Quay lại");

    public ProfileFrm(User user) {
        this.user = user;
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        txtFullName.setText(user.getFullName());
        String role = switch (user.getRole()) {
            case ADMIN -> "Quản trị viên";
            case TEACHER -> "Giáo viên";
            default -> "Học sinh";
        };
        JPanel fields = new JPanel(new GridLayout(3, 2, 10, 10));
        fields.add(new JLabel("Tên đăng nhập:"));
        fields.add(new JLabel(user.getUsername()));
        fields.add(new JLabel("Vai trò:"));
        fields.add(new JLabel(role));
        fields.add(new JLabel("Họ tên:"));
        fields.add(txtFullName);

        JPanel buttons = new JPanel();
        buttons.add(btnSave);
        buttons.add(btnChangePassword);
        buttons.add(btnClose);
        btnSave.addActionListener(this);
        btnChangePassword.addActionListener(this);
        btnClose.addActionListener(this);
        putClientProperty("defaultButton", btnSave);

        JPanel content = new JPanel(new BorderLayout(10, 15));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.add(fields, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        JPanel container = new JPanel(new GridBagLayout());
        container.add(content);
        add(container, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == btnClose) {
            MainFrm.showHome(this, user);
        } else if (event.getSource() == btnChangePassword) {
            MainFrm.showScreen(this, new ChangePasswordFrm(user), "Đổi mật khẩu");
        } else if (event.getSource() == btnSave) {
            try (UserDAO dao = new UserDAO()) {
                dao.updateFullName(user.getId(), txtFullName.getText());
                user.setFullName(txtFullName.getText().strip());
                txtFullName.setText(user.getFullName());
                JOptionPane.showMessageDialog(this, "Đã cập nhật họ tên.");
            } catch (IllegalArgumentException | SQLException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể cập nhật", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
