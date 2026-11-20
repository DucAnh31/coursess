package view.quiz;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import model.Quiz;
import model.User;

public class QuizHistoryFrm extends JFrame implements ActionListener {
    private final User student;
    private final Quiz quiz;

    public QuizHistoryFrm(User student, Quiz quiz) {
        super("Lịch sử làm quiz");
        this.student = student;
        this.quiz = quiz;
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
