package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Lesson;
import model.Question;
import model.Quiz;
import model.User;
import model.enums.QuizStatus;

public class QuizDAO extends DAO {
    private final QuestionDAO questionDAO;

    public QuizDAO() throws SQLException {
        super();
        this.questionDAO = new QuestionDAO();
    }

    public List<Quiz> getQuizzesByLesson(Long lessonId) throws SQLException {
        List<Quiz> quizzes = new ArrayList<>();
        String sql = "SELECT q.id, q.title, q.status, q.published_at, q.lesson_id, q.created_by, q.source_quiz_id, "
                + "u.full_name AS creator_name, l.title AS lesson_title, "
                + "(SELECT COUNT(*) FROM questions qs WHERE qs.quiz_id = q.id) AS question_count "
                + "FROM quizzes q "
                + "LEFT JOIN users u ON q.created_by = u.id "
                + "LEFT JOIN lessons l ON q.lesson_id = l.id "
                + "WHERE q.lesson_id = ? ORDER BY q.id ASC";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setLong(1, lessonId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Quiz quiz = new Quiz();
                    quiz.setId(rs.getLong("id"));
                    quiz.setTitle(rs.getString("title"));
                    quiz.setStatus(QuizStatus.valueOf(rs.getString("status")));

                    Timestamp publishedAtTs = rs.getTimestamp("published_at");
                    if (publishedAtTs != null) {
                        quiz.setPublishedAt(publishedAtTs.toInstant());
                    }

                    Lesson lesson = new Lesson();
                    lesson.setId(lessonId);
                    lesson.setTitle(rs.getString("lesson_title"));
                    quiz.setLesson(lesson);

                    User creator = new User();
                    creator.setId(rs.getLong("created_by"));
                    creator.setFullName(rs.getString("creator_name"));
                    quiz.setCreatedBy(creator);

                    Long sourceQuizId = rs.getObject("source_quiz_id", Long.class);
                    if (sourceQuizId != null) {
                        Quiz sourceQuiz = new Quiz();
                        sourceQuiz.setId(sourceQuizId);
                        quiz.setSourceQuiz(sourceQuiz);
                    }

                    int questionCount = rs.getInt("question_count");
                    List<Question> placeHolderQuestions = new ArrayList<>();
                    for (int i = 0; i < questionCount; i++) {
                        placeHolderQuestions.add(new Question());
                    }
                    quiz.setQuestions(placeHolderQuestions);

                    quizzes.add(quiz);
                }
            }
        }
        return quizzes;
    }

    public Quiz findById(Long id) throws SQLException {
        String sql = "SELECT q.id, q.title, q.status, q.published_at, q.lesson_id, q.created_by, q.source_quiz_id, "
                + "u.full_name AS creator_name, l.title AS lesson_title "
                + "FROM quizzes q "
                + "LEFT JOIN users u ON q.created_by = u.id "
                + "LEFT JOIN lessons l ON q.lesson_id = l.id "
                + "WHERE q.id = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Quiz quiz = new Quiz();
                    quiz.setId(rs.getLong("id"));
                    quiz.setTitle(rs.getString("title"));
                    quiz.setStatus(QuizStatus.valueOf(rs.getString("status")));

                    Timestamp publishedAtTs = rs.getTimestamp("published_at");
                    if (publishedAtTs != null) {
                        quiz.setPublishedAt(publishedAtTs.toInstant());
                    }

                    Lesson lesson = new Lesson();
                    lesson.setId(rs.getLong("lesson_id"));
                    lesson.setTitle(rs.getString("lesson_title"));
                    quiz.setLesson(lesson);

                    User creator = new User();
                    creator.setId(rs.getLong("created_by"));
                    creator.setFullName(rs.getString("creator_name"));
                    quiz.setCreatedBy(creator);

                    Long sourceQuizId = rs.getObject("source_quiz_id", Long.class);
                    if (sourceQuizId != null) {
                        Quiz sourceQuiz = new Quiz();
                        sourceQuiz.setId(sourceQuizId);
                        quiz.setSourceQuiz(sourceQuiz);
                    }

                    quiz.setQuestions(questionDAO.getQuestionsByQuiz(id));
                    return quiz;
                }
            }
        }
        return null;
    }

    public boolean saveDraft(Quiz quiz) throws SQLException {
        boolean originalAutoCommit = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            if (quiz.getId() == null) {
                String insertSql = "INSERT INTO quizzes (title, status, published_at, lesson_id, created_by, source_quiz_id) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, quiz.getTitle());
                    stmt.setString(2, QuizStatus.DRAFT.name());
                    stmt.setNull(3, Types.TIMESTAMP);
                    stmt.setLong(4, quiz.getLesson() != null ? quiz.getLesson().getId() : 1L);
                    stmt.setLong(5, quiz.getCreatedBy() != null ? quiz.getCreatedBy().getId() : 1L);
                    if (quiz.getSourceQuiz() != null && quiz.getSourceQuiz().getId() != null) {
                        stmt.setLong(6, quiz.getSourceQuiz().getId());
                    } else {
                        stmt.setNull(6, Types.BIGINT);
                    }
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            quiz.setId(rs.getLong(1));
                        }
                    }
                }
            } else {
                String updateSql = "UPDATE quizzes SET title = ?, status = ? WHERE id = ?";
                try (PreparedStatement stmt = con.prepareStatement(updateSql)) {
                    stmt.setString(1, quiz.getTitle());
                    stmt.setString(2, QuizStatus.DRAFT.name());
                    stmt.setLong(3, quiz.getId());
                    stmt.executeUpdate();
                }
            }

            questionDAO.saveQuestions(con, quiz.getId(), quiz.getQuestions());
            con.commit();
            quiz.setStatus(QuizStatus.DRAFT);
            return true;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(originalAutoCommit);
        }
    }

    public boolean publish(Quiz quiz) throws SQLException {
        boolean originalAutoCommit = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            Instant now = Instant.now();
            Timestamp tsNow = Timestamp.from(now);

            if (quiz.getId() == null) {
                String insertSql = "INSERT INTO quizzes (title, status, published_at, lesson_id, created_by, source_quiz_id) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, quiz.getTitle());
                    stmt.setString(2, QuizStatus.PUBLISHED.name());
                    stmt.setTimestamp(3, tsNow);
                    stmt.setLong(4, quiz.getLesson() != null ? quiz.getLesson().getId() : 1L);
                    stmt.setLong(5, quiz.getCreatedBy() != null ? quiz.getCreatedBy().getId() : 1L);
                    if (quiz.getSourceQuiz() != null && quiz.getSourceQuiz().getId() != null) {
                        stmt.setLong(6, quiz.getSourceQuiz().getId());
                    } else {
                        stmt.setNull(6, Types.BIGINT);
                    }
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            quiz.setId(rs.getLong(1));
                        }
                    }
                }
            } else {
                String updateSql = "UPDATE quizzes SET title = ?, status = ?, published_at = ? WHERE id = ?";
                try (PreparedStatement stmt = con.prepareStatement(updateSql)) {
                    stmt.setString(1, quiz.getTitle());
                    stmt.setString(2, QuizStatus.PUBLISHED.name());
                    stmt.setTimestamp(3, tsNow);
                    stmt.setLong(4, quiz.getId());
                    stmt.executeUpdate();
                }
            }

            questionDAO.saveQuestions(con, quiz.getId(), quiz.getQuestions());
            con.commit();
            quiz.setStatus(QuizStatus.PUBLISHED);
            quiz.setPublishedAt(now);
            return true;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(originalAutoCommit);
        }
    }

    public boolean closeQuiz(Long quizId) throws SQLException {
        String sql = "UPDATE quizzes SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, QuizStatus.CLOSED.name());
            stmt.setLong(2, quizId);
            return stmt.executeUpdate() > 0;
        }
    }

    public Quiz copyQuiz(Long sourceQuizId, Long teacherId) throws SQLException {
        boolean originalAutoCommit = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            String selectSql = "SELECT title, lesson_id FROM quizzes WHERE id = ?";
            String originalTitle = "";
            long lessonId = 1L;

            try (PreparedStatement selStmt = con.prepareStatement(selectSql)) {
                selStmt.setLong(1, sourceQuizId);
                try (ResultSet rs = selStmt.executeQuery()) {
                    if (rs.next()) {
                        originalTitle = rs.getString("title");
                        lessonId = rs.getLong("lesson_id");
                    } else {
                        throw new SQLException("Không tìm thấy quiz nguồn với ID: " + sourceQuizId);
                    }
                }
            }

            String newTitle = originalTitle + " (Bản sao)";
            long newQuizId;
            String insertSql = "INSERT INTO quizzes (title, status, published_at, lesson_id, created_by, source_quiz_id) VALUES (?, ?, NULL, ?, ?, ?)";
            try (PreparedStatement insStmt = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insStmt.setString(1, newTitle);
                insStmt.setString(2, QuizStatus.DRAFT.name());
                insStmt.setLong(3, lessonId);
                insStmt.setLong(4, teacherId);
                insStmt.setLong(5, sourceQuizId);
                insStmt.executeUpdate();
                try (ResultSet rs = insStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        newQuizId = rs.getLong(1);
                    } else {
                        throw new SQLException("Không tạo được bản ghi quiz bản sao mới.");
                    }
                }
            }

            questionDAO.copyQuestions(con, sourceQuizId, newQuizId);
            con.commit();

            Quiz newQuiz = new Quiz();
            newQuiz.setId(newQuizId);
            newQuiz.setTitle(newTitle);
            newQuiz.setStatus(QuizStatus.DRAFT);
            Lesson lesson = new Lesson();
            lesson.setId(lessonId);
            newQuiz.setLesson(lesson);
            User creator = new User();
            creator.setId(teacherId);
            newQuiz.setCreatedBy(creator);
            Quiz sourceQuiz = new Quiz();
            sourceQuiz.setId(sourceQuizId);
            newQuiz.setSourceQuiz(sourceQuiz);
            newQuiz.setQuestions(questionDAO.getQuestionsByQuiz(newQuizId));
            return newQuiz;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(originalAutoCommit);
        }
    }

    public Quiz copyQuiz(Quiz sourceQuiz, User teacher) throws SQLException {
        if (sourceQuiz == null || teacher == null) {
            return null;
        }
        return copyQuiz(sourceQuiz.getId(), teacher.getId());
    }

    public List<Quiz> getAvailableQuizzesOfLesson(User learner, Lesson lesson) throws SQLException {
        throw new UnsupportedOperationException("Chưa triển khai QuizDAO.getAvailableQuizzesOfLesson()");
    }

    public List<Quiz> getQuizzesOfLesson(Lesson lesson) throws SQLException {
        if (lesson == null || lesson.getId() == null) {
            return Collections.emptyList();
        }
        return getQuizzesByLesson(lesson.getId());
    }

    public Quiz createDraftQuiz(Lesson lesson, User teacher) throws SQLException {
        Quiz draft = new Quiz();
        draft.setTitle("Quiz mới");
        draft.setStatus(QuizStatus.DRAFT);
        draft.setLesson(lesson);
        draft.setCreatedBy(teacher);
        saveDraft(draft);
        return draft;
    }
}
