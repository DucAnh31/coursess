package view.admin;

import dao.CategoryDAO;
import dao.UserDAO;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import model.Category;
import model.User;
import model.enums.UserRole;
import view.MainFrm;

public class CategoryManageFrm extends JPanel implements ActionListener {
    private final User admin;
    private final JTextField inName = new JTextField(25);
    private final JTextArea inDescription = new JTextArea(3, 30);
    private final JLabel outEditing = new JLabel("Thêm danh mục mới");
    private final JLabel outNotice = new JLabel(" ");
    private final JButton subNew = new JButton("Thêm mới");
    private final JButton subSave = new JButton("Lưu");
    private final JButton subDelete = new JButton("Xóa");
    private final JButton subReload = new JButton("Làm mới");
    private final JButton subBack = new JButton("Quay lại");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Mã danh mục", "Tên danh mục", "Mô tả"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == 0 ? Long.class : String.class;
        }
    };
    private final JTable outsubCategories = new JTable(tableModel);
    private List<Category> categories = new ArrayList<>();
    private Long selectedCategoryId;

    public CategoryManageFrm(User admin) throws SQLException {
        if (admin == null || admin.getRole() != UserRole.ADMIN || !Boolean.TRUE.equals(admin.getIsActive())) {
            throw new IllegalArgumentException("Chỉ quản trị viên đang hoạt động được quản lý danh mục.");
        }
        this.admin = admin;
        setLayout(new BorderLayout(12, 12));
        initComponents();
        loadCategories();
    }

    private void initComponents() {
        JPanel header = new JPanel(new BorderLayout(4, 4));
        header.setBorder(BorderFactory.createEtchedBorder());
        JPanel headerContent = new JPanel(new BorderLayout(4, 4));
        headerContent.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel title = new JLabel("QUẢN LÝ DANH MỤC KHÓA HỌC");
        headerContent.add(title, BorderLayout.NORTH);
        headerContent.add(new JLabel("Thêm, sửa danh mục. Chỉ xóa danh mục chưa có khóa học sử dụng."),
                BorderLayout.SOUTH);
        header.add(headerContent, BorderLayout.CENTER);

        outsubCategories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        outsubCategories.setAutoCreateRowSorter(true);
        outsubCategories.setRowHeight(26);
        outsubCategories.getColumnModel().getColumn(0).setMinWidth(100);
        outsubCategories.getColumnModel().getColumn(0).setPreferredWidth(110);
        outsubCategories.getColumnModel().getColumn(0).setMaxWidth(110);
        outsubCategories.getColumnModel().getColumn(1).setPreferredWidth(250);
        outsubCategories.getColumnModel().getColumn(2).setPreferredWidth(450);
        outsubCategories.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                int row = outsubCategories.getSelectedRow();
                if (row >= 0) {
                    Category category = categories.get(outsubCategories.convertRowIndexToModel(row));
                    selectedCategoryId = category.getId();
                    inName.setText(category.getName());
                    inDescription.setText(category.getDescription());
                    outEditing.setText("Chỉnh sửa danh mục: " + category.getId());
                    subDelete.setEnabled(true);
                }
            }
        });
        JScrollPane tableScroll = new JScrollPane(outsubCategories);
        tableScroll.setColumnHeaderView(outsubCategories.getTableHeader());
        tableScroll.setBorder(BorderFactory.createTitledBorder("Danh sách danh mục"));

        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(4, 8, 4, 8);
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.gridx = 0;
        constraints.gridy = 0;
        fields.add(new JLabel("Tên danh mục (*):"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        fields.add(inName, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        fields.add(new JLabel("Mô tả:"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        inDescription.setLineWrap(true);
        inDescription.setWrapStyleWord(true);
        fields.add(new JScrollPane(inDescription), constraints);
        JPanel editor = new JPanel(new BorderLayout(8, 8));
        editor.setBorder(BorderFactory.createTitledBorder("Thông tin danh mục"));
        editor.add(outEditing, BorderLayout.NORTH);
        editor.add(fields, BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.add(tableScroll, BorderLayout.CENTER);
        body.add(editor, BorderLayout.SOUTH);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftActions.add(subNew);
        leftActions.add(subSave);
        leftActions.add(subDelete);
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.add(subReload);
        rightActions.add(subBack);
        JPanel buttons = new JPanel(new BorderLayout());
        buttons.add(leftActions, BorderLayout.WEST);
        buttons.add(rightActions, BorderLayout.EAST);
        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.add(outNotice, BorderLayout.NORTH);
        footer.add(buttons, BorderLayout.SOUTH);
        subNew.addActionListener(this);
        subSave.addActionListener(this);
        subDelete.addActionListener(this);
        subReload.addActionListener(this);
        subBack.addActionListener(this);
        subDelete.setEnabled(false);
        putClientProperty("defaultButton", subSave);

        setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));
        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
        applyFont(this);
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        outEditing.setFont(new Font("Segoe UI", Font.BOLD, 13));
        subSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        outsubCategories.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
    }

    private void loadCategories() throws SQLException {
        checkAdmin();
        List<Category> result;
        try (CategoryDAO dao = new CategoryDAO()) {
            result = dao.getAllCategories();
        }
        clearForm();
        categories = result;
        tableModel.setRowCount(0);
        for (Category category : categories) {
            tableModel.addRow(new Object[]{category.getId(), category.getName(), category.getDescription()});
        }
        outNotice.setText(categories.isEmpty() ? "Chưa có danh mục. Bấm Thêm mới để bắt đầu."
                : "Có " + categories.size() + " danh mục. Chọn một dòng để sửa hoặc xóa.");
    }

    private void checkAdmin() throws SQLException {
        try (UserDAO dao = new UserDAO()) {
            User current = dao.findById(admin.getId());
            if (current == null || current.getRole() != UserRole.ADMIN || !Boolean.TRUE.equals(current.getIsActive())) {
                admin.setIsActive(false);
                throw new SQLException("Tài khoản không còn quyền quản trị hoặc đã bị khóa. Vui lòng đăng xuất.");
            }
        }
    }

    private void clearForm() {
        outsubCategories.clearSelection();
        selectedCategoryId = null;
        inName.setText("");
        inDescription.setText("");
        outEditing.setText("Thêm danh mục mới");
        subDelete.setEnabled(false);
    }

    private void refreshCategories(String notice) {
        clearForm();
        try {
            loadCategories();
            outNotice.setText(notice);
        } catch (SQLException e) {
            outNotice.setText(notice + " Chưa tải lại được danh sách. Vui lòng bấm Làm mới.");
            JOptionPane.showMessageDialog(this, outNotice.getText(), "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == subBack) {
            MainFrm.showHome(this, admin);
        } else if (event.getSource() == subNew) {
            clearForm();
            inName.requestFocusInWindow();
            outNotice.setText("Nhập tên và mô tả, sau đó bấm Lưu để thêm danh mục.");
        } else if (event.getSource() == subSave) {
            Category category = new Category();
            category.setId(selectedCategoryId);
            category.setName(inName.getText());
            category.setDescription(inDescription.getText());
            boolean adding = selectedCategoryId == null;
            try (CategoryDAO dao = new CategoryDAO()) {
                checkAdmin();
                if (adding) {
                    dao.addCategory(category);
                } else {
                    dao.updateCategory(category);
                }
            } catch (IllegalArgumentException | SQLException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể lưu danh mục", JOptionPane.ERROR_MESSAGE);
                return;
            }
            refreshCategories(adding ? "Thêm danh mục thành công." : "Cập nhật danh mục thành công.");
        } else if (event.getSource() == subDelete && selectedCategoryId != null) {
            int choice = JOptionPane.showConfirmDialog(this, "Bạn muốn xóa danh mục đang chọn?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
            try (CategoryDAO dao = new CategoryDAO()) {
                checkAdmin();
                dao.deleteCategory(selectedCategoryId);
            } catch (IllegalArgumentException | SQLException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể xóa danh mục", JOptionPane.ERROR_MESSAGE);
                return;
            }
            refreshCategories("Xóa danh mục thành công.");
        } else if (event.getSource() == subReload) {
            try {
                loadCategories();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Không thể tải danh sách danh mục.", "Lỗi", JOptionPane.ERROR_MESSAGE);
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
