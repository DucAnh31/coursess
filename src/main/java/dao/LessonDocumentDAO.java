package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Lesson;
import model.LessonDocument;

public class LessonDocumentDAO extends DAO {
    public LessonDocumentDAO() throws SQLException {
        super();
    }

    public List<LessonDocument> getDocumentsByLesson(Long lessonId) throws SQLException {
        List<LessonDocument> documents = new ArrayList<>();
        String sql = "SELECT * FROM lesson_documents WHERE lesson_id = ? ORDER BY name, id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, lessonId);
            try (ResultSet rs = ps.executeQuery()) {
                Lesson lesson = new Lesson();
                lesson.setId(lessonId);
                while (rs.next()) {
                    LessonDocument document = new LessonDocument();
                    document.setId(rs.getLong("id"));
                    document.setName(rs.getString("name"));
                    document.setFilePath(rs.getString("file_path"));
                    document.setFileSize(rs.getLong("file_size"));
                    document.setIsAvailable(rs.getBoolean("is_available"));
                    document.setLesson(lesson);
                    documents.add(document);
                }
            }
        }
        return documents;
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
