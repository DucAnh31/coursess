package dao;

import java.sql.SQLException;
import java.util.List;
import model.Question;
import model.QuestionOption;

public class QuestionOptionDAO extends DAO {
    public QuestionOptionDAO() throws SQLException {
        super();
    }

    public List<QuestionOption> getOptionsOfQuestion(Question question) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai QuestionOptionDAO.getOptionsOfQuestion()");
    }

    public boolean saveOptions(Question question, List<QuestionOption> options) throws SQLException {
        // TODO: Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai QuestionOptionDAO.saveOptions()");
    }
}
