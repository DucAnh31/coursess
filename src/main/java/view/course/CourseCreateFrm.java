package view.course;

import dao.CategoryDAO;
import dao.CourseDAO;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import model.Category;
import model.Course;
import model.User;
import model.enums.CourseStatus;

/**
 * Màn hình Tạo khóa học mới.
 * Tham khảo khung form nhập liệu từ prototype.html (Thông tin cơ bản: Tên, Danh mục, Mô tả, Ảnh bìa).
 * Giao diện thuần Swing, loại bỏ màu mè.
 */
public class CourseCreateFrm extends JFrame implements ActionListener {
    private final User teacher;
    private CourseDAO courseDAO;
    private CategoryDAO categoryDAO;

    private JTextField txtTitle;
    private JComboBox<Category> cboCategory;
    private JTextArea txtDescription;
    private JButton btnChooseThumbnail;
    private JLabel lblThumbnailPreview;
    private JLabel lblThumbnailPath;
    private JButton btnSave;
    private JButton btnCancel;

    private String selectedThumbnailPath = null;

    public CourseCreateFrm(User teacher) {
        super("Tạo khóa học mới - COURSESS");
        this.teacher = teacher;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(780, 620);
        setLocationRelativeTo(null);

        try {
            this.courseDAO = new CourseDAO();
            this.categoryDAO = new CategoryDAO();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        initComponents();
        loadCategories();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel headerPanel = new JPanel(new BorderLayout(4, 4));
        headerPanel.setBorder(BorderFactory.createEtchedBorder());
        JPanel headerContent = new JPanel(new BorderLayout(4, 4));
        headerContent.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel lblHeader = new JLabel("TẠO KHÓA HỌC MỚI");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 17));

        JLabel lblSub = new JLabel("Nhập thông tin ban đầu cho khóa học. Khóa học sẽ được tạo ở trạng thái Bản nháp (DRAFT).");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        headerContent.add(lblHeader, BorderLayout.NORTH);
        headerContent.add(lblSub, BorderLayout.SOUTH);
        headerPanel.add(headerContent, BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin khóa học mới"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.22;
        JLabel lblTitle = new JLabel("Tên khóa học (*):");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(lblTitle, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.78;
        txtTitle = new JTextField();
        txtTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTitle.setPreferredSize(new Dimension(300, 28));
        formPanel.add(txtTitle, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.22;
        JLabel lblCategory = new JLabel("Danh mục (*):");
        lblCategory.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(lblCategory, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.78;
        cboCategory = new JComboBox<>();
        cboCategory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboCategory.setPreferredSize(new Dimension(300, 28));
        formPanel.add(cboCategory, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.22;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblDesc = new JLabel("Mô tả khóa học (*):");
        lblDesc.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(lblDesc, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.78;
        txtDescription = new JTextArea(5, 30);
        txtDescription.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setPreferredSize(new Dimension(300, 110));
        formPanel.add(descScroll, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.22;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel lblThumb = new JLabel("Ảnh bìa (Tùy chọn):");
        lblThumb.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(lblThumb, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.78;
        JPanel thumbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnChooseThumbnail = new JButton("Chọn tệp ảnh...");
        btnChooseThumbnail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnChooseThumbnail.addActionListener(this);

        lblThumbnailPath = new JLabel("Chưa chọn tệp ảnh");
        lblThumbnailPath.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        thumbPanel.add(btnChooseThumbnail);
        thumbPanel.add(lblThumbnailPath);
        formPanel.add(thumbPanel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        lblThumbnailPreview = new JLabel("Xem trước ảnh bìa", SwingConstants.CENTER);
        lblThumbnailPreview.setPreferredSize(new Dimension(140, 85));
        lblThumbnailPreview.setBorder(BorderFactory.createEtchedBorder());
        lblThumbnailPreview.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        formPanel.add(lblThumbnailPreview, gbc);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancel.addActionListener(this);

        btnSave = new JButton("Lưu & Tiếp tục thêm bài học ->");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.addActionListener(this);

        bottomPanel.add(btnCancel);
        bottomPanel.add(btnSave);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    public void loadCategories() {
        if (categoryDAO == null) return;
        cboCategory.removeAllItems();
        try {
            List<Category> categories = categoryDAO.getAllCategories();
            for (Category c : categories) {
                cboCategory.addItem(c);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Không thể tải danh mục: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void chooseThumbnail() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn ảnh bìa khóa học");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Hình ảnh (JPG, PNG, GIF, WEBP)", "jpg", "jpeg", "png", "gif", "webp"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            selectedThumbnailPath = file.getAbsolutePath();
            lblThumbnailPath.setText(file.getName());

            try {
                ImageIcon originalIcon = new ImageIcon(selectedThumbnailPath);
                Image img = originalIcon.getImage().getScaledInstance(140, 85, Image.SCALE_SMOOTH);
                lblThumbnailPreview.setIcon(new ImageIcon(img));
                lblThumbnailPreview.setText("");
            } catch (Exception ex) {
                lblThumbnailPreview.setIcon(null);
                lblThumbnailPreview.setText("Lỗi xem trước");
            }
        }
    }

    private void saveCourse() {
        String title = txtTitle.getText().trim();
        Category selectedCategory = (Category) cboCategory.getSelectedItem();
        String description = txtDescription.getText().trim();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên khóa học không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtTitle.requestFocus();
            return;
        }

        if (selectedCategory == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn danh mục cho khóa học!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            cboCategory.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mô tả khóa học không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtDescription.requestFocus();
            return;
        }

        Course course = new Course();
        course.setTitle(title);
        course.setCategory(selectedCategory);
        course.setDescription(description);
        course.setThumbnailUrl(selectedThumbnailPath);
        course.setStatus(CourseStatus.DRAFT);
        course.setTeacher(teacher);

        try {
            boolean success = courseDAO.addCourse(course, teacher.getId());
            if (success && course.getId() != null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Tạo khóa học thành công ở trạng thái Bản nháp (DRAFT)!\nTiếp tục thêm nội dung bài học.",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);

                new LessonAddFrm(teacher, course).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể lưu khóa học vào CSDL!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu khóa học: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == btnChooseThumbnail) {
            chooseThumbnail();
        } else if (event.getSource() == btnSave) {
            saveCourse();
        } else if (event.getSource() == btnCancel) {
            dispose();
        }
    }
}
