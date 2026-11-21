package view.course;

import dao.CourseMemberDAO;
import java.awt.BorderLayout;
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
        subBack.addActionListener(this);
        outsubCourses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        outsubCourses.setRowHeight(32);
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

        JPanel buttons = new JPanel();
        buttons.add(subBack);
        JPanel footer = new JPanel(new BorderLayout());
        footer.add(outNotice, BorderLayout.NORTH);
        footer.add(buttons, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(10, 15));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.add(new JLabel("Khóa học đã tham gia"), BorderLayout.NORTH);
        content.add(new JScrollPane(outsubCourses), BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);
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
}
