package dao;

import java.sql.SQLException;
import java.util.List;
import model.Question;
import model.Quiz;

public class QuestionDAO extends DAO {
    public QuestionDAO() throws SQLException {
        super();
    }

    public List<Question> getQuestionsOfQuiz(Quiz quiz) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai QuestionDAO.getQuestionsOfQuiz()");
    }

    public boolean saveQuestions(Quiz quiz, List<Question> questions) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai QuestionDAO.saveQuestions()");
    }

    public boolean copyQuestionsToQuiz(Quiz sourceQuiz, Quiz newQuiz) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai QuestionDAO.copyQuestionsToQuiz()");
    }
}
