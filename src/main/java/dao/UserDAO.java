package dao;

import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import model.User;
import model.enums.UserRole;
import org.mindrot.jbcrypt.BCrypt;

public class UserDAO extends DAO {
    public UserDAO() throws SQLException {
        super();
    }

    public User checkLogin(String username, char[] password) throws SQLException {
        if (username == null || username.isBlank() || password == null || password.length == 0) {
            return null;
        }
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username.strip());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && BCrypt.checkpw(new String(password), rs.getString("password_hash"))) {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setFullName(rs.getString("full_name"));
                    user.setUsername(rs.getString("username"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setRole(UserRole.valueOf(rs.getString("role")));
                    user.setIsActive(rs.getBoolean("is_active"));
                    return user;
                }
            }
        }
        return null;
    }

    public User register(User user, char[] password) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("Vui lòng nhập thông tin đăng ký.");
        }
        String fullName = validateText(user.getFullName(), "Họ tên");
        String username = validateText(user.getUsername(), "Tên đăng nhập");
        validatePassword(password);
        String passwordHash = BCrypt.hashpw(new String(password), BCrypt.gensalt());
        String sql = "INSERT INTO users (full_name, username, password_hash, role, is_active) "
                + "VALUES (?, ?, ?, 'STUDENT', 1)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fullName);
            ps.setString(2, username);
            ps.setString(3, passwordHash);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Không lấy được mã tài khoản vừa tạo.");
                }
                user.setId(rs.getLong(1));
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new SQLException("Tên đăng nhập đã được sử dụng.", e);
        }
        user.setFullName(fullName);
        user.setUsername(username);
        user.setPasswordHash(passwordHash);
        user.setRole(UserRole.STUDENT);
        user.setIsActive(true);
        return user;
    }

    public void updateFullName(Long userId, String fullName) throws SQLException {
        String sql = "UPDATE users SET full_name = ? WHERE id = ? AND is_active = 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, validateText(fullName, "Họ tên"));
            ps.setObject(2, userId);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Tài khoản không tồn tại hoặc đã bị khóa.");
            }
        }
    }

    public void changePassword(Long userId, char[] currentPassword, char[] newPassword) throws SQLException {
        if (currentPassword == null || currentPassword.length == 0) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu hiện tại.");
        }
        validatePassword(newPassword);
        String passwordHash;
        String sql = "SELECT password_hash FROM users WHERE id = ? AND is_active = 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Tài khoản không tồn tại hoặc đã bị khóa.");
                }
                passwordHash = rs.getString("password_hash");
            }
        }
        if (!BCrypt.checkpw(new String(currentPassword), passwordHash)) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng.");
        }
        sql = "UPDATE users SET password_hash = ? WHERE id = ? AND is_active = 1 AND password_hash = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, BCrypt.hashpw(new String(newPassword), BCrypt.gensalt()));
            ps.setObject(2, userId);
            ps.setString(3, passwordHash);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Tài khoản đã thay đổi hoặc bị khóa. Vui lòng đăng nhập lại.");
            }
        }
    }

    private String validateText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " không được để trống.");
        }
        value = value.strip();
        if (value.length() > 120) {
            throw new IllegalArgumentException(fieldName + " không được vượt quá 120 ký tự.");
        }
        return value;
    }

    private void validatePassword(char[] password) {
        if (password == null || password.length < 8) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự.");
        }
        if (new String(password).getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Mật khẩu vượt quá giới hạn 72 byte của BCrypt.");
        }
    }
}
