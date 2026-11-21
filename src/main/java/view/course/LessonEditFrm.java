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
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import model.Course;
import model.Lesson;
import model.LessonDocument;
import model.User;

public class LessonEditFrm extends JFrame implements ActionListener {
    private final User teacher;
    private final Course course;
    private final Lesson lesson;
    private final Runnable onUpdated;

    private LessonDAO lessonDAO;
    private LessonDocumentDAO lessonDocumentDAO;

    private JTextField txtLessonTitle;
    private JSpinner spnOrderIndex;
    private JTextField txtVideoUrl;
    private JButton btnTestVideo;
    private JButton btnChooseAttachment;
    private JLabel lblAttachmentName;
    private JCheckBox chkIsVisible;
    private JButton btnSave;
    private JButton btnCancel;

    private File selectedAttachmentFile = null;

    public LessonEditFrm(User teacher, Course course, Lesson lesson, Runnable onUpdated) {
        super("Sửa thông tin bài học - COURSESS");
        this.teacher = teacher;
        this.course = course;
        this.lesson = lesson;
        this.onUpdated = onUpdated;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(700, 420);
        setLocationRelativeTo(null);

        try {
            this.lessonDAO = new LessonDAO();
            this.lessonDocumentDAO = new LessonDocumentDAO();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        initComponents();
        populateData();
    }

    public LessonEditFrm(User teacher, Course course, Lesson lesson, CourseReviewFrm parentReviewFrm) {
        this(teacher, course, lesson, parentReviewFrm != null ? parentReviewFrm::loadCourseDetails : null);
    }

    public LessonEditFrm(User teacher, Course course, Lesson lesson) {
        this(teacher, course, lesson, (Runnable) null);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));

