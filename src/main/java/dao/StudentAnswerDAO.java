package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.Question;
import model.QuestionOption;
import model.QuizAttempt;
import model.StudentAnswer;

public class StudentAnswerDAO extends DAO {
    public StudentAnswerDAO() throws SQLException {
        super();
    }

    public List<StudentAnswer> getAnswersOfAttempt(QuizAttempt attempt) throws SQLException {
        return getAnswersOfAttempt(this.con, attempt);
    }

    public void saveAll(List<StudentAnswer> answers) throws SQLException {
        insertAll(this.con, answers);
    }

    /**
     * Chèn toàn bộ câu trả lời trên cùng Connection con của giao dịch nộp bài.
     * DAO con KHÔNG tự commit hoặc rollback riêng.
     */
    public static void insertAll(Connection con, List<StudentAnswer> answers) throws SQLException {
        if (answers == null || answers.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO student_answers (is_correct, attempt_id, question_id, selected_option_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (StudentAnswer ans : answers) {
                ps.setBoolean(1, Boolean.TRUE.equals(ans.getIsCorrect()));
                ps.setLong(2, ans.getAttempt().getId());
                ps.setLong(3, ans.getQuestion().getId());
                if (ans.getSelectedOption() != null && ans.getSelectedOption().getId() != null) {
                    ps.setLong(4, ans.getSelectedOption().getId());
                } else {
                    ps.setNull(4, Types.BIGINT);
                }
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Lấy danh sách câu trả lời đã lưu của lượt làm (theo thứ tự câu hỏi).
     */
    public static List<StudentAnswer> getAnswersOfAttempt(Connection con, QuizAttempt attempt) throws SQLException {
        if (attempt == null || attempt.getId() == null) {
            throw new IllegalArgumentException("Lượt làm bài không hợp lệ.");
        }
        String sql = "SELECT sa.id, sa.is_correct, sa.question_id, sa.selected_option_id, "
                + "q.content AS question_content, q.order_index AS question_order, "
                + "qo.content AS option_content, qo.is_correct AS option_is_correct, qo.order_index AS option_order "
                + "FROM student_answers sa "
                + "JOIN questions q ON sa.question_id = q.id "
                + "LEFT JOIN question_options qo ON sa.selected_option_id = qo.id "
                + "WHERE sa.attempt_id = ? "
                + "ORDER BY q.order_index ASC, sa.id ASC";
        List<StudentAnswer> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, attempt.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StudentAnswer ans = new StudentAnswer();
                    ans.setId(rs.getLong("id"));
                    ans.setIsCorrect(rs.getBoolean("is_correct"));
                    ans.setAttempt(attempt);

                    Question q = new Question();
                    q.setId(rs.getLong("question_id"));
                    q.setContent(rs.getString("question_content"));
                    q.setOrderIndex(rs.getInt("question_order"));
                    ans.setQuestion(q);

                    long selId = rs.getLong("selected_option_id");
                    if (!rs.wasNull()) {
                        QuestionOption opt = new QuestionOption();
                        opt.setId(selId);
                        opt.setContent(rs.getString("option_content"));
                        opt.setIsCorrect(rs.getBoolean("option_is_correct"));
                        opt.setOrderIndex(rs.getInt("option_order"));
                        opt.setQuestion(q);
                        ans.setSelectedOption(opt);
                    } else {
                        ans.setSelectedOption(null);
                    }
                    list.add(ans);
                }
            }
        }
        return list;
    }
}
