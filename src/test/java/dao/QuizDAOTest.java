package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import model.Lesson;
import model.Question;
import model.QuestionOption;
import model.Quiz;
import model.User;
import model.enums.QuizStatus;
import org.junit.jupiter.api.Test;

class QuizDAOTest extends DaoTest {

    private Quiz buildQuiz(String title, Long lessonId, Long creatorId) {
        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setStatus(QuizStatus.DRAFT);

        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        quiz.setLesson(lesson);

        User creator = new User();
        creator.setId(creatorId);
        quiz.setCreatedBy(creator);

        List<Question> questions = new ArrayList<>();
        for (int q = 1; q <= 2; q++) {
            Question question = new Question();
            question.setContent("Câu hỏi số " + q);
            question.setOrderIndex(q);

            List<QuestionOption> options = new ArrayList<>();
            for (int o = 1; o <= 4; o++) {
                QuestionOption opt = new QuestionOption();
                opt.setContent("Lựa chọn " + o + " của câu " + q);
                opt.setIsCorrect(o == 1);
                opt.setOrderIndex(o);
                options.add(opt);
            }
            question.setOptions(options);
            questions.add(question);
        }
        quiz.setQuestions(questions);
        return quiz;
    }

    @Test
    void testSaveDraftAndFindById() throws SQLException {
        try (QuizDAO dao = new QuizDAO()) {
            Quiz quiz = buildQuiz("Kiểm tra Java OOP", 20L, 1L);
            boolean saved = dao.saveDraft(quiz);

            assertTrue(saved);
            assertNotNull(quiz.getId());
            assertEquals(QuizStatus.DRAFT, quiz.getStatus());

            Quiz loaded = dao.findById(quiz.getId());
            assertNotNull(loaded);
            assertEquals("Kiểm tra Java OOP", loaded.getTitle());
            assertEquals(QuizStatus.DRAFT, loaded.getStatus());
            assertEquals(20L, loaded.getLesson().getId());
            assertEquals("Bài hai", loaded.getLesson().getTitle());
            assertEquals(1L, loaded.getCreatedBy().getId());
            assertEquals("Giáo viên", loaded.getCreatedBy().getFullName());
            assertNull(loaded.getPublishedAt());
            assertEquals(2, loaded.getQuestions().size());

            for (Question q : loaded.getQuestions()) {
                assertNotNull(q.getId());
                assertEquals(4, q.getOptions().size());
                long correctCount = q.getOptions().stream().filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect())).count();
                assertEquals(1, correctCount);
            }
        }
    }

    @Test
    void testUpdateDraft() throws SQLException {
        try (QuizDAO dao = new QuizDAO()) {
            Quiz quiz = buildQuiz("Quiz ban đầu", 20L, 1L);
            dao.saveDraft(quiz);

            quiz.setTitle("Quiz đã chỉnh sửa");
            quiz.getQuestions().get(0).setContent("Nội dung câu 1 mới");
            boolean updated = dao.saveDraft(quiz);

            assertTrue(updated);
            Quiz reloaded = dao.findById(quiz.getId());
            assertNotNull(reloaded);
            assertEquals("Quiz đã chỉnh sửa", reloaded.getTitle());
            assertEquals("Nội dung câu 1 mới", reloaded.getQuestions().get(0).getContent());
        }
    }

    @Test
    void testPublishQuiz() throws SQLException {
        try (QuizDAO dao = new QuizDAO()) {
            Quiz quiz = buildQuiz("Quiz chuẩn bị thi", 20L, 1L);
            dao.saveDraft(quiz);

            boolean published = dao.publish(quiz);
            assertTrue(published);
            assertEquals(QuizStatus.PUBLISHED, quiz.getStatus());
            assertNotNull(quiz.getPublishedAt());

            Quiz reloaded = dao.findById(quiz.getId());
            assertNotNull(reloaded);
            assertEquals(QuizStatus.PUBLISHED, reloaded.getStatus());
            assertNotNull(reloaded.getPublishedAt());
        }
    }

    @Test
    void testCloseQuiz() throws SQLException {
        try (QuizDAO dao = new QuizDAO()) {
            Quiz quiz = buildQuiz("Quiz đóng thi", 20L, 1L);
            dao.publish(quiz);

            boolean closed = dao.closeQuiz(quiz.getId());
            assertTrue(closed);

            Quiz reloaded = dao.findById(quiz.getId());
            assertNotNull(reloaded);
            assertEquals(QuizStatus.CLOSED, reloaded.getStatus());
        }
    }

    @Test
    void testCopyQuizWhenAttemptsExist() throws SQLException {
        try (QuizDAO dao = new QuizDAO()) {
            Quiz original = buildQuiz("Quiz trắc nghiệm", 20L, 1L);
            dao.publish(original);

            try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:coursess_m1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1", "sa", "")) {
                String insertAttempt = "INSERT INTO quiz_attempts (status, total_questions, correct_count, score, started_at, student_id, quiz_id) "
                        + "VALUES ('SUBMITTED', 2, 2, 10.0, ?, 2, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertAttempt)) {
                    ps.setTimestamp(1, Timestamp.from(Instant.now()));
                    ps.setLong(2, original.getId());
                    ps.executeUpdate();
                }
            }

            Quiz copy = dao.copyQuiz(original.getId(), 1L);
            assertNotNull(copy);
            assertNotEquals(original.getId(), copy.getId());
            assertEquals("Quiz trắc nghiệm (Bản sao)", copy.getTitle());
            assertEquals(QuizStatus.DRAFT, copy.getStatus());
            assertNull(copy.getPublishedAt());
            assertNotNull(copy.getSourceQuiz());
            assertEquals(original.getId(), copy.getSourceQuiz().getId());
            assertEquals(2, copy.getQuestions().size());

            Quiz originalAfter = dao.findById(original.getId());
            assertEquals(QuizStatus.PUBLISHED, originalAfter.getStatus());
            assertEquals("Quiz trắc nghiệm", originalAfter.getTitle());

            for (Question cq : copy.getQuestions()) {
                assertNotNull(cq.getId());
                assertEquals(4, cq.getOptions().size());
                for (QuestionOption co : cq.getOptions()) {
                    assertNotNull(co.getId());
                }
            }
        }
    }

    @Test
    void testGetQuizzesByLesson() throws SQLException {
        try (QuizDAO dao = new QuizDAO()) {
            Quiz quiz1 = buildQuiz("Quiz 1 của bài 20", 20L, 1L);
            Quiz quiz2 = buildQuiz("Quiz 2 của bài 20", 20L, 1L);
            dao.saveDraft(quiz1);
            dao.saveDraft(quiz2);

            List<Quiz> list = dao.getQuizzesByLesson(20L);
            assertEquals(2, list.size());
            assertEquals(quiz1.getId(), list.get(0).getId());
            assertEquals(quiz2.getId(), list.get(1).getId());
            assertEquals(2, list.get(0).getQuestions().size());
            assertEquals(2, list.get(1).getQuestions().size());

            List<Quiz> emptyList = dao.getQuizzesByLesson(21L);
            assertTrue(emptyList.isEmpty());
        }
    }

    @Test
    void testFindByIdNotFound() throws SQLException {
        try (QuizDAO dao = new QuizDAO()) {
            Quiz notFound = dao.findById(999999L);
            assertNull(notFound);
        }
    }
}

