package view.course;

import dao.LessonDAO;
import dao.LessonDocumentDAO;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Course;
import model.Lesson;
import model.LessonDocument;
import model.User;
import view.quiz.ManageQuizFrm;

public class LessonAddFrm extends JFrame implements ActionListener {
    private final User teacher;
    private final Course course;
    private LessonDAO lessonDAO;
    private LessonDocumentDAO lessonDocumentDAO;

    private JLabel lblCourseTitle;
    private JTextField txtLessonTitle;
    private JSpinner spnOrderIndex;
    private JTextField txtVideoUrl;
    private JButton btnTestVideo;
    private JButton btnChooseAttachment;
    private JLabel lblAttachmentName;
    private JButton btnAddLesson;
    private JTable tblLessons;
    private DefaultTableModel tableModel;
    private JButton btnReviewCourse;
    private JButton btnCreateQuiz;
    private JButton btnEditLesson;
    private JButton btnClose;

    private File selectedAttachmentFile = null;
    private List<Lesson> lessons = new ArrayList<>();

    public LessonAddFrm(User teacher, Course course) {
        super("Quản lý bài học - COURSESS");
        this.teacher = teacher;
        this.course = course;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(960, 660);
        setLocationRelativeTo(null);

        try {
            this.lessonDAO = new LessonDAO();
            this.lessonDocumentDAO = new LessonDocumentDAO();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        initComponents();
        loadLessons();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));

