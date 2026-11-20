package view.course;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import model.Course;
import model.User;

public class CourseLearningFrm extends JFrame implements ActionListener {
    private final User student;
    private final Course course;

    public CourseLearningFrm(User student, Course course) {
        super("Học theo khóa học");
        this.student = student;
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
