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

    public List<Course> getCoursesOfTeacher(Long teacherId) throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT c.id, c.title, c.description, c.thumbnail_url, c.status, c.teacher_id, c.category_id, " +
                     "       cat.name AS category_name, cat.description AS category_desc, " +
                     "       u.full_name AS teacher_name, u.username AS teacher_username " +
                     "FROM courses c " +
                     "JOIN categories cat ON c.category_id = cat.id " +
                     "JOIN users u ON c.teacher_id = u.id " +
                     "WHERE c.teacher_id = ? " +
                     "ORDER BY c.id DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCourse(rs));
                }
            }
        }
        return list;
    }

    public Course findById(Long id) throws SQLException {
        String sql = "SELECT c.id, c.title, c.description, c.thumbnail_url, c.status, c.teacher_id, c.category_id, " +
                     "       cat.name AS category_name, cat.description AS category_desc, " +
                     "       u.full_name AS teacher_name, u.username AS teacher_username " +
                     "FROM courses c " +
                     "JOIN categories cat ON c.category_id = cat.id " +
                     "JOIN users u ON c.teacher_id = u.id " +
                     "WHERE c.id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCourse(rs);
                }
            }
        }
        return null;
    }

    public boolean addCourse(Course course, Long teacherId) throws SQLException {
        String sql = "INSERT INTO courses (title, description, thumbnail_url, status, teacher_id, category_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
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
        String sql = "UPDATE courses SET title = ?, description = ?, thumbnail_url = ?, status = ?, category_id = ? " +
                     "WHERE id = ?";
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

    public List<Course> getPublishedCourses(String keyword, Long categoryId) throws SQLException {
        List<Course> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT c.id, c.title, c.description, c.thumbnail_url, c.status, c.teacher_id, c.category_id, " +
                "       cat.name AS category_name, cat.description AS category_desc, " +
                "       u.full_name AS teacher_name, u.username AS teacher_username " +
                "FROM courses c " +
                "JOIN categories cat ON c.category_id = cat.id " +
                "JOIN users u ON c.teacher_id = u.id " +
                "WHERE c.status = 'OPEN' ");

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (LOWER(c.title) LIKE ? OR LOWER(c.description) LIKE ?) ");
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
        }
        if (categoryId != null && categoryId > 0) {
            sql.append("AND c.category_id = ? ");
            params.add(categoryId);
        }
        sql.append("ORDER BY c.id DESC");

        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCourse(rs));
                }
            }
        }
        return list;
    }

    public Course getPublicDetail(Long courseId) throws SQLException {
        Course course = findById(courseId);
        return course != null && course.getStatus() != CourseStatus.DRAFT ? course : null;
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

    private Course mapCourse(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setId(rs.getLong("id"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setThumbnailUrl(rs.getString("thumbnail_url"));

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try {
                c.setStatus(CourseStatus.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                c.setStatus(CourseStatus.DRAFT);
            }
        }

        Category cat = new Category();
        cat.setId(rs.getLong("category_id"));
        cat.setName(rs.getString("category_name"));
        cat.setDescription(rs.getString("category_desc"));
        c.setCategory(cat);

        User teacher = new User();
        teacher.setId(rs.getLong("teacher_id"));
        teacher.setFullName(rs.getString("teacher_name"));
        teacher.setUsername(rs.getString("teacher_username"));
        c.setTeacher(teacher);

        return c;
    }

    public Course readCourse(ResultSet rs) throws SQLException {
        User teacher = new User();
        teacher.setId(rs.getLong("teacher_id"));
        teacher.setFullName(rs.getString("teacher_name"));
        teacher.setUsername(rs.getString("teacher_username"));
        try {
            teacher.setRole(UserRole.valueOf(rs.getString("teacher_role")));
        } catch (Exception ignored) {
        }
        try {
            teacher.setIsActive(rs.getBoolean("teacher_active"));
        } catch (Exception ignored) {
        }

        Category category = new Category();
        category.setId(rs.getLong("category_id"));
        category.setName(rs.getString("category_name"));
        try {
            category.setDescription(rs.getString("category_description"));
        } catch (Exception ignored) {
        }

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
}
