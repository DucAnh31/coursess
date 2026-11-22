package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Category;
import model.Course;
import model.enums.CourseStatus;
import org.junit.jupiter.api.Test;

class CourseDAOTest extends DaoTest {
    @Test
    void publicListOnlyContainsOpenCourses() throws SQLException {
        try (CourseDAO dao = new CourseDAO()) {
            assertEquals(List.of(13L, 10L), ids(dao.getPublishedCourses("", null)));
        }
    }

    @Test
    void publicListFiltersKeywordAndCategory() throws SQLException {
        try (CourseDAO dao = new CourseDAO()) {
            assertEquals(List.of(10L), ids(dao.getPublishedCourses("Java", null)));
            assertEquals(List.of(13L, 10L), ids(dao.getPublishedCourses("", 1L)));
            assertEquals(List.of(10L), ids(dao.getPublishedCourses("Java", 1L)));
            assertTrue(dao.getPublishedCourses("Java", 2L).isEmpty());
        }
    }

    @Test
    void publicDetailRejectsDraftAndMissingCourse() throws SQLException {
        try (CourseDAO dao = new CourseDAO()) {
            assertEquals(CourseStatus.OPEN, dao.getPublicDetail(10L).getStatus());
            assertEquals(CourseStatus.CLOSED, dao.getPublicDetail(11L).getStatus());
            assertNull(dao.getPublicDetail(12L));
            assertNull(dao.getPublicDetail(999L));
        }
    }

    @Test
    void getCoursesOfTeacherReturnsTeacherCoursesAndEmptyList() throws SQLException {
        try (CourseDAO dao = new CourseDAO()) {
            List<Course> teacherCourses = dao.getCoursesOfTeacher(1L);
            assertNotNull(teacherCourses);
            assertEquals(4, teacherCourses.size());
            for (Course c : teacherCourses) {
                assertEquals(1L, c.getTeacher().getId());
                assertNotNull(c.getTitle());
            }

            List<Course> nonExistentTeacherCourses = dao.getCoursesOfTeacher(999L);
            assertNotNull(nonExistentTeacherCourses);
            assertTrue(nonExistentTeacherCourses.isEmpty());
        }
    }

    @Test
    void findByIdReturnsCourseOrNull() throws SQLException {
        try (CourseDAO dao = new CourseDAO()) {
            Course course = dao.findById(10L);
            assertNotNull(course);
            assertEquals(10L, course.getId());
            assertEquals("Java cơ bản", course.getTitle());
            assertEquals(CourseStatus.OPEN, course.getStatus());
            assertNotNull(course.getCategory());
            assertNotNull(course.getTeacher());

            Course notFound = dao.findById(999999L);
            assertNull(notFound);
        }
    }

    @Test
    void addCourseValidInsertsRecordAndSetsId() throws SQLException {
        try (CourseDAO dao = new CourseDAO()) {
            Category category = new Category();
            category.setId(1L);

            Course newCourse = new Course();
            newCourse.setTitle("Khóa học mới kiểm thử");
            newCourse.setDescription("Mô tả khóa học mới");
            newCourse.setThumbnailUrl("uploads/thumbnails/test.png");
            newCourse.setStatus(CourseStatus.DRAFT);
            newCourse.setCategory(category);

            boolean success = dao.addCourse(newCourse, 1L);
            assertTrue(success);
            assertNotNull(newCourse.getId());

            Course saved = dao.findById(newCourse.getId());
            assertNotNull(saved);
            assertEquals("Khóa học mới kiểm thử", saved.getTitle());
            assertEquals(CourseStatus.DRAFT, saved.getStatus());
            assertEquals(1L, saved.getTeacher().getId());
        }
    }

    @Test
    void addCourseInvalidThrowsSQLException() {
        try (CourseDAO dao = new CourseDAO()) {
            Course invalidCourse = new Course();
            invalidCourse.setTitle(null);
            assertThrows(SQLException.class, () -> dao.addCourse(invalidCourse, 1L));
        } catch (SQLException e) {
            fail("Không thể khởi tạo CourseDAO: " + e.getMessage());
        }
    }

    @Test
    void updateCourseUpdatesDataAccurately() throws SQLException {
        try (CourseDAO dao = new CourseDAO()) {
            Course course = dao.findById(10L);
            assertNotNull(course);

            course.setTitle("Java cơ bản (Đã cập nhật)");
            course.setDescription("Mô tả mới sau cập nhật");
            Category cat2 = new Category();
            cat2.setId(2L);
            course.setCategory(cat2);

            boolean updated = dao.updateCourse(course);
            assertTrue(updated);

            Course after = dao.findById(10L);
            assertNotNull(after);
            assertEquals("Java cơ bản (Đã cập nhật)", after.getTitle());
            assertEquals("Mô tả mới sau cập nhật", after.getDescription());
            assertEquals(2L, after.getCategory().getId());
        }
    }

    @Test
    void getLessonCountReturnsCorrectNumber() throws SQLException {
        try (CourseDAO dao = new CourseDAO()) {
            assertEquals(3, dao.getLessonCount(10L));
            assertEquals(0, dao.getLessonCount(12L));
            assertEquals(0, dao.getLessonCount(999L));
        }
    }

    @Test
    void publishCourseUpdatesStatusFromDraftToOpen() throws SQLException {
        try (CourseDAO dao = new CourseDAO()) {
            Course draftCourse = dao.findById(12L);
            assertNotNull(draftCourse);
            assertEquals(CourseStatus.DRAFT, draftCourse.getStatus());

            boolean success = dao.publishCourse(12L);
            assertTrue(success);

            Course publishedCourse = dao.findById(12L);
            assertNotNull(publishedCourse);
            assertEquals(CourseStatus.OPEN, publishedCourse.getStatus());
        }
    }

    private List<Long> ids(List<Course> courses) {
        List<Long> result = new ArrayList<>();
        for (Course course : courses) {
            result.add(course.getId());
        }
        return result;
    }
}
