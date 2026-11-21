package view;

import model.User;
import view.course.TeacherCourseListFrm;

/**
 * Form khởi động chính của phân hệ Quản lý khóa học (Mô-đun M2).
 * Mở trực tiếp màn hình Quản lý khóa học.
 */
public class MainFrm extends TeacherCourseListFrm {
    public MainFrm() {
        super();
    }

    public MainFrm(User teacher) {
        super(teacher);
    }
}
