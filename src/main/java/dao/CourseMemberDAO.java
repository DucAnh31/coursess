package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import model.Category;
import model.Course;
import model.CourseMember;
import model.User;
import model.enums.CourseStatus;
import model.enums.UserRole;

public class CourseMemberDAO extends DAO {
    public CourseMemberDAO() throws SQLException {
        super();
    }

    public CourseMember getByStudentAndCourse(Long studentId, Long courseId) throws SQLException {
        String sql = "SELECT cm.*, c.title, c.status, u.full_name AS student_name, "
                + "u.username AS student_username, u.role AS student_role, u.is_active AS student_active "
                + "FROM course_members cm "
                + "JOIN courses c ON cm.course_id = c.id "
                + "JOIN users u ON cm.student_id = u.id "
                + "WHERE cm.student_id = ? AND cm.course_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, studentId);
            ps.setObject(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User student = new User();
                    student.setId(rs.getLong("student_id"));
                    student.setFullName(rs.getString("student_name"));
                    student.setUsername(rs.getString("student_username"));
                    student.setRole(UserRole.valueOf(rs.getString("student_role")));
                    student.setIsActive(rs.getBoolean("student_active"));

                    Course course = new Course();
                    course.setId(rs.getLong("course_id"));
                    course.setTitle(rs.getString("title"));
                    course.setStatus(CourseStatus.valueOf(rs.getString("status")));

                    CourseMember member = new CourseMember();
                    member.setId(rs.getLong("id"));
                    member.setStudent(student);
                    member.setCourse(course);
                    member.setEnrolledAt(rs.getTimestamp("enrolled_at").toInstant());
                    return member;
                }
            }
        }
        return null;
    }

    public CourseMember enroll(Long studentId, Long courseId) throws SQLException {
        String sql = "SELECT id FROM users WHERE id = ? AND role = 'STUDENT' AND is_active = 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Chỉ tài khoản học sinh đang hoạt động mới được ghi danh.");
                }
            }
        }

        CourseMember member = getByStudentAndCourse(studentId, courseId);
        if (member != null) {
            return member;
        }

        Course course = new Course();
        sql = "SELECT id, title, status FROM courses WHERE id = ? AND status = 'OPEN'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Khóa học không tồn tại hoặc đã ngừng nhận ghi danh.");
                }
                course.setId(rs.getLong("id"));
                course.setTitle(rs.getString("title"));
                course.setStatus(CourseStatus.valueOf(rs.getString("status")));
            }
        }

        Instant enrolledAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        sql = "INSERT INTO course_members (student_id, course_id, enrolled_at) "
                + "SELECT u.id, c.id, ? FROM users u JOIN courses c ON c.id = ? "
                + "WHERE u.id = ? AND u.role = 'STUDENT' AND u.is_active = 1 AND c.status = 'OPEN'";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.from(enrolledAt));
            ps.setObject(2, courseId);
            ps.setObject(3, studentId);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Không thể ghi danh: khóa học không còn mở hoặc tài khoản không hợp lệ.");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Không lấy được mã ghi danh vừa tạo.");
                }
                User student = new User();
                student.setId(studentId);
                member = new CourseMember();
                member.setId(rs.getLong(1));
                member.setStudent(student);
                member.setCourse(course);
                member.setEnrolledAt(enrolledAt);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            member = getByStudentAndCourse(studentId, courseId);
            if (member != null) {
                return member;
            }
            throw e;
        }
        return member;
    }

    public List<Course> getCoursesByStudent(Long studentId) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.*, u.full_name AS teacher_name, u.username AS teacher_username, "
                + "u.role AS teacher_role, u.is_active AS teacher_active, "
                + "cat.name AS category_name, cat.description AS category_description "
                + "FROM course_members cm JOIN courses c ON cm.course_id = c.id "
                + "JOIN users u ON c.teacher_id = u.id JOIN categories cat ON c.category_id = cat.id "
                + "WHERE cm.student_id = ? AND c.status IN ('OPEN', 'CLOSED') "
                + "ORDER BY cm.enrolled_at DESC, cm.id DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User teacher = new User();
                    teacher.setId(rs.getLong("teacher_id"));
                    teacher.setFullName(rs.getString("teacher_name"));
                    teacher.setUsername(rs.getString("teacher_username"));
                    teacher.setRole(UserRole.valueOf(rs.getString("teacher_role")));
                    teacher.setIsActive(rs.getBoolean("teacher_active"));

                    Category category = new Category();
                    category.setId(rs.getLong("category_id"));
                    category.setName(rs.getString("category_name"));
                    category.setDescription(rs.getString("category_description"));

                    Course course = new Course();
                    course.setId(rs.getLong("id"));
                    course.setTitle(rs.getString("title"));
                    course.setDescription(rs.getString("description"));
                    course.setThumbnailUrl(rs.getString("thumbnail_url"));
                    course.setStatus(CourseStatus.valueOf(rs.getString("status")));
                    course.setTeacher(teacher);
                    course.setCategory(category);
                    courses.add(course);
                }
            }
        }
        return courses;
    }

    public List<CourseMember> getMembersByCourse(Long courseId) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai CourseMemberDAO.getMembersByCourse()");
    }
}
