package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.List;
import model.Lesson;
import org.junit.jupiter.api.Test;

class LessonDAOTest extends DaoTest {
    // JUnit test (M1) 5
    @Test
    void lessonsOnlyReturnVisibleRowsInOrder() throws SQLException {
        try (LessonDAO dao = new LessonDAO()) {
            List<Lesson> lessons = dao.getLessonsByCourse(10L);
            assertEquals(2, lessons.size());
            assertEquals("Bài một", lessons.get(0).getTitle());
            assertEquals("Bài hai", lessons.get(1).getTitle());
            for (Lesson lesson : lessons) {
                assertTrue(lesson.getIsVisible());
            }
        }
    }
}
