package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai CategoryDAO.addCategory()");
    }

    public void updateCategory(Category category) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai CategoryDAO.updateCategory()");
    }

    public void deleteCategory(Long categoryId) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai CategoryDAO.deleteCategory()");
    }
}
