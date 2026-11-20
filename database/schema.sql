CREATE DATABASE IF NOT EXISTS btl_cnpm;
USE btl_cnpm;

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    username VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'TEACHER', 'STUDENT') NOT NULL DEFAULT 'STUDENT',
    is_active TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    thumbnail_url VARCHAR(2048),
    status ENUM('DRAFT', 'OPEN', 'CLOSED') NOT NULL DEFAULT 'DRAFT',
    teacher_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    FOREIGN KEY (teacher_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE lessons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    video_url VARCHAR(2048) NOT NULL,
    order_index INT NOT NULL,
    is_visible TINYINT(1) NOT NULL DEFAULT 1,
    course_id BIGINT NOT NULL,
    FOREIGN KEY (course_id) REFERENCES courses(id),
    UNIQUE (course_id, order_index)
);

CREATE TABLE lesson_documents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    file_size BIGINT NOT NULL,
    is_available TINYINT(1) NOT NULL DEFAULT 1,
    lesson_id BIGINT NOT NULL,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id)
);

CREATE TABLE course_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    enrolled_at DATETIME(6) NOT NULL,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (course_id) REFERENCES courses(id),
    UNIQUE (student_id, course_id)
);

CREATE TABLE quizzes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    status ENUM('DRAFT', 'PUBLISHED', 'CLOSED') NOT NULL DEFAULT 'DRAFT',
    published_at DATETIME(6),
    lesson_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    source_quiz_id BIGINT,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (source_quiz_id) REFERENCES quizzes(id)
);

CREATE TABLE questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT NOT NULL,
    order_index INT NOT NULL,
    quiz_id BIGINT NOT NULL,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id),
    UNIQUE (quiz_id, order_index)
);

CREATE TABLE question_options (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT NOT NULL,
    is_correct TINYINT(1) NOT NULL DEFAULT 0,
    order_index INT NOT NULL,
    question_id BIGINT NOT NULL,
    FOREIGN KEY (question_id) REFERENCES questions(id),
    UNIQUE (question_id, order_index)
);

CREATE TABLE quiz_attempts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    status ENUM('IN_PROGRESS', 'SUBMITTED', 'CANCELLED') NOT NULL DEFAULT 'IN_PROGRESS',
    total_questions INT NOT NULL,
    correct_count INT,
    score DECIMAL(5, 2),
    started_at DATETIME(6) NOT NULL,
    submitted_at DATETIME(6),
    student_id BIGINT NOT NULL,
    quiz_id BIGINT NOT NULL,
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
);

CREATE TABLE student_answers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    is_correct TINYINT(1) NOT NULL DEFAULT 0,
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option_id BIGINT,
    FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id),
    FOREIGN KEY (question_id) REFERENCES questions(id),
    FOREIGN KEY (selected_option_id) REFERENCES question_options(id),
    UNIQUE (attempt_id, question_id)
);
