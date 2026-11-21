package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Category;
import model.Course;
import model.User;
import model.enums.CourseStatus;
import model.enums.UserRole;

public class CourseDAO extends DAO {
    public CourseDAO() throws SQLException {
        super();
    }

    public List<Course> getPublishedCourses(String keyword, Long categoryId) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.*, u.full_name AS teacher_name, u.username AS teacher_username, "
                + "u.role AS teacher_role, u.is_active AS teacher_active, "
                + "cat.name AS category_name, cat.description AS category_description "
                + "FROM courses c JOIN users u ON c.teacher_id = u.id "
                + "JOIN categories cat ON c.category_id = cat.id "
                + "WHERE c.status = 'OPEN' AND c.title LIKE ?";
        if (categoryId != null) {
            sql += " AND c.category_id = ?";
        }
        sql += " ORDER BY c.title, c.id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + (keyword == null ? "" : keyword.strip()) + "%");
            if (categoryId != null) {
                ps.setLong(2, categoryId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    courses.add(readCourse(rs));
                }
            }
        }
        return courses;
    }

    public Course getPublicDetail(Long courseId) throws SQLException {
        String sql = "SELECT c.*, u.full_name AS teacher_name, u.username AS teacher_username, "
                + "u.role AS teacher_role, u.is_active AS teacher_active, "
                + "cat.name AS category_name, cat.description AS category_description "
                + "FROM courses c JOIN users u ON c.teacher_id = u.id "
                + "JOIN categories cat ON c.category_id = cat.id "
                + "WHERE c.id = ? AND c.status IN ('OPEN', 'CLOSED')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return readCourse(rs);
                }
            }
        }
        return null;
    }

    private Course readCourse(ResultSet rs) throws SQLException {
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
        return course;
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
