package view.course;

import dao.LessonDAO;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Course;
import model.Lesson;
import model.LessonDocument;
import model.User;
import view.MainFrm;
import view.quiz.QuizListFrm;

/**
 * Giao diện màn hình học tập của học sinh (Học theo khóa học).
 * Hiển thị danh sách các bài học của khóa học, video bài giảng, tài liệu đính kèm và bài kiểm tra (quiz).
 */
public class CourseLearningFrm extends JPanel implements ActionListener {
    private final User student;
    private final Course course;
    private final JPanel parentFrame;

    private List<Lesson> lessons = new ArrayList<>();
    private Lesson selectedLesson = null;

    private JTable tblLessons;
    private DefaultTableModel tableModel;

    private JLabel lblSelectedTitle;
    private JTextField txtVideoUrl;
    private JLabel lblDocCount;
    private JButton btnWatchVideo;
    private JButton btnOpenDoc;
    private JButton btnTakeQuiz;
    private JButton subBack;
    private JButton btnRefresh;

    public CourseLearningFrm(User student, Course course) {
        this(student, course, null);
    }

    public CourseLearningFrm(User student, Course course, JPanel parentFrame) {
        this.student = student;
        this.course = course;
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        initComponents();
        loadLessons();
    }

    private void initComponents() {
        // --- 1. Header (NORTH) ---
        JPanel headerPanel = new JPanel(new BorderLayout(4, 4));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        String courseTitle = (course != null && course.getTitle() != null) ? course.getTitle() : "Khóa học";
        JLabel lblHeader = new JLabel("KHÓA HỌC: " + courseTitle);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));

        String teacherName = (course != null && course.getTeacher() != null && course.getTeacher().getFullName() != null)
                ? course.getTeacher().getFullName() : "Chưa cập nhật";
        String categoryName = (course != null && course.getCategory() != null && course.getCategory().getName() != null)
                ? course.getCategory().getName() : "Chưa phân loại";
        JLabel lblSub = new JLabel("Giảng viên: " + teacherName + "  |  Danh mục: " + categoryName);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        headerPanel.add(lblHeader, BorderLayout.NORTH);
        headerPanel.add(lblSub, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. Center Content (JSplitPane) ---
        // Left: Danh sách bài học
        JPanel leftPanel = new JPanel(new BorderLayout(6, 6));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Danh sách bài học"));

        String[] columns = {"STT", "Tiêu đề bài học", "Video", "Tài liệu"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblLessons = new JTable(tableModel);
        tblLessons.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblLessons.setRowHeight(26);
        tblLessons.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblLessons.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        tblLessons.getColumnModel().getColumn(0).setPreferredWidth(45);
        tblLessons.getColumnModel().getColumn(1).setPreferredWidth(230);
        tblLessons.getColumnModel().getColumn(2).setPreferredWidth(60);
        tblLessons.getColumnModel().getColumn(3).setPreferredWidth(70);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblLessons.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblLessons.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tblLessons.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        tblLessons.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onLessonSelected();
            }
        });

        tblLessons.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openVideo();
                }
            }
        });

        JScrollPane scrollTable = new JScrollPane(tblLessons);
        leftPanel.add(scrollTable, BorderLayout.CENTER);

        // Right: Chi tiết bài học & thao tác
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Nội dung bài học & Hoạt động"));

        JPanel detailContainer = new JPanel();
        detailContainer.setLayout(new BoxLayout(detailContainer, BoxLayout.Y_AXIS));
        detailContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblSelectedTitle = new JLabel("Chọn một bài học từ danh sách bên trái");
        lblSelectedTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSelectedTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblVideoLabel = new JLabel("Đường dẫn Video bài giảng:");
        lblVideoLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblVideoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtVideoUrl = new JTextField("Chưa chọn bài học");
        txtVideoUrl.setEditable(false);
        txtVideoUrl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtVideoUrl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        txtVideoUrl.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblDocCount = new JLabel("Tài liệu đính kèm: Chưa chọn bài học");
        lblDocCount.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDocCount.setAlignmentX(Component.LEFT_ALIGNMENT);

        detailContainer.add(lblSelectedTitle);
        detailContainer.add(Box.createVerticalStrut(14));
        detailContainer.add(lblVideoLabel);
        detailContainer.add(Box.createVerticalStrut(4));
        detailContainer.add(txtVideoUrl);
        detailContainer.add(Box.createVerticalStrut(12));
        detailContainer.add(lblDocCount);
        detailContainer.add(Box.createVerticalGlue());

        // Buttons in Right panel
        JPanel actionButtonsPanel = new JPanel(new GridLayout(3, 1, 8, 8));
        actionButtonsPanel.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 10));

        btnWatchVideo = new JButton("Xem video bài giảng");
        btnWatchVideo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnWatchVideo.setEnabled(false);
        btnWatchVideo.addActionListener(this);

        btnOpenDoc = new JButton("Mở tài liệu đính kèm");
        btnOpenDoc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnOpenDoc.setEnabled(false);
        btnOpenDoc.addActionListener(this);

        btnTakeQuiz = new JButton("Làm bài kiểm tra (Quiz)");
        btnTakeQuiz.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTakeQuiz.setEnabled(false);
        btnTakeQuiz.addActionListener(this);

        actionButtonsPanel.add(btnWatchVideo);
        actionButtonsPanel.add(btnOpenDoc);
        actionButtonsPanel.add(btnTakeQuiz);

        rightPanel.add(detailContainer, BorderLayout.CENTER);
        rightPanel.add(actionButtonsPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(470);
        splitPane.setResizeWeight(0.55);
        add(splitPane, BorderLayout.CENTER);

        // --- 3. Footer (SOUTH) ---
        JPanel footerPanel = new JPanel(new BorderLayout());
        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        subBack = new JButton("Quay lại");
        subBack.addActionListener(this);
        leftBtns.add(subBack);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRefresh = new JButton("Tải lại");
        btnRefresh.addActionListener(this);
        rightBtns.add(btnRefresh);

        footerPanel.add(leftBtns, BorderLayout.WEST);
        footerPanel.add(rightBtns, BorderLayout.EAST);
        add(footerPanel, BorderLayout.SOUTH);
    }

    public void loadLessons() {
        if (course == null || course.getId() == null) {
            return;
        }
        tableModel.setRowCount(0);
        selectedLesson = null;
        updateDetailPanel();

        try (LessonDAO dao = new LessonDAO()) {
            lessons = dao.getLessonsByCourse(course.getId());
            for (Lesson l : lessons) {
                String hasVideo = (l.getVideoUrl() != null && !l.getVideoUrl().isBlank()) ? "Có" : "-";
                int docCount = (l.getDocuments() != null) ? l.getDocuments().size() : 0;
                String docStr = docCount > 0 ? (docCount + " tệp") : "-";

                tableModel.addRow(new Object[]{
                        l.getOrderIndex(),
                        l.getTitle(),
                        hasVideo,
                        docStr
                });
            }

            if (!lessons.isEmpty()) {
                tblLessons.setRowSelectionInterval(0, 0);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách bài học: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onLessonSelected() {
        int row = tblLessons.getSelectedRow();
        if (row >= 0 && row < lessons.size()) {
            selectedLesson = lessons.get(row);
        } else {
            selectedLesson = null;
        }
        updateDetailPanel();
    }

    private void updateDetailPanel() {
        if (selectedLesson == null) {
            lblSelectedTitle.setText("Chọn một bài học từ danh sách bên trái");
            txtVideoUrl.setText("Chưa chọn bài học");
            lblDocCount.setText("Tài liệu đính kèm: Chưa chọn bài học");
            btnWatchVideo.setEnabled(false);
            btnOpenDoc.setEnabled(false);
            btnTakeQuiz.setEnabled(false);
            return;
        }

        lblSelectedTitle.setText("Bài " + selectedLesson.getOrderIndex() + ": " + selectedLesson.getTitle());

        String videoUrl = selectedLesson.getVideoUrl();
        boolean hasVideo = (videoUrl != null && !videoUrl.isBlank());
        txtVideoUrl.setText(hasVideo ? videoUrl : "Không có video bài giảng");
        btnWatchVideo.setEnabled(hasVideo);

        List<LessonDocument> docs = selectedLesson.getDocuments();
        int docCount = docs != null ? docs.size() : 0;
        if (docCount == 0) {
            lblDocCount.setText("Tài liệu đính kèm: Không có");
            btnOpenDoc.setEnabled(false);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < docs.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(docs.get(i).getName());
            }
            lblDocCount.setText("Tài liệu (" + docCount + "): " + sb);
            btnOpenDoc.setEnabled(true);
        }

        btnTakeQuiz.setEnabled(true);
    }

    private void openVideo() {
        if (selectedLesson == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bài học từ danh sách!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String url = selectedLesson.getVideoUrl();
        if (url == null || url.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bài học này không có video bài giảng.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String cleanUrl = url.trim();
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "https://" + cleanUrl;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(cleanUrl));
            } else {
                Runtime.getRuntime().exec(new String[]{"xdg-open", cleanUrl});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể mở trình duyệt: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openDocument() {
        if (selectedLesson == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bài học từ danh sách!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<LessonDocument> docs = selectedLesson.getDocuments();
        if (docs == null || docs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bài học này không có tài liệu đính kèm.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        LessonDocument docToOpen = docs.get(0);
        if (docs.size() > 1) {
            String[] docNames = docs.stream().map(LessonDocument::getName).toArray(String[]::new);
            String selected = (String) JOptionPane.showInputDialog(
                    this,
                    "Chọn tài liệu cần mở:",
                    "Tài liệu bài học",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    docNames,
                    docNames[0]
            );
            if (selected == null) return;
            for (LessonDocument d : docs) {
                if (d.getName().equals(selected)) {
                    docToOpen = d;
                    break;
                }
            }
        }

        File file = new File(docToOpen.getFilePath());
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "File tài liệu không tồn tại trên hệ thống:\n" + docToOpen.getFilePath(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file);
            } else {
                Runtime.getRuntime().exec(new String[]{"xdg-open", file.getAbsolutePath()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể mở file tài liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openQuiz() {
        if (selectedLesson == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bài học từ danh sách!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new QuizListFrm(student, selectedLesson).setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == subBack) {
            if (parentFrame == null) {
                MainFrm.showHome(this, student);
            } else {
                MainFrm.showScreen(this, parentFrame, "Chi tiết khóa học");
            }
        } else if (event.getSource() == btnWatchVideo) {
            openVideo();
        } else if (event.getSource() == btnOpenDoc) {
            openDocument();
        } else if (event.getSource() == btnTakeQuiz) {
            openQuiz();
        } else if (event.getSource() == btnRefresh) {
            loadLessons();
        }
    }
}
