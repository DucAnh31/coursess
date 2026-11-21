package view.course;

import dao.CourseDAO;
import dao.LessonDAO;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Course;
import model.Lesson;
import model.User;
import model.enums.CourseStatus;
import view.quiz.ManageQuizFrm;

public class CourseReviewFrm extends JFrame implements ActionListener {
    private final User teacher;
    private final Course course;
    private CourseDAO courseDAO;
    private LessonDAO lessonDAO;

    private JLabel lblTitleValue;
    private JLabel lblCategoryValue;
    private JLabel lblStatusValue;
    private JLabel lblLessonCountValue;
    private JLabel lblDescValue;
    private JLabel lblThumbnailPreview;

    private JLabel lblCheckTitleDesc;
    private JLabel lblCheckCategory;
    private JLabel lblCheckLessons;

    private JTable tblLessons;
    private DefaultTableModel tableModel;

    private JButton btnPublish;
    private JButton btnAddLesson;
    private JButton btnEditCourse;
    private JButton btnCreateQuiz;
    private JButton btnWatchVideo;
    private JButton btnBack;

    private List<Lesson> lessons = new ArrayList<>();

    public CourseReviewFrm(User teacher, Course course) {
        super("Xem lại khóa học & Xuất bản - COURSESS");
        this.teacher = teacher;
        this.course = course;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(960, 680);
        setLocationRelativeTo(null);

        try {
            this.courseDAO = new CourseDAO();
            this.lessonDAO = new LessonDAO();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                loadCourseDetails();
            }
        });

        initComponents();
        loadCourseDetails();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));

        JPanel headerPanel = new JPanel(new BorderLayout(4, 4));
        headerPanel.setBorder(BorderFactory.createEtchedBorder());
        JPanel headerContent = new JPanel(new BorderLayout(4, 4));
        headerContent.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel lblHeader = new JLabel("XEM LẠI TỔNG THỂ KHÓA HỌC TRƯỚC KHI XUẤT BẢN");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 17));

        JLabel lblSub = new JLabel("Kiểm tra kỹ thông tin khóa học và nội dung bài học. Khi xuất bản, khóa học sẽ được mở công khai.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        headerContent.add(lblHeader, BorderLayout.NORTH);
        headerContent.add(lblSub, BorderLayout.SOUTH);
        headerPanel.add(headerContent, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        JPanel topDetailsPanel = new JPanel(new BorderLayout(8, 8));

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Thông tin tổng quan khóa học"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblT = new JLabel("Tên khóa học:");
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 13));
        infoPanel.add(lblT, gbc);

        gbc.gridx = 1;
        lblTitleValue = new JLabel("-");
        lblTitleValue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        infoPanel.add(lblTitleValue, gbc);

        gbc.gridx = 2;
        JLabel lblS = new JLabel("Trạng thái hiện tại:");
        lblS.setFont(new Font("Segoe UI", Font.BOLD, 13));
        infoPanel.add(lblS, gbc);

        gbc.gridx = 3;
        lblStatusValue = new JLabel("-");
        lblStatusValue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        infoPanel.add(lblStatusValue, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblC = new JLabel("Danh mục:");
        lblC.setFont(new Font("Segoe UI", Font.BOLD, 13));
        infoPanel.add(lblC, gbc);

        gbc.gridx = 1;
        lblCategoryValue = new JLabel("-");
        lblCategoryValue.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        infoPanel.add(lblCategoryValue, gbc);

        gbc.gridx = 2;
        JLabel lblL = new JLabel("Tổng số bài học:");
        lblL.setFont(new Font("Segoe UI", Font.BOLD, 13));
        infoPanel.add(lblL, gbc);

        gbc.gridx = 3;
        lblLessonCountValue = new JLabel("0 bài học");
        lblLessonCountValue.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        infoPanel.add(lblLessonCountValue, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 1;
        JLabel lblD = new JLabel("Mô tả:");
        lblD.setFont(new Font("Segoe UI", Font.BOLD, 13));
        infoPanel.add(lblD, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        lblDescValue = new JLabel("-");
        lblDescValue.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoPanel.add(lblDescValue, gbc);

        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 3;
        gbc.insets = new Insets(4, 14, 4, 8);
        gbc.anchor = GridBagConstraints.CENTER;
        lblThumbnailPreview = new JLabel("Chưa có ảnh bìa", SwingConstants.CENTER);
        lblThumbnailPreview.setPreferredSize(new Dimension(130, 80));
        lblThumbnailPreview.setBorder(BorderFactory.createEtchedBorder());
        lblThumbnailPreview.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoPanel.add(lblThumbnailPreview, gbc);

        JPanel checklistPanel = new JPanel(new GridLayout(3, 1, 4, 4));
        checklistPanel.setBorder(BorderFactory.createTitledBorder("Kiểm tra điều kiện xuất bản"));

        lblCheckTitleDesc = new JLabel("• Tên và mô tả khóa học: Đạt (Đã điền thông tin)");
        lblCheckTitleDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblCheckCategory = new JLabel("• Danh mục khóa học: Đạt (Đã phân loại danh mục)");
        lblCheckCategory.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblCheckLessons = new JLabel("• Số lượng bài học: Cần có ít nhất 1 bài học (Hiện có: 0 bài)");
        lblCheckLessons.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        checklistPanel.add(lblCheckTitleDesc);
        checklistPanel.add(lblCheckCategory);
        checklistPanel.add(lblCheckLessons);

        topDetailsPanel.add(infoPanel, BorderLayout.CENTER);
        topDetailsPanel.add(checklistPanel, BorderLayout.SOUTH);

        String[] columnNames = {"Thứ tự", "Mã bài học", "Tiêu đề bài học", "URL Video YouTube", "Tài liệu đính kèm"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
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
                    openSelectedLessonVideo();
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(tblLessons);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Danh sách các bài học sẽ được xuất bản"));

        centerPanel.add(topDetailsPanel, BorderLayout.NORTH);
        centerPanel.add(tableScroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnAddLesson = new JButton("+ Thêm bài học khác");
        btnAddLesson.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnAddLesson.addActionListener(this);
        leftActions.add(btnAddLesson);

        btnEditCourse = new JButton("Sửa thông tin");
        btnEditCourse.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnEditCourse.addActionListener(this);
        leftActions.add(btnEditCourse);

        btnCreateQuiz = new JButton("+ Tạo quiz");
        btnCreateQuiz.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCreateQuiz.addActionListener(this);
        leftActions.add(btnCreateQuiz);

        btnWatchVideo = new JButton("Mở video bài học");
        btnWatchVideo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnWatchVideo.addActionListener(this);
        leftActions.add(btnWatchVideo);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnBack = new JButton("Quay lại danh sách");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnBack.addActionListener(this);

        btnPublish = new JButton("Xuất bản khóa học (OPEN)");
        btnPublish.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnPublish.addActionListener(this);

        rightActions.add(btnBack);
        rightActions.add(btnPublish);

        bottomPanel.add(leftActions, BorderLayout.WEST);
        bottomPanel.add(rightActions, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    public void loadCourseDetails() {
        if (courseDAO == null || course == null || course.getId() == null) return;

        try {
            Course freshCourse = courseDAO.findById(course.getId());
            if (freshCourse != null) {
                course.setTitle(freshCourse.getTitle());
                course.setDescription(freshCourse.getDescription());
                course.setCategory(freshCourse.getCategory());
                course.setStatus(freshCourse.getStatus());
                course.setThumbnailUrl(freshCourse.getThumbnailUrl());
            }

            lblTitleValue.setText(course.getTitle() != null ? course.getTitle() : "Chưa đặt tên");
            lblDescValue.setText(course.getDescription() != null && !course.getDescription().isBlank() ? course.getDescription() : "Chưa có mô tả");

            String categoryName = (course.getCategory() != null && course.getCategory().getName() != null)
                    ? course.getCategory().getName() : "Chưa phân loại";
            lblCategoryValue.setText(categoryName);

            updateStatusLabel(course.getStatus());

            String thumbPath = course.getThumbnailUrl();
            if (thumbPath != null && !thumbPath.isBlank()) {
                File f = new File(thumbPath);
                if (f.exists()) {
                    try {
                        ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                        Image img = icon.getImage().getScaledInstance(130, 80, Image.SCALE_SMOOTH);
                        lblThumbnailPreview.setIcon(new ImageIcon(img));
                        lblThumbnailPreview.setText("");
                    } catch (Exception ex) {
                        lblThumbnailPreview.setIcon(null);
                        lblThumbnailPreview.setText("Lỗi ảnh bìa");
                    }
                } else {
                    lblThumbnailPreview.setIcon(null);
                    lblThumbnailPreview.setText("Ảnh không tồn tại");
                }
            } else {
                lblThumbnailPreview.setIcon(null);
                lblThumbnailPreview.setText("Chưa có ảnh bìa");
            }

            lessons = lessonDAO.getLessonsOfCourse(course.getId());
            tableModel.setRowCount(0);

            for (Lesson l : lessons) {
                String docInfo = "Không có";
                if (l.getDocuments() != null && !l.getDocuments().isEmpty()) {
                    docInfo = l.getDocuments().size() + " tệp đính kèm";
                }
                tableModel.addRow(new Object[]{
                        l.getOrderIndex(),
                        l.getId(),
                        l.getTitle(),
                        (l.getVideoUrl() != null && !l.getVideoUrl().isBlank()) ? l.getVideoUrl() : "Chưa có URL",
                        docInfo
                });
            }

            lblLessonCountValue.setText(lessons.size() + " bài học");

            if (course.getTitle() != null && !course.getTitle().isBlank()
                    && course.getDescription() != null && !course.getDescription().isBlank()) {
                lblCheckTitleDesc.setText("• Tên và mô tả khóa học: Đạt (Đã đầy đủ)");
            } else {
                lblCheckTitleDesc.setText("• Tên và mô tả khóa học: Chưa đạt (Cần điền đầy đủ tên và mô tả)");
            }

            if (course.getCategory() != null && course.getCategory().getId() != null) {
                lblCheckCategory.setText("• Danh mục khóa học: Đạt (" + categoryName + ")");
            } else {
                lblCheckCategory.setText("• Danh mục khóa học: Chưa đạt (Chưa chọn danh mục)");
            }

            if (lessons.isEmpty()) {
                lblCheckLessons.setText("• Số lượng bài học: Cần có ít nhất 1 bài học (Hiện có: 0 bài - Chưa đạt)");
            } else {
                lblCheckLessons.setText("• Số lượng bài học: Đạt (Hiện có " + lessons.size() + " bài học trong giáo trình)");
            }

            if (course.getStatus() == CourseStatus.OPEN) {
                btnPublish.setEnabled(false);
                btnPublish.setText("Khóa học đã xuất bản (OPEN)");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Không thể tải chi tiết khóa học: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStatusLabel(CourseStatus status) {
        if (status == null) {
            lblStatusValue.setText("Chưa rõ");
            return;
        }
        switch (status) {
            case DRAFT:
                lblStatusValue.setText("BẢN NHÁP (DRAFT)");
                break;
            case OPEN:
                lblStatusValue.setText("ĐANG MỞ GHI DANH (OPEN)");
                break;
            case CLOSED:
                lblStatusValue.setText("NGỪNG GHI DANH (CLOSED)");
                break;
            default:
                lblStatusValue.setText(status.name());
                break;
        }
    }

    private void publishCourse() {
        if (lessons == null || lessons.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Khóa học phải có ít nhất một bài học hợp lệ mới có thể xuất bản!\nVui lòng nhấn \"+ Thêm bài học khác\" trước.",
                    "Không đủ điều kiện xuất bản",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (course.getStatus() == CourseStatus.OPEN) {
            JOptionPane.showMessageDialog(this, "Khóa học này đã ở trạng thái Đang mở ghi danh (OPEN) rồi!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xuất bản khóa học \"" + course.getTitle() + "\" không?\n" +
                "Sau khi xuất bản, học sinh có thể nhìn thấy và đăng ký học.",
                "Xác nhận xuất bản khóa học",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean success = courseDAO.publishCourse(course.getId());
            if (success) {
                course.setStatus(CourseStatus.OPEN);
                updateStatusLabel(CourseStatus.OPEN);
                btnPublish.setEnabled(false);
                btnPublish.setText("Khóa học đã xuất bản (OPEN)");

                JOptionPane.showMessageDialog(
                        this,
                        "Xuất bản khóa học thành công!\nTrạng thái khóa học đã chuyển sang Đang mở ghi danh (OPEN).",
                        "Xuất bản thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Không thể cập nhật trạng thái xuất bản trong CSDL!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi xuất bản khóa học: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openCreateQuiz() {
        int selectedRow = tblLessons.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= lessons.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bài học trong bảng để tạo quiz!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Lesson selectedLesson = lessons.get(selectedRow);
        new ManageQuizFrm(teacher, selectedLesson).setVisible(true);
    }

    private void openSelectedLessonVideo() {
        int selectedRow = tblLessons.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= lessons.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bài học trong bảng để mở video!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Lesson selectedLesson = lessons.get(selectedRow);
        String url = selectedLesson.getVideoUrl();
        if (url == null || url.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bài học \"" + selectedLesson.getTitle() + "\" chưa có đường dẫn video!", "Thông báo", JOptionPane.WARNING_MESSAGE);
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
        if (event.getSource() == btnPublish) {
            publishCourse();
        } else if (event.getSource() == btnEditCourse) {
            new CourseCreateFrm(teacher, course).setVisible(true);
        } else if (event.getSource() == btnCreateQuiz) {
            openCreateQuiz();
        } else if (event.getSource() == btnWatchVideo) {
            openSelectedLessonVideo();
        } else if (event.getSource() == btnAddLesson) {
            new LessonAddFrm(teacher, course).setVisible(true);
            dispose();
        } else if (event.getSource() == btnBack) {
            dispose();
        }
    }
}
