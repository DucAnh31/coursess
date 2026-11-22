package view.quiz;

import dao.QuizAttemptDAO;
import dao.QuizDAO;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Lesson;
import model.Quiz;
import model.User;
import model.enums.QuizStatus;

public class ManageQuizFrm extends JFrame implements ActionListener {
    private final Long lessonId;
    private final Long teacherId;
    private List<Quiz> quizList = new ArrayList<>();

    private QuizDAO quizDAO;
    private QuizAttemptDAO quizAttemptDAO;

    private JLabel lblHeader;
    private JTable tblQuizzes;
    private DefaultTableModel modelQuizzes;
    private JLabel lblNotice;
    private JButton btnCreateQuiz;
    private JButton btnEditQuiz;
    private JButton btnCopyQuiz;
    private JButton btnCloseQuiz;
    private JButton btnBack;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    public ManageQuizFrm(Long lessonId, Long teacherId) {
        super("Quản lý danh sách quiz của bài học");
        this.lessonId = lessonId != null ? lessonId : 1L;
        this.teacherId = teacherId != null ? teacherId : 2L;

        try {
            this.quizDAO = new QuizDAO();
            this.quizAttemptDAO = new QuizAttemptDAO();
        } catch (SQLException e) {
            if (!java.awt.GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }

        initComponents();
        loadQuizzes();
    }

    public ManageQuizFrm(User teacher, Lesson lesson) {
        this(lesson != null ? lesson.getId() : 1L,
             teacher != null ? teacher.getId() : 2L);
    }

    private void initComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(880, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlNorth = new JPanel(new BorderLayout(5, 5));
        pnlNorth.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));

        lblHeader = new JLabel("Danh sách đề thi Quiz - Mã bài học: " + lessonId);
        pnlNorth.add(lblHeader, BorderLayout.WEST);
        add(pnlNorth, BorderLayout.NORTH);

        JPanel pnlCenter = new JPanel(new BorderLayout(5, 5));
        pnlCenter.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        String[] columns = {"ID", "Tiêu đề quiz", "Trạng thái", "Số câu", "Lượt làm", "Ngày xuất bản"};
        modelQuizzes = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblQuizzes = new JTable(modelQuizzes);
        tblQuizzes.setRowHeight(24);
        tblQuizzes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tblQuizzes.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblQuizzes.getColumnModel().getColumn(0).setMaxWidth(70);
        tblQuizzes.getColumnModel().getColumn(1).setPreferredWidth(320);
        tblQuizzes.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblQuizzes.getColumnModel().getColumn(3).setPreferredWidth(70);
        tblQuizzes.getColumnModel().getColumn(4).setPreferredWidth(80);
        tblQuizzes.getColumnModel().getColumn(5).setPreferredWidth(150);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tblQuizzes.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblQuizzes.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tblQuizzes.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tblQuizzes.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tblQuizzes.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        pnlCenter.add(new JScrollPane(tblQuizzes), BorderLayout.CENTER);

        lblNotice = new JLabel("Chọn một bài quiz trên bảng để thực hiện các chức năng.");
        pnlCenter.add(lblNotice, BorderLayout.SOUTH);
        add(pnlCenter, BorderLayout.CENTER);

        JPanel pnlSouth = new JPanel(new BorderLayout());
        pnlSouth.setBorder(BorderFactory.createEmptyBorder(5, 15, 10, 15));

        btnBack = new JButton("Quay lại");
        btnBack.addActionListener(this);
        pnlSouth.add(btnBack, BorderLayout.WEST);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnCreateQuiz = new JButton("Tạo quiz mới");
        btnEditQuiz = new JButton("Sửa quiz");
        btnCopyQuiz = new JButton("Sao chép quiz");
        btnCloseQuiz = new JButton("Đóng quiz");

        btnCreateQuiz.addActionListener(this);
        btnEditQuiz.addActionListener(this);
        btnCopyQuiz.addActionListener(this);
        btnCloseQuiz.addActionListener(this);

        pnlActions.add(btnCreateQuiz);
        pnlActions.add(btnEditQuiz);
        pnlActions.add(btnCopyQuiz);
        pnlActions.add(btnCloseQuiz);
        pnlSouth.add(pnlActions, BorderLayout.EAST);

