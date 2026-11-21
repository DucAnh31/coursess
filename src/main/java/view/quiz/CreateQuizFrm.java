package view.quiz;

import dao.QuestionDAO;
import dao.QuizDAO;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import model.Lesson;
import model.Question;
import model.QuestionOption;
import model.Quiz;
import model.User;
import model.enums.QuizStatus;

public class CreateQuizFrm extends JFrame implements ActionListener {
    private final ManageQuizFrm parentFrm;
    private final Long lessonId;
    private final Long teacherId;
    private Long currentQuizId;
    private Quiz currentQuiz;
    private List<Question> questions = new ArrayList<>();

    private QuizDAO quizDAO;
    private QuestionDAO questionDAO;

    private JTextField txtTitle;
    private JLabel lblStatus;
    private JLabel lblNotice;
    private JTable tblQuestions;
    private DefaultTableModel modelQuestions;
    private JButton btnAddQuestion;
    private JButton btnEditQuestion;
    private JButton btnDeleteQuestion;
    private JButton btnSaveDraft;
    private JButton btnPublish;
    private JButton btnCancel;

    public CreateQuizFrm(ManageQuizFrm parent, Long lessonId, Long teacherId, Long quizId) {
        super(quizId == null ? "Soạn quiz mới" : "Sửa quiz");
        this.parentFrm = parent;
        this.lessonId = lessonId != null ? lessonId : 1L;
        this.teacherId = teacherId != null ? teacherId : 2L;
        this.currentQuizId = quizId;

        try {
            this.quizDAO = new QuizDAO();
            this.questionDAO = new QuestionDAO();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        initComponents();

        if (quizId != null) {
            loadQuizData(quizId);
        }
    }

    public CreateQuizFrm(User teacher, Quiz quiz) {
        this(null,
             (quiz != null && quiz.getLesson() != null) ? quiz.getLesson().getId() : 1L,
             teacher != null ? teacher.getId() : 2L,
             quiz != null ? quiz.getId() : null);
    }

    private void initComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(850, 550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlNorth = new JPanel(new BorderLayout(5, 5));
        pnlNorth.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));

        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlTitle.add(new JLabel("Tiêu đề quiz:"));
        txtTitle = new JTextField(35);
        pnlTitle.add(txtTitle);

        pnlTitle.add(new JLabel("Trạng thái:"));
        lblStatus = new JLabel("DRAFT");
        pnlTitle.add(lblStatus);
        pnlNorth.add(pnlTitle, BorderLayout.CENTER);

        add(pnlNorth, BorderLayout.NORTH);

        JPanel pnlCenter = new JPanel(new BorderLayout(5, 5));
        pnlCenter.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        JPanel pnlTableHeader = new JPanel(new BorderLayout());
        pnlTableHeader.add(new JLabel("Danh sách câu hỏi trong đề:"), BorderLayout.WEST);

        JPanel pnlTableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnAddQuestion = new JButton("Thêm câu hỏi");
        btnEditQuestion = new JButton("Sửa câu hỏi");
        btnDeleteQuestion = new JButton("Xóa câu hỏi");

        btnAddQuestion.addActionListener(this);
        btnEditQuestion.addActionListener(this);
        btnDeleteQuestion.addActionListener(this);

        pnlTableActions.add(btnAddQuestion);
        pnlTableActions.add(btnEditQuestion);
        pnlTableActions.add(btnDeleteQuestion);
        pnlTableHeader.add(pnlTableActions, BorderLayout.EAST);
        pnlCenter.add(pnlTableHeader, BorderLayout.NORTH);

        String[] columns = {"TT", "Nội dung câu hỏi", "Số phương án", "Đáp án đúng"};
        modelQuestions = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblQuestions = new JTable(modelQuestions);
        tblQuestions.setRowHeight(24);
        tblQuestions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pnlCenter.add(new JScrollPane(tblQuestions), BorderLayout.CENTER);

        lblNotice = new JLabel("Lưu ý: Tiêu đề không rỗng và có ít nhất 1 câu hỏi để xuất bản.");
        pnlCenter.add(lblNotice, BorderLayout.SOUTH);
        add(pnlCenter, BorderLayout.CENTER);

        JPanel pnlSouth = new JPanel(new BorderLayout());
        pnlSouth.setBorder(BorderFactory.createEmptyBorder(5, 15, 10, 15));

        btnCancel = new JButton("Hủy / Quay lại");
        btnCancel.addActionListener(this);
        pnlSouth.add(btnCancel, BorderLayout.WEST);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnSaveDraft = new JButton("Lưu nháp (DRAFT)");
        btnPublish = new JButton("Xuất bản quiz (PUBLISHED)");

        btnSaveDraft.addActionListener(this);
        btnPublish.addActionListener(this);

        pnlButtons.add(btnSaveDraft);
        pnlButtons.add(btnPublish);
        pnlSouth.add(pnlButtons, BorderLayout.EAST);

