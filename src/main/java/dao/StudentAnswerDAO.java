package dao;

import java.sql.SQLException;
import java.util.List;
import model.QuizAttempt;
import model.StudentAnswer;

public class StudentAnswerDAO extends DAO {
    public StudentAnswerDAO() throws SQLException {
        super();
    }

    public List<StudentAnswer> getAnswersOfAttempt(QuizAttempt attempt) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai StudentAnswerDAO.getAnswersOfAttempt()");
    }

    public void saveAll(List<StudentAnswer> answers) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai StudentAnswerDAO.saveAll()");
    }
}
