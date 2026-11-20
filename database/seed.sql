USE btl_cnpm;

-- Chạy một lần sau schema.sql, trên CSDL mới.

-- Mật khẩu chung: Demo@123 (BCrypt).

START TRANSACTION;

INSERT INTO users (id, full_name, username, password_hash, role, is_active) VALUES
    (1, 'Quản trị viên', 'admin', '$2a$10$QHdRrScBfkfG8rWEChRmA.PNKvFyJ57CUHLBQS291ikYIfDTgnqC2', 'ADMIN', 1),
    (2, 'Nguyễn Minh Anh', 'teacher01', '$2a$10$QHdRrScBfkfG8rWEChRmA.PNKvFyJ57CUHLBQS291ikYIfDTgnqC2', 'TEACHER', 1),
    (3, 'Trần Quốc Bảo', 'teacher02', '$2a$10$QHdRrScBfkfG8rWEChRmA.PNKvFyJ57CUHLBQS291ikYIfDTgnqC2', 'TEACHER', 1),
    (4, 'Lê Thu Hà', 'student01', '$2a$10$QHdRrScBfkfG8rWEChRmA.PNKvFyJ57CUHLBQS291ikYIfDTgnqC2', 'STUDENT', 1),
    (5, 'Phạm Hoàng Nam', 'student02', '$2a$10$QHdRrScBfkfG8rWEChRmA.PNKvFyJ57CUHLBQS291ikYIfDTgnqC2', 'STUDENT', 1),
    (6, 'Đỗ Ngọc Linh', 'student03', '$2a$10$QHdRrScBfkfG8rWEChRmA.PNKvFyJ57CUHLBQS291ikYIfDTgnqC2', 'STUDENT', 1),
    (7, 'Vũ Đức Long', 'student_blocked', '$2a$10$QHdRrScBfkfG8rWEChRmA.PNKvFyJ57CUHLBQS291ikYIfDTgnqC2', 'STUDENT', 0);

INSERT INTO categories (id, name, description) VALUES
    (1, 'Lập trình', 'Các khóa học lập trình cơ bản.'),
    (2, 'Cơ sở dữ liệu', 'Thiết kế và truy vấn cơ sở dữ liệu.'),
    (3, 'Kỹ năng', 'Kỹ năng học tập và làm việc.');

INSERT INTO courses (id, title, description, thumbnail_url, status, teacher_id, category_id) VALUES
    (1, 'Java cơ bản', 'Làm quen với biến, kiểu dữ liệu, vòng lặp và mảng trong Java.', NULL, 'OPEN', 2, 1),
    (2, 'SQL cho người mới bắt đầu', 'Thực hành truy vấn dữ liệu bằng các câu lệnh SQL cơ bản.', NULL, 'OPEN', 3, 2),
    (3, 'Nhập môn Python', 'Các kiến thức Python cơ bản; khóa hiện ngừng nhận ghi danh.', NULL, 'CLOSED', 2, 1),
    (4, 'Kỹ năng thuyết trình', 'Khóa nháp đang được giáo viên chuẩn bị.', NULL, 'DRAFT', 3, 3),
    (5, 'Git và GitHub cơ bản', 'Quản lý phiên bản mã nguồn khi làm việc nhóm.', NULL, 'OPEN', 2, 1);

INSERT INTO lessons (id, title, video_url, order_index, is_visible, course_id) VALUES
    (1, 'Biến và kiểu dữ liệu', 'https://www.youtube.com/watch?v=eIrMbAQSU34', 1, 1, 1),
    (2, 'Vòng lặp và mảng', 'https://www.youtube.com/watch?v=eIrMbAQSU34&t=3600', 2, 1, 1),
    (3, 'Lập trình hướng đối tượng', 'https://www.youtube.com/watch?v=eIrMbAQSU34&t=7200', 3, 0, 1),
    (4, 'Câu lệnh SELECT', 'https://www.youtube.com/watch?v=HXV3zeQKqGY', 1, 1, 2),
    (5, 'Lọc và sắp xếp dữ liệu', 'https://www.youtube.com/watch?v=HXV3zeQKqGY&t=3600', 2, 1, 2),
    (6, 'Cú pháp Python cơ bản', 'https://www.youtube.com/watch?v=rfscVS0vtbw', 1, 1, 3),
    (7, 'Các lệnh Git cơ bản', 'https://www.youtube.com/watch?v=RGOj5yH7evk', 1, 1, 5);

-- Thông tin tài liệu giả lập, chưa có file thực tế nên để is_available = 0.

INSERT INTO lesson_documents (id, name, file_path, file_size, is_available, lesson_id) VALUES
    (1, 'Ghi chú Java cơ bản.pdf', 'storage/documents/java-notes.pdf', 102400, 0, 1),
    (2, 'Bài tập SQL.pdf', 'storage/documents/sql-exercises.pdf', 153600, 0, 4);

INSERT INTO course_members (id, enrolled_at, student_id, course_id) VALUES
    (1, '2026-11-15 08:00:00', 4, 1),
    (2, '2026-11-15 09:00:00', 5, 1),
    (3, '2026-11-15 10:00:00', 6, 2),
    (4, '2026-11-11 08:00:00', 4, 3),
    (5, '2026-11-18 08:00:00', 5, 5);

