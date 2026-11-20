package view.course;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import model.Course;
import model.User;

public class LessonAddFrm extends JFrame implements ActionListener {
    private final User teacher;
    private final Course course;

    public LessonAddFrm(User teacher, Course course) {
        super("Thêm bài học");
        this.teacher = teacher;
        this.course = course;
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
