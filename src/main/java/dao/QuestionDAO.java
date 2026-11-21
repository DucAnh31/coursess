package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Question;
import model.QuestionOption;
import model.Quiz;

public class QuestionDAO extends DAO {
    public QuestionDAO() throws SQLException {
        super();
    }

    public List<Question> getQuestionsOfQuiz(Quiz quiz) throws SQLException {
        return getQuestionsOfQuiz(this.con, quiz, true);
    }

    public static List<Question> getQuestionsOfQuiz(Connection con, Quiz quiz, boolean includeCorrectFlag) throws SQLException {
        if (quiz == null || quiz.getId() == null) {
            throw new IllegalArgumentException("Quiz không hợp lệ.");
        }
        String sql = "SELECT id, content, order_index FROM questions WHERE quiz_id = ? ORDER BY order_index ASC, id ASC";
        List<Question> questions = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, quiz.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question();
                    q.setId(rs.getLong("id"));
                    q.setContent(rs.getString("content"));
                    q.setOrderIndex(rs.getInt("order_index"));
                    q.setQuiz(quiz);
                    List<QuestionOption> opts = includeCorrectFlag
                            ? QuestionOptionDAO.getOptionsWithAnswer(con, q)
                            : QuestionOptionDAO.getOptionsForAttempt(con, q);
                    q.setOptions(opts);
                    questions.add(q);
                }
            }
        }
        return questions;
    }

    public boolean saveQuestions(Quiz quiz, List<Question> questions) throws SQLException {
        // TODO (M4): Triển khai lưu câu hỏi khi soạn quiz.
        throw new UnsupportedOperationException("Chưa triển khai QuestionDAO.saveQuestions()");
    }

    public boolean copyQuestionsToQuiz(Quiz sourceQuiz, Quiz newQuiz) throws SQLException {
        // TODO (M4): Triển khai sao chép câu hỏi sang quiz mới.
        throw new UnsupportedOperationException("Chưa triển khai QuestionDAO.copyQuestionsToQuiz()");
    }
}
