package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.Test;

class CategoryDAOTest extends DaoTest {
    // JUnit test (M1) 1
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
}
