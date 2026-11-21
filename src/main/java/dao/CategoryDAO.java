package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Category;

public class CategoryDAO extends DAO {
    public CategoryDAO() throws SQLException {
        super();
    }

    public List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT id, name, description FROM categories ORDER BY name, id";
        try (PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getLong("id"));
                category.setName(rs.getString("name"));
                category.setDescription(rs.getString("description"));
                categories.add(category);
            }
        }
        return categories;
    }

    public Category addCategory(Category category) throws SQLException {
        String name = validateName(category);
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, category.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Không lấy được mã danh mục vừa tạo.");
                }
                category.setId(rs.getLong(1));
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new SQLException("Tên danh mục đã được sử dụng.", e);
        }
        category.setName(name);
        return category;
    }

    public void updateCategory(Category category) throws SQLException {
        String name = validateName(category);
        if (category.getId() == null || category.getId() <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn danh mục cần sửa.");
        }
        String sql = "UPDATE categories SET name = ?, description = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, category.getDescription());
            ps.setLong(3, category.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Danh mục không tồn tại.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new SQLException("Tên danh mục đã được sử dụng.", e);
        }
        category.setName(name);
    }

    public void deleteCategory(Long categoryId) throws SQLException {
        if (categoryId == null || categoryId <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn danh mục cần xóa.");
        }
        String sql = "SELECT id FROM courses WHERE category_id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new SQLException("Không thể xóa danh mục đang có khóa học sử dụng.");
                }
            }
        }
        sql = "DELETE FROM categories WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, categoryId);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Danh mục không tồn tại.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new SQLException("Không thể xóa danh mục đang có khóa học sử dụng.", e);
        }
    }

    private String validateName(Category category) {
        if (category == null || category.getName() == null || category.getName().isBlank()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống.");
        }
        String name = category.getName().strip();
        if (name.length() > 120) {
            throw new IllegalArgumentException("Tên danh mục không được vượt quá 120 ký tự.");
        }
        return name;
    }
}
