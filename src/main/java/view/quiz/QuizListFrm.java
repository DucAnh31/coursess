package view.quiz;

import dao.QuizAttemptDAO;
import dao.QuizDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import model.Lesson;
import model.Quiz;
import model.QuizAttempt;
import model.User;

public class QuizListFrm extends JFrame implements ActionListener {
    private final User student;
    private final Lesson lesson;

    private List<Quiz> quizzes = new ArrayList<>();
    private JLabel lblLessonTitle;
    private JLabel lblNotice;
    private JTable tblQuizzes;
    private DefaultTableModel tableModel;
    private JButton btnStartQuiz;
    private JButton btnOpenHistory;
    private JButton btnRefresh;
    private JButton btnBack;

    public QuizListFrm(User student, Lesson lesson) {
        super("Danh sách quiz");
        this.student = student;
        this.lesson = lesson;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        initComponents();
        loadQuizzes();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel topPanel = new JPanel(new BorderLayout(4, 4));
        String lessonName = (lesson != null && lesson.getTitle() != null && !lesson.getTitle().isBlank())
                ? lesson.getTitle()
                : "Bài học";
        lblLessonTitle = new JLabel("Danh sách quiz – " + lessonName);
        lblLessonTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        topPanel.add(lblLessonTitle, BorderLayout.NORTH);

        lblNotice = new JLabel("Đang tải danh sách quiz đang nhận lượt làm mới...");
        lblNotice.setForeground(new Color(45, 90, 140));
        topPanel.add(lblNotice, BorderLayout.SOUTH);
        root.add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"STT", "Tên quiz", "Số câu hỏi", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblQuizzes = new JTable(tableModel);
        tblQuizzes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblQuizzes.setRowHeight(28);
        tblQuizzes.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblQuizzes.getColumnModel().getColumn(1).setPreferredWidth(460);
        tblQuizzes.getColumnModel().getColumn(2).setPreferredWidth(120);
        tblQuizzes.getColumnModel().getColumn(3).setPreferredWidth(180);
        root.add(new JScrollPane(tblQuizzes), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRefresh = new JButton("Làm mới");
        btnOpenHistory = new JButton("Lịch sử quiz");
        btnStartQuiz = new JButton("Bắt đầu");
        btnBack = new JButton("Quay lại");

        btnRefresh.addActionListener(this);
        btnOpenHistory.addActionListener(this);
        btnStartQuiz.addActionListener(this);
        btnBack.addActionListener(this);

        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnOpenHistory);
        bottomPanel.add(btnStartQuiz);
        bottomPanel.add(btnBack);
        root.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    public void loadQuizzes() {
        setControlsEnabled(false);
        lblNotice.setForeground(new Color(45, 90, 140));
        lblNotice.setText("Đang tải danh sách quiz...");

        new SwingWorker<List<Quiz>, Void>() {
            @Override
            protected List<Quiz> doInBackground() throws Exception {
                try (QuizDAO dao = new QuizDAO()) {
                    return dao.getAvailableQuizzesOfLesson(student, lesson);
                }
            }

            @Override
            protected void done() {
                setControlsEnabled(true);
                try {
                    quizzes = get();
                    tableModel.setRowCount(0);
                    for (int i = 0; i < quizzes.size(); i++) {
                        Quiz q = quizzes.get(i);
                        int count = q.getQuestions() != null ? q.getQuestions().size() : 0;
                        tableModel.addRow(new Object[]{i + 1, q.getTitle(), count, "Đang nhận lượt"});
                    }
                    if (quizzes.isEmpty()) {
                        lblNotice.setText("Hiện chưa có quiz nào đang nhận lượt làm mới trong bài học này.");
                    } else {
                        lblNotice.setText("Tìm thấy " + quizzes.size() + " quiz khả dụng. Chọn một quiz và nhấn Bắt đầu.");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    lblNotice.setForeground(Color.RED);
                    lblNotice.setText(cause.getMessage());
                    JOptionPane.showMessageDialog(QuizListFrm.this, cause.getMessage(),
                            "Không thể tải danh sách quiz", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void startSelectedQuiz() {
        int row = tblQuizzes.getSelectedRow();
        if (row < 0 || row >= quizzes.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một quiz trước khi bắt đầu.",
                    "Chưa chọn quiz", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Quiz selectedQuiz = quizzes.get(row);
        setControlsEnabled(false);
        lblNotice.setForeground(new Color(45, 90, 140));
        lblNotice.setText("Đang khởi tạo lượt làm bài...");

        new SwingWorker<QuizAttempt, Void>() {
            @Override
            protected QuizAttempt doInBackground() throws Exception {
                try (QuizAttemptDAO dao = new QuizAttemptDAO()) {
                    return dao.startAttempt(student, selectedQuiz);
                }
            }

            @Override
            protected void done() {
                setControlsEnabled(true);
                try {
                    QuizAttempt attempt = get();
                    lblNotice.setText("Đã sẵn sàng lượt làm bài #" + attempt.getId());
                    new QuizAttemptFrm(student, attempt).setVisible(true);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    lblNotice.setForeground(Color.RED);
                    lblNotice.setText(cause.getMessage());
                    JOptionPane.showMessageDialog(QuizListFrm.this, cause.getMessage(),
                            "Không thể bắt đầu lượt làm", JOptionPane.ERROR_MESSAGE);
                    loadQuizzes();
                }
            }
        }.execute();
    }

    private void openSelectedHistory() {
        int row = tblQuizzes.getSelectedRow();
        Quiz selectedQuiz = (row >= 0 && row < quizzes.size()) ? quizzes.get(row) : null;
        new QuizHistoryFrm(student, selectedQuiz).setVisible(true);
    }

    private void setControlsEnabled(boolean enabled) {
        btnStartQuiz.setEnabled(enabled);
        btnOpenHistory.setEnabled(enabled);
        btnRefresh.setEnabled(enabled);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object src = event.getSource();
        if (src == btnStartQuiz) {
            startSelectedQuiz();
        } else if (src == btnOpenHistory) {
            openSelectedHistory();
        } else if (src == btnRefresh) {
            loadQuizzes();
        } else if (src == btnBack) {
            dispose();
        }
    }
}
