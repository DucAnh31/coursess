package dao;

import java.sql.SQLException;
import java.util.List;
import model.Lesson;
import model.Quiz;
import model.User;

public class QuizDAO extends DAO {
    public QuizDAO() throws SQLException {
        super();
    }

    public List<Quiz> getAvailableQuizzesOfLesson(User learner, Lesson lesson) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai QuizDAO.getAvailableQuizzesOfLesson()");
    }

    public List<Quiz> getQuizzesOfLesson(Lesson lesson) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai QuizDAO.getQuizzesOfLesson()");
    }

    public Quiz createDraftQuiz(Lesson lesson, User teacher) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai QuizDAO.createDraftQuiz()");
    }

    public boolean saveDraft(Quiz quiz) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai QuizDAO.saveDraft()");
    }

    public boolean publish(Quiz quiz) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai QuizDAO.publish()");
    }

    public Quiz copyQuiz(Quiz sourceQuiz, User teacher) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai QuizDAO.copyQuiz()");
    }
}
