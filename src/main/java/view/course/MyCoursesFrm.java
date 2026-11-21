package view.course;

import dao.CourseMemberDAO;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import model.Course;
import model.User;
import model.enums.CourseStatus;
import view.MainFrm;

public class MyCoursesFrm extends JPanel implements ActionListener {
    private final User student;
    private final JButton subBack = new JButton("Quay lại");
    private final JLabel outNotice = new JLabel(" ");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Tên khóa học", "Danh mục", "Giáo viên", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable outsubCourses = new JTable(tableModel);
    private List<Course> courses = new ArrayList<>();

    public MyCoursesFrm(User student) throws SQLException {
        this.student = student;
        setLayout(new BorderLayout());
        initComponents();
        try (CourseMemberDAO dao = new CourseMemberDAO()) {
            courses = dao.getCoursesByStudent(student.getId());
            for (Course course : courses) {
                tableModel.addRow(new Object[]{course.getTitle(), course.getCategory().getName(),
                    course.getTeacher().getFullName(), course.getStatus() == CourseStatus.OPEN
                    ? "Đang mở ghi danh" : "Đã ngừng ghi danh"});
            }
            outNotice.setText(courses.isEmpty() ? "Bạn chưa tham gia khóa học nào."
                    : "Bạn đã tham gia " + courses.size() + " khóa học. Nháy đúp vào một dòng để xem chi tiết.");
        }
    }

    private void initComponents() {
        JPanel headerPanel = new JPanel(new BorderLayout(4, 4));
        headerPanel.setBorder(BorderFactory.createEtchedBorder());
        JPanel headerContent = new JPanel(new BorderLayout(4, 4));
        headerContent.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel title = new JLabel("KHÓA HỌC ĐÃ THAM GIA");
        String studentName = student.getFullName() == null ? "Học sinh" : student.getFullName();
        headerContent.add(title, BorderLayout.NORTH);
        headerContent.add(new JLabel("Học sinh: " + studentName
                + " | Xem các khóa đã ghi danh và tiếp tục học."), BorderLayout.SOUTH);
        headerPanel.add(headerContent, BorderLayout.CENTER);

        subBack.addActionListener(this);
        outsubCourses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        outsubCourses.setRowHeight(26);
        outsubCourses.setAutoCreateRowSorter(true);
        outsubCourses.getColumnModel().getColumn(0).setPreferredWidth(300);
        outsubCourses.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                int row = outsubCourses.rowAtPoint(event.getPoint());
                if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 2 && row >= 0) {
                    outsubCourses.setRowSelectionInterval(row, row);
                    actionPerformed(new ActionEvent(outsubCourses, ActionEvent.ACTION_PERFORMED, "detail"));
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(outsubCourses);
        scrollPane.setColumnHeaderView(outsubCourses.getTableHeader());
        scrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách khóa học đã ghi danh"));

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.add(subBack);
        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.add(outNotice, BorderLayout.NORTH);
        footer.add(rightActions, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));
        content.add(headerPanel, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);
        applyFont(this);
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        outsubCourses.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == subBack) {
            MainFrm.showHome(this, student);
        } else if ("detail".equals(event.getActionCommand())) {
            int selected = outsubCourses.getSelectedRow();
            if (selected < 0) {
                return;
            }
            Course course = courses.get(outsubCourses.convertRowIndexToModel(selected));
            try {
                MainFrm.showScreen(this, new CourseDetailFrm(this, student, course), "Chi tiết khóa học");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể xem khóa học",
                        JOptionPane.ERROR_MESSAGE);
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
