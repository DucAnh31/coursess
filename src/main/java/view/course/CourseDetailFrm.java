package view.course;

import dao.CourseDAO;
import dao.CourseMemberDAO;
import dao.LessonDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import model.Course;
import model.CourseMember;
import model.Lesson;
import model.User;
import model.enums.CourseStatus;
import model.enums.UserRole;
import view.MainFrm;

public class CourseDetailFrm extends JPanel implements ActionListener {
    private final JPanel parentFrame;
    private final User student;
    private Course course;
    private CourseMember member;
    private final JLabel lblThumbnail = new JLabel();
    private final JLabel lblTitle = new JLabel();
    private final JLabel lblTeacher = new JLabel();
    private final JLabel lblCategory = new JLabel();
    private final JLabel lblStatus = new JLabel();
    private final JLabel outEnrollment = new JLabel(" ");
    private final JLabel outNotice = new JLabel(" ");
    private final JTextArea txtDescription = new JTextArea(4, 40);
    private final DefaultListModel<String> lessonModel = new DefaultListModel<>();
    private final JList<String> outLessons = new JList<>(lessonModel);
    private final JPanel outCourse = new JPanel(new BorderLayout(20, 10));
    private final JButton subEnroll = new JButton("Ghi danh");
    private final JButton subOpenLearning = new JButton("Vào học");
    private final JButton subBack = new JButton("Quay lại");

    public CourseDetailFrm(CourseListFrm parentFrame, User student, Course course) throws SQLException {
        this((JPanel) parentFrame, student, course);
    }

    public CourseDetailFrm(JPanel parentFrame, User student, Course course) throws SQLException {
        this.parentFrame = parentFrame;
        this.student = student;
        this.course = course;
        setLayout(new BorderLayout());
        initComponents();
        try (CourseDAO dao = new CourseDAO()) {
            Course detail = dao.getPublicDetail(course.getId());
            if (detail == null) {
                throw new SQLException("Khóa học không tồn tại hoặc không còn được phép xem.");
            }
            this.course = detail;
        }
        try (LessonDAO dao = new LessonDAO()) {
            this.course.setLessons(dao.getLessonsByCourse(this.course.getId()));
        }
        try (CourseMemberDAO dao = new CourseMemberDAO()) {
            member = dao.getByStudentAndCourse(student.getId(), this.course.getId());
        }
        lblThumbnail.setIcon(loadThumbnail(this.course.getThumbnailUrl(), 200, 112));
        lblTitle.setText("Tên khóa học: " + this.course.getTitle());
        lblTeacher.setText("Giáo viên: " + this.course.getTeacher().getFullName());
        lblCategory.setText("Danh mục: " + this.course.getCategory().getName());
        lblStatus.setText("Trạng thái: " + (this.course.getStatus() == CourseStatus.OPEN
                ? "Đang mở ghi danh" : "Đã ngừng ghi danh"));
        txtDescription.setText(this.course.getDescription());
        txtDescription.setCaretPosition(0);
        for (Lesson lesson : this.course.getLessons()) {
            lessonModel.addElement(lesson.getOrderIndex() + ". " + lesson.getTitle());
        }
        boolean isStudent = student != null && student.getRole() == UserRole.STUDENT;
        subEnroll.setEnabled(isStudent && member == null && this.course.getStatus() == CourseStatus.OPEN);
        subOpenLearning.setEnabled(isStudent && member != null);
        outEnrollment.setText(member == null ? "Chưa ghi danh" : "Đã ghi danh");
        if (!isStudent) {
            outNotice.setText("Chỉ tài khoản học sinh mới được ghi danh và tham gia học.");
        } else if (member == null && this.course.getStatus() == CourseStatus.CLOSED) {
            outNotice.setText("Khóa học đã ngừng nhận ghi danh mới.");
        }
    }

