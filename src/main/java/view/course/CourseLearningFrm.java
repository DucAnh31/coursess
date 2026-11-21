package view.course;

import dao.CourseMemberDAO;
import dao.LessonDAO;
import dao.LessonDocumentDAO;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import model.Course;
import model.CourseMember;
import model.Lesson;
import model.LessonDocument;
import model.User;
import model.enums.CourseStatus;
import model.enums.UserRole;
import view.MainFrm;
import view.quiz.QuizListFrm;

public class CourseLearningFrm extends JPanel implements ActionListener {
    private final User student;
    private final Course course;
    private final JPanel parentFrame;
    private final JLabel outCourse = new JLabel();
    private final JLabel outLesson = new JLabel("Chọn một bài học để bắt đầu.");
    private final JLabel outNotice = new JLabel(" ");
    private final JTextField outVideoUrl = new JTextField();
    private final DefaultListModel<String> lessonModel = new DefaultListModel<>();
    private final JList<String> outLessons = new JList<>(lessonModel);
    private final DefaultTableModel documentModel = new DefaultTableModel(
            new String[]{"Tên tài liệu", "Dung lượng"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable outDocuments = new JTable(documentModel);
    private final JButton subOpenVideo = new JButton("Xem video trên YouTube");
    private final JButton subQuiz = new JButton("Làm bài kiểm tra (Quiz)");
    private final JButton subDownload = new JButton("Tải tài liệu");
    private final JButton subReload = new JButton("Tải lại bài học");
    private final JButton subBack = new JButton("Quay lại");
    private List<Lesson> lessons = new ArrayList<>();
    private final List<LessonDocument> documents = new ArrayList<>();
    private QuizListFrm quizListFrm;

    public CourseLearningFrm(User student, Course course) throws SQLException {
        this(student, course, null);
    }

    public CourseLearningFrm(User student, Course course, JPanel parentFrame) throws SQLException {
        this.student = student;
        this.course = course;
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        initComponents();
        loadLessons();
    }

    private void initComponents() {
        JPanel headerPanel = new JPanel(new BorderLayout(4, 4));
        headerPanel.setBorder(BorderFactory.createEtchedBorder());
        JPanel headerContent = new JPanel(new BorderLayout(4, 4));
        headerContent.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel title = new JLabel("HỌC THEO KHÓA HỌC");
        headerContent.add(title, BorderLayout.NORTH);
        headerContent.add(outCourse, BorderLayout.SOUTH);
        headerPanel.add(headerContent, BorderLayout.CENTER);

        outVideoUrl.setEditable(false);
        outLessons.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        outLessons.setFixedCellHeight(28);
        outLessons.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && outLessons.getSelectedIndex() >= 0) {
                actionPerformed(new ActionEvent(outLessons, ActionEvent.ACTION_PERFORMED, "lesson"));
            }
        });
        JScrollPane lessonScroll = new JScrollPane(outLessons);
        lessonScroll.setBorder(BorderFactory.createTitledBorder("Danh sách bài học"));

        JPanel videoInformation = new JPanel(new GridLayout(3, 1, 5, 8));
        videoInformation.add(outLesson);
        videoInformation.add(outVideoUrl);
        videoInformation.add(new JLabel("Video được mở bằng trình duyệt mặc định. Cần kết nối Internet."));
        JPanel videoButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        videoButtons.add(subOpenVideo);
        videoButtons.add(subQuiz);
        JPanel video = new JPanel(new BorderLayout(10, 10));
        video.setBorder(BorderFactory.createTitledBorder("Video bài học"));
        video.add(videoInformation, BorderLayout.CENTER);
        video.add(videoButtons, BorderLayout.SOUTH);

