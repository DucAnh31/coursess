package model;

import java.util.ArrayList;
import java.util.List;

public class Question {
    private Long id;
    private String content;
    private Integer orderIndex;
    private Quiz quiz;
    private List<QuestionOption> options = new ArrayList<>();

    public Question() {
    }

    public Question(String content, Integer orderIndex, List<QuestionOption> options) {
        this.content = content;
        this.orderIndex = orderIndex;
        if (options != null) {
            this.options = options;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public List<QuestionOption> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOption> options) {
        this.options = options;
    }
}
