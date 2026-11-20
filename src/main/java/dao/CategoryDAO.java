package dao;

import java.sql.SQLException;
import java.util.List;
import model.Category;

public class CategoryDAO extends DAO {
    public CategoryDAO() throws SQLException {
        super();
    }

    public List<Category> getAllCategories() throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai CategoryDAO.getAllCategories()");
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
