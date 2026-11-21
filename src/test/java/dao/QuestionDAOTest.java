package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Lesson;
import model.Question;
import model.QuestionOption;
import model.Quiz;
import model.User;
import model.enums.QuizStatus;
import org.junit.jupiter.api.Test;

class QuestionDAOTest extends DaoTest {

    @Test
    void testSaveAndGetQuestionsAndOptions() throws SQLException {
        try (QuizDAO quizDAO = new QuizDAO();
             QuestionDAO questionDAO = new QuestionDAO();
             QuestionOptionDAO optionDAO = new QuestionOptionDAO()) {

            Quiz quiz = new Quiz();
            quiz.setTitle("Quiz câu hỏi");
            quiz.setStatus(QuizStatus.DRAFT);
            Lesson lesson = new Lesson();
            lesson.setId(20L);
            quiz.setLesson(lesson);
            User creator = new User();
            creator.setId(1L);
            quiz.setCreatedBy(creator);
            quizDAO.saveDraft(quiz);

            List<Question> questions = new ArrayList<>();
            Question q1 = new Question();
            q1.setContent("Câu hỏi 1");
            q1.setOrderIndex(1);

            List<QuestionOption> options = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                QuestionOption opt = new QuestionOption();
                opt.setContent("Đáp án " + i);
                opt.setIsCorrect(i == 1);
                opt.setOrderIndex(i);
                options.add(opt);
            }
            q1.setOptions(options);
            questions.add(q1);

            boolean saved = questionDAO.saveQuestions(quiz, questions);
            assertTrue(saved);

            List<Question> retrieved = questionDAO.getQuestionsOfQuiz(quiz);
            assertEquals(1, retrieved.size());
            assertEquals("Câu hỏi 1", retrieved.get(0).getContent());

            List<QuestionOption> retrievedOptions = optionDAO.getOptionsByQuestion(retrieved.get(0).getId());
            assertEquals(4, retrievedOptions.size());
            assertEquals("Đáp án 1", retrievedOptions.get(0).getContent());
            assertTrue(retrievedOptions.get(0).getIsCorrect());
        }
    }
}

