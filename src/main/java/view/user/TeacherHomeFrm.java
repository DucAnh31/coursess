package view.user;

import model.User;
import view.course.TeacherCourseListFrm;

/**
 * Trang chính giảng viên - chuyển tiếp trực tiếp vào giao diện Quản lý khóa học.
 */
public class TeacherHomeFrm extends TeacherCourseListFrm {
    public TeacherHomeFrm() {
        super();
    }

    public TeacherHomeFrm(User teacher) {
        super(teacher);
    }
}
