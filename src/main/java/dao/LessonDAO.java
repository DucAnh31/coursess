package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Lesson;
import model.LessonDocument;

public class LessonDAO extends DAO {
    public LessonDAO() throws SQLException {
        super();
    }

    public List<Lesson> getLessonsOfCourse(Long courseId) throws SQLException {
        List<Lesson> lessons = new ArrayList<>();
        String sql = "SELECT id, title, video_url, order_index, is_visible, course_id " +
                     "FROM lessons WHERE course_id = ? ORDER BY order_index ASC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Lesson lesson = new Lesson();
                    lesson.setId(rs.getLong("id"));
                    lesson.setTitle(rs.getString("title"));
                    lesson.setVideoUrl(rs.getString("video_url"));
                    lesson.setOrderIndex(rs.getInt("order_index"));
                    lesson.setIsVisible(rs.getBoolean("is_visible"));
                    lessons.add(lesson);
                }
            }
        }

        for (Lesson lesson : lessons) {
            lesson.setDocuments(getDocumentsOfLesson(lesson.getId()));
        }

        return lessons;
    }

    public List<Lesson> getLessonsByCourse(Long courseId) throws SQLException {
        return getLessonsOfCourse(courseId);
    }

    public boolean addLesson(Lesson lesson, Long courseId) throws SQLException {
        String sql = "INSERT INTO lessons (title, video_url, order_index, is_visible, course_id) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, lesson.getTitle());
            ps.setString(2, lesson.getVideoUrl() != null ? lesson.getVideoUrl() : "");
            ps.setInt(3, lesson.getOrderIndex() != null ? lesson.getOrderIndex() : 1);
            ps.setBoolean(4, lesson.getIsVisible() != null ? lesson.getIsVisible() : true);
            ps.setLong(5, courseId);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        lesson.setId(rs.getLong(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public boolean existsByCourseIdAndOrderIndex(Long courseId, Integer orderIndex) throws SQLException {
        String sql = "SELECT 1 FROM lessons WHERE course_id = ? AND order_index = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, courseId);
            ps.setInt(2, orderIndex);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existsByCourseIdAndOrderIndexAndNotId(Long courseId, Integer orderIndex, Long excludeLessonId) throws SQLException {
        String sql = "SELECT 1 FROM lessons WHERE course_id = ? AND order_index = ? AND id != ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, courseId);
            ps.setInt(2, orderIndex);
            ps.setLong(3, excludeLessonId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean updateLesson(Lesson lesson) throws SQLException {
        String sql = "UPDATE lessons SET title = ?, video_url = ?, order_index = ?, is_visible = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, lesson.getTitle());
            ps.setString(2, lesson.getVideoUrl() != null ? lesson.getVideoUrl() : "");
            ps.setInt(3, lesson.getOrderIndex() != null ? lesson.getOrderIndex() : 1);
            ps.setBoolean(4, lesson.getIsVisible() != null ? lesson.getIsVisible() : true);
            ps.setLong(5, lesson.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public int getNextOrderIndex(Long courseId) throws SQLException {
        String sql = "SELECT COALESCE(MAX(order_index), 0) + 1 FROM lessons WHERE course_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 1;
    }

    public Lesson findById(Long id) throws SQLException {
        String sql = "SELECT id, title, video_url, order_index, is_visible, course_id FROM lessons WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Lesson lesson = new Lesson();
                    lesson.setId(rs.getLong("id"));
                    lesson.setTitle(rs.getString("title"));
                    lesson.setVideoUrl(rs.getString("video_url"));
                    lesson.setOrderIndex(rs.getInt("order_index"));
                    lesson.setIsVisible(rs.getBoolean("is_visible"));
                    lesson.setDocuments(getDocumentsOfLesson(lesson.getId()));
                    return lesson;
                }
            }
        }
        return null;
    }

    private List<LessonDocument> getDocumentsOfLesson(Long lessonId) throws SQLException {
        List<LessonDocument> docs = new ArrayList<>();
        String sql = "SELECT id, name, file_path, file_size, is_available FROM lesson_documents WHERE lesson_id = ?";
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
                    docs.add(doc);
                }
            }
        }
        return docs;
    }
}
