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
import javax.swing.JTextField;
import model.User;
import view.MainFrm;

public class RegisterFrm extends JPanel implements ActionListener {
    private final JTextField txtFullName = new JTextField(20);
    private final JTextField txtUsername = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);
    private final JPasswordField txtConfirmPassword = new JPasswordField(20);
    private final JButton btnRegister = new JButton("Đăng ký");
    private final JButton btnCancel = new JButton("Hủy");

    public RegisterFrm() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JPanel fields = new JPanel(new GridLayout(4, 2, 10, 10));
        fields.add(new JLabel("Họ tên:"));
        fields.add(txtFullName);
        fields.add(new JLabel("Tên đăng nhập:"));
        fields.add(txtUsername);
        fields.add(new JLabel("Mật khẩu (ít nhất 8 ký tự):"));
        fields.add(txtPassword);
        fields.add(new JLabel("Nhập lại mật khẩu:"));
        fields.add(txtConfirmPassword);

        JPanel buttons = new JPanel();
        buttons.add(btnRegister);
        buttons.add(btnCancel);
        btnRegister.addActionListener(this);
        btnCancel.addActionListener(this);
        putClientProperty("defaultButton", btnRegister);

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
            MainFrm.showScreen(this, new LoginFrm(), "Đăng nhập");
            return;
        }
        char[] password = txtPassword.getPassword();
        char[] confirmPassword = txtConfirmPassword.getPassword();
        try {
            if (!Arrays.equals(password, confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Hai ô mật khẩu không trùng nhau.");
                return;
            }
            User user = new User();
            user.setFullName(txtFullName.getText());
            user.setUsername(txtUsername.getText());
            try (UserDAO dao = new UserDAO()) {
                dao.register(user, password);
            }
            JOptionPane.showMessageDialog(this, "Đăng ký thành công. Bạn có thể đăng nhập bằng tài khoản vừa tạo.");
            MainFrm.showScreen(this, new LoginFrm(), "Đăng nhập");
        } catch (IllegalArgumentException | SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể đăng ký", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(confirmPassword, '\0');
            txtPassword.setText("");
            txtConfirmPassword.setText("");
        }
    }
}
