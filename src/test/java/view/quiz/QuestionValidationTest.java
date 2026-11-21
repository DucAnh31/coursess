package view.quiz;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Frame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestionValidationTest {

    private QuestionDialog dialog;

    @BeforeEach
    public void setUp() {
        dialog = new QuestionDialog((Frame) null, null, 1);
    }

    @Test
    public void testEmptyContentFails() {
        dialog.setFormData("", "A", "B", "C", "D", 1);
        boolean result = dialog.validateInput();

        assertFalse(result);
        assertEquals("Lỗi: Nội dung câu hỏi không được để trống.", dialog.getNoticeText());
    }

    @Test
    public void testWhitespaceOnlyContentFails() {
        dialog.setFormData("    ", "A", "B", "C", "D", 1);
        boolean result = dialog.validateInput();

        assertFalse(result);
        assertEquals("Lỗi: Nội dung câu hỏi không được để trống.", dialog.getNoticeText());
    }

    @Test
    public void testEmptyOptionAFails() {
        dialog.setFormData("Nội dung câu hỏi", "", "B", "C", "D", 1);
        boolean result = dialog.validateInput();

        assertFalse(result);
        assertEquals("Lỗi: Phải nhập đầy đủ cả 4 phương án A, B, C, D.", dialog.getNoticeText());
    }

    @Test
    public void testEmptyOptionBFails() {
        dialog.setFormData("Nội dung câu hỏi", "A", "", "C", "D", 1);
        boolean result = dialog.validateInput();

        assertFalse(result);
        assertEquals("Lỗi: Phải nhập đầy đủ cả 4 phương án A, B, C, D.", dialog.getNoticeText());
    }

    @Test
    public void testEmptyOptionCFails() {
        dialog.setFormData("Nội dung câu hỏi", "A", "B", "   ", "D", 1);
        boolean result = dialog.validateInput();

        assertFalse(result);
        assertEquals("Lỗi: Phải nhập đầy đủ cả 4 phương án A, B, C, D.", dialog.getNoticeText());
    }

    @Test
    public void testEmptyOptionDFails() {
        dialog.setFormData("Nội dung câu hỏi", "A", "B", "C", "", 1);
        boolean result = dialog.validateInput();

        assertFalse(result);
        assertEquals("Lỗi: Phải nhập đầy đủ cả 4 phương án A, B, C, D.", dialog.getNoticeText());
    }

    @Test
    public void testDuplicateOptionAAndBFails() {
        dialog.setFormData("Nội dung câu hỏi", "Phương án 1", "Phương án 1", "Phương án 3", "Phương án 4", 1);
        boolean result = dialog.validateInput();

        assertFalse(result);
        assertEquals("Lỗi: Các phương án không được trùng lặp nội dung.", dialog.getNoticeText());
    }

    @Test
    public void testDuplicateOptionCaseInsensitiveFails() {
        dialog.setFormData("Nội dung câu hỏi", "Hà Nội", "TP HCM", "hà nội", "Đà Nẵng", 2);
        boolean result = dialog.validateInput();

        assertFalse(result);
        assertEquals("Lỗi: Các phương án không được trùng lặp nội dung.", dialog.getNoticeText());
    }

    @Test
    public void testDuplicateOptionCAndDFails() {
        dialog.setFormData("Nội dung câu hỏi", "A", "B", "C", "C", 1);
        boolean result = dialog.validateInput();

        assertFalse(result);
        assertEquals("Lỗi: Các phương án không được trùng lặp nội dung.", dialog.getNoticeText());
    }

    @Test
    public void testNoCorrectOptionSelectedFails() {
        dialog.setFormData("Nội dung câu hỏi", "A", "B", "C", "D", null);
        boolean result = dialog.validateInput();

        assertFalse(result);
        assertEquals("Lỗi: Bắt buộc chọn 1 đáp án đúng.", dialog.getNoticeText());
    }

    @Test
    public void testValidInputWithOptionASelectedPasses() {
        dialog.setFormData("Thủ đô của Việt Nam là gì?", "Hà Nội", "TP HCM", "Đà Nẵng", "Hải Phòng", 1);
        boolean result = dialog.validateInput();

        assertTrue(result);
        assertEquals("", dialog.getNoticeText());
    }

    @Test
    public void testValidInputWithOptionDSelectedPasses() {
        dialog.setFormData("Một năm có bao nhiêu quý?", "1", "2", "3", "4", 4);
        boolean result = dialog.validateInput();

        assertTrue(result);
        assertEquals("", dialog.getNoticeText());
    }
}