INSERT INTO quizzes (id, title, status, published_at, lesson_id, created_by, source_quiz_id) VALUES
    (1, 'Ôn tập Java cơ bản', 'PUBLISHED', '2026-11-12 08:00:00', 1, 2, NULL),
    (2, 'Ôn tập truy vấn SQL', 'PUBLISHED', '2026-11-12 08:00:00', 4, 3, NULL),
    (3, 'Ôn tập Python', 'CLOSED', '2026-11-10 08:00:00', 6, 2, NULL),
    (4, 'Ôn tập Java cơ bản - bản chỉnh sửa', 'DRAFT', NULL, 1, 2, 1),
    (5, 'Ôn tập Git', 'PUBLISHED', '2026-11-12 08:00:00', 7, 2, NULL),
    (6, 'Bài tập vòng lặp - bản nháp', 'DRAFT', NULL, 2, 2, NULL);

INSERT INTO questions (id, content, order_index, quiz_id) VALUES
    (1, 'Kiểu dữ liệu nào lưu số nguyên 32 bit trong Java?', 1, 1),
    (2, 'Vòng lặp nào luôn thực hiện thân ít nhất một lần?', 2, 1),
    (3, 'Câu lệnh nào dùng để lấy dữ liệu từ bảng?', 1, 2),
    (4, 'Mệnh đề nào dùng để lọc dòng theo điều kiện?', 2, 2),
    (5, 'Từ khóa nào dùng để định nghĩa hàm trong Python?', 1, 3),
    (6, 'Kiểu dữ liệu nào lưu số nguyên 32 bit có dấu trong Java?', 1, 4),
    (7, 'Vòng lặp nào kiểm tra điều kiện sau khi chạy thân?', 2, 4),
    (8, 'Lệnh nào ghi nhận thay đổi vào lịch sử Git cục bộ?', 1, 5);

INSERT INTO question_options (id, content, is_correct, order_index, question_id) VALUES
    (1, 'int', 1, 1, 1),
    (2, 'boolean', 0, 2, 1),
    (3, 'double', 0, 3, 1),
    (4, 'String', 0, 4, 1),
    (5, 'for', 0, 1, 2),
    (6, 'while', 0, 2, 2),
    (7, 'do-while', 1, 3, 2),
    (8, 'for-each', 0, 4, 2),
    (9, 'SELECT', 1, 1, 3),
    (10, 'INSERT', 0, 2, 3),
    (11, 'DELETE', 0, 3, 3),
    (12, 'UPDATE', 0, 4, 3),
    (13, 'ORDER BY', 0, 1, 4),
    (14, 'GROUP BY', 0, 2, 4),
    (15, 'WHERE', 1, 3, 4),
    (16, 'LIMIT', 0, 4, 4),
    (17, 'def', 1, 1, 5),
    (18, 'function', 0, 2, 5),
    (19, 'fun', 0, 3, 5),
    (20, 'method', 0, 4, 5),
    (21, 'int', 1, 1, 6),
    (22, 'long', 0, 2, 6),
    (23, 'boolean', 0, 3, 6),
    (24, 'String', 0, 4, 6),
    (25, 'for', 0, 1, 7),
    (26, 'while', 0, 2, 7),
    (27, 'do-while', 1, 3, 7),
    (28, 'for-each', 0, 4, 7),
    (29, 'git commit', 1, 1, 8),
    (30, 'git clone', 0, 2, 8),
    (31, 'git status', 0, 3, 8),
    (32, 'git pull', 0, 4, 8);

INSERT INTO quiz_attempts (id, status, total_questions, correct_count, score, started_at, submitted_at, student_id, quiz_id) VALUES
    (1, 'SUBMITTED', 2, 2, '10.00', '2026-11-16 09:00:00', '2026-11-16 09:05:00', 4, 1),
    (2, 'SUBMITTED', 2, 1, '5.00', '2026-11-17 09:00:00', '2026-11-17 09:04:00', 4, 1),
    (3, 'CANCELLED', 2, NULL, NULL, '2026-11-18 09:00:00', NULL, 5, 1),
    (4, 'SUBMITTED', 2, 1, '5.00', '2026-11-17 10:00:00', '2026-11-17 10:06:00', 6, 2),
    (5, 'SUBMITTED', 1, 0, '0.00', '2026-11-17 11:00:00', '2026-11-17 11:02:00', 4, 3),
    (6, 'IN_PROGRESS', 1, NULL, NULL, '2026-11-19 08:30:00', NULL, 5, 5);

INSERT INTO student_answers (id, is_correct, attempt_id, question_id, selected_option_id) VALUES
    (1, 1, 1, 1, 1),
    (2, 1, 1, 2, 7),
    (3, 1, 2, 1, 1),
    (4, 0, 2, 2, NULL),
    (5, 1, 4, 3, 9),
    (6, 0, 4, 4, 13),
    (7, 0, 5, 5, 18);

COMMIT;
