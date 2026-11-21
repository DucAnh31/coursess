package view.quiz;

import dao.QuizAttemptDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import model.Question;
import model.QuestionOption;
import model.QuizAttempt;
import model.StudentAnswer;
import model.User;

public class QuizAttemptFrm extends JFrame implements ActionListener {
    private final User student;
    private QuizAttempt attempt;

    private List<Question> questions = new ArrayList<>();
    // Lựa chọn làm dở CHỈ lưu trong bộ nhớ RAM (questionId -> optionId), không ghi DB/file.
    private final Map<Long, Long> selectedOptions = new LinkedHashMap<>();

    private JLabel lblQuizTitle;
    private JLabel lblAttemptInfo;
    private JLabel lblNotice;
    private JPanel pnlQuestions;
    private JButton btnSubmit;
    private JButton btnExit;
    private boolean busy = false;

    public QuizAttemptFrm(User student, QuizAttempt attempt) {
        super("Làm quiz");
        this.student = student;
        this.attempt = attempt;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(940, 660);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!busy) {
                    requestExitAttempt();
                }
            }
        });
        initComponents();
        loadAttemptContent();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel topPanel = new JPanel(new BorderLayout(4, 4));
        lblQuizTitle = new JLabel("Đang tải đề quiz...");
        lblQuizTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        topPanel.add(lblQuizTitle, BorderLayout.NORTH);

        lblAttemptInfo = new JLabel("Lượt làm #" + (attempt != null ? attempt.getId() : ""));
        lblAttemptInfo.setForeground(new Color(70, 70, 70));
        topPanel.add(lblAttemptInfo, BorderLayout.SOUTH);
        root.add(topPanel, BorderLayout.NORTH);

        pnlQuestions = new JPanel();
        pnlQuestions.setLayout(new BoxLayout(pnlQuestions, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(pnlQuestions);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        lblNotice = new JLabel("Các lựa chọn chỉ lưu tạm trong bộ nhớ cho tới khi bạn xác nhận nộp bài.");
        lblNotice.setForeground(new Color(45, 90, 140));
        bottomPanel.add(lblNotice, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnExit = new JButton("Thoát bài");
        btnSubmit = new JButton("Nộp bài");
        btnExit.addActionListener(this);
        btnSubmit.addActionListener(this);
        btnPanel.add(btnExit);
        btnPanel.add(btnSubmit);
        bottomPanel.add(btnPanel, BorderLayout.EAST);

        root.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void loadAttemptContent() {
        setBusy(true);
        new SwingWorker<QuizAttempt, Void>() {
            @Override
            protected QuizAttempt doInBackground() throws Exception {
                try (QuizAttemptDAO dao = new QuizAttemptDAO()) {
                    return dao.getAttemptContent(student, attempt);
                }
            }

            @Override
            protected void done() {
                setBusy(false);
                try {
                    attempt = get();
                    questions = attempt.getQuiz() != null && attempt.getQuiz().getQuestions() != null
                            ? attempt.getQuiz().getQuestions()
                            : new ArrayList<>();
                    String title = attempt.getQuiz() != null ? attempt.getQuiz().getTitle() : "Làm quiz";
                    lblQuizTitle.setText(title);
                    updateProgressLabel();
                    renderQuestions();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(QuizAttemptFrm.this, cause.getMessage(),
                            "Không thể tải nội dung bài làm", JOptionPane.ERROR_MESSAGE);
                    dispose();
                }
            }
        }.execute();
    }

    private void renderQuestions() {
        pnlQuestions.removeAll();
        char[] letters = {'A', 'B', 'C', 'D'};

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            JPanel card = new JPanel(new BorderLayout(6, 6));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 215, 230)),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)));
            card.setBackground(Color.WHITE);

            JPanel header = new JPanel(new BorderLayout(8, 0));
            header.setOpaque(false);
            JLabel lblQ = new JLabel("<html><b>Câu " + (i + 1) + ".</b> " + escapeHtml(q.getContent()) + "</html>");
            lblQ.setFont(new Font("SansSerif", Font.PLAIN, 14));
            header.add(lblQ, BorderLayout.CENTER);

            ButtonGroup group = new ButtonGroup();
            JButton btnClear = new JButton("Bỏ chọn");
            btnClear.addActionListener(e -> {
                group.clearSelection();
                selectedOptions.remove(q.getId());
                updateProgressLabel();
            });
            header.add(btnClear, BorderLayout.EAST);
            card.add(header, BorderLayout.NORTH);

            JPanel optionsPanel = new JPanel(new GridLayout(0, 1, 4, 4));
            optionsPanel.setOpaque(false);
            List<QuestionOption> opts = q.getOptions() != null ? q.getOptions() : List.of();
            for (int j = 0; j < opts.size(); j++) {
                QuestionOption opt = opts.get(j);
                String prefix = j < letters.length ? (letters[j] + ". ") : ((j + 1) + ". ");
                JRadioButton radio = new JRadioButton(prefix + opt.getContent());
                radio.setOpaque(false);
                radio.addActionListener(e -> {
                    selectedOptions.put(q.getId(), opt.getId());
                    updateProgressLabel();
                });
                group.add(radio);
                optionsPanel.add(radio);
            }
            card.add(optionsPanel, BorderLayout.CENTER);

            pnlQuestions.add(card);
            pnlQuestions.add(Box.createVerticalStrut(10));
        }

        pnlQuestions.revalidate();
        pnlQuestions.repaint();
    }

    private void updateProgressLabel() {
        int total = questions.size();
        int answered = selectedOptions.size();
        lblAttemptInfo.setText("Lượt làm #" + attempt.getId()
                + "  |  Đã chọn: " + answered + "/" + total + " câu (được phép bỏ trống)");
    }

    private List<StudentAnswer> buildDraftAnswers() {
        List<StudentAnswer> list = new ArrayList<>(questions.size());
        for (Question q : questions) {
            StudentAnswer sa = new StudentAnswer();
            sa.setQuestion(q);
            Long chosenId = selectedOptions.get(q.getId());
            if (chosenId != null) {
                QuestionOption opt = new QuestionOption();
                opt.setId(chosenId);
                sa.setSelectedOption(opt);
            } else {
                sa.setSelectedOption(null);
            }
            // Không gán điểm hoặc isCorrect ở phía giao diện
            sa.setIsCorrect(null);
            list.add(sa);
        }
        return list;
    }

    private void requestSubmitAttempt() {
        int unanswered = Math.max(0, questions.size() - selectedOptions.size());
        String msg = unanswered > 0
                ? "Bạn còn " + unanswered + " câu chưa trả lời (câu bỏ trống tính 0 điểm).\nBạn có chắc chắn muốn nộp bài không?"
                : "Bạn đã chọn phương án cho tất cả các câu.\nBạn có chắc chắn muốn nộp bài không?";
        int choice = JOptionPane.showConfirmDialog(this, msg, "Xác nhận nộp bài",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        List<StudentAnswer> draftAnswers = buildDraftAnswers();
        setBusy(true);
        lblNotice.setForeground(new Color(45, 90, 140));
        lblNotice.setText("Đang chấm điểm và lưu kết quả...");

        new SwingWorker<QuizAttempt, Void>() {
            @Override
            protected QuizAttempt doInBackground() throws Exception {
                try (QuizAttemptDAO dao = new QuizAttemptDAO()) {
                    return dao.submitAttempt(student, attempt, draftAnswers);
                }
            }

            @Override
            protected void done() {
                setBusy(false);
                try {
                    QuizAttempt submitted = get();
                    selectedOptions.clear();
                    new QuizResultFrm(student, submitted).setVisible(true);
                    dispose();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    lblNotice.setForeground(Color.RED);
                    lblNotice.setText("Chưa nộp bài thành công, vui lòng thử lại.");
                    JOptionPane.showMessageDialog(QuizAttemptFrm.this,
                            "Chưa nộp bài thành công: " + cause.getMessage()
                                    + "\nCác lựa chọn của bạn vẫn được giữ tạm để thử lại.",
                            "Lỗi nộp bài", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void requestExitAttempt() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Thoát bài sẽ hủy lượt đang làm (không tính điểm và không lưu đáp án đang làm dở).\nBạn có chắc muốn thoát không?",
                "Xác nhận hủy lượt", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        setBusy(true);
        lblNotice.setForeground(new Color(45, 90, 140));
        lblNotice.setText("Đang hủy lượt làm bài...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (QuizAttemptDAO dao = new QuizAttemptDAO()) {
                    dao.cancelAttempt(student, attempt);
                }
                return null;
            }

            @Override
            protected void done() {
                setBusy(false);
                try {
                    get();
                    selectedOptions.clear();
                    dispose();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    lblNotice.setForeground(Color.RED);
                    lblNotice.setText("Hủy lượt thất bại: " + cause.getMessage());
                    JOptionPane.showMessageDialog(QuizAttemptFrm.this, cause.getMessage(),
                            "Không thể hủy lượt làm", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void setBusy(boolean isBusy) {
        this.busy = isBusy;
        btnSubmit.setEnabled(!isBusy);
        btnExit.setEnabled(!isBusy);
    }

    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (busy) {
            return;
        }
        Object src = event.getSource();
        if (src == btnSubmit) {
            requestSubmitAttempt();
        } else if (src == btnExit) {
            requestExitAttempt();
        }
    }
}
