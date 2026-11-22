package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import model.Category;
import org.junit.jupiter.api.Test;

class CategoryDAOTest extends DaoTest {
    @Test
    void categoriesReturnDataAndEmptyList() throws SQLException {
        try (CategoryDAO dao = new CategoryDAO()) {
            assertEquals(2, dao.getAllCategories().size());
            assertEquals("Lập trình", dao.getAllCategories().get(0).getName());
            assertEquals("Ngoại ngữ", dao.getAllCategories().get(1).getName());
            try (Statement st = dao.getConnection().createStatement()) {
                st.executeUpdate("DELETE FROM categories");
            }
            assertTrue(dao.getAllCategories().isEmpty());
        }
    }

    @Test
    void findAllReturnsCategoriesAndEmptyList() throws SQLException {
        try (CategoryDAO dao = new CategoryDAO()) {
            List<Category> categories = dao.findAll();
            assertNotNull(categories);
            assertEquals(2, categories.size());
            assertEquals("Lập trình", categories.get(0).getName());
            assertEquals("Ngoại ngữ", categories.get(1).getName());
            try (Statement st = dao.getConnection().createStatement()) {
                st.executeUpdate("DELETE FROM categories");
            }
            assertTrue(dao.findAll().isEmpty());
        }
    }
}
