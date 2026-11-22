package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.List;
import model.Course;
import model.enums.CourseStatus;
import org.junit.jupiter.api.Test;

class CourseDAOTest extends DaoTest {
    // JUnit test (M1) 2
    @Test
    void publicListOnlyContainsOpenCourses() throws SQLException {
        try (CourseDAO dao = new CourseDAO()) {
            assertEquals(List.of(13L, 10L), ids(dao.getPublishedCourses("", null)));
        }
    }

    // JUnit test (M1) 3
    @Test
    void publicListFiltersKeywordAndCategory() throws SQLException {
        try (CourseDAO dao = new CourseDAO()) {
            assertEquals(List.of(10L), ids(dao.getPublishedCourses("Java", null)));
            assertEquals(List.of(13L, 10L), ids(dao.getPublishedCourses("", 1L)));
            assertEquals(List.of(10L), ids(dao.getPublishedCourses("Java", 1L)));
            assertTrue(dao.getPublishedCourses("Java", 2L).isEmpty());
        }
    }

    // JUnit test (M1) 4
    @Test
    void publicDetailRejectsDraftAndMissingCourse() throws SQLException {
        try (CourseDAO dao = new CourseDAO()) {
            assertEquals(CourseStatus.OPEN, dao.getPublicDetail(10L).getStatus());
            assertEquals(CourseStatus.CLOSED, dao.getPublicDetail(11L).getStatus());
            assertNull(dao.getPublicDetail(12L));
            assertNull(dao.getPublicDetail(999L));
        }
    }
    private List<Long> ids(List<Course> courses) {
        List<Long> result = new java.util.ArrayList<>();
        for (Course course : courses) {
            result.add(course.getId());
        }
        return result;
    }

}