        JPanel headerPanel = new JPanel(new BorderLayout(4, 4));
        headerPanel.setBorder(BorderFactory.createEtchedBorder());
        JPanel headerContent = new JPanel(new BorderLayout(4, 4));
        headerContent.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        String courseName = (course != null && course.getTitle() != null) ? course.getTitle() : "Khóa học";
        lblCourseTitle = new JLabel("QUẢN LÝ BÀI HỌC: " + courseName);
        lblCourseTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));

        JLabel lblSub = new JLabel("Thêm các bài học vào giáo trình của khóa học trước khi tiến hành xuất bản.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        headerContent.add(lblCourseTitle, BorderLayout.NORTH);
        headerContent.add(lblSub, BorderLayout.SOUTH);
        headerPanel.add(headerContent, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Thêm bài học mới"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.15;
        JLabel lblTitle = new JLabel("Tiêu đề bài học (*):");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        inputPanel.add(lblTitle, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.55;
        txtLessonTitle = new JTextField();
        txtLessonTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtLessonTitle.setPreferredSize(new Dimension(280, 28));
        inputPanel.add(txtLessonTitle, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.1;
        JLabel lblOrder = new JLabel("Thứ tự (*):");
        lblOrder.setFont(new Font("Segoe UI", Font.BOLD, 13));
        inputPanel.add(lblOrder, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.2;
        spnOrderIndex = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spnOrderIndex.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        spnOrderIndex.setPreferredSize(new Dimension(80, 28));
        inputPanel.add(spnOrderIndex, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.15;
        JLabel lblVideo = new JLabel("URL Video YouTube:");
        lblVideo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        inputPanel.add(lblVideo, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.55;
        JPanel videoPanel = new JPanel(new BorderLayout(5, 0));
        txtVideoUrl = new JTextField("https://www.youtube.com/watch?v=demo");
        txtVideoUrl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnTestVideo = new JButton("Xem thử");
        btnTestVideo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnTestVideo.addActionListener(this);
        videoPanel.add(txtVideoUrl, BorderLayout.CENTER);
        videoPanel.add(btnTestVideo, BorderLayout.EAST);
        inputPanel.add(videoPanel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.1;
        JLabel lblDoc = new JLabel("Tài liệu (≤20MB):");
        lblDoc.setFont(new Font("Segoe UI", Font.BOLD, 13));
        inputPanel.add(lblDoc, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.2;
        JPanel docPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        btnChooseAttachment = new JButton("Chọn tệp...");
        btnChooseAttachment.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnChooseAttachment.addActionListener(this);

        lblAttachmentName = new JLabel("Chưa chọn");
        lblAttachmentName.setFont(new Font("Segoe UI", Font.ITALIC, 11));

        docPanel.add(btnChooseAttachment);
        docPanel.add(lblAttachmentName);
        inputPanel.add(docPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        JPanel addBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        btnAddLesson = new JButton("+ Thêm bài học vào khóa");
        btnAddLesson.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAddLesson.addActionListener(this);
        addBtnPanel.add(btnAddLesson);
        inputPanel.add(addBtnPanel, gbc);

        String[] columnNames = {"Thứ tự", "Mã bài học", "Tiêu đề bài học", "URL Video", "Tài liệu đính kèm"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tblLessons = new JTable(tableModel);
        tblLessons.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblLessons.setRowHeight(26);
        tblLessons.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblLessons.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        tblLessons.getColumnModel().getColumn(0).setPreferredWidth(70);
        tblLessons.getColumnModel().getColumn(1).setPreferredWidth(90);
        tblLessons.getColumnModel().getColumn(2).setPreferredWidth(320);
        tblLessons.getColumnModel().getColumn(3).setPreferredWidth(260);
        tblLessons.getColumnModel().getColumn(4).setPreferredWidth(160);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tblLessons.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblLessons.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        tblLessons.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tblLessons.getSelectedRow();
                    if (row >= 0 && row < lessons.size()) {
                        openVideo(lessons.get(row).getVideoUrl());
                    }
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(tblLessons);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Danh sách bài học hiện có của khóa học"));

        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(tableScroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnCreateQuiz = new JButton("Tạo quiz cho bài học");
        btnCreateQuiz.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCreateQuiz.addActionListener(this);
        leftActions.add(btnCreateQuiz);

        btnEditLesson = new JButton("Sửa thông tin bài học");
        btnEditLesson.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnEditLesson.addActionListener(this);
        leftActions.add(btnEditLesson);
        bottomPanel.add(leftActions, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnClose.addActionListener(this);

        btnReviewCourse = new JButton("Xem lại khóa học (Chuẩn bị xuất bản) ->");
        btnReviewCourse.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnReviewCourse.addActionListener(this);

        rightActions.add(btnClose);
        rightActions.add(btnReviewCourse);
        bottomPanel.add(rightActions, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    public void loadLessons() {
        if (lessonDAO == null || course == null || course.getId() == null) return;
        tableModel.setRowCount(0);
        try {
            lessons = lessonDAO.getLessonsOfCourse(course.getId());
            for (Lesson l : lessons) {
                String docName = "Không có";
                if (l.getDocuments() != null && !l.getDocuments().isEmpty()) {
                    docName = l.getDocuments().get(0).getName();
                }
                tableModel.addRow(new Object[]{
                        l.getOrderIndex(),
                        l.getId(),
                        l.getTitle(),
                        l.getVideoUrl(),
                        docName
                });
            }

            int nextOrder = lessonDAO.getNextOrderIndex(course.getId());
            spnOrderIndex.setValue(nextOrder);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách bài học: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void chooseAttachment() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn tài liệu bài học (PDF, DOCX, PPTX, TXT)");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Tài liệu học tập (*.pdf, *.docx, *.pptx, *.txt)", "pdf", "docx", "pptx", "txt"));
        int res = fileChooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            long maxBytes = 20 * 1024 * 1024L;
            if (f.length() > maxBytes) {
                JOptionPane.showMessageDialog(this, "Dung lượng tệp vượt quá 20MB. Vui lòng chọn tệp nhỏ hơn!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            selectedAttachmentFile = f;
            lblAttachmentName.setText(f.getName());
        }
    }

    private void addLesson() {
        String title = txtLessonTitle.getText().trim();
        int orderIndex = (Integer) spnOrderIndex.getValue();
        String videoUrl = txtVideoUrl.getText().trim();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tiêu đề bài học không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtLessonTitle.requestFocus();
            return;
        }

        try {
            boolean duplicate = lessonDAO.existsByCourseIdAndOrderIndex(course.getId(), orderIndex);
            if (duplicate) {
                JOptionPane.showMessageDialog(this, "Thứ tự bài học " + orderIndex + " đã được sử dụng trong khóa học này!\nVui lòng chọn số thứ tự khác.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                spnOrderIndex.requestFocus();
                return;
            }

            Lesson lesson = new Lesson();
            lesson.setTitle(title);
            lesson.setOrderIndex(orderIndex);
            lesson.setVideoUrl(videoUrl);
            lesson.setCourse(course);

            boolean success = lessonDAO.addLesson(lesson, course.getId());
            if (success && lesson.getId() != null) {
                if (selectedAttachmentFile != null && lessonDocumentDAO != null) {
                    saveAttachment(lesson.getId(), selectedAttachmentFile);
                }

                JOptionPane.showMessageDialog(this, "Thêm bài học thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);

                txtLessonTitle.setText("");
                selectedAttachmentFile = null;
                lblAttachmentName.setText("Chưa chọn");
                loadLessons();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể lưu bài học vào CSDL!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu bài học: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAttachment(Long lessonId, File file) {
        try {
            File uploadDir = new File("uploads/documents");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String storedFileName = System.currentTimeMillis() + "_" + file.getName();
            File destFile = new File(uploadDir, storedFileName);
            Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            LessonDocument doc = new LessonDocument();
            doc.setName(file.getName());
            doc.setFilePath(destFile.getPath());
            doc.setFileSize(file.length());
            doc.setIsAvailable(true);

            Lesson lessonRef = new Lesson();
            lessonRef.setId(lessonId);
            doc.setLesson(lessonRef);

            lessonDocumentDAO.save(doc);
        } catch (IOException | SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu tệp tài liệu đính kèm: " + e.getMessage(), "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openVideo(String url) {
        if (url == null || url.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Đường dẫn video trống!", "Thông báo", JOptionPane.WARNING_MESSAGE);
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

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == btnCreateQuiz) {
            int selectedRow = tblLessons.getSelectedRow();
            if (selectedRow < 0 || selectedRow >= lessons.size()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một bài học trong bảng để tạo quiz!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Lesson selectedLesson = lessons.get(selectedRow);
            new ManageQuizFrm(teacher, selectedLesson).setVisible(true);
        } else if (event.getSource() == btnEditLesson) {
            int selectedRow = tblLessons.getSelectedRow();
            if (selectedRow < 0 || selectedRow >= lessons.size()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một bài học trong bảng để sửa thông tin!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Lesson selectedLesson = lessons.get(selectedRow);
            new LessonEditFrm(teacher, course, selectedLesson, this::loadLessons).setVisible(true);
        } else if (event.getSource() == btnChooseAttachment) {
            chooseAttachment();
        } else if (event.getSource() == btnTestVideo) {
            openVideo(txtVideoUrl.getText().trim());
        } else if (event.getSource() == btnAddLesson) {
            addLesson();
        } else if (event.getSource() == btnReviewCourse) {
            new CourseReviewFrm(teacher, course).setVisible(true);
            dispose();
        } else if (event.getSource() == btnClose) {
            dispose();
        }
    }
}
