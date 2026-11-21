package view.quiz;

import dao.QuizAttemptDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import model.Quiz;
import model.QuizAttempt;
import model.User;
import model.enums.QuizStatus;

public class QuizHistoryFrm extends JFrame implements ActionListener {
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

    private final User student;
    private final Quiz initialQuiz;

    private List<QuizAttempt> attempts = new ArrayList<>();
    private long historyLoadSeq = 0;
    private JComboBox<Quiz> cboQuiz;
    private JLabel lblBestScore;
    private JLabel lblNotice;
    private JTable tblAttempts;
    private DefaultTableModel tableModel;
    private JButton btnOpenResult;
    private JButton btnClose;
    private boolean updatingCombo = false;

    public QuizHistoryFrm(User student, Quiz quiz) {
        super("Lịch sử làm quiz");
        this.student = student;
        this.initialQuiz = quiz;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        initComponents();
        loadHistoryQuizzes();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        JLabel lblHeader = new JLabel("Lịch sử và kết quả quiz cá nhân");
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 18));
        topPanel.add(lblHeader, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new BorderLayout(10, 0));
        filterPanel.add(new JLabel("Chọn quiz:"), BorderLayout.WEST);
        cboQuiz = new JComboBox<>();
        cboQuiz.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Quiz q) {
                    String suffix = q.getStatus() == QuizStatus.CLOSED ? " (Đã ngừng nhận lượt mới)" : "";
                    setText(q.getTitle() + suffix);
                }
                return this;
            }
        });
        cboQuiz.addActionListener(this);
        filterPanel.add(cboQuiz, BorderLayout.CENTER);
        topPanel.add(filterPanel, BorderLayout.CENTER);

        JPanel scorePanel = new JPanel(new BorderLayout(8, 4));
        scorePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        lblBestScore = new JLabel("Điểm cao nhất: —");
        lblBestScore.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblNotice = new JLabel(" ");
        scorePanel.add(lblBestScore, BorderLayout.WEST);
        scorePanel.add(lblNotice, BorderLayout.EAST);
        topPanel.add(scorePanel, BorderLayout.SOUTH);

        root.add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"Mã lượt", "Thời gian nộp", "Số câu đúng", "Điểm (thang 10)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblAttempts = new JTable(tableModel);
        tblAttempts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblAttempts.setRowHeight(28);
        root.add(new JScrollPane(tblAttempts), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnOpenResult = new JButton("Xem chi tiết");
        btnClose = new JButton("Đóng");
        btnOpenResult.addActionListener(this);
        btnClose.addActionListener(this);
        bottomPanel.add(btnOpenResult);
        bottomPanel.add(btnClose);
        root.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void loadHistoryQuizzes() {
        btnOpenResult.setEnabled(false);
        new SwingWorker<List<Quiz>, Void>() {
            @Override
            protected List<Quiz> doInBackground() throws Exception {
                try (QuizAttemptDAO dao = new QuizAttemptDAO()) {
                    return dao.getHistoryQuizzes(student);
                }
            }

            @Override
            protected void done() {
                try {
                    List<Quiz> list = get();
                    updatingCombo = true;
                    cboQuiz.removeAllItems();
                    Quiz toSelect = null;

                    if (initialQuiz != null && initialQuiz.getId() != null) {
                        boolean exists = list.stream().anyMatch(q -> initialQuiz.getId().equals(q.getId()));
                        if (!exists) {
                            list.add(0, initialQuiz);
                        }
                    }
                    for (Quiz q : list) {
                        cboQuiz.addItem(q);
                        if (initialQuiz != null && initialQuiz.getId() != null && initialQuiz.getId().equals(q.getId())) {
                            toSelect = q;
                        }
                    }
                    if (toSelect != null) {
                        cboQuiz.setSelectedItem(toSelect);
                    } else if (cboQuiz.getItemCount() > 0) {
                        cboQuiz.setSelectedIndex(0);
                    }
                    updatingCombo = false;

                    Quiz current = (Quiz) cboQuiz.getSelectedItem();
                    if (current != null) {
                        loadHistory(current);
                    } else {
                        tableModel.setRowCount(0);
                        lblBestScore.setText("Điểm cao nhất: Chưa nộp bài");
                        lblNotice.setText("Bạn chưa có lượt nộp bài nào.");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    lblNotice.setForeground(Color.RED);
                    lblNotice.setText(cause.getMessage());
                    JOptionPane.showMessageDialog(QuizHistoryFrm.this, cause.getMessage(),
                            "Không thể tải lịch sử", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private static final class HistoryData {
        final List<QuizAttempt> attempts;
        final BigDecimal bestScore;

        HistoryData(List<QuizAttempt> attempts, BigDecimal bestScore) {
            this.attempts = attempts;
            this.bestScore = bestScore;
        }
    }

    private void loadHistory(Quiz quiz) {
        final long seq = ++historyLoadSeq;
        btnOpenResult.setEnabled(false);
        lblNotice.setForeground(UIManager.getColor("Label.foreground"));
        lblNotice.setText("Đang tải các lượt đã nộp...");

        new SwingWorker<HistoryData, Void>() {
            @Override
            protected HistoryData doInBackground() throws Exception {
                try (QuizAttemptDAO dao = new QuizAttemptDAO()) {
                    List<QuizAttempt> list = dao.getHistory(student, quiz);
                    BigDecimal best = dao.getBestScore(student, quiz);
                    return new HistoryData(list, best);
                }
            }

            @Override
            protected void done() {
                if (seq != historyLoadSeq || !isDisplayable()) {
                    return;
                }
                try {
                    HistoryData data = get();
                    attempts = data.attempts;
                    tableModel.setRowCount(0);
                    for (QuizAttempt qa : attempts) {
                        String time = qa.getSubmittedAt() != null ? TIME_FMT.format(qa.getSubmittedAt()) : "—";
                        String ratio = (qa.getCorrectCount() != null ? qa.getCorrectCount() : 0)
                                + "/" + (qa.getTotalQuestions() != null ? qa.getTotalQuestions() : 0);
                        String score = QuizResultFrm.formatScore(qa.getScore()) + "/10";
                        tableModel.addRow(new Object[]{"#" + qa.getId(), time, ratio, score});
                    }
                    if (data.bestScore == null || attempts.isEmpty()) {
                        lblBestScore.setText("Điểm cao nhất: Chưa nộp bài");
                        lblNotice.setText("Chưa có lượt nộp nào cho quiz này.");
                        btnOpenResult.setEnabled(false);
                    } else {
                        lblBestScore.setText("Điểm cao nhất: " + QuizResultFrm.formatScore(data.bestScore) + "/10");
                        lblNotice.setText("Tổng số lượt đã nộp: " + attempts.size());
                        btnOpenResult.setEnabled(true);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    attempts = new ArrayList<>();
                    tableModel.setRowCount(0);
                    lblBestScore.setText("Điểm cao nhất: —");
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    lblNotice.setForeground(Color.RED);
                    lblNotice.setText(cause.getMessage());
                    JOptionPane.showMessageDialog(QuizHistoryFrm.this, cause.getMessage(),
                            "Không thể tải lịch sử quiz", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void openSelectedResult() {
        int row = tblAttempts.getSelectedRow();
        if (row < 0 || row >= attempts.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một lượt đã nộp để xem chi tiết.",
                    "Chưa chọn lượt", JOptionPane.WARNING_MESSAGE);
            return;
        }
        QuizAttempt selected = attempts.get(row);
        new QuizResultFrm(student, selected).setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object src = event.getSource();
        if (src == cboQuiz && !updatingCombo) {
            Quiz selected = (Quiz) cboQuiz.getSelectedItem();
            if (selected != null) {
                loadHistory(selected);
            }
        } else if (src == btnOpenResult) {
            openSelectedResult();
        } else if (src == btnClose) {
            dispose();
        }
    }
}
