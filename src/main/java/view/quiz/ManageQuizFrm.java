package view.quiz;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import model.Lesson;
import model.User;

public class ManageQuizFrm extends JFrame implements ActionListener {
    private final User teacher;
    private final Lesson lesson;

    private JTextField txtQuizTitle;
    private JTable tblQuestions;
    private DefaultTableModel tableModel;
    private JButton btnAddQuestion;
    private JButton btnSaveQuiz;
    private JButton btnClose;

    public ManageQuizFrm(User teacher, Lesson lesson) {
        super("Tạo & Quản lý quiz - COURSESS");
        this.teacher = teacher;
        this.lesson = lesson;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(800, 520);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));

        JPanel headerPanel = new JPanel(new BorderLayout(4, 4));
        headerPanel.setBorder(BorderFactory.createEtchedBorder());
        JPanel headerContent = new JPanel(new BorderLayout(4, 4));
        headerContent.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel lblHeader = new JLabel("TẠO & QUẢN LÝ QUIZ");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 17));

        String lessonTitle = (lesson != null && lesson.getTitle() != null) ? lesson.getTitle() : "Chưa xác định";
        JLabel lblSub = new JLabel("Bài học: " + lessonTitle + " | Giảng viên: " + (teacher != null ? teacher.getFullName() : ""));
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        headerContent.add(lblHeader, BorderLayout.NORTH);
        headerContent.add(lblSub, BorderLayout.SOUTH);
        headerPanel.add(headerContent, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        titlePanel.setBorder(BorderFactory.createTitledBorder("Thông tin bài kiểm tra"));
        JLabel lblTitle = new JLabel("Tiêu đề Quiz:");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtQuizTitle = new JTextField(30);
        txtQuizTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtQuizTitle.setText("Quiz ôn tập - " + lessonTitle);
        titlePanel.add(lblTitle);
        titlePanel.add(txtQuizTitle);

        String[] columns = {"STT", "Nội dung câu hỏi", "Số phương án", "Đáp án đúng"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tblQuestions = new JTable(tableModel);
        tblQuestions.setRowHeight(26);
        tblQuestions.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblQuestions.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(tblQuestions);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách câu hỏi"));

        centerPanel.add(titlePanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnAddQuestion = new JButton("+ Thêm câu hỏi");
        btnAddQuestion.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnAddQuestion.addActionListener(this);
        leftActions.add(btnAddQuestion);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnSaveQuiz = new JButton("Lưu quiz");
        btnSaveQuiz.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSaveQuiz.addActionListener(this);

        btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnClose.addActionListener(this);

        rightActions.add(btnSaveQuiz);
        rightActions.add(btnClose);

        bottomPanel.add(leftActions, BorderLayout.WEST);
        bottomPanel.add(rightActions, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == btnAddQuestion) {
            JOptionPane.showMessageDialog(this, "Giao diện soạn câu hỏi và đáp án chi tiết sẽ do phân hệ Quản lý Quiz hoàn thiện.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else if (event.getSource() == btnSaveQuiz) {
            JOptionPane.showMessageDialog(this, "Chức năng lưu và xử lý dữ liệu quiz backend sẽ do phân hệ Quản lý Quiz đảm nhiệm.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else if (event.getSource() == btnClose) {
            dispose();
        }
    }
}