        outDocuments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        outDocuments.setRowHeight(26);
        outDocuments.setAutoCreateRowSorter(true);
        outDocuments.getColumnModel().getColumn(0).setPreferredWidth(350);
        outDocuments.getSelectionModel().addListSelectionListener(event ->
                subDownload.setEnabled(outDocuments.getSelectedRow() >= 0));
        JPanel downloadButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        downloadButtons.add(subDownload);
        JPanel documentPanel = new JPanel(new BorderLayout(10, 10));
        documentPanel.setBorder(BorderFactory.createTitledBorder("Tài liệu bài học"));
        JScrollPane documentScroll = new JScrollPane(outDocuments);
        documentScroll.setColumnHeaderView(outDocuments.getTableHeader());
        documentPanel.add(documentScroll, BorderLayout.CENTER);
        documentPanel.add(downloadButtons, BorderLayout.SOUTH);

        JPanel lessonContent = new JPanel(new BorderLayout(10, 10));
        lessonContent.add(video, BorderLayout.NORTH);
        lessonContent.add(documentPanel, BorderLayout.CENTER);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, lessonScroll, lessonContent);
        split.setDividerLocation(230);
        split.setResizeWeight(0.28);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.add(subReload);
        rightActions.add(subBack);
        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.add(outNotice, BorderLayout.NORTH);
        footer.add(rightActions, BorderLayout.SOUTH);
        subOpenVideo.addActionListener(this);
        subQuiz.addActionListener(this);
        subDownload.addActionListener(this);
        subReload.addActionListener(this);
        subBack.addActionListener(this);
        subOpenVideo.setEnabled(false);
        subQuiz.setEnabled(false);
        subDownload.setEnabled(false);

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));
        add(headerPanel, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
        applyFont(this);
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        outLesson.setFont(new Font("Segoe UI", Font.BOLD, 13));
        outDocuments.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        subOpenVideo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        subDownload.setFont(new Font("Segoe UI", Font.BOLD, 13));
    }

    private void checkAccess() throws SQLException {
        if (student == null || student.getId() == null || course == null || course.getId() == null) {
            throw new SQLException("Thông tin tài khoản hoặc khóa học không hợp lệ.");
        }
        try (CourseMemberDAO dao = new CourseMemberDAO()) {
            CourseMember member = dao.getByStudentAndCourse(student.getId(), course.getId());
            if (member == null) {
                throw new SQLException("Bạn chưa ghi danh khóa học này.");
            }
            if (member.getStudent().getRole() != UserRole.STUDENT
                    || !Boolean.TRUE.equals(member.getStudent().getIsActive())) {
                throw new SQLException("Tài khoản không có quyền học hoặc đã bị khóa.");
            }
            CourseStatus status = member.getCourse().getStatus();
            if (status != CourseStatus.OPEN && status != CourseStatus.CLOSED) {
                throw new SQLException("Khóa học chưa được xuất bản.");
            }
            course.setTitle(member.getCourse().getTitle());
            course.setStatus(status);
        }
    }

    private void loadLessons() throws SQLException {
        clearLesson();
        outLessons.clearSelection();
        lessonModel.clear();
        lessons.clear();
        checkAccess();
        try (LessonDAO dao = new LessonDAO()) {
            lessons = dao.getLessonsByCourse(course.getId());
        }
        for (Lesson lesson : lessons) {
            lessonModel.addElement(lesson.getOrderIndex() + ". " + lesson.getTitle());
        }
        outCourse.setText(course.getTitle());
        outNotice.setText(lessons.isEmpty() ? "Khóa học hiện chưa có bài học được hiển thị."
                : "Chọn bài học trong danh sách bên trái.");
    }

    private Lesson checkLesson() throws SQLException {
        checkAccess();
        int selected = outLessons.getSelectedIndex();
        if (selected < 0) {
            throw new SQLException("Vui lòng chọn một bài học.");
        }
        try (LessonDAO dao = new LessonDAO()) {
            Lesson lesson = dao.findById(lessons.get(selected).getId());
            if (lesson == null || !Boolean.TRUE.equals(lesson.getIsVisible())
                    || !course.getId().equals(lesson.getCourse().getId())) {
                throw new SQLException("Bài học không còn được cung cấp trong khóa học này.");
            }
            return lesson;
        }
    }

    private void clearLesson() {
        subOpenVideo.setEnabled(false);
        subQuiz.setEnabled(false);
        subDownload.setEnabled(false);
        outLesson.setText("Chọn một bài học để bắt đầu.");
        outVideoUrl.setText("");
        documentModel.setRowCount(0);
        documents.clear();
    }

    private URI getVideoUri(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Bài học chưa có đường dẫn video.");
        }
        URI uri = URI.create(url.strip());
        String scheme = uri.getScheme();
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        boolean shortUrl = host.equals("youtu.be") || host.equals("www.youtu.be");
        boolean youtube = host.equals("youtube.com") || host.equals("www.youtube.com")
                || host.equals("m.youtube.com") || host.equals("youtube-nocookie.com")
                || host.equals("www.youtube-nocookie.com");
        if ((!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme))
                || (!shortUrl && !youtube) || uri.getUserInfo() != null) {
            throw new IllegalArgumentException("Đường dẫn video phải là liên kết YouTube hợp lệ.");
        }
        String path = uri.getPath() == null ? "" : uri.getPath();
        String videoId = "";
        if (shortUrl) {
            videoId = path.startsWith("/") ? path.substring(1) : path;
        } else if (path.equals("/watch") && uri.getRawQuery() != null) {
            for (String parameter : uri.getRawQuery().split("&")) {
                String[] pair = parameter.split("=", 2);
                if (pair.length == 2 && pair[0].equals("v")) {
                    videoId = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                }
            }
        } else {
            String[] parts = path.split("/");
            if (parts.length == 3 && (parts[1].equals("embed") || parts[1].equals("shorts")
                    || parts[1].equals("live"))) {
                videoId = parts[2];
            }
        }
        if (!videoId.matches("[A-Za-z0-9_-]{11}")) {
            throw new IllegalArgumentException("Không tìm thấy mã video YouTube hợp lệ trong đường dẫn.");
        }
        return uri;
    }

    private void downloadDocument(Long documentId, Path destination, boolean overwrite) throws SQLException, IOException {
        Lesson lesson = checkLesson();
        LessonDocument document = null;
        try (LessonDocumentDAO dao = new LessonDocumentDAO()) {
            for (LessonDocument candidate : dao.getDocumentsByLesson(lesson.getId())) {
                if (documentId.equals(candidate.getId()) && Boolean.TRUE.equals(candidate.getIsAvailable())) {
                    document = candidate;
                    break;
                }
            }
        }
        if (document == null) {
            throw new SQLException("Tài liệu không còn được cung cấp cho bài học này.");
        }
        Path source = Path.of(document.getFilePath()).toRealPath();
        if (!Files.isRegularFile(source)) {
            throw new IOException("Không tìm thấy file tài liệu.");
        }
        if (Files.exists(destination) && !Files.isRegularFile(destination)) {
            throw new IOException("Vui lòng chọn tên file để lưu, không chọn thư mục.");
        }
        if (Files.exists(destination) && Files.isSameFile(source, destination)) {
            throw new IOException("Vui lòng chọn nơi lưu khác với file tài liệu gốc.");
        }
        if (overwrite) {
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.copy(source, destination);
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == subBack) {
            if (parentFrame == null) {
                MainFrm.showHome(this, student);
            } else {
                MainFrm.showScreen(this, parentFrame, "Chi tiết khóa học");
            }
        } else if (event.getSource() == subReload) {
            try {
                loadLessons();
            } catch (SQLException e) {
                outNotice.setText(e.getMessage());
                JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể tải bài học", JOptionPane.ERROR_MESSAGE);
            }
        } else if ("lesson".equals(event.getActionCommand())) {
            clearLesson();
            try {
                Lesson lesson = checkLesson();
                try (LessonDocumentDAO dao = new LessonDocumentDAO()) {
                    for (LessonDocument document : dao.getDocumentsByLesson(lesson.getId())) {
                        if (Boolean.TRUE.equals(document.getIsAvailable())) {
                            documents.add(document);
                            long size = document.getFileSize() == null ? 0 : document.getFileSize();
                            String sizeText = size >= 1024 * 1024
                                    ? String.format(Locale.ROOT, "%.2f MB", size / (1024.0 * 1024))
                                    : size >= 1024 ? String.format(Locale.ROOT, "%.1f KB", size / 1024.0)
                                    : size + " B";
                            documentModel.addRow(new Object[]{document.getName(), sizeText});
                        }
                    }
                }
                outLesson.setText(lesson.getOrderIndex() + ". " + lesson.getTitle());
                outVideoUrl.setText(lesson.getVideoUrl());
                subOpenVideo.setEnabled(true);
                subQuiz.setEnabled(true);
                outNotice.setText(documents.isEmpty() ? "Bài học này chưa có tài liệu được cung cấp."
                        : "Chọn tài liệu rồi bấm Tải tài liệu để lưu về máy.");
            } catch (SQLException e) {
                clearLesson();
                outNotice.setText(e.getMessage());
                JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể mở bài học", JOptionPane.ERROR_MESSAGE);
            }
        } else if (event.getSource() == subOpenVideo) {
            try {
                URI uri = getVideoUri(checkLesson().getVideoUrl());
                if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    throw new UnsupportedOperationException("Máy tính không hỗ trợ mở trình duyệt mặc định.");
                }
                Desktop.getDesktop().browse(uri);
                outNotice.setText("Đã gửi yêu cầu mở video. Cần kết nối Internet để xem YouTube.");
            } catch (SQLException | IOException | IllegalArgumentException | UnsupportedOperationException | SecurityException e) {
                outNotice.setText(e.getMessage());
                JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể mở video", JOptionPane.ERROR_MESSAGE);
            }
        } else if (event.getSource() == subQuiz) {
            try {
                Lesson lesson = checkLesson();
                if (quizListFrm != null && quizListFrm.isDisplayable()) {
                    quizListFrm.toFront();
                    quizListFrm.requestFocus();
                } else {
                    quizListFrm = new QuizListFrm(student, lesson);
                    quizListFrm.setVisible(true);
                }
            } catch (SQLException e) {
                clearLesson();
                outNotice.setText(e.getMessage());
                JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể mở danh sách quiz", JOptionPane.ERROR_MESSAGE);
                try {
                    loadLessons();
                } catch (SQLException ignored) {
                }
            }
        } else if (event.getSource() == subDownload) {
            int selected = outDocuments.getSelectedRow();
            if (selected < 0) {
                return;
            }
            LessonDocument document = documents.get(outDocuments.convertRowIndexToModel(selected));
            try {
                checkLesson();
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Lưu tài liệu bài học");
                chooser.setSelectedFile(new File(Path.of(document.getName()).getFileName().toString()));
                if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                Path destination = chooser.getSelectedFile().toPath();
                boolean overwrite = Files.exists(destination);
                if (overwrite && JOptionPane.showConfirmDialog(this, "File đã tồn tại. Bạn muốn ghi đè?",
                        "Xác nhận ghi đè", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }
                downloadDocument(document.getId(), destination, overwrite);
                outNotice.setText("Đã tải tài liệu: " + document.getName());
                JOptionPane.showMessageDialog(this, "Tải tài liệu thành công.");
            } catch (SQLException | IOException | IllegalArgumentException | SecurityException e) {
                outNotice.setText("Tải tài liệu thất bại: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Tải tài liệu thất bại: " + e.getMessage(),
                        "Không thể tải tài liệu", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void applyFont(Component component) {
        component.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                applyFont(child);
            }
        }
    }

}
