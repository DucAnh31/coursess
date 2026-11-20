package dao;

import java.sql.SQLException;
import java.util.List;
import model.Lesson;

public class LessonDAO extends DAO {
    public LessonDAO() throws SQLException {
        super();
    }

    public List<Lesson> getLessonsByCourse(Long courseId) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai LessonDAO.getLessonsByCourse()");
    }

    public List<Lesson> getLessonsOfCourse(Long courseId) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai LessonDAO.getLessonsOfCourse()");
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
