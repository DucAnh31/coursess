package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Question;
import model.QuestionOption;

public class QuestionOptionDAO extends DAO {
    public QuestionOptionDAO() throws SQLException {
        super();
    }

    public List<QuestionOption> getOptionsByQuestion(Long questionId) throws SQLException {
        List<QuestionOption> options = new ArrayList<>();
        String sql = "SELECT id, content, is_correct, order_index FROM question_options WHERE question_id = ? ORDER BY order_index ASC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setLong(1, questionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    QuestionOption opt = new QuestionOption();
                    opt.setId(rs.getLong("id"));
                    opt.setContent(rs.getString("content"));
                    opt.setIsCorrect(rs.getBoolean("is_correct"));
                    opt.setOrderIndex(rs.getInt("order_index"));
                    options.add(opt);
                }
            }
        }
        return options;
    }

    public boolean saveOptions(Connection con, Long questionId, List<QuestionOption> options) throws SQLException {
        String deleteSql = "DELETE FROM question_options WHERE question_id = ?";
        try (PreparedStatement delStmt = con.prepareStatement(deleteSql)) {
            delStmt.setLong(1, questionId);
            delStmt.executeUpdate();
        }

        if (options != null && !options.isEmpty()) {
            String insertSql = "INSERT INTO question_options (content, is_correct, order_index, question_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insStmt = con.prepareStatement(insertSql)) {
                for (QuestionOption opt : options) {
                    insStmt.setString(1, opt.getContent());
                    insStmt.setBoolean(2, Boolean.TRUE.equals(opt.getIsCorrect()));
                    insStmt.setInt(3, opt.getOrderIndex() != null ? opt.getOrderIndex() : 1);
                    insStmt.setLong(4, questionId);
                    insStmt.addBatch();
                }
                insStmt.executeBatch();
            }
        }
        return true;
    }

    public boolean copyOptions(Connection con, Long sourceQuestionId, Long targetQuestionId) throws SQLException {
        String sql = "INSERT INTO question_options (content, is_correct, order_index, question_id) "
                + "SELECT content, is_correct, order_index, ? FROM question_options WHERE question_id = ? ORDER BY order_index ASC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setLong(1, targetQuestionId);
            stmt.setLong(2, sourceQuestionId);
            stmt.executeUpdate();
        }
        return true;
    }

    public List<QuestionOption> getOptionsOfQuestion(Question question) throws SQLException {
        if (question == null || question.getId() == null) {
            return Collections.emptyList();
        }
        return getOptionsByQuestion(question.getId());
    }

    public boolean saveOptions(Question question, List<QuestionOption> options) throws SQLException {
        if (question == null || question.getId() == null) {
            return false;
        }
        return saveOptions(this.con, question.getId(), options);
    }
}
