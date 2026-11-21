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
import javax.swing.SwingConstants;
import model.User;
import view.MainFrm;

public class LoginFrm extends JPanel implements ActionListener {
    private final JTextField txtUsername = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);
    private final JButton btnLogin = new JButton("Đăng nhập");
    private final JButton btnRegister = new JButton("Đăng ký học sinh");

    public LoginFrm() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JPanel fields = new JPanel(new GridLayout(2, 2, 10, 10));
        fields.add(new JLabel("Tên đăng nhập:"));
        fields.add(txtUsername);
        fields.add(new JLabel("Mật khẩu:"));
        fields.add(txtPassword);

        JPanel buttons = new JPanel();
        buttons.add(btnLogin);
        buttons.add(btnRegister);
        btnLogin.addActionListener(this);
        btnRegister.addActionListener(this);
        putClientProperty("defaultButton", btnLogin);

        JPanel content = new JPanel(new BorderLayout(10, 20));
        content.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        content.add(new JLabel("COURSESS", SwingConstants.CENTER), BorderLayout.NORTH);
        content.add(fields, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        JPanel container = new JPanel(new GridBagLayout());
        container.add(content);
        add(container, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == btnRegister) {
            MainFrm.showScreen(this, new RegisterFrm(), "Đăng ký tài khoản học sinh");
            return;
        }
        char[] password = txtPassword.getPassword();
        try {
            if (txtUsername.getText().isBlank() || password.length == 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên đăng nhập và mật khẩu.");
                return;
            }
            User user;
            try (UserDAO dao = new UserDAO()) {
                user = dao.checkLogin(txtUsername.getText(), password);
            }
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Thông tin đăng nhập không đúng hoặc tài khoản đã bị khóa.");
                return;
            }
            MainFrm.showHome(this, user);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Không thể đăng nhập. Vui lòng kiểm tra kết nối cơ sở dữ liệu.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(password, '\0');
            txtPassword.setText("");
        }
    }
}
