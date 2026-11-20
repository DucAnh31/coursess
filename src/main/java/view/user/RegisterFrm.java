package view.user;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;

public class RegisterFrm extends JFrame implements ActionListener {

    public RegisterFrm() {
        super("Đăng ký tài khoản");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        // TODO: Tạo thành phần Swing, bố trí giao diện và đăng ký listener.
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        // TODO: Xử lý sự kiện của các nút trên form.
    }
}
