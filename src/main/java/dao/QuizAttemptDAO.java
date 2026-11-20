package dao;

import java.sql.SQLException;
import java.util.List;
import model.Quiz;
import model.QuizAttempt;
import model.StudentAnswer;
import model.User;

public class QuizAttemptDAO extends DAO {
    public QuizAttemptDAO() throws SQLException {
        super();
    }

    public QuizAttempt startAttempt(User learner, Quiz quiz) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai QuizAttemptDAO.startAttempt()");
    }

    public QuizAttempt submitAttempt(User learner, QuizAttempt attempt, List<StudentAnswer> answers) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai QuizAttemptDAO.submitAttempt()");
    }

    public void cancelAttempt(User learner, QuizAttempt attempt) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai QuizAttemptDAO.cancelAttempt()");
    }

    public void cancelUnfinishedAttempts(User user) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai QuizAttemptDAO.cancelUnfinishedAttempts()");
    }

    public List<QuizAttempt> getHistory(User learner, Quiz quiz) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai QuizAttemptDAO.getHistory()");
    }

    public QuizAttempt getResult(User learner, QuizAttempt attempt) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai QuizAttemptDAO.getResult()");
    }
}
