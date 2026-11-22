package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.List;
import model.Lesson;
import model.LessonDocument;
import org.junit.jupiter.api.Test;

class LessonDAOTest extends DaoTest {
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

    @Test
    void getLessonsOfCourseReturnsAscendingOrder() throws SQLException {
        try (LessonDAO dao = new LessonDAO()) {
            List<Lesson> lessons = dao.getLessonsOfCourse(10L);
            assertNotNull(lessons);
            assertEquals(3, lessons.size());
            assertEquals(1, lessons.get(0).getOrderIndex());
            assertEquals(2, lessons.get(1).getOrderIndex());
            assertEquals(3, lessons.get(2).getOrderIndex());
            assertEquals("Bài một", lessons.get(0).getTitle());
            assertEquals("Bài hai", lessons.get(1).getTitle());
            assertEquals("Bài ẩn", lessons.get(2).getTitle());
        }
    }

    @Test
    void existsByCourseIdAndOrderIndexChecksOrderExistence() throws SQLException {
        try (LessonDAO dao = new LessonDAO()) {
            assertTrue(dao.existsByCourseIdAndOrderIndex(10L, 1));
            assertTrue(dao.existsByCourseIdAndOrderIndex(10L, 2));
            assertFalse(dao.existsByCourseIdAndOrderIndex(10L, 99));
            assertFalse(dao.existsByCourseIdAndOrderIndex(999L, 1));
        }
    }

    @Test
    void addLessonValidInsertsRecordAndSetsId() throws SQLException {
        try (LessonDAO dao = new LessonDAO()) {
            int nextOrder = dao.getNextOrderIndex(10L);
            assertEquals(4, nextOrder);

            Lesson newLesson = new Lesson();
            newLesson.setTitle("Bài học mới tạo");
            newLesson.setVideoUrl("https://youtube.com/watch?v=demo");
            newLesson.setOrderIndex(nextOrder);
            newLesson.setIsVisible(true);

            boolean success = dao.addLesson(newLesson, 10L);
            assertTrue(success);
            assertNotNull(newLesson.getId());

            Lesson saved = dao.findById(newLesson.getId());
            assertNotNull(saved);
            assertEquals("Bài học mới tạo", saved.getTitle());
            assertEquals(4, saved.getOrderIndex());
        }
    }

    @Test
    void getLessonsOfCoursePopulatesDocuments() throws SQLException {
        try (LessonDocumentDAO docDAO = new LessonDocumentDAO();
             LessonDAO lessonDAO = new LessonDAO()) {
            LessonDocument doc = new LessonDocument();
            doc.setName("Doc1.pdf");
            doc.setFilePath("/uploads/doc1.pdf");
            doc.setFileSize(1024L);
            doc.setIsAvailable(true);
            Lesson lesson = new Lesson();
            lesson.setId(22L);
            doc.setLesson(lesson);
            docDAO.save(doc);

            List<Lesson> lessons = lessonDAO.getLessonsOfCourse(10L);
            Lesson lesson22 = lessons.stream().filter(l -> l.getId().equals(22L)).findFirst().orElse(null);
            assertNotNull(lesson22);
            assertNotNull(lesson22.getDocuments());
            assertEquals(1, lesson22.getDocuments().size());
            assertEquals("Doc1.pdf", lesson22.getDocuments().get(0).getName());
        }
    }
}
