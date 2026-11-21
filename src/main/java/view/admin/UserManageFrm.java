package view.admin;

import dao.UserDAO;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import model.User;
import model.enums.UserRole;
import view.MainFrm;

public class UserManageFrm extends JPanel implements ActionListener {
    private final User admin;
    private final JTextField inKeyword = new JTextField(26);
    private final JButton subSearch = new JButton("Tìm kiếm");
    private final JButton subLock = new JButton("Khóa tài khoản");
    private final JButton subUnlock = new JButton("Mở khóa");
    private final JButton subReload = new JButton("Làm mới");
    private final JButton subBack = new JButton("Quay lại");
    private final JLabel outUsername = new JLabel("-");
    private final JLabel outFullName = new JLabel("-");
    private final JLabel outRole = new JLabel("-");
    private final JLabel outStatus = new JLabel("-");
    private final JLabel outNotice = new JLabel(" ");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Mã", "Tên đăng nhập", "Họ tên", "Vai trò", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == 0 ? Long.class : String.class;
        }
    };
    private final JTable outsubUsers = new JTable(tableModel);
    private List<User> users = new ArrayList<>();
    private User selectedUser;

    public UserManageFrm(User admin) throws SQLException {
        if (admin == null || admin.getId() == null) {
            throw new IllegalArgumentException("Thông tin quản trị viên không hợp lệ.");
        }
        this.admin = admin;
        setLayout(new BorderLayout(12, 12));
        initComponents();
        loadUsers();
    }

    private void initComponents() {
        JPanel header = new JPanel(new BorderLayout(4, 4));
        header.setBorder(BorderFactory.createEtchedBorder());
        JPanel headerContent = new JPanel(new BorderLayout(4, 4));
        headerContent.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel title = new JLabel("QUẢN LÝ TÀI KHOẢN NGƯỜI DÙNG");
        headerContent.add(title, BorderLayout.NORTH);
        headerContent.add(new JLabel("Tìm kiếm, xem thông tin và khóa hoặc mở khóa tài khoản."), BorderLayout.SOUTH);
        header.add(headerContent, BorderLayout.CENTER);
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filters.setBorder(BorderFactory.createTitledBorder("Tìm kiếm tài khoản"));
        filters.add(new JLabel("Tên đăng nhập / họ tên:"));
        filters.add(inKeyword);
        filters.add(subSearch);
        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(header, BorderLayout.NORTH);
        top.add(filters, BorderLayout.SOUTH);

        outsubUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        outsubUsers.setAutoCreateRowSorter(true);
        outsubUsers.setRowHeight(26);
        outsubUsers.getColumnModel().getColumn(0).setMaxWidth(65);
        outsubUsers.getColumnModel().getColumn(1).setPreferredWidth(170);
        outsubUsers.getColumnModel().getColumn(2).setPreferredWidth(250);
        outsubUsers.getColumnModel().getColumn(3).setPreferredWidth(140);
        outsubUsers.getColumnModel().getColumn(4).setPreferredWidth(140);
        outsubUsers.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                int row = outsubUsers.getSelectedRow();
                if (row < 0) {
                    clearDetails();
                    return;
                }
                Long id = users.get(outsubUsers.convertRowIndexToModel(row)).getId();
                try (UserDAO dao = new UserDAO()) {
                    checkAdmin(dao);
                    selectedUser = dao.findById(id);
                    if (selectedUser == null) {
                        throw new SQLException("Tài khoản không còn tồn tại. Vui lòng bấm Làm mới.");
                    }
                    outUsername.setText(selectedUser.getUsername());
                    outFullName.setText(selectedUser.getFullName());
                    outFullName.setToolTipText(selectedUser.getFullName());
                    outRole.setText(formatRole(selectedUser.getRole()));
                    boolean active = Boolean.TRUE.equals(selectedUser.getIsActive());
                    boolean self = admin.getId().equals(selectedUser.getId());
                    outStatus.setText(active ? "Đang hoạt động" : "Đã khóa");
                    subLock.setEnabled(active && !self);
                    subUnlock.setEnabled(!active && !self);
                    outNotice.setText(self ? "Đây là tài khoản đang sử dụng. Bạn không được tự khóa tài khoản này."
                            : "Đang chọn tài khoản: " + selectedUser.getUsername());
                } catch (SQLException e) {
                    clearDetails();
                    outNotice.setText(e.getMessage());
                    JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể xem tài khoản", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        JScrollPane tableScroll = new JScrollPane(outsubUsers);
        tableScroll.setColumnHeaderView(outsubUsers.getTableHeader());
        tableScroll.setBorder(BorderFactory.createTitledBorder("Danh sách tài khoản"));
        JPanel details = new JPanel(new GridLayout(2, 4, 12, 10));
        details.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Thông tin tài khoản đang chọn"),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        details.add(new JLabel("Tên đăng nhập:"));
        details.add(outUsername);
        details.add(new JLabel("Họ tên:"));
        details.add(outFullName);
        details.add(new JLabel("Vai trò:"));
        details.add(outRole);
        details.add(new JLabel("Trạng thái:"));
        details.add(outStatus);
        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.add(tableScroll, BorderLayout.CENTER);
        body.add(details, BorderLayout.SOUTH);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftActions.add(subLock);
        leftActions.add(subUnlock);
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.add(subReload);
        rightActions.add(subBack);
        JPanel buttons = new JPanel(new BorderLayout());
        buttons.add(leftActions, BorderLayout.WEST);
        buttons.add(rightActions, BorderLayout.EAST);
        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.add(outNotice, BorderLayout.NORTH);
        footer.add(buttons, BorderLayout.SOUTH);
        subSearch.addActionListener(this);
        inKeyword.addActionListener(this);
        subLock.addActionListener(this);
        subUnlock.addActionListener(this);
        subReload.addActionListener(this);
        subBack.addActionListener(this);
        subLock.setEnabled(false);
        subUnlock.setEnabled(false);
        putClientProperty("defaultButton", subSearch);

        setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));
        add(top, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
        applyFont(this);
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        outsubUsers.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        subLock.setFont(new Font("Segoe UI", Font.BOLD, 13));
        subUnlock.setFont(new Font("Segoe UI", Font.BOLD, 13));
    }

    private void checkAdmin(UserDAO dao) throws SQLException {
        User current = dao.findById(admin.getId());
        if (current == null || current.getRole() != UserRole.ADMIN || !Boolean.TRUE.equals(current.getIsActive())) {
            admin.setIsActive(false);
            throw new SQLException("Tài khoản không còn quyền quản trị hoặc đã bị khóa. Vui lòng đăng xuất.");
        }
        admin.setFullName(current.getFullName());
        admin.setRole(current.getRole());
        admin.setIsActive(true);
    }

    private void loadUsers() throws SQLException {
        List<User> result;
        try (UserDAO dao = new UserDAO()) {
            checkAdmin(dao);
            result = dao.searchUsers(inKeyword.getText());
        }
        outsubUsers.clearSelection();
        clearDetails();
        users = result;
        tableModel.setRowCount(0);
        for (User user : users) {
            tableModel.addRow(new Object[]{user.getId(), user.getUsername(), user.getFullName(),
                formatRole(user.getRole()), Boolean.TRUE.equals(user.getIsActive()) ? "Đang hoạt động" : "Đã khóa"});
        }
        outNotice.setText(users.isEmpty() ? "Không tìm thấy tài khoản phù hợp."
                : "Tìm thấy " + users.size() + " tài khoản. Chọn một dòng để xem thông tin.");
    }

    private void clearDetails() {
        selectedUser = null;
        outUsername.setText("-");
        outFullName.setText("-");
        outFullName.setToolTipText(null);
        outRole.setText("-");
        outStatus.setText("-");
        subLock.setEnabled(false);
        subUnlock.setEnabled(false);
    }

    private String formatRole(UserRole role) {
        return switch (role) {
            case ADMIN -> "Quản trị viên";
            case TEACHER -> "Giáo viên";
            default -> "Học sinh";
        };
    }

    private void updateActive(boolean active) {
        if (selectedUser == null) {
            return;
        }
        Long userId = selectedUser.getId();
        String action = active ? "mở khóa" : "khóa";
        int choice = JOptionPane.showConfirmDialog(this, "Bạn muốn " + action + " tài khoản "
                + selectedUser.getUsername() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try (UserDAO dao = new UserDAO()) {
            dao.updateActive(admin.getId(), userId, active);
        } catch (IllegalArgumentException | SQLException e) {
            outNotice.setText(e.getMessage());
            JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể cập nhật tài khoản", JOptionPane.ERROR_MESSAGE);
            return;
        }
        clearDetails();
        try {
            loadUsers();
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getId().equals(userId)) {
                    int row = outsubUsers.convertRowIndexToView(i);
                    outsubUsers.setRowSelectionInterval(row, row);
                    break;
                }
            }
            outNotice.setText(active ? "Mở khóa tài khoản thành công." : "Khóa tài khoản thành công.");
        } catch (SQLException e) {
            outNotice.setText("Đã cập nhật trạng thái nhưng chưa tải lại được danh sách. Vui lòng bấm Làm mới.");
            JOptionPane.showMessageDialog(this, outNotice.getText(), "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == subBack) {
            MainFrm.showHome(this, admin);
        } else if (event.getSource() == subLock && subLock.isEnabled()) {
            updateActive(false);
        } else if (event.getSource() == subUnlock && subUnlock.isEnabled()) {
            updateActive(true);
        } else if (event.getSource() == subSearch || event.getSource() == inKeyword || event.getSource() == subReload) {
            try {
                loadUsers();
            } catch (SQLException e) {
                outsubUsers.clearSelection();
                clearDetails();
                users.clear();
                tableModel.setRowCount(0);
                outNotice.setText(e.getMessage());
                JOptionPane.showMessageDialog(this, e.getMessage(), "Không thể tải tài khoản", JOptionPane.ERROR_MESSAGE);
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
