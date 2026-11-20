package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Form khởi động tối thiểu; các màn hình nghiệp vụ sẽ được thêm ở từng nhánh. */
public class MainFrm extends JFrame implements ActionListener {
    private final JButton btnExit = new JButton("Thoát");

    public MainFrm() {
        super("COURSESS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JLabel title = new JLabel("COURSESS - Ứng dụng quản lý và học tập theo khóa học", SwingConstants.CENTER);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(btnExit);
        btnExit.addActionListener(this);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.add(title, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);
        setContentPane(content);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == btnExit) {
            dispose();
        }
    }
}
