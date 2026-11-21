package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    public List<Course> getCoursesOfTeacher(Long teacherId) throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT c.*, cat.name AS category_name, cat.description AS category_description, "
                + "u.full_name AS teacher_name, u.username AS teacher_username, "
                + "u.role AS teacher_role, u.is_active AS teacher_active "
                + "FROM courses c "
                + "JOIN categories cat ON c.category_id = cat.id "
                + "JOIN users u ON c.teacher_id = u.id "
                + "WHERE c.teacher_id = ? "
                + "ORDER BY c.id DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(readCourse(rs));
                }
            }
        }
        return list;
    }

    public Course findById(Long id) throws SQLException {
        String sql = "SELECT c.*, cat.name AS category_name, cat.description AS category_description, "
                + "u.full_name AS teacher_name, u.username AS teacher_username, "
                + "u.role AS teacher_role, u.is_active AS teacher_active "
                + "FROM courses c "
                + "JOIN categories cat ON c.category_id = cat.id "
                + "JOIN users u ON c.teacher_id = u.id "
                + "WHERE c.id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return readCourse(rs);
                }
            }
        }
        return null;
    }

    public boolean addCourse(Course course, Long teacherId) throws SQLException {
        String sql = "INSERT INTO courses (title, description, thumbnail_url, status, teacher_id, category_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, course.getTitle());
            ps.setString(2, course.getDescription());
            ps.setString(3, course.getThumbnailUrl());
            ps.setString(4, course.getStatus() != null ? course.getStatus().name() : CourseStatus.DRAFT.name());
            ps.setLong(5, teacherId);
            ps.setLong(6, course.getCategory() != null ? course.getCategory().getId() : 1L);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        course.setId(rs.getLong(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public boolean updateCourse(Course course) throws SQLException {
        String sql = "UPDATE courses SET title = ?, description = ?, thumbnail_url = ?, status = ?, category_id = ? "
                + "WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, course.getTitle());
            ps.setString(2, course.getDescription());
            ps.setString(3, course.getThumbnailUrl());
            ps.setString(4, course.getStatus() != null ? course.getStatus().name() : CourseStatus.DRAFT.name());
            ps.setLong(5, course.getCategory() != null ? course.getCategory().getId() : 1L);
            ps.setLong(6, course.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean publishCourse(Long courseId) throws SQLException {
        String sql = "UPDATE courses SET status = 'OPEN' WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, courseId);
            return ps.executeUpdate() > 0;
        }
    }

    public int getLessonCount(Long courseId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM lessons WHERE course_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private Course readCourse(ResultSet rs) throws SQLException {
        User teacher = new User();
        teacher.setId(rs.getLong("teacher_id"));
        teacher.setFullName(rs.getString("teacher_name"));
        teacher.setUsername(rs.getString("teacher_username"));
        try {
            teacher.setRole(UserRole.valueOf(rs.getString("teacher_role")));
        } catch (Exception ignored) {
            teacher.setRole(UserRole.TEACHER);
        }
        try {
            teacher.setIsActive(rs.getBoolean("teacher_active"));
        } catch (Exception ignored) {
            teacher.setIsActive(true);
        }

        Category category = new Category();
        category.setId(rs.getLong("category_id"));
        category.setName(rs.getString("category_name"));
        category.setDescription(rs.getString("category_description"));

        Course course = new Course();
        course.setId(rs.getLong("id"));
        course.setTitle(rs.getString("title"));
        course.setDescription(rs.getString("description"));
        course.setThumbnailUrl(rs.getString("thumbnail_url"));
        try {
            course.setStatus(CourseStatus.valueOf(rs.getString("status")));
        } catch (Exception ignored) {
            course.setStatus(CourseStatus.DRAFT);
        }
        course.setTeacher(teacher);
        course.setCategory(category);
        return course;
    }

    public Course mapCourse(ResultSet rs) throws SQLException {
        return readCourse(rs);
    }
}
