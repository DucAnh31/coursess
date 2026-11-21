package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Question;
import model.QuestionOption;

public class QuestionOptionDAO extends DAO {
    public QuestionOptionDAO() throws SQLException {
        super();
    }

    public List<QuestionOption> getOptionsOfQuestion(Question question) throws SQLException {
        return getOptionsWithAnswer(this.con, question);
    }

    // Omit is_correct while an attempt is in progress (null = unloaded).
    public static List<QuestionOption> getOptionsForAttempt(Connection con, Question question) throws SQLException {
        if (question == null || question.getId() == null) {
            throw new IllegalArgumentException("Câu hỏi không hợp lệ.");
        }
        String sql = "SELECT id, content, order_index FROM question_options WHERE question_id = ? ORDER BY order_index ASC, id ASC";
        List<QuestionOption> options = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, question.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    QuestionOption opt = new QuestionOption();
                    opt.setId(rs.getLong("id"));
                    opt.setContent(rs.getString("content"));
                    opt.setIsCorrect(null);
                    opt.setOrderIndex(rs.getInt("order_index"));
                    opt.setQuestion(question);
                    options.add(opt);
                }
            }
        }
        return options;
    }

    public static List<QuestionOption> getOptionsWithAnswer(Connection con, Question question) throws SQLException {
        if (question == null || question.getId() == null) {
            throw new IllegalArgumentException("Câu hỏi không hợp lệ.");
        }
        String sql = "SELECT id, content, is_correct, order_index FROM question_options WHERE question_id = ? ORDER BY order_index ASC, id ASC";
        List<QuestionOption> options = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, question.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    QuestionOption opt = new QuestionOption();
                    opt.setId(rs.getLong("id"));
                    opt.setContent(rs.getString("content"));
                    opt.setIsCorrect(rs.getBoolean("is_correct"));
                    opt.setOrderIndex(rs.getInt("order_index"));
                    opt.setQuestion(question);
                    options.add(opt);
                }
            }
        }
        return options;
    }

    public boolean saveOptions(Question question, List<QuestionOption> options) throws SQLException {
        // TODO (M4): Triển khai lưu phương án khi soạn quiz.
        throw new UnsupportedOperationException("Chưa triển khai QuestionOptionDAO.saveOptions()");
    }
}
