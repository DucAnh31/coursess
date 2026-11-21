package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.LessonDocument;

public class LessonDocumentDAO extends DAO {
    public LessonDocumentDAO() throws SQLException {
        super();
    }

    public List<LessonDocument> getDocumentsByLesson(Long lessonId) throws SQLException {
        List<LessonDocument> list = new ArrayList<>();
        String sql = "SELECT id, name, file_path, file_size, is_available, lesson_id FROM lesson_documents WHERE lesson_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, lessonId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LessonDocument doc = new LessonDocument();
                    doc.setId(rs.getLong("id"));
                    doc.setName(rs.getString("name"));
                    doc.setFilePath(rs.getString("file_path"));
                    doc.setFileSize(rs.getLong("file_size"));
                    doc.setIsAvailable(rs.getBoolean("is_available"));
                    list.add(doc);
                }
            }
        }
        return list;
    }

    public LessonDocument save(LessonDocument document) throws SQLException {
        String sql = "INSERT INTO lesson_documents (name, file_path, file_size, is_available, lesson_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, document.getName());
            ps.setString(2, document.getFilePath());
            ps.setLong(3, document.getFileSize() != null ? document.getFileSize() : 0L);
            ps.setBoolean(4, document.getIsAvailable() != null ? document.getIsAvailable() : true);
            ps.setLong(5, document.getLesson() != null ? document.getLesson().getId() : 0L);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    document.setId(rs.getLong(1));
                }
            }
        }
        return document;
    }

    public void delete(Long documentId) throws SQLException {
        String sql = "DELETE FROM lesson_documents WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, documentId);
            ps.executeUpdate();
        }
    }
}
