package model;

import model.enums.QuizStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuizModelTest {

    @Test
    public void testQuizDefaultConstructor() {
        Quiz quiz = new Quiz();
        assertNull(quiz.getId());
        assertNull(quiz.getTitle());
        assertEquals(QuizStatus.DRAFT, quiz.getStatus());
        assertNotNull(quiz.getQuestions());
        assertTrue(quiz.getQuestions().isEmpty());
    }

    @Test
    public void testQuizConvenienceConstructor() {
        List<Question> questions = new ArrayList<>();
        Quiz quiz = new Quiz("Quiz Lập trình Hướng đối tượng", QuizStatus.DRAFT, questions);

        assertEquals("Quiz Lập trình Hướng đối tượng", quiz.getTitle());
        assertEquals(QuizStatus.DRAFT, quiz.getStatus());
        assertEquals(questions, quiz.getQuestions());
    }

    @Test
    public void testQuizStatusTransitions() {
        Quiz quiz = new Quiz();
        quiz.setStatus(QuizStatus.DRAFT);
        assertEquals(QuizStatus.DRAFT, quiz.getStatus());

        quiz.setStatus(QuizStatus.PUBLISHED);
        quiz.setPublishedAt(Instant.now());
        assertEquals(QuizStatus.PUBLISHED, quiz.getStatus());
        assertNotNull(quiz.getPublishedAt());

        quiz.setStatus(QuizStatus.CLOSED);
        assertEquals(QuizStatus.CLOSED, quiz.getStatus());
    }

    @Test
    public void testQuestionModelAndAssociation() {
        List<QuestionOption> options = new ArrayList<>();
        QuestionOption optA = new QuestionOption("Phương án A", true, 1);
        QuestionOption optB = new QuestionOption("Phương án B", false, 2);
        options.add(optA);
        options.add(optB);

        Question question = new Question("Câu hỏi số 1", 1, options);
        for (QuestionOption opt : options) {
            opt.setQuestion(question);
        }

        assertEquals("Câu hỏi số 1", question.getContent());
        assertEquals(1, question.getOrderIndex());
        assertEquals(2, question.getOptions().size());
        assertEquals(question, optA.getQuestion());
        assertTrue(optA.getIsCorrect());
        assertFalse(optB.getIsCorrect());
    }

    @Test
    public void testQuestionOptionProperties() {
        QuestionOption option = new QuestionOption();
        option.setId(10L);
        option.setContent("Đáp án đúng");
        option.setIsCorrect(true);
        option.setOrderIndex(3);

        assertEquals(10L, option.getId());
        assertEquals("Đáp án đúng", option.getContent());
        assertTrue(option.getIsCorrect());
        assertEquals(3, option.getOrderIndex());
    }
}
