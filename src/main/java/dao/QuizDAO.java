package dao;

import java.sql.Connection;
import model.Course;
import model.enums.CourseStatus;
import model.enums.UserRole;

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
        if (learner == null || learner.getId() == null) {
            throw new SecurityException("Phiên đăng nhập không hợp lệ.");
        }
        if (lesson == null || lesson.getId() == null) {
            throw new IllegalArgumentException("Bài học không hợp lệ.");
        }

        QuizAttemptDAO.requireActiveLearner(this.con, learner.getId());

        String lessonSql = "SELECT l.id, l.title, l.is_visible, c.id AS course_id, c.title AS course_title, "
                + "c.status AS course_status, c.teacher_id "
                + "FROM lessons l JOIN courses c ON l.course_id = c.id WHERE l.id = ?";
        Long courseId;
        Long teacherId;
        boolean lessonVisible;
        String lessonTitle;
        String courseTitle;
        CourseStatus courseStatus;

        try (PreparedStatement ps = this.con.prepareStatement(lessonSql)) {
            ps.setLong(1, lesson.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Không tìm thấy bài học.");
                }
                lessonVisible = rs.getBoolean("is_visible");
                courseId = rs.getLong("course_id");
                teacherId = rs.getLong("teacher_id");
                lessonTitle = rs.getString("title");
                courseTitle = rs.getString("course_title");
                courseStatus = CourseStatus.valueOf(rs.getString("course_status"));
            }
        }

        if (teacherId.equals(learner.getId())) {
            throw new SecurityException("Giảng viên không được làm bài quiz trong khóa học mình phụ trách.");
        }
        if (!QuizAttemptDAO.isEnrolled(this.con, learner.getId(), courseId)) {
            throw new SecurityException("Bạn chưa ghi danh khóa học chứa bài học này.");
        }
        if (!lessonVisible) {
            throw new IllegalStateException("Bài học hiện ngừng cung cấp nội dung học.");
        }

        Course course = new Course();
        course.setId(courseId);
        course.setTitle(courseTitle);
        course.setStatus(courseStatus);
        User teacher = new User();
        teacher.setId(teacherId);
        course.setTeacher(teacher);

        Lesson loadedLesson = new Lesson();
        loadedLesson.setId(lesson.getId());
        loadedLesson.setTitle(lessonTitle);
        loadedLesson.setIsVisible(true);
        loadedLesson.setCourse(course);

        String quizSql = "SELECT q.id, q.title, q.status, q.published_at, q.created_by, q.source_quiz_id, "
                + "(SELECT COUNT(*) FROM questions qu WHERE qu.quiz_id = q.id) AS question_count "
                + "FROM quizzes q WHERE q.lesson_id = ? AND q.status = 'PUBLISHED' ORDER BY q.id ASC";
        List<Quiz> quizzes = new ArrayList<>();
        try (PreparedStatement ps = this.con.prepareStatement(quizSql)) {
            ps.setLong(1, lesson.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Quiz q = new Quiz();
                    q.setId(rs.getLong("id"));
                    q.setTitle(rs.getString("title"));
                    q.setStatus(QuizStatus.valueOf(rs.getString("status")));
                    Timestamp pubTs = rs.getTimestamp("published_at");
                    q.setPublishedAt(pubTs != null ? pubTs.toInstant() : null);
                    q.setLesson(loadedLesson);

                    User creator = new User();
                    creator.setId(rs.getLong("created_by"));
                    q.setCreatedBy(creator);

                    long srcId = rs.getLong("source_quiz_id");
                    if (!rs.wasNull()) {
                        Quiz src = new Quiz();
                        src.setId(srcId);
                        q.setSourceQuiz(src);
                    }

                    int qCount = rs.getInt("question_count");
                    List<Question> stubs = new ArrayList<>(qCount);
                    for (int i = 0; i < qCount; i++) {
                        stubs.add(new Question());
                    }
                    q.setQuestions(stubs);
                    quizzes.add(q);
                }
            }
        }
        return quizzes;
    }

    public static Quiz findByIdForUpdate(Connection con, Long quizId) throws SQLException {
        if (quizId == null) {
            throw new IllegalArgumentException("Mã quiz không hợp lệ.");
        }
        String sql = "SELECT q.id, q.title, q.status, q.published_at, q.lesson_id, q.created_by, q.source_quiz_id, "
                + "l.title AS lesson_title, l.is_visible AS lesson_visible, "
                + "c.id AS course_id, c.title AS course_title, c.status AS course_status, c.teacher_id "
                + "FROM quizzes q "
                + "JOIN lessons l ON q.lesson_id = l.id "
                + "JOIN courses c ON l.course_id = c.id "
                + "WHERE q.id = ? FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                User teacher = new User();
                teacher.setId(rs.getLong("teacher_id"));
                teacher.setRole(UserRole.TEACHER);

                Course course = new Course();
                course.setId(rs.getLong("course_id"));
                course.setTitle(rs.getString("course_title"));
                course.setStatus(CourseStatus.valueOf(rs.getString("course_status")));
                course.setTeacher(teacher);

                Lesson lesson = new Lesson();
                lesson.setId(rs.getLong("lesson_id"));
                lesson.setTitle(rs.getString("lesson_title"));
                lesson.setIsVisible(rs.getBoolean("lesson_visible"));
                lesson.setCourse(course);

                Quiz quiz = new Quiz();
                quiz.setId(rs.getLong("id"));
                quiz.setTitle(rs.getString("title"));
                quiz.setStatus(QuizStatus.valueOf(rs.getString("status")));
                Timestamp pubTs = rs.getTimestamp("published_at");
                quiz.setPublishedAt(pubTs != null ? pubTs.toInstant() : null);
                quiz.setLesson(lesson);

                User creator = new User();
                creator.setId(rs.getLong("created_by"));
                quiz.setCreatedBy(creator);

                long srcId = rs.getLong("source_quiz_id");
                if (!rs.wasNull()) {
                    Quiz src = new Quiz();
                    src.setId(srcId);
                    quiz.setSourceQuiz(src);
                }
                return quiz;
            }
        }
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
