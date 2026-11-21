package view.user;

import dao.UserDAO;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import model.User;
import view.MainFrm;

public class ChangePasswordFrm extends JPanel implements ActionListener {
    private final User user;
    private final JPasswordField txtCurrentPassword = new JPasswordField(20);
    private final JPasswordField txtNewPassword = new JPasswordField(20);
    private final JPasswordField txtConfirmPassword = new JPasswordField(20);
    private final JButton btnSave = new JButton("Đổi mật khẩu");
    private final JButton btnCancel = new JButton("Hủy");

    public ChangePasswordFrm(User user) {
        this.user = user;
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JPanel fields = new JPanel(new GridLayout(3, 2, 10, 10));
        fields.add(new JLabel("Mật khẩu hiện tại:"));
        fields.add(txtCurrentPassword);
        fields.add(new JLabel("Mật khẩu mới (ít nhất 8 ký tự):"));
        fields.add(txtNewPassword);
        fields.add(new JLabel("Nhập lại mật khẩu mới:"));
        fields.add(txtConfirmPassword);

        JPanel buttons = new JPanel();
        buttons.add(btnSave);
        buttons.add(btnCancel);
        btnSave.addActionListener(this);
        btnCancel.addActionListener(this);
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
        if (event.getSource() == btnCancel) {
            MainFrm.showScreen(this, new ProfileFrm(user), "Thông tin cá nhân");
            return;
        }
        char[] currentPassword = txtCurrentPassword.getPassword();
        char[] newPassword = txtNewPassword.getPassword();
        char[] confirmPassword = txtConfirmPassword.getPassword();
        try {
            if (!Arrays.equals(newPassword, confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Hai ô mật khẩu mới không trùng nhau.");
                return;
            }
            try (UserDAO dao = new UserDAO()) {
                dao.changePassword(user.getId(), currentPassword, newPassword);
            }
            // Không giữ mã băm cũ trong đối tượng đang dùng sau khi đổi mật khẩu.
            user.setPasswordHash(null);
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công.");
            MainFrm.showScreen(this, new ProfileFrm(user), "Thông tin cá nhân");
        } catch (IllegalArgumentException | SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể đổi mật khẩu", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(currentPassword, '\0');
            Arrays.fill(newPassword, '\0');
            Arrays.fill(confirmPassword, '\0');
            txtCurrentPassword.setText("");
            txtNewPassword.setText("");
            txtConfirmPassword.setText("");
        }
    }
}
