package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Course;
import model.Lesson;

public class LessonDAO extends DAO {
    public LessonDAO() throws SQLException {
        super();
    }

    public List<Lesson> getLessonsByCourse(Long courseId) throws SQLException {
        List<Lesson> lessons = new ArrayList<>();
        String sql = "SELECT * FROM lessons WHERE course_id = ? AND is_visible = 1 ORDER BY order_index, id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                Course course = new Course();
                course.setId(courseId);
                while (rs.next()) {
                    Lesson lesson = new Lesson();
                    lesson.setId(rs.getLong("id"));
                    lesson.setTitle(rs.getString("title"));
                    lesson.setVideoUrl(rs.getString("video_url"));
                    lesson.setOrderIndex(rs.getInt("order_index"));
                    lesson.setIsVisible(rs.getBoolean("is_visible"));
                    lesson.setCourse(course);
                    lessons.add(lesson);
                }
            }
        }
        return lessons;
    }

    public List<Lesson> getLessonsOfCourse(Long courseId) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai LessonDAO.getLessonsOfCourse()");
    }

    public Lesson findById(Long id) throws SQLException {
        String sql = "SELECT * FROM lessons WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Course course = new Course();
                    course.setId(rs.getLong("course_id"));

                    Lesson lesson = new Lesson();
                    lesson.setId(rs.getLong("id"));
                    lesson.setTitle(rs.getString("title"));
                    lesson.setVideoUrl(rs.getString("video_url"));
                    lesson.setOrderIndex(rs.getInt("order_index"));
                    lesson.setIsVisible(rs.getBoolean("is_visible"));
                    lesson.setCourse(course);
                    return lesson;
                }
            }
        }
        return null;
    }

    public boolean addLesson(Lesson lesson, Long courseId) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai LessonDAO.addLesson()");
    }

    public boolean existsByCourseIdAndOrderIndex(Long courseId, Integer orderIndex) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai LessonDAO.existsByCourseIdAndOrderIndex()");
    }
}
