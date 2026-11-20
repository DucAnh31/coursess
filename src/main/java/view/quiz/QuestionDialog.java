package view.quiz;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import javax.swing.JFrame;
import model.Question;

public class QuestionDialog extends JDialog implements ActionListener {
    private final JFrame parentFrame;
    private final Question question;

    public QuestionDialog(JFrame parentFrame, Question question) {
        super(parentFrame, "Soạn câu hỏi", true);
        this.parentFrame = parentFrame;
        this.question = question;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(parentFrame);
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
