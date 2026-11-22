package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import model.Lesson;
import model.Question;
import model.QuestionOption;
import model.Quiz;
import model.QuizAttempt;
import model.StudentAnswer;
import model.User;
import model.enums.AttemptStatus;
import org.junit.jupiter.api.Test;

class QuizAttemptDAOTest extends DaoTest {
    private static final String H2_URL = "jdbc:h2:mem:coursess_m1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";

    private static User user(long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    private static Lesson lesson(long id) {
        Lesson l = new Lesson();
        l.setId(id);
        return l;
    }

    private static Quiz quiz(long id) {
        Quiz q = new Quiz();
        q.setId(id);
        return q;
    }

    private static StudentAnswer answer(long questionId, long optionId) {
        StudentAnswer sa = new StudentAnswer();
        Question q = new Question();
        q.setId(questionId);
        sa.setQuestion(q);
        QuestionOption opt = new QuestionOption();
        opt.setId(optionId);
        sa.setSelectedOption(opt);
        return sa;
    }

    /**
     * Khởi tạo dữ liệu mẫu phục vụ các ca kiểm thử mô-đun M3:
     * - User 5 (STUDENT, đã ghi danh khóa 10), User 6 (STUDENT, chưa ghi danh), User 7 (TEACHER khác).
     * - Quiz 101 (PUBLISHED, 3 câu hỏi, 4 phương án/câu; đáp án đúng: 1001, 1006, 1011).
     * - Quiz 102 (DRAFT), Quiz 103 (CLOSED, 1 câu), Quiz 104 (PUBLISHED, 0 câu hỏi).
     */
    private void seedM3Data() throws SQLException {
        try (Connection conn = DriverManager.getConnection(H2_URL, "sa", "");
             Statement st = conn.createStatement()) {
            st.execute("INSERT INTO users VALUES (5,'Học sinh 2','student2','STUDENT',TRUE),"
                    + "(6,'Học sinh chưa ghi danh','student3','STUDENT',TRUE),"
                    + "(7,'Giáo viên khác','teacher2','TEACHER',TRUE)");
            st.execute("INSERT INTO course_members (student_id, course_id, enrolled_at) VALUES "
                    + "(2, 10, CURRENT_TIMESTAMP), (5, 10, CURRENT_TIMESTAMP)");
            st.execute("INSERT INTO quizzes (id, title, status, published_at, lesson_id, created_by) VALUES "
                    + "(101, 'Quiz Python cơ bản', 'PUBLISHED', CURRENT_TIMESTAMP, 20, 1),"
                    + "(102, 'Quiz nháp', 'DRAFT', NULL, 20, 1),"
                    + "(103, 'Quiz đã đóng', 'CLOSED', CURRENT_TIMESTAMP, 20, 1),"
                    + "(104, 'Quiz rỗng 0 câu', 'PUBLISHED', CURRENT_TIMESTAMP, 20, 1)");
            st.execute("INSERT INTO questions (id, content, order_index, quiz_id) VALUES "
                    + "(201, 'Kiểu dữ liệu số nguyên trong Python?', 1, 101),"
                    + "(202, 'Câu lệnh gán 5 cho x?', 2, 101),"
                    + "(203, 'Hàm xem kiểu dữ liệu?', 3, 101),"
                    + "(204, 'Câu hỏi của quiz đóng', 1, 103)");
            st.execute("INSERT INTO question_options (id, content, is_correct, order_index, question_id) VALUES "
                    + "(1001, 'int', TRUE, 1, 201), (1002, 'str', FALSE, 2, 201), (1003, 'float', FALSE, 3, 201), (1004, 'bool', FALSE, 4, 201),"
                    + "(1005, 'x == 5', FALSE, 1, 202), (1006, 'x = 5', TRUE, 2, 202), (1007, '5 = x', FALSE, 3, 202), (1008, 'int x = 5', FALSE, 4, 202),"
                    + "(1009, 'print()', FALSE, 1, 203), (1010, 'input()', FALSE, 2, 203), (1011, 'type()', TRUE, 3, 203), (1012, 'len()', FALSE, 4, 203),"
                    + "(1013, 'Đúng', TRUE, 1, 204), (1014, 'Sai 1', FALSE, 2, 204), (1015, 'Sai 2', FALSE, 3, 204), (1016, 'Sai 3', FALSE, 4, 204)");
        }
    }

    @Test
    void testCountAttemptsByQuiz() throws SQLException {
        try (Connection conn = DriverManager.getConnection(H2_URL, "sa", "");
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

    @Test
    void testGetAvailableQuizzesOfLesson_filtersPublishedAndEnforcesAccess() throws SQLException {
        seedM3Data();
        try (QuizDAO quizDAO = new QuizDAO()) {
            List<Quiz> available = quizDAO.getAvailableQuizzesOfLesson(user(2), lesson(20));
            assertEquals(1, available.size());
            assertEquals(101L, available.get(0).getId());
            assertEquals(3, available.get(0).getQuestions().size());

            assertThrows(SecurityException.class, () -> quizDAO.getAvailableQuizzesOfLesson(user(3), lesson(20)));
            assertThrows(SecurityException.class, () -> quizDAO.getAvailableQuizzesOfLesson(user(1), lesson(20)));
            assertThrows(SecurityException.class, () -> quizDAO.getAvailableQuizzesOfLesson(user(7), lesson(20)));
            assertThrows(SecurityException.class, () -> quizDAO.getAvailableQuizzesOfLesson(user(6), lesson(20)));

            try (Statement st = quizDAO.getConnection().createStatement()) {
                st.execute("UPDATE courses SET status = 'DRAFT' WHERE id = 10");
            }
            assertThrows(IllegalStateException.class, () -> quizDAO.getAvailableQuizzesOfLesson(user(2), lesson(20)));
        }
    }

    @Test
    void testStartAttempt_createsReusesInProgressAndRejectsInvalidQuiz() throws SQLException {
        seedM3Data();
        try (QuizAttemptDAO dao = new QuizAttemptDAO()) {
            assertThrows(IllegalStateException.class, () -> dao.startAttempt(user(2), quiz(103)));
            assertThrows(IllegalStateException.class, () -> dao.startAttempt(user(2), quiz(104)));

            QuizAttempt first = dao.startAttempt(user(2), quiz(101));
            QuizAttempt reused = dao.startAttempt(user(2), quiz(101));
            assertNotNull(first.getId());
            assertEquals(first.getId(), reused.getId());
            assertEquals(AttemptStatus.IN_PROGRESS, first.getStatus());
            assertNull(first.getScore());
            assertNull(first.getSubmittedAt());
        }
    }

    @Test
    void testGetAttemptContent_hidesCorrectAnswersAndBlocksOtherUser() throws SQLException {
        seedM3Data();
        try (QuizAttemptDAO dao = new QuizAttemptDAO()) {
            QuizAttempt att = dao.startAttempt(user(2), quiz(101));
            QuizAttempt content = dao.getAttemptContent(user(2), att);

            assertEquals(3, content.getQuiz().getQuestions().size());
            boolean allCorrectHidden = content.getQuiz().getQuestions().stream()
                    .flatMap(q -> q.getOptions().stream())
                    .allMatch(o -> o.getIsCorrect() == null);
            assertTrue(allCorrectHidden);

            assertThrows(SecurityException.class, () -> dao.getAttemptContent(user(5), att));
        }
    }

    @Test
    void testSubmitAttempt_gradesAtomicallyWithBigDecimalAndAllowsClosedQuiz() throws SQLException {
        seedM3Data();
        try (QuizAttemptDAO dao = new QuizAttemptDAO()) {
            QuizAttempt att = dao.startAttempt(user(2), quiz(101));
            try (Statement st = dao.getConnection().createStatement()) {
                st.execute("UPDATE quizzes SET status = 'CLOSED' WHERE id = 101");
            }

            List<StudentAnswer> selections = new ArrayList<>();
            selections.add(answer(201L, 1001L)); // đúng
            selections.add(answer(202L, 1005L)); // sai
            // câu 203 bỏ trống

            QuizAttempt submitted = dao.submitAttempt(user(2), att, selections);
            assertEquals(AttemptStatus.SUBMITTED, submitted.getStatus());
            assertEquals(1, submitted.getCorrectCount());
            assertEquals(3, submitted.getTotalQuestions());
            assertEquals(0, new BigDecimal("3.33").compareTo(submitted.getScore()));
            assertEquals(3, submitted.getAnswers().size());
            assertNull(submitted.getAnswers().get(2).getSelectedOption());
            assertFalse(submitted.getAnswers().get(2).getIsCorrect());

            // Nộp lặp cùng lượt trả về kết quả đã lưu, không ghi đè thành 0
            QuizAttempt repeat = dao.submitAttempt(user(2), att, List.of());
            assertEquals(0, new BigDecimal("3.33").compareTo(repeat.getScore()));
            assertEquals(1, repeat.getCorrectCount());
        }
    }

    @Test
    void testSubmitAttempt_rejectsInvalidSelectionAndIgnoresClientIsCorrect() throws SQLException {
        seedM3Data();
        try (QuizAttemptDAO dao = new QuizAttemptDAO()) {
            QuizAttempt att = dao.startAttempt(user(2), quiz(101));

            // Trùng câu hỏi
            assertThrows(IllegalArgumentException.class,
                    () -> dao.submitAttempt(user(2), att, List.of(answer(201L, 1001L), answer(201L, 1002L))));

            // Phương án không thuộc câu hỏi
            assertThrows(IllegalArgumentException.class,
                    () -> dao.submitAttempt(user(2), att, List.of(answer(201L, 1005L))));

            // Giả mạo isCorrect = true từ UI cho phương án sai phải bị bỏ qua
            StudentAnswer spoofed = answer(202L, 1005L);
            spoofed.setIsCorrect(true);
            QuizAttempt graded = dao.submitAttempt(user(2), att, List.of(answer(201L, 1001L), spoofed));
            assertEquals(0, new BigDecimal("3.33").compareTo(graded.getScore()));
        }
    }

    @Test
    void testGetResult_returnsDetailsAndBlocksUnauthorizedUsers() throws SQLException {
        seedM3Data();
        try (QuizAttemptDAO dao = new QuizAttemptDAO()) {
            QuizAttempt att = dao.startAttempt(user(2), quiz(101));
            dao.submitAttempt(user(2), att, List.of(answer(201L, 1001L), answer(202L, 1005L)));

            QuizAttempt res = dao.getResult(user(2), att);
            assertEquals(3, res.getAnswers().size());
            assertTrue(res.getAnswers().get(0).getIsCorrect());
            assertFalse(res.getAnswers().get(1).getIsCorrect());

            assertThrows(SecurityException.class, () -> dao.getResult(user(5), att));
            assertThrows(SecurityException.class, () -> dao.getResult(user(3), att));
        }
    }

    @Test
    void testCancelAttempt_cancelsInProgressAndRejectsSubmitted() throws SQLException {
        seedM3Data();
        try (QuizAttemptDAO dao = new QuizAttemptDAO()) {
            QuizAttempt att = dao.startAttempt(user(2), quiz(101));
            dao.cancelAttempt(user(2), att);
            assertThrows(IllegalStateException.class, () -> dao.submitAttempt(user(2), att, List.of()));
            assertThrows(IllegalStateException.class, () -> dao.getResult(user(2), att));

            QuizAttempt att2 = dao.startAttempt(user(2), quiz(101));
            dao.submitAttempt(user(2), att2, List.of(answer(201L, 1001L)));
            assertThrows(IllegalStateException.class, () -> dao.cancelAttempt(user(2), att2));
        }
    }

    @Test
    void testCancelUnfinishedAttempts_cleansOnlyOwnInProgress() throws SQLException {
        seedM3Data();
        try (QuizAttemptDAO dao = new QuizAttemptDAO()) {
            QuizAttempt s2Submitted = dao.startAttempt(user(2), quiz(101));
            dao.submitAttempt(user(2), s2Submitted, List.of(answer(201L, 1001L)));

            QuizAttempt s2InProgress = dao.startAttempt(user(2), quiz(101));
            QuizAttempt s5InProgress = dao.startAttempt(user(5), quiz(101));

            dao.cancelUnfinishedAttempts(user(2));

            assertThrows(IllegalStateException.class, () -> dao.getAttemptContent(user(2), s2InProgress));
            assertEquals(AttemptStatus.SUBMITTED, dao.getResult(user(2), s2Submitted).getStatus());
            assertEquals(3, dao.getAttemptContent(user(5), s5InProgress).getQuiz().getQuestions().size());
        }
    }

    @Test
    void testHistoryAndBestScore_keepsClosedQuizAndReturnsNullWhenNoSubmission() throws SQLException {
        seedM3Data();
        try (QuizAttemptDAO dao = new QuizAttemptDAO()) {
            QuizAttempt att1 = dao.startAttempt(user(2), quiz(101));
            dao.submitAttempt(user(2), att1, List.of(answer(201L, 1001L))); // 3.33

            QuizAttempt att2 = dao.startAttempt(user(2), quiz(101));
            dao.submitAttempt(user(2), att2, List.of(
                    answer(201L, 1001L), answer(202L, 1006L), answer(203L, 1011L))); // 10.00

            try (Statement st = dao.getConnection().createStatement()) {
                st.execute("UPDATE quizzes SET status = 'CLOSED' WHERE id = 101");
            }

            List<Quiz> histQuizzes = dao.getHistoryQuizzes(user(2));
            assertTrue(histQuizzes.stream().anyMatch(q -> Long.valueOf(101L).equals(q.getId())));

            List<QuizAttempt> history = dao.getHistory(user(2), quiz(101));
            assertEquals(2, history.size());
            assertEquals(0, new BigDecimal("10.00").compareTo(dao.getBestScore(user(2), quiz(101))));
            assertNull(dao.getBestScore(user(2), quiz(103)));
        }
    }
}

