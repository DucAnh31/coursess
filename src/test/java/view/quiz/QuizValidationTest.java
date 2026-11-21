package view.quiz;

import model.Question;
import model.QuestionOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuizValidationTest {

    private CreateQuizFrm frm;

    @BeforeEach
    public void setUp() {
        frm = new CreateQuizFrm(null, 1L, 2L, null);
    }

    @Test
    public void testValidateDraftEmptyTitleFails() {
        frm.setTitleText("");
        boolean result = frm.validateDraft();

        assertFalse(result);
        assertEquals("Lỗi: Tiêu đề quiz không được để trống khi lưu nháp.", frm.getNoticeText());
    }

    @Test
    public void testValidateDraftWhitespaceTitleFails() {
        frm.setTitleText("     ");
        boolean result = frm.validateDraft();

        assertFalse(result);
        assertEquals("Lỗi: Tiêu đề quiz không được để trống khi lưu nháp.", frm.getNoticeText());
    }

    @Test
    public void testValidateDraftValidTitlePasses() {
        frm.setTitleText("Đề trắc nghiệm kiểm thử chương 1");
        boolean result = frm.validateDraft();

        assertTrue(result);
        assertEquals("", frm.getNoticeText());
    }

    @Test
    public void testValidatePublishEmptyTitleFails() {
        frm.setTitleText("");
        frm.setQuestions(createSampleQuestions());
        boolean result = frm.validatePublish();

        assertFalse(result);
        assertEquals("Lỗi: Tiêu đề quiz không được để trống trước khi xuất bản.", frm.getNoticeText());
    }

    @Test
    public void testValidatePublishWhitespaceTitleFails() {
        frm.setTitleText("   ");
        frm.setQuestions(createSampleQuestions());
        boolean result = frm.validatePublish();

        assertFalse(result);
        assertEquals("Lỗi: Tiêu đề quiz không được để trống trước khi xuất bản.", frm.getNoticeText());
    }

    @Test
    public void testValidatePublishEmptyQuestionsFails() {
        frm.setTitleText("Đề trắc nghiệm kiểm thử");
        frm.setQuestions(Collections.emptyList());
        boolean result = frm.validatePublish();

        assertFalse(result);
        assertEquals("Lỗi: Quiz phải có ít nhất 1 câu hỏi hợp lệ mới được xuất bản.", frm.getNoticeText());
    }

    @Test
    public void testValidatePublishNullQuestionsFails() {
        frm.setTitleText("Đề trắc nghiệm kiểm thử");
        frm.setQuestions(null);
        boolean result = frm.validatePublish();

        assertFalse(result);
        assertEquals("Lỗi: Quiz phải có ít nhất 1 câu hỏi hợp lệ mới được xuất bản.", frm.getNoticeText());
    }

    @Test
    public void testValidatePublishValidTitleAndQuestionsPasses() {
        frm.setTitleText("Đề trắc nghiệm kiểm thử phần mềm");
        frm.setQuestions(createSampleQuestions());
        boolean result = frm.validatePublish();

        assertTrue(result);
        assertEquals("", frm.getNoticeText());
    }

    private List<Question> createSampleQuestions() {
        List<Question> list = new ArrayList<>();
        List<QuestionOption> options = new ArrayList<>();
        options.add(new QuestionOption("A", true, 1));
        options.add(new QuestionOption("B", false, 2));
        options.add(new QuestionOption("C", false, 3));
        options.add(new QuestionOption("D", false, 4));

        Question q = new Question("Câu hỏi mẫu 1", 1, options);
        list.add(q);
        return list;
    }
}