        add(pnlSouth, BorderLayout.SOUTH);
    }

    public void loadQuizzes() {
        if (quizDAO == null) {
            return;
        }
        try {
            this.quizList = quizDAO.getQuizzesByLesson(this.lessonId);
            modelQuizzes.setRowCount(0);

            if (this.quizList != null) {
                for (Quiz q : this.quizList) {
                    int questionCount = q.getQuestions() != null ? q.getQuestions().size() : 0;
                    int attemptCount = 0;
                    if (quizAttemptDAO != null && q.getId() != null) {
                        attemptCount = quizAttemptDAO.countAttemptsByQuiz(q.getId());
                    }

                    String publishedText = "-";
                    if (q.getPublishedAt() != null) {
                        publishedText = DATE_FORMATTER.format(q.getPublishedAt());
                    }

                    modelQuizzes.addRow(new Object[]{
                            q.getId(),
                            q.getTitle(),
                            q.getStatus() != null ? q.getStatus().name() : "DRAFT",
                            questionCount,
                            attemptCount,
                            publishedText
                    });
                }
            }
        } catch (SQLException e) {
            lblNotice.setText("Lỗi nạp danh sách quiz: " + e.getMessage());
        }
    }

    public Quiz getSelectedQuiz() {
        int selectedRow = tblQuizzes.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < quizList.size()) {
            return quizList.get(selectedRow);
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnCreateQuiz) {
            new CreateQuizFrm(this, this.lessonId, this.teacherId, null).setVisible(true);
        } else if (e.getSource() == btnEditQuiz) {
            Quiz selectedQuiz = getSelectedQuiz();
            if (selectedQuiz == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một bài quiz trên bảng để sửa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int attemptCount = 0;
            if (quizAttemptDAO != null) {
                try {
                    attemptCount = quizAttemptDAO.countAttemptsByQuiz(selectedQuiz.getId());
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi kiểm tra lượt làm bài: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (attemptCount == 0) {
                new CreateQuizFrm(this, this.lessonId, this.teacherId, selectedQuiz.getId()).setVisible(true);
            } else {
                int choice = JOptionPane.showConfirmDialog(this,
                        "Quiz này đã có " + attemptCount + " lượt làm bài từ sinh viên, không thể sửa trực tiếp để bảo toàn kết quả thi.\n"
                        + "Bạn có muốn tạo một bản sao độc lập (Bản nháp) để chỉnh sửa không?",
                        "Cảnh báo quiz đã có lượt làm",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (choice == JOptionPane.YES_OPTION) {
                    try {
                        Quiz copy = quizDAO.copyQuiz(selectedQuiz.getId(), this.teacherId);
                        if (copy != null) {
                            loadQuizzes();
                            new CreateQuizFrm(this, this.lessonId, this.teacherId, copy.getId()).setVisible(true);
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Lỗi sao chép quiz: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } else if (e.getSource() == btnCopyQuiz) {
            Quiz selectedQuiz = getSelectedQuiz();
            if (selectedQuiz == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một bài quiz trên bảng để sao chép.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int choice = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn sao chép quiz \"" + selectedQuiz.getTitle() + "\" thành bản nháp mới không?",
                    "Xác nhận sao chép",
                    JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                try {
                    Quiz copy = quizDAO.copyQuiz(selectedQuiz.getId(), this.teacherId);
                    if (copy != null) {
                        loadQuizzes();
                        JOptionPane.showMessageDialog(this, "Sao chép quiz thành công! Đã tạo bản nháp mới.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        new CreateQuizFrm(this, this.lessonId, this.teacherId, copy.getId()).setVisible(true);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi sao chép quiz: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (e.getSource() == btnCloseQuiz) {
            Quiz selectedQuiz = getSelectedQuiz();
            if (selectedQuiz == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một bài quiz trên bảng để đóng.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (selectedQuiz.getStatus() == QuizStatus.CLOSED) {
                JOptionPane.showMessageDialog(this, "Quiz này đã ở trạng thái đóng.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if (selectedQuiz.getStatus() == QuizStatus.DRAFT) {
                JOptionPane.showMessageDialog(this, "Quiz đang ở trạng thái Nháp, chưa thể đóng.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int choice = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn đóng quiz này không?\nSau khi đóng, sinh viên sẽ không thể vào làm bài nữa.",
                    "Xác nhận đóng quiz",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                try {
                    boolean ok = quizDAO.closeQuiz(selectedQuiz.getId());
                    if (ok) {
                        loadQuizzes();
                        JOptionPane.showMessageDialog(this, "Đã đóng quiz thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi đóng quiz: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (e.getSource() == btnBack) {
            dispose();
        }
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new ManageQuizFrm(1L, 2L).setVisible(true);
        });
    }
}
