package view.course;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import model.Course;
import model.User;
import view.MainFrm;

public class CourseLearningFrm extends JPanel implements ActionListener {
    private final User student;
    private final Course course;
    private final JPanel parentFrame;
    private final JButton subBack = new JButton("Quay lại");

    public CourseLearningFrm(User student, Course course) {
        this(student, course, null);
    }

    public CourseLearningFrm(User student, Course course, JPanel parentFrame) {
        this.student = student;
        this.course = course;
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JPanel buttons = new JPanel();
        buttons.add(subBack);
        subBack.addActionListener(this);
        add(buttons, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == subBack) {
            if (parentFrame == null) {
                MainFrm.showHome(this, student);
            } else {
                MainFrm.showScreen(this, parentFrame, "Chi tiết khóa học");
            }
        }
    }
}
