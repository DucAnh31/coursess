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

/**
 * Kiểm thử tự động toàn bộ 14 quy tắc nghiệp vụ cốt lõi của Mô-đun M3
 * trên CSDL thật (MySQL btl_cnpm đã chạy schema.sql + seed.sql).
 * Lưu ý: chạy trên DB test riêng hoặc nạp lại seed.sql sau khi test.
 */
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
        // 1. Tài khoản bị khóa (student_blocked id=7) bị từ chối ở mọi thao tác
        try (QuizDAO qDao = new QuizDAO()) {
            assertThrows("1. Tài khoản bị khóa không xem được danh sách quiz", SecurityException.class,
                    () -> qDao.getAvailableQuizzesOfLesson(user(7), lesson(1)));
        }

        // 2. Giảng viên chủ khóa (teacher01 id=2, chủ course 1) không được làm quiz trong khóa mình dạy
        try (QuizDAO qDao = new QuizDAO()) {
            assertThrows("2. Giảng viên không được làm quiz trong khóa mình dạy", SecurityException.class,
                    () -> qDao.getAvailableQuizzesOfLesson(user(2), lesson(1)));
        }

        // 3. Học sinh chưa ghi danh (student03 id=6 chưa ghi danh course 1) bị từ chối
        try (QuizDAO qDao = new QuizDAO()) {
            assertThrows("3. Người chưa ghi danh bị từ chối", SecurityException.class,
                    () -> qDao.getAvailableQuizzesOfLesson(user(6), lesson(1)));
        }

        // 4. Học sinh đã ghi danh (student01 id=4, course 1, lesson 1) chỉ thấy quiz PUBLISHED (quiz 1, không thấy draft quiz 4)
        try (QuizDAO qDao = new QuizDAO()) {
            List<Quiz> list = qDao.getAvailableQuizzesOfLesson(user(4), lesson(1));
            assertTrue("4. Chỉ trả quiz PUBLISHED của bài học 1 (đúng 1 quiz id=1)",
                    list.size() == 1 && list.get(0).getId().equals(1L) && list.get(0).getQuestions().size() == 2);
        }

        // 5. Course CLOSED (course 3, lesson 6) không thu hồi quyền học; nhưng Quiz CLOSED (quiz 3) không nhận lượt mới
        try (QuizAttemptDAO aDao = new QuizAttemptDAO()) {
            assertThrows("5. Quiz CLOSED không cho bắt đầu lượt mới", IllegalStateException.class,
                    () -> aDao.startAttempt(user(4), quiz(3)));
        }

        // 6. Bắt đầu lượt mới + đảm bảo tối đa 1 IN_PROGRESS / người / quiz (gọi lặp trả cùng attemptId)
        QuizAttempt att1;
        try (QuizAttemptDAO aDao = new QuizAttemptDAO()) {
            att1 = aDao.startAttempt(user(4), quiz(1));
            QuizAttempt att1Again = aDao.startAttempt(user(4), quiz(1));
            assertTrue("6. Tối đa 1 lượt IN_PROGRESS/người/quiz (trả lại cùng mã lượt)",
                    att1.getId() != null && att1.getId().equals(att1Again.getId())
                            && att1.getStatus() == AttemptStatus.IN_PROGRESS
                            && att1.getScore() == null && att1.getSubmittedAt() == null);
        }

        // 7. Lấy nội dung làm bài: KHÔNG lộ isCorrect (Boolean isCorrect == null)
        QuizAttempt content;
        try (QuizAttemptDAO aDao = new QuizAttemptDAO()) {
            content = aDao.getAttemptContent(user(4), att1);
            boolean allNull = content.getQuiz().getQuestions().stream()
                    .flatMap(q -> q.getOptions().stream())
                    .allMatch(o -> o.getIsCorrect() == null);
            assertTrue("7. Tải đề làm bài không lộ đáp án đúng (mọi isCorrect == null)",
                    content.getQuiz().getQuestions().size() == 2 && allNull);
        }

        // 8. Chống xem/nộp lượt của người khác (student02 id=5 không truy cập được lượt của student01 id=4)
        try (QuizAttemptDAO aDao = new QuizAttemptDAO()) {
            assertThrows("8. Chặn truy cập lượt của người học khác", SecurityException.class,
                    () -> aDao.getAttemptContent(user(5), att1));
        }

        // 9. Hủy lượt khi rời bài -> CANCELLED, không điểm; lượt đã hủy không nộp được
        try (QuizAttemptDAO aDao = new QuizAttemptDAO()) {
            aDao.cancelAttempt(user(4), att1);
            assertThrows("9. Lượt đã CANCELLED bị từ chối nộp bài", IllegalStateException.class,
                    () -> aDao.submitAttempt(user(4), att1, List.of()));
        }

        // 10. Chấm điểm thang 10 làm tròn 2 chữ số (ví dụ chuẩn 3 câu Python: chọn A đúng, A sai, bỏ trống -> 1/3 -> 3.33)
        // Tạo tạm 1 quiz 3 câu trong lesson 1 rồi kiểm tra chấm điểm + nộp lặp trả cũ
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
            // Đóng quiz sang CLOSED sau khi đã bắt đầu hợp lệ -> vẫn PHẢI nộp được!
            try (PreparedStatement ps = aDao.getConnection().prepareStatement("UPDATE quizzes SET status = 'CLOSED' WHERE id = ?")) {
                ps.setLong(1, pyQuizId);
                ps.executeUpdate();
            }

            List<StudentAnswer> selections = new ArrayList<>();
            // Câu 1: chọn A (đúng)
            StudentAnswer a1 = new StudentAnswer();
            Question q1 = new Question(); q1.setId(pyQIds[0]); a1.setQuestion(q1);
            QuestionOption o1 = new QuestionOption(); o1.setId(pyOptA[0]); a1.setSelectedOption(o1);
            selections.add(a1);
            // Câu 2: chọn A (sai)
            StudentAnswer a2 = new StudentAnswer();
            Question q2 = new Question(); q2.setId(pyQIds[1]); a2.setQuestion(q2);
            QuestionOption o2 = new QuestionOption(); o2.setId(pyOptA[1]); a2.setSelectedOption(o2);
            selections.add(a2);
            // Câu 3: bỏ trống (không gửi hoặc selectedOption = null)

            QuizAttempt submitted = aDao.submitAttempt(user(4), pyAtt, selections);
            assertTrue("10. Quiz đóng sau khi bắt đầu vẫn nộp được + chấm đúng 1/3 -> 3.33/10",
                    submitted.getStatus() == AttemptStatus.SUBMITTED
                            && Integer.valueOf(1).equals(submitted.getCorrectCount())
                            && Integer.valueOf(3).equals(submitted.getTotalQuestions())
                            && new BigDecimal("3.33").compareTo(submitted.getScore()) == 0
                            && submitted.getAnswers().size() == 3
                            && submitted.getAnswers().get(2).getSelectedOption() == null);

            // 11. Nộp lặp lượt đã SUBMITTED trả lại kết quả cũ, không chấm lại/ghi đè
            QuizAttempt repeat = aDao.submitAttempt(user(4), pyAtt, List.of());
            assertTrue("11. Nộp lặp trả kết quả đã lưu (vẫn 3.33/10, không bị ghi đè thành 0)",
                    new BigDecimal("3.33").compareTo(repeat.getScore()) == 0
                            && Integer.valueOf(1).equals(repeat.getCorrectCount()));

            // 12. Lượt đã SUBMITTED không bị hủy thành CANCELLED
            assertThrows("12. Lượt đã SUBMITTED không được hủy", IllegalStateException.class,
                    () -> aDao.cancelAttempt(user(4), pyAtt));
        }

        // 13. Lịch sử & bestScore: quiz 5 chưa có lượt nộp của student01 (id=4) -> bestScore == null (không phải 0)
        //     và quiz 3 (CLOSED) vẫn hiện trong lịch sử của student01 (seed có attempt id=5)
        try (QuizAttemptDAO aDao = new QuizAttemptDAO()) {
            BigDecimal noneScore = aDao.getBestScore(user(4), quiz(5));
            List<Quiz> histQuizzes = aDao.getHistoryQuizzes(user(4));
            boolean hasClosedQuiz3 = histQuizzes.stream().anyMatch(q -> Long.valueOf(3L).equals(q.getId()));
            assertTrue("13. Chưa nộp bài -> bestScore == null (không phải 0) & lịch sử giữ quiz CLOSED",
                    noneScore == null && hasClosedQuiz3);
        }

        // 14. Đăng nhập lại dọn lượt IN_PROGRESS còn sót sau crash (seed có attempt id=6 của student02 id=5)
        try (QuizAttemptDAO aDao = new QuizAttemptDAO()) {
            aDao.cancelUnfinishedAttempts(user(5));
            QuizAttempt att6 = new QuizAttempt(); att6.setId(6L);
            assertThrows("14. cancelUnfinishedAttempts chuyển IN_PROGRESS còn sót -> CANCELLED",
                    IllegalStateException.class,
                    () -> aDao.getAttemptContent(user(5), att6));
        }

        System.out.println("\n=== KET QUA TEST M3: PASS=" + passed + ", FAIL=" + failed + " ===");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
