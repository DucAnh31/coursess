package dao;

import java.sql.SQLException;
import java.util.List;
import model.Course;

public class CourseDAO extends DAO {
    public CourseDAO() throws SQLException {
        super();
    }

    public List<Course> getPublishedCourses(String keyword, Long categoryId) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai CourseDAO.getPublishedCourses()");
    }

    public Course getPublicDetail(Long courseId) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai CourseDAO.getPublicDetail()");
    }

    public List<Course> getCoursesOfTeacher(Long teacherId) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai CourseDAO.getCoursesOfTeacher()");
    }

    public Course findById(Long id) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai CourseDAO.findById()");
    }

    public boolean addCourse(Course course, Long teacherId) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai CourseDAO.addCourse()");
    }

    public boolean updateCourse(Course course) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai CourseDAO.updateCourse()");
    }

    public boolean publishCourse(Long courseId) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai CourseDAO.publishCourse()");
    }
}
