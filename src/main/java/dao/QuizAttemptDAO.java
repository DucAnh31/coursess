package dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.Question;
import model.QuestionOption;
import model.Quiz;
import model.QuizAttempt;
import model.StudentAnswer;
import model.User;
import model.enums.AttemptStatus;
import model.enums.QuizStatus;
import model.enums.UserRole;

public class QuizAttemptDAO extends DAO {
    public QuizAttemptDAO() throws SQLException {
        super();
    }

    /**
     * Kiểm tra trực tiếp CSDL: tài khoản tồn tại, đang hoạt động và có vai trò được học (STUDENT hoặc TEACHER).
     */
    static User requireActiveLearner(Connection con, Long userId) throws SQLException {
        if (userId == null) {
            throw new SecurityException("Phiên đăng nhập không hợp lệ.");
        }
        String sql = "SELECT id, full_name, username, role, is_active FROM users WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SecurityException("Tài khoản không tồn tại.");
                }
                boolean active = rs.getBoolean("is_active");
                if (!active) {
                    throw new SecurityException("Tài khoản của bạn đã bị khóa.");
                }
                UserRole role = UserRole.valueOf(rs.getString("role"));
                if (role != UserRole.STUDENT && role != UserRole.TEACHER) {
                    throw new SecurityException("Vai trò tài khoản không được phép thực hiện chức năng làm quiz.");
                }
                User u = new User();
                u.setId(rs.getLong("id"));
                u.setFullName(rs.getString("full_name"));
                u.setUsername(rs.getString("username"));
                u.setRole(role);
                u.setIsActive(true);
                return u;
            }
        }
    }

    static boolean isEnrolled(Connection con, Long learnerId, Long courseId) throws SQLException {
        String sql = "SELECT 1 FROM course_members WHERE student_id = ? AND course_id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, learnerId);
            ps.setLong(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Bắt đầu lượt làm mới hoặc trả lại lượt IN_PROGRESS hiện có của (learner, quiz).
     * Khóa bản ghi quiz trong cùng giao dịch để bảo đảm tối đa một IN_PROGRESS và đề bất biến.
     */
    public QuizAttempt startAttempt(User learner, Quiz quiz) throws SQLException {
        if (learner == null || learner.getId() == null) {
            throw new SecurityException("Phiên đăng nhập không hợp lệ.");
        }
        if (quiz == null || quiz.getId() == null) {
            throw new IllegalArgumentException("Quiz không hợp lệ.");
        }

        boolean prevAutoCommit = this.con.getAutoCommit();
        this.con.setAutoCommit(false);
        try {
            User activeUser = requireActiveLearner(this.con, learner.getId());
            Quiz lockedQuiz = QuizDAO.findByIdForUpdate(this.con, quiz.getId());
            if (lockedQuiz == null) {
                throw new IllegalArgumentException("Không tìm thấy bài quiz.");
            }
            if (lockedQuiz.getLesson().getCourse().getTeacher().getId().equals(activeUser.getId())) {
                throw new SecurityException("Giảng viên không được tạo lượt làm trong khóa học mình phụ trách.");
            }
            if (!isEnrolled(this.con, activeUser.getId(), lockedQuiz.getLesson().getCourse().getId())) {
                throw new SecurityException("Bạn chưa ghi danh khóa học chứa quiz này.");
            }
            if (!Boolean.TRUE.equals(lockedQuiz.getLesson().getIsVisible())
                    || lockedQuiz.getStatus() != QuizStatus.PUBLISHED) {
                throw new IllegalStateException("Quiz không còn khả dụng để bắt đầu lượt mới.");
            }

            // Kiểm tra lượt IN_PROGRESS hiện có (tối đa 1 lượt IN_PROGRESS / người / quiz)
            String findSql = "SELECT id, status, total_questions, started_at FROM quiz_attempts "
                    + "WHERE student_id = ? AND quiz_id = ? AND status = 'IN_PROGRESS' ORDER BY id DESC LIMIT 1";
            try (PreparedStatement ps = this.con.prepareStatement(findSql)) {
                ps.setLong(1, activeUser.getId());
                ps.setLong(2, lockedQuiz.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        QuizAttempt existing = new QuizAttempt();
                        existing.setId(rs.getLong("id"));
                        existing.setStatus(AttemptStatus.IN_PROGRESS);
                        existing.setTotalQuestions(rs.getInt("total_questions"));
                        existing.setStartedAt(rs.getTimestamp("started_at").toInstant());
                        existing.setStudent(activeUser);
                        existing.setQuiz(lockedQuiz);
                        this.con.commit();
                        return existing;
                    }
                }
            }

            // Kiểm tra đề có ít nhất 1 câu hỏi hợp lệ
            int totalQuestions;
            try (PreparedStatement ps = this.con.prepareStatement("SELECT COUNT(*) FROM questions WHERE quiz_id = ?")) {
                ps.setLong(1, lockedQuiz.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    totalQuestions = rs.getInt(1);
                }
            }
            if (totalQuestions <= 0) {
                throw new IllegalStateException("Quiz chưa có câu hỏi hợp lệ.");
            }

            Instant now = Instant.now();
            String insertSql = "INSERT INTO quiz_attempts (status, total_questions, correct_count, score, started_at, submitted_at, student_id, quiz_id) "
                    + "VALUES ('IN_PROGRESS', ?, NULL, NULL, ?, NULL, ?, ?)";
            long newId;
            try (PreparedStatement ps = this.con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, totalQuestions);
                ps.setTimestamp(2, Timestamp.from(now));
                ps.setLong(3, activeUser.getId());
                ps.setLong(4, lockedQuiz.getId());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Không lấy được mã lượt làm vừa tạo.");
                    }
                    newId = keys.getLong(1);
                }
            }

            this.con.commit();

            QuizAttempt created = new QuizAttempt();
            created.setId(newId);
            created.setStatus(AttemptStatus.IN_PROGRESS);
            created.setTotalQuestions(totalQuestions);
            created.setCorrectCount(null);
            created.setScore(null);
            created.setStartedAt(now);
            created.setSubmittedAt(null);
            created.setStudent(activeUser);
            created.setQuiz(lockedQuiz);
            return created;
        } catch (SQLException | RuntimeException ex) {
            rollbackQuietly();
            throw ex;
        } finally {
            restoreAutoCommit(prevAutoCommit);
        }
    }

    /**
     * Lấy nội dung đề cho lượt IN_PROGRESS hợp lệ của chính người học.
     * KHÔNG nạp cờ isCorrect (đặt null) để không lộ đáp án đúng trước khi nộp.
     * Không chặn nếu quiz đã chuyển CLOSED sau khi lượt đã bắt đầu hợp lệ.
     */
    public QuizAttempt getAttemptContent(User learner, QuizAttempt attempt) throws SQLException {
        if (learner == null || learner.getId() == null) {
            throw new SecurityException("Phiên đăng nhập không hợp lệ.");
        }
        if (attempt == null || attempt.getId() == null) {
            throw new IllegalArgumentException("Lượt làm không hợp lệ.");
        }

        User activeUser = requireActiveLearner(this.con, learner.getId());
        String sql = "SELECT qa.id, qa.status, qa.total_questions, qa.started_at, qa.student_id, "
                + "q.id AS quiz_id, q.title AS quiz_title, q.status AS quiz_status "
                + "FROM quiz_attempts qa JOIN quizzes q ON qa.quiz_id = q.id WHERE qa.id = ?";
        QuizAttempt loaded = new QuizAttempt();
        Quiz quiz = new Quiz();
        try (PreparedStatement ps = this.con.prepareStatement(sql)) {
            ps.setLong(1, attempt.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Không tìm thấy lượt làm bài.");
                }
                if (rs.getLong("student_id") != activeUser.getId()) {
                    throw new SecurityException("Bạn không có quyền truy cập lượt làm bài này.");
                }
                AttemptStatus st = AttemptStatus.valueOf(rs.getString("status"));
                if (st != AttemptStatus.IN_PROGRESS) {
                    throw new IllegalStateException("Lượt làm bài không còn ở trạng thái đang làm.");
                }
                loaded.setId(rs.getLong("id"));
                loaded.setStatus(st);
                loaded.setTotalQuestions(rs.getInt("total_questions"));
                loaded.setStartedAt(rs.getTimestamp("started_at").toInstant());
                loaded.setStudent(activeUser);

                quiz.setId(rs.getLong("quiz_id"));
                quiz.setTitle(rs.getString("quiz_title"));
                quiz.setStatus(QuizStatus.valueOf(rs.getString("quiz_status")));
            }
        }
        // includeCorrectFlag = false -> isCorrect = null (chưa tải)
        List<Question> questions = QuestionDAO.getQuestionsOfQuiz(this.con, quiz, false);
        quiz.setQuestions(questions);
        loaded.setQuiz(quiz);
        return loaded;
    }

    /**
     * Nộp bài nguyên tử trong 1 transaction: khóa lượt FOR UPDATE, kiểm tra quyền/lựa chọn,
     * tự lấy đáp án đúng từ DB để chấm thang 10 (HALF_UP 2 chữ số), lưu StudentAnswer và cập nhật SUBMITTED.
     * Nộp lặp lượt đã SUBMITTED trả lại kết quả đã lưu mà không chấm lại/ghi đè.
     */
    public QuizAttempt submitAttempt(User learner, QuizAttempt attempt, List<StudentAnswer> answers) throws SQLException {
        if (learner == null || learner.getId() == null) {
            throw new SecurityException("Phiên đăng nhập không hợp lệ.");
        }
        if (attempt == null || attempt.getId() == null) {
            throw new IllegalArgumentException("Lượt làm bài không hợp lệ.");
        }

        boolean prevAutoCommit = this.con.getAutoCommit();
        this.con.setAutoCommit(false);
        boolean alreadySubmitted = false;
        try {
            User activeUser = requireActiveLearner(this.con, learner.getId());

            String lockSql = "SELECT qa.id, qa.status, qa.total_questions, qa.started_at, qa.student_id, qa.quiz_id, q.title AS quiz_title "
                    + "FROM quiz_attempts qa JOIN quizzes q ON qa.quiz_id = q.id WHERE qa.id = ? FOR UPDATE";
            AttemptStatus currentStatus;
            long quizId;
            String quizTitle;
            Instant startedAt;
            try (PreparedStatement ps = this.con.prepareStatement(lockSql)) {
                ps.setLong(1, attempt.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Không tìm thấy lượt làm bài.");
                    }
                    if (rs.getLong("student_id") != activeUser.getId()) {
                        throw new SecurityException("Bạn không có quyền nộp lượt làm bài này.");
                    }
                    currentStatus = AttemptStatus.valueOf(rs.getString("status"));
                    quizId = rs.getLong("quiz_id");
                    quizTitle = rs.getString("quiz_title");
                    startedAt = rs.getTimestamp("started_at").toInstant();
                }
            }

            if (currentStatus == AttemptStatus.SUBMITTED) {
                alreadySubmitted = true;
                this.con.commit();
            } else if (currentStatus == AttemptStatus.CANCELLED) {
                throw new IllegalStateException("Lượt làm bài đã bị hủy, không thể nộp bài.");
            } else {
                Quiz quiz = new Quiz();
                quiz.setId(quizId);
                quiz.setTitle(quizTitle);
                List<Question> dbQuestions = QuestionDAO.getQuestionsOfQuiz(this.con, quiz, true);
                if (dbQuestions.isEmpty()) {
                    throw new IllegalStateException("Đề quiz không có câu hỏi hợp lệ.");
                }
                quiz.setQuestions(dbQuestions);

                Map<Long, Question> questionMap = new HashMap<>();
                Map<Long, Map<Long, QuestionOption>> optionMapByQuestion = new HashMap<>();
                for (Question q : dbQuestions) {
                    questionMap.put(q.getId(), q);
                    Map<Long, QuestionOption> optMap = new HashMap<>();
                    for (QuestionOption o : q.getOptions()) {
                        optMap.put(o.getId(), o);
                    }
                    optionMapByQuestion.put(q.getId(), optMap);
                }

                // Kiểm tra lựa chọn đầu vào: không trùng câu, câu thuộc quiz, phương án thuộc đúng câu
                Map<Long, Long> selectedOptionIdByQuestion = new HashMap<>();
                Set<Long> seenQuestions = new HashSet<>();
                if (answers != null) {
                    for (StudentAnswer input : answers) {
                        if (input == null || input.getQuestion() == null || input.getQuestion().getId() == null) {
                            throw new IllegalArgumentException("Dữ liệu câu trả lời thiếu mã câu hỏi.");
                        }
                        Long qId = input.getQuestion().getId();
                        if (!questionMap.containsKey(qId)) {
                            throw new IllegalArgumentException("Câu hỏi không thuộc bài quiz của lượt làm.");
                        }
                        if (!seenQuestions.add(qId)) {
                            throw new IllegalArgumentException("Mỗi câu hỏi chỉ được gửi tối đa một lựa chọn.");
                        }
                        if (input.getSelectedOption() != null && input.getSelectedOption().getId() != null) {
                            Long optId = input.getSelectedOption().getId();
                            if (!optionMapByQuestion.get(qId).containsKey(optId)) {
                                throw new IllegalArgumentException("Phương án đã chọn không thuộc câu hỏi tương ứng.");
                            }
                            selectedOptionIdByQuestion.put(qId, optId);
                        }
                    }
                }

                // Chấm bài theo đáp án từ DB (bỏ qua hoàn toàn mọi điểm/isCorrect do giao diện truyền)
                QuizAttempt gradedAttempt = new QuizAttempt();
                gradedAttempt.setId(attempt.getId());
                gradedAttempt.setStudent(activeUser);
                gradedAttempt.setQuiz(quiz);
                gradedAttempt.setStartedAt(startedAt);

                List<StudentAnswer> gradedAnswers = new ArrayList<>(dbQuestions.size());
                int correctCount = 0;
                for (Question q : dbQuestions) {
                    StudentAnswer sa = new StudentAnswer();
                    sa.setAttempt(gradedAttempt);
                    sa.setQuestion(q);

                    Long chosenOptId = selectedOptionIdByQuestion.get(q.getId());
                    if (chosenOptId != null) {
                        QuestionOption dbOpt = optionMapByQuestion.get(q.getId()).get(chosenOptId);
                        sa.setSelectedOption(dbOpt);
                        boolean right = Boolean.TRUE.equals(dbOpt.getIsCorrect());
                        sa.setIsCorrect(right);
                        if (right) {
                            correctCount++;
                        }
                    } else {
                        sa.setSelectedOption(null);
                        sa.setIsCorrect(false);
                    }
                    gradedAnswers.add(sa);
                }

                int totalQuestions = dbQuestions.size();
                BigDecimal score = BigDecimal.valueOf(correctCount)
                        .multiply(BigDecimal.TEN)
                        .divide(BigDecimal.valueOf(totalQuestions), 2, RoundingMode.HALF_UP);
                Instant submittedAt = Instant.now();

                // Lưu toàn bộ câu trả lời và cập nhật lượt làm trong cùng 1 transaction (dùng chung this.con)
                StudentAnswerDAO.insertAll(this.con, gradedAnswers);

                String updSql = "UPDATE quiz_attempts SET status = 'SUBMITTED', total_questions = ?, "
                        + "correct_count = ?, score = ?, submitted_at = ? WHERE id = ?";
                try (PreparedStatement ps = this.con.prepareStatement(updSql)) {
                    ps.setInt(1, totalQuestions);
                    ps.setInt(2, correctCount);
                    ps.setBigDecimal(3, score);
                    ps.setTimestamp(4, Timestamp.from(submittedAt));
                    ps.setLong(5, attempt.getId());
                    ps.executeUpdate();
                }

                this.con.commit();

                gradedAttempt.setStatus(AttemptStatus.SUBMITTED);
                gradedAttempt.setTotalQuestions(totalQuestions);
                gradedAttempt.setCorrectCount(correctCount);
                gradedAttempt.setScore(score);
                gradedAttempt.setSubmittedAt(submittedAt);
                gradedAttempt.setAnswers(gradedAnswers);
                return gradedAttempt;
            }
        } catch (SQLException | RuntimeException ex) {
            rollbackQuietly();
            throw ex;
        } finally {
            restoreAutoCommit(prevAutoCommit);
        }

        // Nộp lặp lượt đã SUBMITTED -> trả kết quả đã lưu
        if (alreadySubmitted) {
            return getResult(learner, attempt);
        }
        throw new IllegalStateException("Không thể hoàn tất nộp bài.");
    }

    /**
     * Hủy lượt đang làm (khi người học rời bài / đóng cửa sổ / đăng xuất và xác nhận hủy).
     * Chỉ chuyển IN_PROGRESS -> CANCELLED; không đổi SUBMITTED thành CANCELLED.
     */
    public void cancelAttempt(User learner, QuizAttempt attempt) throws SQLException {
        if (learner == null || learner.getId() == null) {
            throw new SecurityException("Phiên đăng nhập không hợp lệ.");
        }
        if (attempt == null || attempt.getId() == null) {
            throw new IllegalArgumentException("Lượt làm bài không hợp lệ.");
        }

        boolean prevAutoCommit = this.con.getAutoCommit();
        this.con.setAutoCommit(false);
        try {
            User activeUser = requireActiveLearner(this.con, learner.getId());
            String lockSql = "SELECT status, student_id FROM quiz_attempts WHERE id = ? FOR UPDATE";
            AttemptStatus st;
            try (PreparedStatement ps = this.con.prepareStatement(lockSql)) {
                ps.setLong(1, attempt.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Không tìm thấy lượt làm bài.");
                    }
                    if (rs.getLong("student_id") != activeUser.getId()) {
                        throw new SecurityException("Bạn không có quyền hủy lượt làm bài này.");
                    }
                    st = AttemptStatus.valueOf(rs.getString("status"));
                }
            }
            if (st == AttemptStatus.SUBMITTED) {
                throw new IllegalStateException("Lượt làm đã được nộp trước đó, không thể hủy.");
            }
            if (st == AttemptStatus.IN_PROGRESS) {
                String updSql = "UPDATE quiz_attempts SET status = 'CANCELLED', correct_count = NULL, "
                        + "score = NULL, submitted_at = NULL WHERE id = ?";
                try (PreparedStatement ps = this.con.prepareStatement(updSql)) {
                    ps.setLong(1, attempt.getId());
                    ps.executeUpdate();
                }
            }
            this.con.commit();
        } catch (SQLException | RuntimeException ex) {
            rollbackQuietly();
            throw ex;
        } finally {
            restoreAutoCommit(prevAutoCommit);
        }
    }

    /**
     * Điểm tích hợp cho luồng đăng nhập M1: hủy toàn bộ lượt IN_PROGRESS còn sót của tài khoản sau dừng bất thường.
     * Không xóa bản ghi và không ảnh hưởng các lượt SUBMITTED.
     */
    public void cancelUnfinishedAttempts(User user) throws SQLException {
        if (user == null || user.getId() == null) {
            throw new SecurityException("Tài khoản không hợp lệ.");
        }
        boolean prevAutoCommit = this.con.getAutoCommit();
        this.con.setAutoCommit(false);
        try {
            String updSql = "UPDATE quiz_attempts SET status = 'CANCELLED', correct_count = NULL, "
                    + "score = NULL, submitted_at = NULL WHERE student_id = ? AND status = 'IN_PROGRESS'";
            try (PreparedStatement ps = this.con.prepareStatement(updSql)) {
                ps.setLong(1, user.getId());
                ps.executeUpdate();
            }
            this.con.commit();
        } catch (SQLException | RuntimeException ex) {
            rollbackQuietly();
            throw ex;
        } finally {
            restoreAutoCommit(prevAutoCommit);
        }
    }

    /**
     * Lấy danh sách các quiz từng có lượt SUBMITTED của người học (kể cả quiz CLOSED / bài học ẩn / khóa CLOSED).
     */
    public List<Quiz> getHistoryQuizzes(User learner) throws SQLException {
        if (learner == null || learner.getId() == null) {
            throw new SecurityException("Phiên đăng nhập không hợp lệ.");
        }
        User activeUser = requireActiveLearner(this.con, learner.getId());

        String sql = "SELECT DISTINCT q.id, q.title, q.status, q.published_at "
                + "FROM quiz_attempts qa JOIN quizzes q ON qa.quiz_id = q.id "
                + "WHERE qa.student_id = ? AND qa.status = 'SUBMITTED' "
                + "ORDER BY q.id ASC";
        List<Quiz> quizzes = new ArrayList<>();
        try (PreparedStatement ps = this.con.prepareStatement(sql)) {
            ps.setLong(1, activeUser.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Quiz q = new Quiz();
                    q.setId(rs.getLong("id"));
                    q.setTitle(rs.getString("title"));
                    q.setStatus(QuizStatus.valueOf(rs.getString("status")));
                    Timestamp pubTs = rs.getTimestamp("published_at");
                    q.setPublishedAt(pubTs != null ? pubTs.toInstant() : null);
                    quizzes.add(q);
                }
            }
        }
        return quizzes;
    }

    /**
     * Lấy danh sách lượt SUBMITTED của người học cho một quiz (sắp thời gian nộp giảm dần).
     */
    public List<QuizAttempt> getHistory(User learner, Quiz quiz) throws SQLException {
        if (learner == null || learner.getId() == null) {
            throw new SecurityException("Phiên đăng nhập không hợp lệ.");
        }
        if (quiz == null || quiz.getId() == null) {
            throw new IllegalArgumentException("Quiz không hợp lệ.");
        }
        User activeUser = requireActiveLearner(this.con, learner.getId());

        String sql = "SELECT id, status, total_questions, correct_count, score, started_at, submitted_at "
                + "FROM quiz_attempts WHERE student_id = ? AND quiz_id = ? AND status = 'SUBMITTED' "
                + "ORDER BY submitted_at DESC, id DESC";
        List<QuizAttempt> list = new ArrayList<>();
        try (PreparedStatement ps = this.con.prepareStatement(sql)) {
            ps.setLong(1, activeUser.getId());
            ps.setLong(2, quiz.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    QuizAttempt qa = new QuizAttempt();
                    qa.setId(rs.getLong("id"));
                    qa.setStatus(AttemptStatus.SUBMITTED);
                    qa.setTotalQuestions(rs.getInt("total_questions"));
                    qa.setCorrectCount(rs.getInt("correct_count"));
                    qa.setScore(rs.getBigDecimal("score"));
                    qa.setStartedAt(rs.getTimestamp("started_at").toInstant());
                    Timestamp subTs = rs.getTimestamp("submitted_at");
                    qa.setSubmittedAt(subTs != null ? subTs.toInstant() : null);
                    qa.setStudent(activeUser);
                    qa.setQuiz(quiz);
                    list.add(qa);
                }
            }
        }
        return list;
    }

    /**
     * Lấy điểm cao nhất trong các lượt SUBMITTED của (learner, quiz).
     * Trả về null nếu chưa có lượt nộp nào (giao diện hiển thị "Chưa nộp bài", không phải 0).
     */
    public BigDecimal getBestScore(User learner, Quiz quiz) throws SQLException {
        if (learner == null || learner.getId() == null) {
            throw new SecurityException("Phiên đăng nhập không hợp lệ.");
        }
        if (quiz == null || quiz.getId() == null) {
            throw new IllegalArgumentException("Quiz không hợp lệ.");
        }
        User activeUser = requireActiveLearner(this.con, learner.getId());

        String sql = "SELECT MAX(score) AS best_score FROM quiz_attempts "
                + "WHERE student_id = ? AND quiz_id = ? AND status = 'SUBMITTED'";
        try (PreparedStatement ps = this.con.prepareStatement(sql)) {
            ps.setLong(1, activeUser.getId());
            ps.setLong(2, quiz.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("best_score"); // null nếu chưa có lượt SUBMITTED
                }
                return null;
            }
        }
    }

    /**
     * Lấy kết quả chi tiết của lượt SUBMITTED thuộc chính người học, kèm câu hỏi, 4 phương án và đáp án đúng.
     * Không phụ thuộc quiz còn PUBLISHED hay bài học còn hiển thị.
     */
    public QuizAttempt getResult(User learner, QuizAttempt attempt) throws SQLException {
        if (learner == null || learner.getId() == null) {
            throw new SecurityException("Phiên đăng nhập không hợp lệ.");
        }
        if (attempt == null || attempt.getId() == null) {
            throw new IllegalArgumentException("Lượt làm bài không hợp lệ.");
        }
        User activeUser = requireActiveLearner(this.con, learner.getId());

        String sql = "SELECT qa.id, qa.status, qa.total_questions, qa.correct_count, qa.score, "
                + "qa.started_at, qa.submitted_at, qa.student_id, "
                + "q.id AS quiz_id, q.title AS quiz_title, q.status AS quiz_status "
                + "FROM quiz_attempts qa JOIN quizzes q ON qa.quiz_id = q.id WHERE qa.id = ?";
        QuizAttempt result = new QuizAttempt();
        Quiz quiz = new Quiz();
        try (PreparedStatement ps = this.con.prepareStatement(sql)) {
            ps.setLong(1, attempt.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Không tìm thấy lượt làm bài.");
                }
                if (rs.getLong("student_id") != activeUser.getId()) {
                    throw new SecurityException("Bạn chỉ được xem kết quả bài làm của chính mình.");
                }
                AttemptStatus st = AttemptStatus.valueOf(rs.getString("status"));
                if (st != AttemptStatus.SUBMITTED) {
                    throw new IllegalStateException("Lượt làm bài chưa được nộp nên không có kết quả.");
                }
                result.setId(rs.getLong("id"));
                result.setStatus(st);
                result.setTotalQuestions(rs.getInt("total_questions"));
                result.setCorrectCount(rs.getInt("correct_count"));
                result.setScore(rs.getBigDecimal("score"));
                result.setStartedAt(rs.getTimestamp("started_at").toInstant());
                Timestamp subTs = rs.getTimestamp("submitted_at");
                result.setSubmittedAt(subTs != null ? subTs.toInstant() : null);
                result.setStudent(activeUser);

                quiz.setId(rs.getLong("quiz_id"));
                quiz.setTitle(rs.getString("quiz_title"));
                quiz.setStatus(QuizStatus.valueOf(rs.getString("quiz_status")));
            }
        }

        List<Question> questions = QuestionDAO.getQuestionsOfQuiz(this.con, quiz, true);
        quiz.setQuestions(questions);
        result.setQuiz(quiz);

        List<StudentAnswer> savedAnswers = StudentAnswerDAO.getAnswersOfAttempt(this.con, result);
        // Gắn lại đối tượng Question đầy đủ (kèm 4 phương án & đáp án đúng) vào mỗi StudentAnswer
        Map<Long, Question> qById = new HashMap<>();
        for (Question q : questions) {
            qById.put(q.getId(), q);
        }
        for (StudentAnswer sa : savedAnswers) {
            Question fullQ = qById.get(sa.getQuestion().getId());
            if (fullQ != null) {
                sa.setQuestion(fullQ);
            }
        }
        result.setAnswers(savedAnswers);
        return result;
    }

    /**
     * Phương thức dùng chung với M4: đếm tổng số lượt bắt đầu của quiz (tính cả CANCELLED)
     * để khóa chỉnh sửa trực tiếp đề đã phát sinh lượt làm.
     */
    public int countAttemptsByQuiz(Long quizId) throws SQLException {
        if (quizId == null) {
            return 0;
        }
        try (PreparedStatement ps = this.con.prepareStatement("SELECT COUNT(*) FROM quiz_attempts WHERE quiz_id = ?")) {
            ps.setLong(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private void rollbackQuietly() {
        try {
            this.con.rollback();
        } catch (SQLException ignored) {
            // Bỏ qua lỗi phụ khi rollback
        }
    }

    private void restoreAutoCommit(boolean prev) {
        try {
            this.con.setAutoCommit(prev);
        } catch (SQLException ignored) {
            // Bỏ qua lỗi khôi phục autoCommit
        }
    }
}
