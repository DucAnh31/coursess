package view.course;

import dao.CategoryDAO;
import dao.CourseDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import model.Category;
import model.Course;
import model.User;
import view.MainFrm;

public class CourseListFrm extends JPanel implements ActionListener {
    private final User student;
    private final JTextField inKeyword = new JTextField(18);
    private final JComboBox<String> inoutCategory = new JComboBox<>();
    private final JButton subSearch = new JButton("Tìm kiếm");
    private final JButton subBack = new JButton("Quay lại");
    private final JLabel outNotice = new JLabel(" ");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Ảnh bìa", "Tên khóa học", "Danh mục", "Giáo viên"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == 0 ? Icon.class : String.class;
        }
    };
    private final JTable outsubCourses = new JTable(tableModel);
    private final List<Category> categories = new ArrayList<>();
    private List<Course> courses = new ArrayList<>();

    public CourseListFrm(User student) throws SQLException {
        this.student = student;
        setLayout(new BorderLayout());
        initComponents();
        try (CategoryDAO dao = new CategoryDAO()) {
            categories.addAll(dao.getAllCategories());
        }
        try (CourseDAO dao = new CourseDAO()) {
            courses = dao.getPublishedCourses("", null);
        }
        inoutCategory.addItem("Tất cả");
        for (Category category : categories) {
            inoutCategory.addItem(category.getName());
        }
        displayCourses();
    }

    private void initComponents() {
        JPanel filters = new JPanel();
        filters.add(new JLabel("Tên khóa học:"));
        filters.add(inKeyword);
        filters.add(new JLabel("Danh mục:"));
        filters.add(inoutCategory);
        filters.add(subSearch);
        subSearch.addActionListener(this);
        subBack.addActionListener(this);

        outsubCourses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        outsubCourses.setRowHeight(68);
        outsubCourses.setAutoCreateRowSorter(true);
        outsubCourses.getColumnModel().getColumn(0).setMaxWidth(110);
        outsubCourses.getColumnModel().getColumn(1).setPreferredWidth(300);
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
        content.add(filters, BorderLayout.NORTH);
        content.add(new JScrollPane(outsubCourses), BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);
    }

    private void displayCourses() {
        tableModel.setRowCount(0);
        for (Course course : courses) {
            tableModel.addRow(new Object[]{loadThumbnail(course.getThumbnailUrl(), 96, 54),
                course.getTitle(), course.getCategory().getName(), course.getTeacher().getFullName()});
        }
        outNotice.setText(courses.isEmpty() ? "Không có khóa học phù hợp. Bạn có thể đổi điều kiện tìm kiếm."
                : "Tìm thấy " + courses.size() + " khóa học. Nháy đúp vào một dòng để xem chi tiết.");
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == subBack) {
            MainFrm.showHome(this, student);
        } else if (event.getSource() == subSearch) {
            try {
                String keyword = inKeyword.getText();
                int selected = inoutCategory.getSelectedIndex();
                Long categoryId = selected > 0 ? categories.get(selected - 1).getId() : null;
                try (CourseDAO dao = new CourseDAO()) {
                    courses = dao.getPublishedCourses(keyword, categoryId);
                }
                displayCourses();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Không thể tải danh sách khóa học.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else if ("detail".equals(event.getActionCommand())) {
            int selected = outsubCourses.getSelectedRow();
            if (selected < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một khóa học.");
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
}
