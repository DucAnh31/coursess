package dao;

import java.sql.SQLException;
import java.util.List;
import model.Course;
import model.CourseMember;

public class CourseMemberDAO extends DAO {
    public CourseMemberDAO() throws SQLException {
        super();
    }

    public CourseMember getByStudentAndCourse(Long studentId, Long courseId) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai CourseMemberDAO.getByStudentAndCourse()");
    }

    public CourseMember enroll(Long studentId, Long courseId) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai CourseMemberDAO.enroll()");
    }

    public List<Course> getCoursesByStudent(Long studentId) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai CourseMemberDAO.getCoursesByStudent()");
    }

    public List<CourseMember> getMembersByCourse(Long courseId) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai CourseMemberDAO.getMembersByCourse()");
    }
}
