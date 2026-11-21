package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class QuizAttemptDAOTest extends DaoTest {

    @Test
    void testCountAttemptsByQuiz() throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:coursess_m1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1", "sa", "");
             Statement st = conn.createStatement()) {
            st.execute("INSERT INTO quizzes (id, title, status, lesson_id, created_by) VALUES (100, 'Quiz test attempt', 'PUBLISHED', 20, 1)");

            try (QuizAttemptDAO dao = new QuizAttemptDAO()) {
                assertEquals(0, dao.countAttemptsByQuiz(100L));

                String insertAttempt = "INSERT INTO quiz_attempts (status, total_questions, correct_count, score, started_at, student_id, quiz_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertAttempt)) {
                    ps.setString(1, "SUBMITTED");
                    ps.setInt(2, 5);
                    ps.setInt(3, 4);
                    ps.setDouble(4, 8.0);
                    ps.setTimestamp(5, Timestamp.from(Instant.now()));
                    ps.setLong(6, 2L);
                    ps.setLong(7, 100L);
                    ps.executeUpdate();

                    ps.setString(1, "IN_PROGRESS");
                    ps.setInt(2, 5);
                    ps.setInt(3, 0);
                    ps.setDouble(4, 0.0);
                    ps.setTimestamp(5, Timestamp.from(Instant.now()));
                    ps.setLong(6, 2L);
                    ps.setLong(7, 100L);
                    ps.executeUpdate();

                    ps.setString(1, "CANCELLED");
                    ps.setInt(2, 5);
                    ps.setInt(3, 0);
                    ps.setDouble(4, 0.0);
                    ps.setTimestamp(5, Timestamp.from(Instant.now()));
                    ps.setLong(6, 2L);
                    ps.setLong(7, 100L);
                    ps.executeUpdate();
                }

                assertEquals(2, dao.countAttemptsByQuiz(100L));
            }
        }
    }
}

