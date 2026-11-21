package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Question;
import model.Quiz;

public class QuestionDAO extends DAO {
    private final QuestionOptionDAO questionOptionDAO;

    public QuestionDAO() throws SQLException {
        super();
        this.questionOptionDAO = new QuestionOptionDAO();
    }

    public List<Question> getQuestionsByQuiz(Long quizId) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT id, content, order_index, quiz_id FROM questions WHERE quiz_id = ? ORDER BY order_index ASC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setLong(1, quizId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question();
                    q.setId(rs.getLong("id"));
                    q.setContent(rs.getString("content"));
                    q.setOrderIndex(rs.getInt("order_index"));
                    q.setOptions(questionOptionDAO.getOptionsByQuestion(q.getId()));
                    questions.add(q);
                }
            }
        }
        return questions;
    }

    public boolean saveQuestions(Connection con, Long quizId, List<Question> questions) throws SQLException {
        String deleteOptionsSql = "DELETE FROM question_options WHERE question_id IN (SELECT id FROM questions WHERE quiz_id = ?)";
        try (PreparedStatement delOptStmt = con.prepareStatement(deleteOptionsSql)) {
            delOptStmt.setLong(1, quizId);
            delOptStmt.executeUpdate();
        }

        String deleteQuestionsSql = "DELETE FROM questions WHERE quiz_id = ?";
        try (PreparedStatement delQStmt = con.prepareStatement(deleteQuestionsSql)) {
            delQStmt.setLong(1, quizId);
            delQStmt.executeUpdate();
        }

        if (questions != null && !questions.isEmpty()) {
            String insertQuestionSql = "INSERT INTO questions (content, order_index, quiz_id) VALUES (?, ?, ?)";
            for (Question q : questions) {
                try (PreparedStatement insQStmt = con.prepareStatement(insertQuestionSql, Statement.RETURN_GENERATED_KEYS)) {
                    insQStmt.setString(1, q.getContent());
                    insQStmt.setInt(2, q.getOrderIndex() != null ? q.getOrderIndex() : 1);
                    insQStmt.setLong(3, quizId);
                    insQStmt.executeUpdate();
                    try (ResultSet rs = insQStmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            long newQuestionId = rs.getLong(1);
                            q.setId(newQuestionId);
                            questionOptionDAO.saveOptions(con, newQuestionId, q.getOptions());
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean copyQuestions(Connection con, Long sourceQuizId, Long targetQuizId) throws SQLException {
        String selectSql = "SELECT id, content, order_index FROM questions WHERE quiz_id = ? ORDER BY order_index ASC";
        String insertSql = "INSERT INTO questions (content, order_index, quiz_id) VALUES (?, ?, ?)";

        try (PreparedStatement selectStmt = con.prepareStatement(selectSql)) {
            selectStmt.setLong(1, sourceQuizId);
            try (ResultSet rs = selectStmt.executeQuery()) {
                while (rs.next()) {
                    long sourceQuestionId = rs.getLong("id");
                    String content = rs.getString("content");
                    int orderIndex = rs.getInt("order_index");

                    try (PreparedStatement insStmt = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                        insStmt.setString(1, content);
                        insStmt.setInt(2, orderIndex);
                        insStmt.setLong(3, targetQuizId);
                        insStmt.executeUpdate();

                        try (ResultSet genKeys = insStmt.getGeneratedKeys()) {
                            if (genKeys.next()) {
                                long targetQuestionId = genKeys.getLong(1);
                                questionOptionDAO.copyOptions(con, sourceQuestionId, targetQuestionId);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public List<Question> getQuestionsOfQuiz(Quiz quiz) throws SQLException {
        if (quiz == null || quiz.getId() == null) {
            return Collections.emptyList();
        }
        return getQuestionsByQuiz(quiz.getId());
    }

    public boolean saveQuestions(Quiz quiz, List<Question> questions) throws SQLException {
        if (quiz == null || quiz.getId() == null) {
            return false;
        }
        return saveQuestions(this.con, quiz.getId(), questions);
    }

    public boolean copyQuestionsToQuiz(Quiz sourceQuiz, Quiz newQuiz) throws SQLException {
        if (sourceQuiz == null || newQuiz == null || sourceQuiz.getId() == null || newQuiz.getId() == null) {
            return false;
        }
        return copyQuestions(this.con, sourceQuiz.getId(), newQuiz.getId());
    }
}
