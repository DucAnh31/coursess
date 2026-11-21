import dao.QuizAttemptDAO;
import dao.QuizDAO;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Lesson;
import model.Question;
import model.QuestionOption;
import model.Quiz;
import model.QuizAttempt;
import model.StudentAnswer;
import model.User;
import model.enums.AttemptStatus;

public class M3SelfTest {
    private static int passed = 0;
    private static int failed = 0;

    private static void assertTrue(String name, boolean cond) {
        if (cond) {
            passed++;
            System.out.println("[PASS] " + name);
        } else {
            failed++;
            System.err.println("[FAIL] " + name);
        }
    }

    private static void assertThrows(String name, Class<? extends Throwable> expected, ThrowingRunnable fn) {
        try {
            fn.run();
            failed++;
            System.err.println("[FAIL] " + name + " (không ném ngoại lệ " + expected.getSimpleName() + ")");
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                passed++;
                System.out.println("[PASS] " + name + " -> " + t.getMessage());
            } else {
                failed++;
                System.err.println("[FAIL] " + name + " (ném sai loại: " + t.getClass().getSimpleName() + ": " + t.getMessage() + ")");
            }
        }
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static User user(long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    private static Lesson lesson(long id) {
        Lesson l = new Lesson();
        l.setId(id);
        return l;
    }

    private static Quiz quiz(long id) {
        Quiz q = new Quiz();
        q.setId(id);
        return q;
    }

    public static void main(String[] args) throws Exception {
        try (QuizAttemptDAO cleanupDao = new QuizAttemptDAO()) {
            Connection c = cleanupDao.getConnection();
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE quizzes SET status = 'CLOSED' WHERE lesson_id = 1 AND id <> 1 AND title = 'Quiz bài 1 - Biến và kiểu dữ liệu'")) {
                ps.executeUpdate();
            }
        }
        try (QuizDAO qDao = new QuizDAO()) {
            assertThrows("1. Tài khoản bị khóa không xem được danh sách quiz", SecurityException.class,
                    () -> qDao.getAvailableQuizzesOfLesson(user(7), lesson(1)));
        }

        try (QuizDAO qDao = new QuizDAO()) {
            assertThrows("2. Giảng viên không được làm quiz trong khóa mình dạy", SecurityException.class,
                    () -> qDao.getAvailableQuizzesOfLesson(user(2), lesson(1)));
            assertThrows("2b. Giảng viên không được tham gia/làm quiz trong khóa học của giảng viên khác",
                    SecurityException.class,
                    () -> qDao.getAvailableQuizzesOfLesson(user(3), lesson(1)));
        }

        try (QuizDAO qDao = new QuizDAO()) {
            assertThrows("3. Người chưa ghi danh bị từ chối", SecurityException.class,
                    () -> qDao.getAvailableQuizzesOfLesson(user(6), lesson(1)));
        }

        try (QuizDAO qDao = new QuizDAO()) {
            List<Quiz> list = qDao.getAvailableQuizzesOfLesson(user(4), lesson(1));
            assertTrue("4. Chỉ trả quiz PUBLISHED của bài học 1 (đúng 1 quiz id=1)",
                    list.size() == 1 && list.get(0).getId().equals(1L) && list.get(0).getQuestions().size() == 2);
        }

        try (QuizAttemptDAO aDao = new QuizAttemptDAO()) {
            assertThrows("5. Quiz CLOSED không cho bắt đầu lượt mới", IllegalStateException.class,
                    () -> aDao.startAttempt(user(4), quiz(3)));
        }

        QuizAttempt att1;
        try (QuizAttemptDAO aDao = new QuizAttemptDAO()) {
            att1 = aDao.startAttempt(user(4), quiz(1));
            QuizAttempt att1Again = aDao.startAttempt(user(4), quiz(1));
            assertTrue("6. Tối đa 1 lượt IN_PROGRESS/người/quiz (trả lại cùng mã lượt)",
                    att1.getId() != null && att1.getId().equals(att1Again.getId())
                            && att1.getStatus() == AttemptStatus.IN_PROGRESS
                            && att1.getScore() == null && att1.getSubmittedAt() == null);
        }

        QuizAttempt content;
        try (QuizAttemptDAO aDao = new QuizAttemptDAO()) {
            content = aDao.getAttemptContent(user(4), att1);
            boolean allNull = content.getQuiz().getQuestions().stream()
                    .flatMap(q -> q.getOptions().stream())
                    .allMatch(o -> o.getIsCorrect() == null);
            assertTrue("7. Tải đề làm bài không lộ đáp án đúng (mọi isCorrect == null)",
                    content.getQuiz().getQuestions().size() == 2 && allNull);
        }

        try (QuizAttemptDAO aDao = new QuizAttemptDAO()) {
            assertThrows("8. Chặn truy cập lượt của người học khác", SecurityException.class,
                    () -> aDao.getAttemptContent(user(5), att1));
        }

        try (QuizAttemptDAO aDao = new QuizAttemptDAO()) {
            aDao.cancelAttempt(user(4), att1);
            assertThrows("9. Lượt đã CANCELLED bị từ chối nộp bài", IllegalStateException.class,
                    () -> aDao.submitAttempt(user(4), att1, List.of()));
        }

        long pyQuizId;
        long[] pyQIds = new long[3];
        long[] pyOptA = new long[3];
        try (QuizAttemptDAO aDao = new QuizAttemptDAO()) {
            Connection c = aDao.getConnection();
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO quizzes (title, status, published_at, lesson_id, created_by) VALUES ('Quiz bài 1 - Biến và kiểu dữ liệu', 'PUBLISHED', NOW(), 1, 2)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.executeUpdate();
                var rs = ps.getGeneratedKeys();
                rs.next();
                pyQuizId = rs.getLong(1);
            }
            String[] qTexts = {
                "Kiểu dữ liệu nào dùng để lưu số nguyên trong Python?",
                "Câu lệnh nào gán giá trị 5 cho biến x?",
                "Hàm nào dùng để xem kiểu dữ liệu của một biến?"
            };
            String[][] opts = {
                {"int", "str", "float", "bool"},
                {"x == 5", "x = 5", "5 = x", "int x = 5"},
                {"print()", "input()", "type()", "len()"}
            };
            int[] correctIdx = {0, 1, 2};
            for (int i = 0; i < 3; i++) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO questions (content, order_index, quiz_id) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, qTexts[i]);
                    ps.setInt(2, i + 1);
                    ps.setLong(3, pyQuizId);
                    ps.executeUpdate();
                    var rs = ps.getGeneratedKeys();
                    rs.next();
                    pyQIds[i] = rs.getLong(1);
                }
                for (int j = 0; j < 4; j++) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO question_options (content, is_correct, order_index, question_id) VALUES (?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, opts[i][j]);
                        ps.setBoolean(2, j == correctIdx[i]);
                        ps.setInt(3, j + 1);
                        ps.setLong(4, pyQIds[i]);
                        ps.executeUpdate();
                        var rs = ps.getGeneratedKeys();
                        rs.next();
                        if (j == 0) {
                            pyOptA[i] = rs.getLong(1);
                        }
                    }
                }
            }
        }

        try (QuizAttemptDAO aDao = new QuizAttemptDAO()) {
            QuizAttempt pyAtt = aDao.startAttempt(user(4), quiz(pyQuizId));
            try (PreparedStatement ps = aDao.getConnection().prepareStatement("UPDATE quizzes SET status = 'CLOSED' WHERE id = ?")) {
                ps.setLong(1, pyQuizId);
                ps.executeUpdate();
            }

            List<StudentAnswer> selections = new ArrayList<>();
            StudentAnswer a1 = new StudentAnswer();
            Question q1 = new Question(); q1.setId(pyQIds[0]); a1.setQuestion(q1);
            QuestionOption o1 = new QuestionOption(); o1.setId(pyOptA[0]); a1.setSelectedOption(o1);
            selections.add(a1);
            StudentAnswer a2 = new StudentAnswer();
            Question q2 = new Question(); q2.setId(pyQIds[1]); a2.setQuestion(q2);
            QuestionOption o2 = new QuestionOption(); o2.setId(pyOptA[1]); a2.setSelectedOption(o2);
            selections.add(a2);

            QuizAttempt submitted = aDao.submitAttempt(user(4), pyAtt, selections);
            assertTrue("10. Quiz đóng sau khi bắt đầu vẫn nộp được + chấm đúng 1/3 -> 3.33/10",
                    submitted.getStatus() == AttemptStatus.SUBMITTED
                            && Integer.valueOf(1).equals(submitted.getCorrectCount())
                            && Integer.valueOf(3).equals(submitted.getTotalQuestions())
                            && new BigDecimal("3.33").compareTo(submitted.getScore()) == 0
                            && submitted.getAnswers().size() == 3
                            && submitted.getAnswers().get(2).getSelectedOption() == null);

            QuizAttempt repeat = aDao.submitAttempt(user(4), pyAtt, List.of());
            assertTrue("11. Nộp lặp trả kết quả đã lưu (vẫn 3.33/10, không bị ghi đè thành 0)",
                    new BigDecimal("3.33").compareTo(repeat.getScore()) == 0
                            && Integer.valueOf(1).equals(repeat.getCorrectCount()));

            assertThrows("12. Lượt đã SUBMITTED không được hủy", IllegalStateException.class,
                    () -> aDao.cancelAttempt(user(4), pyAtt));

            assertThrows("12b. Người học khác không xem được chi tiết kết quả", SecurityException.class,
                    () -> aDao.getResult(user(5), pyAtt));

            QuizAttempt res = aDao.getResult(user(4), pyAtt);
            assertTrue("12c. getResult trả đầy đủ chi tiết 3 câu kèm đáp án đúng",
                    res.getAnswers().size() == 3
                            && res.getAnswers().get(1).getSelectedOption().getId().equals(pyOptA[1])
                            && !res.getAnswers().get(1).getIsCorrect());

            try (PreparedStatement ps = aDao.getConnection().prepareStatement("UPDATE quizzes SET status = 'PUBLISHED' WHERE id = ?")) {
                ps.setLong(1, pyQuizId);
                ps.executeUpdate();
            }
            QuizAttempt dupAttempt = aDao.startAttempt(user(4), quiz(pyQuizId));
            StudentAnswer dup1 = new StudentAnswer(); dup1.setQuestion(q1); dup1.setSelectedOption(o1); dup1.setIsCorrect(false);
            StudentAnswer dup2 = new StudentAnswer(); dup2.setQuestion(q1); dup2.setSelectedOption(o1);
            assertThrows("12d. Một câu gửi nhiều lựa chọn bị từ chối", IllegalArgumentException.class,
                    () -> aDao.submitAttempt(user(4), dupAttempt, List.of(dup1, dup2)));

            StudentAnswer wrongOpt = new StudentAnswer(); wrongOpt.setQuestion(q1); wrongOpt.setSelectedOption(o2);
            assertThrows("12e. Phương án không thuộc câu hỏi bị từ chối", IllegalArgumentException.class,
                    () -> aDao.submitAttempt(user(4), dupAttempt, List.of(wrongOpt)));

            StudentAnswer spoofed = new StudentAnswer(); spoofed.setQuestion(q2); spoofed.setSelectedOption(o2); spoofed.setIsCorrect(true);
            QuizAttempt secondSub = aDao.submitAttempt(user(4), dupAttempt, List.of(dup1, spoofed));
            List<QuizAttempt> hist = aDao.getHistory(user(4), quiz(pyQuizId));
            BigDecimal best = aDao.getBestScore(user(4), quiz(pyQuizId));
            assertTrue("12f. Làm lượt mới không ghi đè lượt cũ & không tin isCorrect giả từ UI",
                    hist.size() == 2 && new BigDecimal("3.33").compareTo(secondSub.getScore()) == 0
                            && new BigDecimal("3.33").compareTo(best) == 0);
            try (PreparedStatement ps = aDao.getConnection().prepareStatement("UPDATE quizzes SET status = 'CLOSED' WHERE id = ?")) {
                ps.setLong(1, pyQuizId);
                ps.executeUpdate();
            }
        }

        try (QuizAttemptDAO aDao = new QuizAttemptDAO()) {
            BigDecimal noneScore = aDao.getBestScore(user(4), quiz(5));
            List<Quiz> histQuizzes = aDao.getHistoryQuizzes(user(4));
            boolean hasClosedQuiz3 = histQuizzes.stream().anyMatch(q -> Long.valueOf(3L).equals(q.getId()));
            assertTrue("13. Chưa nộp bài -> bestScore == null (không phải 0) & lịch sử giữ quiz CLOSED",
                    noneScore == null && hasClosedQuiz3);
        }

        try (QuizAttemptDAO aDao = new QuizAttemptDAO()) {
            aDao.cancelUnfinishedAttempts(user(5));
            QuizAttempt att6 = new QuizAttempt(); att6.setId(6L);
            assertThrows("14. cancelUnfinishedAttempts chuyển IN_PROGRESS còn sót -> CANCELLED",
                    IllegalStateException.class,
                    () -> aDao.getAttemptContent(user(5), att6));
            assertThrows("14b. Lượt đã CANCELLED không xem được kết quả", IllegalStateException.class,
                    () -> aDao.getResult(user(5), att6));
        }

        try (QuizDAO qDao = new QuizDAO()) {
            assertThrows("15. Tài khoản bị khóa bị từ chối khi xem kết quả", SecurityException.class,
                    () -> {
                        try (QuizAttemptDAO aDao = new QuizAttemptDAO()) {
                            aDao.getResult(user(7), new QuizAttempt() {{ setId(1L); }});
                        }
                    });
        }

        try (QuizDAO qDao = new QuizDAO()) {
            Connection c = qDao.getConnection();
            try (PreparedStatement ps = c.prepareStatement("UPDATE courses SET status = 'DRAFT' WHERE id = 1")) {
                ps.executeUpdate();
            }
            try {
                assertThrows("16. Khóa học DRAFT không cho xem danh sách quiz", IllegalStateException.class,
                        () -> qDao.getAvailableQuizzesOfLesson(user(4), lesson(1)));
                try (QuizAttemptDAO aDao = new QuizAttemptDAO()) {
                    assertThrows("16b. Khóa học DRAFT không cho bắt đầu lượt làm mới", IllegalStateException.class,
                            () -> aDao.startAttempt(user(4), quiz(1)));
                }
            } finally {
                try (PreparedStatement ps = c.prepareStatement("UPDATE courses SET status = 'OPEN' WHERE id = 1")) {
                    ps.executeUpdate();
                }
            }
        }

        System.out.println("\n=== KET QUA TEST M3: PASS=" + passed + ", FAIL=" + failed + " ===");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
