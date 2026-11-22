package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.List;
import model.Lesson;
import model.LessonDocument;
import org.junit.jupiter.api.Test;

class LessonDocumentDAOTest extends DaoTest {
    @Test
    void saveAndGetDocumentsByLessonSavesValidAttachment() throws SQLException {
        try (LessonDocumentDAO dao = new LessonDocumentDAO()) {
            LessonDocument doc = new LessonDocument();
            doc.setName("TaiLieuHuongDan.pdf");
            doc.setFilePath("/uploads/documents/TaiLieuHuongDan.pdf");
            doc.setFileSize(2048576L);
            doc.setIsAvailable(true);

            Lesson lesson = new Lesson();
            lesson.setId(20L);
            doc.setLesson(lesson);

            LessonDocument saved = dao.save(doc);
            assertNotNull(saved);
            assertNotNull(saved.getId());

            List<LessonDocument> docs = dao.getDocumentsByLesson(20L);
            assertNotNull(docs);
            assertEquals(1, docs.size());
            LessonDocument firstDoc = docs.get(0);
            assertEquals("TaiLieuHuongDan.pdf", firstDoc.getName());
            assertEquals(2048576L, firstDoc.getFileSize());
            assertTrue(firstDoc.getIsAvailable());
        }
    }

    @Test
    void deleteDocumentRemovesAttachment() throws SQLException {
        try (LessonDocumentDAO dao = new LessonDocumentDAO()) {
            LessonDocument doc = new LessonDocument();
            doc.setName("TaiLieuXoa.pdf");
            doc.setFilePath("/uploads/documents/TaiLieuXoa.pdf");
            doc.setFileSize(512L);
            doc.setIsAvailable(true);

            Lesson lesson = new Lesson();
            lesson.setId(20L);
            doc.setLesson(lesson);

            LessonDocument saved = dao.save(doc);
            assertNotNull(saved.getId());

            dao.delete(saved.getId());
            List<LessonDocument> docs = dao.getDocumentsByLesson(20L);
            assertTrue(docs.stream().noneMatch(d -> d.getId().equals(saved.getId())));
        }
    }
}