        JPanel headerPanel = new JPanel(new BorderLayout(4, 4));
        headerPanel.setBorder(BorderFactory.createEtchedBorder());
        JPanel headerContent = new JPanel(new BorderLayout(4, 4));
        headerContent.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        String cTitle = (course != null && course.getTitle() != null) ? course.getTitle() : "Khóa học";
        JLabel lblHeader = new JLabel("CHỈNH SỬA THÔNG TIN BÀI HỌC");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 17));

        JLabel lblSub = new JLabel("Khóa học: " + cTitle + " | Mã bài học: " + (lesson != null ? lesson.getId() : "-"));
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        headerContent.add(lblHeader, BorderLayout.NORTH);
        headerContent.add(lblSub, BorderLayout.SOUTH);
        headerPanel.add(headerContent, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Thông tin bài học"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        JLabel lblTitle = new JLabel("Tiêu đề bài học (*):");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        inputPanel.add(lblTitle, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.8;
        txtLessonTitle = new JTextField();
        txtLessonTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtLessonTitle.setPreferredSize(new Dimension(320, 28));
        inputPanel.add(txtLessonTitle, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        JLabel lblOrder = new JLabel("Thứ tự bài học (*):");
        lblOrder.setFont(new Font("Segoe UI", Font.BOLD, 13));
        inputPanel.add(lblOrder, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.8;
        spnOrderIndex = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spnOrderIndex.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        spnOrderIndex.setPreferredSize(new Dimension(80, 28));
        JPanel spnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        spnPanel.add(spnOrderIndex);
        inputPanel.add(spnPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        JLabel lblVideo = new JLabel("URL Video YouTube:");
        lblVideo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        inputPanel.add(lblVideo, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.8;
        JPanel videoPanel = new JPanel(new BorderLayout(6, 0));
        txtVideoUrl = new JTextField();
        txtVideoUrl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtVideoUrl.setPreferredSize(new Dimension(280, 28));
        btnTestVideo = new JButton("Xem thử");
        btnTestVideo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnTestVideo.addActionListener(this);
        videoPanel.add(txtVideoUrl, BorderLayout.CENTER);
        videoPanel.add(btnTestVideo, BorderLayout.EAST);
        inputPanel.add(videoPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        JLabel lblDoc = new JLabel("Tài liệu đính kèm:");
        lblDoc.setFont(new Font("Segoe UI", Font.BOLD, 13));
        inputPanel.add(lblDoc, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.8;
        JPanel docPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnChooseAttachment = new JButton("Chọn tệp mới...");
        btnChooseAttachment.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnChooseAttachment.addActionListener(this);
        lblAttachmentName = new JLabel("Chưa có tệp đính kèm");
        lblAttachmentName.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        docPanel.add(btnChooseAttachment);
        docPanel.add(lblAttachmentName);
        inputPanel.add(docPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        JLabel lblStatus = new JLabel("Trạng thái:");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        inputPanel.add(lblStatus, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.8;
        chkIsVisible = new JCheckBox("Hiển thị bài học cho học sinh", true);
        chkIsVisible.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        inputPanel.add(chkIsVisible, gbc);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancel.addActionListener(this);

        btnSave = new JButton("Lưu thay đổi");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.addActionListener(this);

        bottomPanel.add(btnCancel);
        bottomPanel.add(btnSave);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void populateData() {
        if (lesson == null) return;
        txtLessonTitle.setText(lesson.getTitle() != null ? lesson.getTitle() : "");
        if (lesson.getOrderIndex() != null) {
            spnOrderIndex.setValue(lesson.getOrderIndex());
        }
        txtVideoUrl.setText(lesson.getVideoUrl() != null ? lesson.getVideoUrl() : "");
        chkIsVisible.setSelected(lesson.getIsVisible() == null || lesson.getIsVisible());

        if (lesson.getDocuments() != null && !lesson.getDocuments().isEmpty()) {
            lblAttachmentName.setText(lesson.getDocuments().get(0).getName() + " (Hiện có)");
        } else {
            lblAttachmentName.setText("Chưa có tệp đính kèm");
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
            lblAttachmentName.setText(f.getName() + " (Mới chọn)");
        }
    }

    private void saveLesson() {
        String title = txtLessonTitle.getText().trim();
        int orderIndex = (Integer) spnOrderIndex.getValue();
        String videoUrl = txtVideoUrl.getText().trim();
        boolean isVisible = chkIsVisible.isSelected();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tiêu đề bài học không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtLessonTitle.requestFocus();
            return;
        }

        try {
            if (course != null && course.getId() != null) {
                boolean duplicate = lessonDAO.existsByCourseIdAndOrderIndexAndNotId(course.getId(), orderIndex, lesson.getId());
                if (duplicate) {
                    JOptionPane.showMessageDialog(this, "Thứ tự bài học " + orderIndex + " đã được sử dụng bởi bài học khác trong khóa học này!\nVui lòng chọn số thứ tự khác.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    spnOrderIndex.requestFocus();
                    return;
                }
            }

            lesson.setTitle(title);
            lesson.setOrderIndex(orderIndex);
            lesson.setVideoUrl(videoUrl);
            lesson.setIsVisible(isVisible);

            boolean success = lessonDAO.updateLesson(lesson);
            if (success) {
                if (selectedAttachmentFile != null && lessonDocumentDAO != null) {
                    saveAttachment(lesson.getId(), selectedAttachmentFile);
                }

                JOptionPane.showMessageDialog(this, "Cập nhật thông tin bài học thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);

                if (onUpdated != null) {
                    onUpdated.run();
                }

                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể cập nhật bài học trong CSDL!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật bài học: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
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
        if (!cleanUrl.startsWith("http:") && !cleanUrl.startsWith("https:")) {
            cleanUrl = "https:" + (cleanUrl.startsWith("/") ? "" : "//") + cleanUrl;
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
        if (event.getSource() == btnSave) {
            saveLesson();
        } else if (event.getSource() == btnCancel) {
            dispose();
        } else if (event.getSource() == btnChooseAttachment) {
            chooseAttachment();
        } else if (event.getSource() == btnTestVideo) {
            openVideo(txtVideoUrl.getText().trim());
        }
    }
}
