package view.course;

import dao.CourseDAO;
import dao.LessonDAO;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Course;
import model.Lesson;
import model.User;
import model.enums.CourseStatus;
import model.enums.UserRole;
import view.quiz.ManageQuizFrm;

public class TeacherCourseListFrm extends JFrame implements ActionListener {
    private final User teacher;
    private List<Course> allCourses = new ArrayList<>();
    private List<Course> displayedCourses = new ArrayList<>();
    private CourseDAO courseDAO;

    private JTextField txtSearch;
    private JComboBox<String> cboStatusFilter;
    private JButton btnFilter;
    private JButton btnResetFilter;

    private JTable tblCourses;
    private DefaultTableModel tableModel;
    private JButton btnCreateCourse;
    private JButton btnEditCourse;
    private JButton btnCreateQuiz;
    private JButton btnOpenCourse;
    private JButton btnAddLesson;
    private JButton btnRefresh;
    private JButton btnExit;

    public TeacherCourseListFrm() {
        this(createDefaultTeacher());
    }

    public TeacherCourseListFrm(User teacher) {
        super("Quản lý khóa học - COURSESS");
        this.teacher = teacher != null ? teacher : createDefaultTeacher();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(960, 600);
        setLocationRelativeTo(null);

        try {
            this.courseDAO = new CourseDAO();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        initComponents();
        loadCourses();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                loadCourses();
            }
        });
    }

    private static User createDefaultTeacher() {
        User defaultTeacher = new User();
        defaultTeacher.setId(2L);
        defaultTeacher.setFullName("Nguyễn Minh Anh");
        defaultTeacher.setUsername("teacher01");
        defaultTeacher.setRole(UserRole.TEACHER);
        return defaultTeacher;
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));

        JPanel headerPanel = new JPanel(new BorderLayout(4, 4));
        headerPanel.setBorder(BorderFactory.createEtchedBorder());
        JPanel headerContent = new JPanel(new BorderLayout(4, 4));
        headerContent.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel lblHeader = new JLabel("DANH SÁCH KHÓA HỌC PHỤ TRÁCH");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 17));

        JLabel lblSub = new JLabel("Giảng viên: " + teacher.getFullName() + " (@" + teacher.getUsername() + ")");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        headerContent.add(lblHeader, BorderLayout.NORTH);
        headerContent.add(lblSub, BorderLayout.SOUTH);
        headerPanel.add(headerContent, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Tìm kiếm & Bộ lọc khóa học"));

        JLabel lblSearch = new JLabel("Từ khóa:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtSearch = new JTextField(18);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.addActionListener(this);

        JLabel lblStatus = new JLabel("Trạng thái:");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        String[] statuses = {"-- Tất cả trạng thái --", "Bản nháp (DRAFT)", "Đang mở (OPEN)", "Ngừng ghi danh (CLOSED)"};
        cboStatusFilter = new JComboBox<>(statuses);
        cboStatusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cboStatusFilter.addActionListener(this);

        btnFilter = new JButton("Tìm kiếm");
        btnFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnFilter.addActionListener(this);

        btnResetFilter = new JButton("Đặt lại");
        btnResetFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnResetFilter.addActionListener(this);

        filterPanel.add(lblSearch);
        filterPanel.add(txtSearch);
        filterPanel.add(lblStatus);
        filterPanel.add(cboStatusFilter);
        filterPanel.add(btnFilter);
        filterPanel.add(btnResetFilter);

        topPanel.add(filterPanel, BorderLayout.SOUTH);

        String[] columnNames = {"Mã KH", "Tên khóa học", "Danh mục", "Số bài học", "Trạng thái"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblCourses = new JTable(tableModel);
        tblCourses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblCourses.setRowHeight(26);
        tblCourses.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblCourses.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        tblCourses.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblCourses.getColumnModel().getColumn(1).setPreferredWidth(320);
        tblCourses.getColumnModel().getColumn(2).setPreferredWidth(160);
        tblCourses.getColumnModel().getColumn(3).setPreferredWidth(90);
        tblCourses.getColumnModel().getColumn(4).setPreferredWidth(160);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tblCourses.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblCourses.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tblCourses.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        tblCourses.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedCourse();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblCourses);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách khóa học"));

        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnCreateCourse = new JButton("+ Tạo khóa học mới");
        btnCreateCourse.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCreateCourse.addActionListener(this);

        btnEditCourse = new JButton("Sửa thông tin");
        btnEditCourse.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnEditCourse.addActionListener(this);

        btnCreateQuiz = new JButton("Tạo quiz");
        btnCreateQuiz.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCreateQuiz.addActionListener(this);

        btnAddLesson = new JButton("Thêm bài học");
        btnAddLesson.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnAddLesson.addActionListener(this);

        btnOpenCourse = new JButton("Xem lại & Xuất bản");
        btnOpenCourse.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnOpenCourse.addActionListener(this);

        leftActions.add(btnCreateCourse);
        leftActions.add(btnEditCourse);
        leftActions.add(btnCreateQuiz);
        leftActions.add(btnAddLesson);
        leftActions.add(btnOpenCourse);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRefresh = new JButton("Làm mới");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnRefresh.addActionListener(this);

        btnExit = new JButton("Thoát");
        btnExit.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnExit.addActionListener(this);

        rightActions.add(btnRefresh);
        rightActions.add(btnExit);

        bottomPanel.add(leftActions, BorderLayout.WEST);
        bottomPanel.add(rightActions, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    public void loadCourses() {
        if (courseDAO == null || teacher == null || teacher.getId() == null) {
            return;
        }
        try {
            allCourses = courseDAO.getCoursesOfTeacher(teacher.getId());
            applyFilter();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách khóa học: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilter() {
        String keyword = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";
        int statusIndex = cboStatusFilter != null ? cboStatusFilter.getSelectedIndex() : 0;

        displayedCourses.clear();
        tableModel.setRowCount(0);

        for (Course c : allCourses) {
            boolean matchKeyword = keyword.isEmpty()
                    || (c.getTitle() != null && c.getTitle().toLowerCase().contains(keyword))
                    || (c.getCategory() != null && c.getCategory().getName() != null && c.getCategory().getName().toLowerCase().contains(keyword));

            boolean matchStatus = true;
            if (statusIndex == 1) {
                matchStatus = c.getStatus() == CourseStatus.DRAFT;
            } else if (statusIndex == 2) {
                matchStatus = c.getStatus() == CourseStatus.OPEN;
            } else if (statusIndex == 3) {
                matchStatus = c.getStatus() == CourseStatus.CLOSED;
            }

            if (matchKeyword && matchStatus) {
                displayedCourses.add(c);
                int lessonCount = 0;
                try {
                    lessonCount = courseDAO.getLessonCount(c.getId());
                } catch (SQLException ignored) {
                }

                String statusDisplay = formatStatus(c.getStatus());
                String categoryName = (c.getCategory() != null && c.getCategory().getName() != null)
                        ? c.getCategory().getName()
                        : "Chưa phân loại";

                tableModel.addRow(new Object[]{
                        c.getId(),
                        c.getTitle(),
                        categoryName,
                        lessonCount + " bài học",
                        statusDisplay
                });
            }
        }
    }

    private String formatStatus(CourseStatus status) {
        if (status == null) {
            return "Chưa rõ";
        }
        switch (status) {
            case DRAFT:
                return "Bản nháp (DRAFT)";
            case OPEN:
                return "Đang mở (OPEN)";
            case CLOSED:
                return "Ngừng ghi danh (CLOSED)";
            default:
                return status.name();
        }
    }

    private Course getSelectedCourse() {
        int selectedRow = tblCourses.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= displayedCourses.size()) {
            return null;
        }
        return displayedCourses.get(selectedRow);
    }

    private void editSelectedCourse() {
        Course selected = getSelectedCourse();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khóa học trong bảng để chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new CourseCreateFrm(teacher, selected).setVisible(true);
    }

    private void openCreateQuiz() {
        Course selected = getSelectedCourse();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khóa học trong bảng để tạo quiz!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            LessonDAO lessonDAO = new LessonDAO();
            List<Lesson> courseLessons = lessonDAO.getLessonsOfCourse(selected.getId());
            if (courseLessons.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Khóa học này chưa có bài học nào. Vui lòng thêm bài học trước khi tạo quiz!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (courseLessons.size() == 1) {
                new ManageQuizFrm(teacher, courseLessons.get(0)).setVisible(true);
                return;
            }
            String[] lessonTitles = courseLessons.stream()
                    .map(l -> l.getOrderIndex() + ". " + l.getTitle())
                    .toArray(String[]::new);
            String picked = (String) JOptionPane.showInputDialog(
                    this,
                    "Chọn bài học cần tạo quiz:",
                    "Tạo quiz cho bài học",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    lessonTitles,
                    lessonTitles[0]
            );
            if (picked != null) {
                for (int i = 0; i < lessonTitles.length; i++) {
                    if (lessonTitles[i].equals(picked)) {
                        new ManageQuizFrm(teacher, courseLessons.get(i)).setVisible(true);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải bài học: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSelectedCourse() {
        Course selected = getSelectedCourse();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khóa học trong bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new CourseReviewFrm(teacher, selected).setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == btnCreateCourse) {
            CourseCreateFrm createFrm = new CourseCreateFrm(teacher);
            createFrm.setVisible(true);
        } else if (event.getSource() == btnEditCourse) {
            editSelectedCourse();
        } else if (event.getSource() == btnCreateQuiz) {
            openCreateQuiz();
        } else if (event.getSource() == btnOpenCourse) {
            openSelectedCourse();
        } else if (event.getSource() == btnAddLesson) {
            Course selected = getSelectedCourse();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn khóa học cần thêm bài học!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            new LessonAddFrm(teacher, selected).setVisible(true);
        } else if (event.getSource() == btnRefresh) {
            loadCourses();
        } else if (event.getSource() == btnFilter) {
            applyFilter();
        } else if (event.getSource() == btnResetFilter) {
            txtSearch.setText("");
            cboStatusFilter.setSelectedIndex(0);
            applyFilter();
        } else if (event.getSource() == btnExit) {
            dispose();
        }
    }
}
