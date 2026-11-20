package dao;

import java.sql.SQLException;
import java.util.List;
import model.LessonDocument;

public class LessonDocumentDAO extends DAO {
    public LessonDocumentDAO() throws SQLException {
        super();
    }

    public List<LessonDocument> getDocumentsByLesson(Long lessonId) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai LessonDocumentDAO.getDocumentsByLesson()");
    }

    public LessonDocument save(LessonDocument document) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai LessonDocumentDAO.save()");
    }

    public void delete(Long documentId) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai LessonDocumentDAO.delete()");
    }
}
