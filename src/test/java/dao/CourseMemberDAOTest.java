package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import model.CourseMember;
import org.junit.jupiter.api.Test;

class CourseMemberDAOTest extends DaoTest {
    // JUnit test (M1) 6
    @Test
    void membershipLookupReturnsMemberOrNull() throws SQLException {
        try (CourseMemberDAO dao = new CourseMemberDAO()) {
            assertNull(dao.getByStudentAndCourse(2L, 10L));
            CourseMember member = dao.enroll(2L, 10L);
            assertEquals(member.getId(), dao.getByStudentAndCourse(2L, 10L).getId());
        }
    }

    // JUnit test (M1) 7
    @Test
    void validEnrollmentCreatesOneRow() throws SQLException {
        try (CourseMemberDAO dao = new CourseMemberDAO()) {
            CourseMember member = dao.enroll(2L, 10L);
            assertNotNull(member.getId());
            assertEquals(2L, member.getStudent().getId());
            assertEquals(10L, member.getCourse().getId());
            assertNotNull(member.getEnrolledAt());
            assertEquals(1, count(dao, 2L, 10L));
        }
    }

    // JUnit test (M1) 8
    @Test
    void repeatedEnrollmentKeepsIdAndDate() throws SQLException {
        try (CourseMemberDAO dao = new CourseMemberDAO()) {
            CourseMember first = dao.enroll(2L, 10L);
            CourseMember second = dao.enroll(2L, 10L);
            assertEquals(first.getId(), second.getId());
            assertEquals(first.getEnrolledAt(), second.getEnrolledAt());
            assertEquals(1, count(dao, 2L, 10L));
        }
    }

    // JUnit test (M1) 9
    @Test
    void invalidStudentCannotEnroll() throws SQLException {
        try (CourseMemberDAO dao = new CourseMemberDAO()) {
            for (long id : new long[]{1, 3, 4, 999}) {
                assertThrows(SQLException.class, () -> dao.enroll(id, 10L));
                assertEquals(0, count(dao, id, 10L));
            }
        }
    }

    // JUnit test (M1) 10
    @Test
    void closedDraftAndMissingCourseRejectNewEnrollment() throws SQLException {
        try (CourseMemberDAO dao = new CourseMemberDAO()) {
            for (long id : new long[]{11, 12, 999}) {
                assertThrows(SQLException.class, () -> dao.enroll(2L, id));
                assertEquals(0, count(dao, 2L, id));
            }
        }
    }

    // JUnit test (M1) 11
    @Test
    void existingMembershipSurvivesCourseClosure() throws SQLException {
        try (CourseMemberDAO dao = new CourseMemberDAO()) {
            CourseMember first = dao.enroll(2L, 10L);
            try (Statement st = dao.getConnection().createStatement()) {
                st.executeUpdate("UPDATE courses SET status = 'CLOSED' WHERE id = 10");
            }
            CourseMember second = dao.enroll(2L, 10L);
            assertEquals(first.getId(), second.getId());
            assertEquals(first.getEnrolledAt(), second.getEnrolledAt());
            assertEquals(1, count(dao, 2L, 10L));
        }
    }
    private int count(CourseMemberDAO dao, long studentId, long courseId) throws SQLException {
        try (var ps = dao.getConnection().prepareStatement("SELECT COUNT(*) FROM course_members WHERE student_id = ? AND course_id = ?")) {
            ps.setLong(1, studentId);
            ps.setLong(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
