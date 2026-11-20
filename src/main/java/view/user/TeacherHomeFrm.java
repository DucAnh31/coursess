package view.user;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import model.User;

public class TeacherHomeFrm extends JFrame implements ActionListener {
    private final User teacher;

    public TeacherHomeFrm(User teacher) {
        super("Trang chính giáo viên");
        this.teacher = teacher;
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
