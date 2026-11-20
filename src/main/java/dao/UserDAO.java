package dao;

import java.sql.SQLException;
import model.User;

public class UserDAO extends DAO {
    public UserDAO() throws SQLException {
        super();
    }

    public User checkLogin(String username, char[] password) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai UserDAO.checkLogin()");
    }

    public User register(User user) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai UserDAO.register()");
    }

    public void updateFullName(Long userId, String fullName) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai UserDAO.updateFullName()");
    }

    public void changePassword(Long userId, char[] currentPassword, char[] newPassword) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai UserDAO.changePassword()");
    }
}
