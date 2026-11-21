package view.quiz;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import model.Question;
import model.QuestionOption;

public class QuestionDialog extends JDialog implements ActionListener {
    private final JFrame parentFrame;
    private Question question;
    private boolean isConfirmed = false;
    private Integer orderIndex;

    private JTextArea txtContent;
    private JTextField txtOptionA;
    private JTextField txtOptionB;
    private JTextField txtOptionC;
    private JTextField txtOptionD;
    private JRadioButton radCorrectA;
    private JRadioButton radCorrectB;
    private JRadioButton radCorrectC;
    private JRadioButton radCorrectD;
    private ButtonGroup grpCorrect;
    private JLabel lblNotice;
    private JButton btnSaveQuestion;
    private JButton btnCancelQuestion;

    public QuestionDialog(Frame parent, Question questionToEdit, Integer orderIndex) {
        super(parent, questionToEdit == null ? "Soạn câu hỏi" : "Sửa câu hỏi", true);
        this.parentFrame = parent instanceof JFrame ? (JFrame) parent : null;
        this.question = questionToEdit;
        this.orderIndex = orderIndex != null ? orderIndex : (questionToEdit != null ? questionToEdit.getOrderIndex() : 1);

        initComponents();
        if (questionToEdit != null) {
            populateData(questionToEdit);
        }
    }

    public QuestionDialog(JFrame parentFrame, Question question) {
        this(parentFrame, question, question != null ? question.getOrderIndex() : 1);
    }

    private void initComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(650, 480);
        setLocationRelativeTo(parentFrame);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlCenter = new JPanel(new BorderLayout(5, 10));
        pnlCenter.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JPanel pnlContent = new JPanel(new BorderLayout(5, 5));
        pnlContent.add(new JLabel("Nội dung câu hỏi:"), BorderLayout.NORTH);
        txtContent = new JTextArea(4, 30);
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        pnlContent.add(new JScrollPane(txtContent), BorderLayout.CENTER);
        pnlCenter.add(pnlContent, BorderLayout.NORTH);

        JPanel pnlOptions = new JPanel(new GridBagLayout());
        pnlOptions.setBorder(BorderFactory.createTitledBorder("4 phương án lựa chọn (Chọn 1 đáp án đúng)"));

        txtOptionA = new JTextField();
        txtOptionB = new JTextField();
        txtOptionC = new JTextField();
        txtOptionD = new JTextField();

        radCorrectA = new JRadioButton("Đáp án đúng");
        radCorrectB = new JRadioButton("Đáp án đúng");
        radCorrectC = new JRadioButton("Đáp án đúng");
        radCorrectD = new JRadioButton("Đáp án đúng");

        grpCorrect = new ButtonGroup();
        grpCorrect.add(radCorrectA);
        grpCorrect.add(radCorrectB);
        grpCorrect.add(radCorrectC);
        grpCorrect.add(radCorrectD);

        addOptionRow(pnlOptions, 0, "A", txtOptionA, radCorrectA);
        addOptionRow(pnlOptions, 1, "B", txtOptionB, radCorrectB);
        addOptionRow(pnlOptions, 2, "C", txtOptionC, radCorrectC);
        addOptionRow(pnlOptions, 3, "D", txtOptionD, radCorrectD);

        pnlCenter.add(pnlOptions, BorderLayout.CENTER);

        lblNotice = new JLabel("Lưu ý: Bắt buộc đủ 4 phương án và chọn 1 đáp án đúng.");
        pnlCenter.add(lblNotice, BorderLayout.SOUTH);
        add(pnlCenter, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnSaveQuestion = new JButton("Lưu câu hỏi");
        btnCancelQuestion = new JButton("Hủy bỏ");

        btnSaveQuestion.addActionListener(this);
        btnCancelQuestion.addActionListener(this);

        pnlFooter.add(btnSaveQuestion);
        pnlFooter.add(btnCancelQuestion);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private void addOptionRow(JPanel pnl, int row, String label, JTextField tf, JRadioButton rad) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        pnl.add(new JLabel("Phương án " + label + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        pnl.add(tf, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        pnl.add(rad, gbc);
    }

    private void populateData(Question q) {
        txtContent.setText(q.getContent() != null ? q.getContent() : "");
        List<QuestionOption> options = q.getOptions();
        if (options != null && options.size() >= 4) {
            txtOptionA.setText(options.get(0).getContent());
            radCorrectA.setSelected(Boolean.TRUE.equals(options.get(0).getIsCorrect()));

            txtOptionB.setText(options.get(1).getContent());
            radCorrectB.setSelected(Boolean.TRUE.equals(options.get(1).getIsCorrect()));

            txtOptionC.setText(options.get(2).getContent());
            radCorrectC.setSelected(Boolean.TRUE.equals(options.get(2).getIsCorrect()));

            txtOptionD.setText(options.get(3).getContent());
            radCorrectD.setSelected(Boolean.TRUE.equals(options.get(3).getIsCorrect()));
        }
    }

    public boolean validateInput() {
        String content = txtContent.getText().trim();
        String optA = txtOptionA.getText().trim();
        String optB = txtOptionB.getText().trim();
        String optC = txtOptionC.getText().trim();
        String optD = txtOptionD.getText().trim();

        if (content.isEmpty()) {
            lblNotice.setText("Lỗi: Nội dung câu hỏi không được để trống.");
            txtContent.requestFocus();
            return false;
        }

        if (optA.isEmpty() || optB.isEmpty() || optC.isEmpty() || optD.isEmpty()) {
            lblNotice.setText("Lỗi: Phải nhập đầy đủ cả 4 phương án A, B, C, D.");
            return false;
        }

        if (optA.equalsIgnoreCase(optB) || optA.equalsIgnoreCase(optC) || optA.equalsIgnoreCase(optD)
                || optB.equalsIgnoreCase(optC) || optB.equalsIgnoreCase(optD)
                || optC.equalsIgnoreCase(optD)) {
            lblNotice.setText("Lỗi: Các phương án không được trùng lặp nội dung.");
            return false;
        }

        if (!radCorrectA.isSelected() && !radCorrectB.isSelected()
                && !radCorrectC.isSelected() && !radCorrectD.isSelected()) {
            lblNotice.setText("Lỗi: Bắt buộc chọn 1 đáp án đúng.");
            return false;
        }

        lblNotice.setText("");
        return true;
    }

    public Question getQuestion() {
        return this.question;
    }

    public boolean isConfirmed() {
        return this.isConfirmed;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnSaveQuestion) {
            if (!validateInput()) {
                return;
            }

            if (this.question == null) {
                this.question = new Question();
            }
            this.question.setContent(txtContent.getText().trim());
            this.question.setOrderIndex(this.orderIndex != null ? this.orderIndex : 1);

            List<QuestionOption> options = new ArrayList<>();
            options.add(new QuestionOption(txtOptionA.getText().trim(), radCorrectA.isSelected(), 1));
            options.add(new QuestionOption(txtOptionB.getText().trim(), radCorrectB.isSelected(), 2));
            options.add(new QuestionOption(txtOptionC.getText().trim(), radCorrectC.isSelected(), 3));
            options.add(new QuestionOption(txtOptionD.getText().trim(), radCorrectD.isSelected(), 4));

            for (QuestionOption opt : options) {
                opt.setQuestion(this.question);
            }
            this.question.setOptions(options);

            this.isConfirmed = true;
            dispose();
        } else if (e.getSource() == btnCancelQuestion) {
            this.isConfirmed = false;
            dispose();
        }
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new QuestionDialog((JFrame) null, null, 1).setVisible(true);
        });
    }
}