    private void initComponents() {
        JPanel headerPanel = new JPanel(new BorderLayout(4, 4));
        headerPanel.setBorder(BorderFactory.createEtchedBorder());
        JPanel headerContent = new JPanel(new BorderLayout(4, 4));
        headerContent.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel title = new JLabel("THÔNG TIN CHI TIẾT KHÓA HỌC");
        headerContent.add(title, BorderLayout.NORTH);
        headerContent.add(new JLabel("Xem thông tin khóa học, nội dung bài học và tình trạng ghi danh."),
                BorderLayout.SOUTH);
        headerPanel.add(headerContent, BorderLayout.CENTER);

        JPanel information = new JPanel(new GridLayout(4, 1, 5, 8));
        information.add(lblTitle);
        information.add(lblTeacher);
        information.add(lblCategory);
        information.add(lblStatus);
        outCourse.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Thông tin tổng quan khóa học"),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        outCourse.add(lblThumbnail, BorderLayout.WEST);
        outCourse.add(information, BorderLayout.CENTER);

        txtDescription.setEditable(false);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setRows(3);
        JScrollPane description = new JScrollPane(txtDescription);
        description.setBorder(BorderFactory.createTitledBorder("Mô tả khóa học"));
        outLessons.setFixedCellHeight(26);
        JScrollPane lessons = new JScrollPane(outLessons);
        lessons.setBorder(BorderFactory.createTitledBorder("Danh sách bài học"));
        JPanel lessonContent = new JPanel(new BorderLayout(10, 10));
        lessonContent.add(description, BorderLayout.NORTH);
        lessonContent.add(lessons, BorderLayout.CENTER);
        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.add(outCourse, BorderLayout.NORTH);
        body.add(lessonContent, BorderLayout.CENTER);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftActions.add(subEnroll);
        leftActions.add(subOpenLearning);
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.add(subBack);
        JPanel buttons = new JPanel(new BorderLayout());
        buttons.add(leftActions, BorderLayout.WEST);
        buttons.add(rightActions, BorderLayout.EAST);
        subEnroll.addActionListener(this);
        subOpenLearning.addActionListener(this);
        subBack.addActionListener(this);
        subEnroll.setEnabled(false);
        subOpenLearning.setEnabled(false);
        JPanel notices = new JPanel(new BorderLayout(10, 0));
        notices.add(outEnrollment, BorderLayout.WEST);
        notices.add(outNotice, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.add(notices, BorderLayout.NORTH);
        footer.add(buttons, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));
        content.add(headerPanel, BorderLayout.NORTH);
        content.add(body, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);
        applyFont(this);
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        outEnrollment.setFont(new Font("Segoe UI", Font.BOLD, 13));
        subEnroll.setFont(new Font("Segoe UI", Font.BOLD, 13));
        subOpenLearning.setFont(new Font("Segoe UI", Font.BOLD, 13));
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == subBack) {
            MainFrm.showScreen(this, parentFrame, parentFrame instanceof MyCoursesFrm
                    ? "Khóa học đã tham gia" : "Danh sách khóa học");
        } else if (event.getSource() == subEnroll) {
            if (student == null || student.getRole() != UserRole.STUDENT) {
                JOptionPane.showMessageDialog(this, "Chỉ tài khoản học sinh mới được ghi danh khóa học.",
                        "Không có quyền ghi danh", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try (CourseMemberDAO dao = new CourseMemberDAO()) {
                member = dao.enroll(student.getId(), course.getId());
                subEnroll.setEnabled(false);
                subOpenLearning.setEnabled(true);
                outEnrollment.setText("Đã ghi danh");
                outNotice.setText("Ghi danh thành công.");
                JOptionPane.showMessageDialog(this, "Ghi danh thành công.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể ghi danh", JOptionPane.ERROR_MESSAGE);
                outNotice.setText(e.getMessage());
            }
        } else if (event.getSource() == subOpenLearning && member != null) {
            try {
                MainFrm.showScreen(this, new CourseLearningFrm(student, course, this), "Học theo khóa học");
            } catch (SQLException e) {
                outNotice.setText(e.getMessage());
                JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể vào học", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private ImageIcon loadThumbnail(String path, int width, int height) {
        BufferedImage image = null;
        if (path != null && !path.isBlank()) {
            try {
                if (path.startsWith("https://") || path.startsWith("http://")) {
                    URLConnection connection = URI.create(path).toURL().openConnection();
                    connection.setConnectTimeout(3000);
                    connection.setReadTimeout(3000);
                    try (InputStream stream = connection.getInputStream()) {
                        image = ImageIO.read(stream);
                    }
                } else {
                    image = ImageIO.read(new File(path));
                }
            } catch (IOException | IllegalArgumentException e) {
                image = null;
            }
        }
        if (image == null) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(new Color(230, 230, 230));
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(Color.DARK_GRAY);
            FontMetrics metrics = graphics.getFontMetrics();
            graphics.drawString("COURSESS", (width - metrics.stringWidth("COURSESS")) / 2,
                    (height + metrics.getAscent() - metrics.getDescent()) / 2);
            graphics.dispose();
        }
        return new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
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
