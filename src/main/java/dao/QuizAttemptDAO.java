package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    public int countAttemptsByQuiz(Long quizId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM quiz_attempts WHERE quiz_id = ? AND status != 'CANCELLED'";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setLong(1, quizId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public QuizAttempt startAttempt(User learner, Quiz quiz) throws SQLException {
        throw new UnsupportedOperationException("Chưa triển khai QuizAttemptDAO.startAttempt()");
    }

    public QuizAttempt submitAttempt(User learner, QuizAttempt attempt, List<StudentAnswer> answers) throws SQLException {
        throw new UnsupportedOperationException("Chưa triển khai QuizAttemptDAO.submitAttempt()");
    }

    public void cancelAttempt(User learner, QuizAttempt attempt) throws SQLException {
        throw new UnsupportedOperationException("Chưa triển khai QuizAttemptDAO.cancelAttempt()");
    }

    public void cancelUnfinishedAttempts(User user) throws SQLException {
        throw new UnsupportedOperationException("Chưa triển khai QuizAttemptDAO.cancelUnfinishedAttempts()");
    }

    public List<QuizAttempt> getHistory(User learner, Quiz quiz) throws SQLException {
        throw new UnsupportedOperationException("Chưa triển khai QuizAttemptDAO.getHistory()");
    }

    public QuizAttempt getResult(User learner, QuizAttempt attempt) throws SQLException {
        throw new UnsupportedOperationException("Chưa triển khai QuizAttemptDAO.getResult()");
    }
}
