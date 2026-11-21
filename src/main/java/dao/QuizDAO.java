package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.Course;
import model.Lesson;
import model.Question;
import model.Quiz;
import model.User;
import model.enums.CourseStatus;
import model.enums.QuizStatus;
import model.enums.UserRole;

public class QuizDAO extends DAO {
    public QuizDAO() throws SQLException {
        super();
    }

    /**
     * Lấy danh sách quiz đang nhận lượt làm mới (PUBLISHED) của bài học.
     * Kiểm tra trực tiếp DB: tài khoản active (STUDENT hoặc TEACHER), bài học đang hiển thị,
     * người học đã ghi danh và không phải chủ khóa học. Course CLOSED không thu hồi quyền học đã ghi danh.
     */
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

                    // Đóng gói danh sách câu hỏi rỗng đúng số lượng để giao diện đọc q.getQuestions().size()
                    // mà không nạp nội dung/đáp án trước khi bắt đầu lượt làm.
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

    /**
     * Khóa bản ghi quiz (SELECT ... FOR UPDATE) trong giao dịch bắt đầu lượt làm.
     */
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
        // TODO (M4): Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai QuizDAO.getQuizzesOfLesson()");
    }

    public Quiz createDraftQuiz(Lesson lesson, User teacher) throws SQLException {
        // TODO (M4): Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai QuizDAO.createDraftQuiz()");
    }

    public boolean saveDraft(Quiz quiz) throws SQLException {
        // TODO (M4): Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai QuizDAO.saveDraft()");
    }

    public boolean publish(Quiz quiz) throws SQLException {
        // TODO (M4): Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai QuizDAO.publish()");
    }

    public Quiz copyQuiz(Quiz sourceQuiz, User teacher) throws SQLException {
        // TODO (M4): Triển khai truy vấn và kiểm tra nghiệp vụ.
        throw new UnsupportedOperationException("Chưa triển khai QuizDAO.copyQuiz()");
    }
}