        add(pnlSouth, BorderLayout.SOUTH);
    }

    public void loadQuizData(Long quizId) {
        if (quizDAO == null) {
            return;
        }
        try {
            this.currentQuiz = quizDAO.findById(quizId);
            if (this.currentQuiz != null) {
                txtTitle.setText(this.currentQuiz.getTitle());
                this.questions = this.currentQuiz.getQuestions();
                if (this.questions == null) {
                    this.questions = new ArrayList<>();
                }
                lblStatus.setText(this.currentQuiz.getStatus() != null ? this.currentQuiz.getStatus().name() : "DRAFT");
                refreshQuestionTable();
            }
        } catch (SQLException e) {
            lblNotice.setText("Lỗi nạp thông tin quiz: " + e.getMessage());
        }
    }

    public void refreshQuestionTable() {
        modelQuestions.setRowCount(0);
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            q.setOrderIndex(i + 1);

            String correctText = "";
            List<QuestionOption> options = q.getOptions();
            if (options != null) {
                for (QuestionOption opt : options) {
                    if (Boolean.TRUE.equals(opt.getIsCorrect())) {
                        correctText = "Phương án " + getOptionLabel(opt.getOrderIndex()) + " (" + opt.getContent() + ")";
                        break;
                    }
                }
            }

            modelQuestions.addRow(new Object[]{
                    q.getOrderIndex(),
                    q.getContent(),
                    (options != null ? options.size() : 0) + " phương án",
                    correctText
            });
        }
    }

    private String getOptionLabel(Integer orderIndex) {
        if (orderIndex == null) {
            return "A";
        }
        return switch (orderIndex) {
            case 1 -> "A";
            case 2 -> "B";
            case 3 -> "C";
            case 4 -> "D";
            default -> String.valueOf(orderIndex);
        };
    }

    public boolean validateDraft() {
        String title = txtTitle.getText().trim();
        if (title.isEmpty()) {
            lblNotice.setText("Lỗi: Tiêu đề quiz không được để trống khi lưu nháp.");
            txtTitle.requestFocus();
            return false;
        }
        lblNotice.setText("");
        return true;
    }

    public boolean validatePublish() {
        String title = txtTitle.getText().trim();
        if (title.isEmpty()) {
            lblNotice.setText("Lỗi: Tiêu đề quiz không được để trống trước khi xuất bản.");
            txtTitle.requestFocus();
            return false;
        }

        if (questions == null || questions.isEmpty()) {
            lblNotice.setText("Lỗi: Quiz phải có ít nhất 1 câu hỏi hợp lệ mới được xuất bản.");
            return false;
        }

        lblNotice.setText("");
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAddQuestion) {
            QuestionDialog dialog = new QuestionDialog(this, null, questions.size() + 1);
            dialog.setVisible(true);
            if (dialog.isConfirmed() && dialog.getQuestion() != null) {
                questions.add(dialog.getQuestion());
                refreshQuestionTable();
            }
        } else if (e.getSource() == btnEditQuestion) {
            int selectedRow = tblQuestions.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một câu hỏi để sửa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Question q = questions.get(selectedRow);
            QuestionDialog dialog = new QuestionDialog(this, q, q.getOrderIndex());
            dialog.setVisible(true);
            if (dialog.isConfirmed() && dialog.getQuestion() != null) {
                questions.set(selectedRow, dialog.getQuestion());
                refreshQuestionTable();
            }
        } else if (e.getSource() == btnDeleteQuestion) {
            int selectedRow = tblQuestions.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một câu hỏi để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn xóa câu hỏi này?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                questions.remove(selectedRow);
                refreshQuestionTable();
            }
        } else if (e.getSource() == btnSaveDraft) {
            if (!validateDraft()) {
                return;
            }

            if (quizDAO == null) {
                JOptionPane.showMessageDialog(this, "Chưa kết nối cơ sở dữ liệu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (this.currentQuiz == null) {
                this.currentQuiz = new Quiz();
            }
            this.currentQuiz.setTitle(txtTitle.getText().trim());
            this.currentQuiz.setStatus(QuizStatus.DRAFT);

            Lesson l = new Lesson();
            l.setId(this.lessonId);
            this.currentQuiz.setLesson(l);

            User u = new User();
            u.setId(this.teacherId);
            this.currentQuiz.setCreatedBy(u);

            this.currentQuiz.setQuestions(this.questions);

            try {
                boolean ok = quizDAO.saveDraft(this.currentQuiz);
                if (ok) {
                    this.currentQuizId = this.currentQuiz.getId();
                    lblStatus.setText("DRAFT");
                    JOptionPane.showMessageDialog(this, "Lưu nháp quiz thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);

                    if (parentFrm != null) {
                        parentFrm.loadQuizzes();
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu nháp: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == btnPublish) {
            if (!validatePublish()) {
                return;
            }

            if (quizDAO == null) {
                JOptionPane.showMessageDialog(this, "Chưa kết nối cơ sở dữ liệu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn xuất bản quiz này không?",
                    "Xác nhận xuất bản",
                    JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            if (this.currentQuiz == null) {
                this.currentQuiz = new Quiz();
            }
            this.currentQuiz.setTitle(txtTitle.getText().trim());
            this.currentQuiz.setStatus(QuizStatus.PUBLISHED);

            Lesson l = new Lesson();
            l.setId(this.lessonId);
            this.currentQuiz.setLesson(l);

            User u = new User();
            u.setId(this.teacherId);
            this.currentQuiz.setCreatedBy(u);

            this.currentQuiz.setQuestions(this.questions);

            try {
                boolean ok = quizDAO.publish(this.currentQuiz);
                if (ok) {
                    this.currentQuizId = this.currentQuiz.getId();
                    lblStatus.setText("PUBLISHED");
                    JOptionPane.showMessageDialog(this, "Xuất bản quiz thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);

                    if (parentFrm != null) {
                        parentFrm.loadQuizzes();
                    }
                    dispose();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất bản: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == btnCancel) {
            dispose();
        }
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new CreateQuizFrm(null, 1L, 2L, null).setVisible(true);
        });
    }
}
