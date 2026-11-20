package view.quiz;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import model.Lesson;
import model.User;

public class QuizListFrm extends JFrame implements ActionListener {
    private final User student;
    private final Lesson lesson;

    public QuizListFrm(User student, Lesson lesson) {
        super("Danh sách quiz");
        this.student = student;
        this.lesson = lesson;
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
