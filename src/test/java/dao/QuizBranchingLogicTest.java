package dao;

import model.Lesson;
import model.Question;
import model.QuestionOption;
import model.Quiz;
import model.enums.QuizStatus;
import model.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuizBranchingLogicTest {

    public static boolean canEditDirectly(int attemptCount) {
        return attemptCount == 0;
    }

    public static String generateCopyTitle(String originalTitle) {
        return (originalTitle != null ? originalTitle : "") + " (Bản sao)";
    }

    public static Quiz createDraftClone(Quiz sourceQuiz, User teacher) {
        Quiz clone = new Quiz();
        clone.setTitle(generateCopyTitle(sourceQuiz.getTitle()));
        clone.setStatus(QuizStatus.DRAFT);
        clone.setPublishedAt(null);
        clone.setLesson(sourceQuiz.getLesson());
        clone.setCreatedBy(teacher != null ? teacher : sourceQuiz.getCreatedBy());
        clone.setSourceQuiz(sourceQuiz);

        List<Question> copiedQuestions = new ArrayList<>();
        if (sourceQuiz.getQuestions() != null) {
            for (Question sq : sourceQuiz.getQuestions()) {
                Question nq = new Question();
                nq.setContent(sq.getContent());
                nq.setOrderIndex(sq.getOrderIndex());
                nq.setQuiz(clone);

                List<QuestionOption> copiedOptions = new ArrayList<>();
                if (sq.getOptions() != null) {
                    for (QuestionOption so : sq.getOptions()) {
                        QuestionOption no = new QuestionOption();
                        no.setContent(so.getContent());
                        no.setIsCorrect(so.getIsCorrect());
                        no.setOrderIndex(so.getOrderIndex());
                        no.setQuestion(nq);
                        copiedOptions.add(no);
                    }
                }
                nq.setOptions(copiedOptions);
                copiedQuestions.add(nq);
            }
        }
        clone.setQuestions(copiedQuestions);
        return clone;
    }

    @Test
    public void testBranchingWhenNoAttemptsAllowsDirectEdit() {
        int attemptCount = 0;
        assertTrue(canEditDirectly(attemptCount));
    }

    @Test
    public void testBranchingWhenHasAttemptsDisallowsDirectEdit() {
        int attemptCount1 = 1;
        int attemptCount50 = 50;

        assertFalse(canEditDirectly(attemptCount1));
        assertFalse(canEditDirectly(attemptCount50));
    }

    @Test
    public void testCopyTitleGeneration() {
        String originalTitle = "Kiểm thử phần mềm";
        String copyTitle = generateCopyTitle(originalTitle);

        assertEquals("Kiểm thử phần mềm (Bản sao)", copyTitle);
    }

    @Test
    public void testCopyQuizAlwaysInitializesInDraftStatus() {
        Lesson lesson = new Lesson();
        lesson.setId(5L);

        User teacher = new User();
        teacher.setId(2L);

        Quiz publishedQuiz = new Quiz();
        publishedQuiz.setId(10L);
        publishedQuiz.setTitle("Đề thi cuối kỳ");
        publishedQuiz.setStatus(QuizStatus.PUBLISHED);
        publishedQuiz.setPublishedAt(Instant.now());
        publishedQuiz.setLesson(lesson);
        publishedQuiz.setCreatedBy(teacher);

        List<Question> questions = new ArrayList<>();
        List<QuestionOption> options = new ArrayList<>();
        options.add(new QuestionOption("A", true, 1));
        options.add(new QuestionOption("B", false, 2));
        options.add(new QuestionOption("C", false, 3));
        options.add(new QuestionOption("D", false, 4));
        questions.add(new Question("Câu 1", 1, options));
        publishedQuiz.setQuestions(questions);

        Quiz clone = createDraftClone(publishedQuiz, teacher);

        assertNull(clone.getId());
        assertEquals("Đề thi cuối kỳ (Bản sao)", clone.getTitle());
        assertEquals(QuizStatus.DRAFT, clone.getStatus());
        assertNull(clone.getPublishedAt());
        assertEquals(publishedQuiz, clone.getSourceQuiz());
        assertEquals(5L, clone.getLesson().getId());
        assertEquals(2L, clone.getCreatedBy().getId());

        assertNotNull(clone.getQuestions());
        assertEquals(1, clone.getQuestions().size());
        assertEquals(4, clone.getQuestions().get(0).getOptions().size());
    }

    @Test
    public void testCopyClosedQuizBecomesDraft() {
        Quiz closedQuiz = new Quiz();
        closedQuiz.setTitle("Bài test đã đóng");
        closedQuiz.setStatus(QuizStatus.CLOSED);

        Quiz clone = createDraftClone(closedQuiz, null);

        assertEquals("Bài test đã đóng (Bản sao)", clone.getTitle());
        assertEquals(QuizStatus.DRAFT, clone.getStatus());
        assertNull(clone.getPublishedAt());
    }
}
