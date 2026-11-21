package view.quiz;

import dao.QuizAttemptDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import model.Question;
import model.QuestionOption;
import model.QuizAttempt;
import model.StudentAnswer;
import model.User;

public class QuizResultFrm extends JFrame implements ActionListener {
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

    private final User user;
    private QuizAttempt attempt;

    private JLabel lblQuizTitle;
    private JLabel lblScoreSummary;
    private JLabel lblSubmittedAt;
    private JLabel lblNotice;
    private DefaultTableModel tableModel;
    private JButton btnRetryQuiz;
    private JButton btnOpenHistory;
    private JButton btnClose;

    public QuizResultFrm(User user, QuizAttempt attempt) {
        super("Kết quả quiz");
        this.user = user;
        this.attempt = attempt;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(960, 620);
        setLocationRelativeTo(null);
        initComponents();
        loadResult();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel topPanel = new JPanel(new BorderLayout(6, 6));
        lblQuizTitle = new JLabel("Đang tải kết quả...");
        lblQuizTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        topPanel.add(lblQuizTitle, BorderLayout.NORTH);

        JPanel summaryPanel = new JPanel(new BorderLayout(4, 4));
        summaryPanel.setBackground(new Color(232, 244, 253));
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(122, 207, 245)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        lblScoreSummary = new JLabel("—");
        lblScoreSummary.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblSubmittedAt = new JLabel("");
        summaryPanel.add(lblScoreSummary, BorderLayout.WEST);
        summaryPanel.add(lblSubmittedAt, BorderLayout.EAST);
        topPanel.add(summaryPanel, BorderLayout.CENTER);

        lblNotice = new JLabel(" ");
        lblNotice.setForeground(new Color(45, 90, 140));
        topPanel.add(lblNotice, BorderLayout.SOUTH);
        root.add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"Câu", "Nội dung câu hỏi", "Lựa chọn của bạn", "Đáp án đúng", "Kết quả"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tblDetails = new JTable(tableModel);
        tblDetails.setRowHeight(30);
        tblDetails.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblDetails.getColumnModel().getColumn(1).setPreferredWidth(340);
        tblDetails.getColumnModel().getColumn(2).setPreferredWidth(210);
        tblDetails.getColumnModel().getColumn(3).setPreferredWidth(210);
        tblDetails.getColumnModel().getColumn(4).setPreferredWidth(90);
        root.add(new JScrollPane(tblDetails), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnOpenHistory = new JButton("Lịch sử");
        btnRetryQuiz = new JButton("Làm lại");
        btnClose = new JButton("Đóng");

        btnOpenHistory.addActionListener(this);
        btnRetryQuiz.addActionListener(this);
        btnClose.addActionListener(this);

        bottomPanel.add(btnOpenHistory);
        bottomPanel.add(btnRetryQuiz);
        bottomPanel.add(btnClose);
        root.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void loadResult() {
        setButtonsEnabled(false);
        new SwingWorker<QuizAttempt, Void>() {
            @Override
            protected QuizAttempt doInBackground() throws Exception {
                try (QuizAttemptDAO dao = new QuizAttemptDAO()) {
                    return dao.getResult(user, attempt);
                }
            }

            @Override
            protected void done() {
                setButtonsEnabled(true);
                try {
                    attempt = get();
                    displayResult(attempt);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    lblNotice.setForeground(Color.RED);
                    lblNotice.setText(cause.getMessage());
                    JOptionPane.showMessageDialog(QuizResultFrm.this, cause.getMessage(),
                            "Không thể xem kết quả", JOptionPane.ERROR_MESSAGE);
                    dispose();
                }
            }
        }.execute();
    }

    private void displayResult(QuizAttempt result) {
        String title = result.getQuiz() != null ? result.getQuiz().getTitle() : "Kết quả quiz";
        lblQuizTitle.setText("Kết quả – " + title);

        int correct = result.getCorrectCount() != null ? result.getCorrectCount() : 0;
        int total = result.getTotalQuestions() != null ? result.getTotalQuestions() : 0;
        String scoreText = formatScore(result.getScore());
        lblScoreSummary.setText(correct + "/" + total + " câu đúng – " + scoreText + "/10 điểm");

        String subTime = result.getSubmittedAt() != null ? TIME_FMT.format(result.getSubmittedAt()) : "—";
        lblSubmittedAt.setText("Lượt #" + result.getId() + "  |  Nộp lúc: " + subTime);

        tableModel.setRowCount(0);
        List<StudentAnswer> answers = result.getAnswers();
        if (answers != null) {
            for (int i = 0; i < answers.size(); i++) {
                StudentAnswer sa = answers.get(i);
                Question q = sa.getQuestion();
                String qContent = q != null ? q.getContent() : "";
                String chosenText = sa.getSelectedOption() != null
                        ? formatOption(q, sa.getSelectedOption())
                        : "Chưa trả lời";
                String correctText = findCorrectOptionText(q);
                String verdict = Boolean.TRUE.equals(sa.getIsCorrect()) ? "Đúng" : "Sai";
                tableModel.addRow(new Object[]{i + 1, qContent, chosenText, correctText, verdict});
            }
        }
    }

    private static String formatOption(Question q, QuestionOption opt) {
        char[] letters = {'A', 'B', 'C', 'D'};
        if (q != null && q.getOptions() != null) {
            for (int i = 0; i < q.getOptions().size(); i++) {
                QuestionOption o = q.getOptions().get(i);
                if (o.getId() != null && o.getId().equals(opt.getId())) {
                    String prefix = i < letters.length ? (letters[i] + ". ") : "";
                    return prefix + o.getContent();
                }
            }
        }
        return opt.getContent() != null ? opt.getContent() : "";
    }

    private static String findCorrectOptionText(Question q) {
        char[] letters = {'A', 'B', 'C', 'D'};
        if (q == null || q.getOptions() == null) {
            return "";
        }
        for (int i = 0; i < q.getOptions().size(); i++) {
            QuestionOption o = q.getOptions().get(i);
            if (Boolean.TRUE.equals(o.getIsCorrect())) {
                String prefix = i < letters.length ? (letters[i] + ". ") : "";
                return prefix + o.getContent();
            }
        }
        return "";
    }

    static String formatScore(BigDecimal score) {
        if (score == null) {
            return "—";
        }
        return String.format("%.2f", score).replace('.', ',');
    }

    private void retryQuiz() {
        if (attempt == null || attempt.getQuiz() == null) {
            return;
        }
        setButtonsEnabled(false);
        new SwingWorker<QuizAttempt, Void>() {
            @Override
            protected QuizAttempt doInBackground() throws Exception {
                try (QuizAttemptDAO dao = new QuizAttemptDAO()) {
                    return dao.startAttempt(user, attempt.getQuiz());
                }
            }

            @Override
            protected void done() {
                setButtonsEnabled(true);
                try {
                    QuizAttempt newAttempt = get();
                    new QuizAttemptFrm(user, newAttempt).setVisible(true);
                    dispose();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(QuizResultFrm.this, cause.getMessage(),
                            "Không thể bắt đầu lượt mới", JOptionPane.WARNING_MESSAGE);
                }
            }
        }.execute();
    }

    private void setButtonsEnabled(boolean enabled) {
        btnRetryQuiz.setEnabled(enabled);
        btnOpenHistory.setEnabled(enabled);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object src = event.getSource();
        if (src == btnRetryQuiz) {
            retryQuiz();
        } else if (src == btnOpenHistory) {
            new QuizHistoryFrm(user, attempt != null ? attempt.getQuiz() : null).setVisible(true);
        } else if (src == btnClose) {
            dispose();
        }
    }
}
