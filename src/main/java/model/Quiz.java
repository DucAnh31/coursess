package model;

import model.enums.QuizStatus;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

public class Quiz {
    private Long id;
    private String title;
    private QuizStatus status = QuizStatus.DRAFT;
    private Instant publishedAt;
    private Lesson lesson;
    private User createdBy;
    private Quiz sourceQuiz;
    private List<Question> questions = new ArrayList<>();

    public Quiz() {
    }

    public Quiz(String title, QuizStatus status, List<Question> questions) {
        this.title = title;
        this.status = status;
        if (questions != null) {
            this.questions = questions;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public QuizStatus getStatus() {
        return status;
    }

    public void setStatus(QuizStatus status) {
        this.status = status;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Quiz getSourceQuiz() {
        return sourceQuiz;
    }

    public void setSourceQuiz(Quiz sourceQuiz) {
        this.sourceQuiz = sourceQuiz;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
