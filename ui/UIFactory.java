package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class UIFactory {

    // ألوان ثابتة
    static final Color BLUE   = new Color(41, 128, 185);
    static final Color RED    = new Color(231, 76, 60);
    static final Color GREEN  = new Color(46, 204, 113);
    static final Color ORANGE = new Color(243, 156, 18);

    static JButton button(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        return btn;
    }

    static JButton primary(String text) { return button(text, BLUE); }
    static JButton danger(String text)  { return button(text, RED); }
    static JButton accent(String text)  { return button(text, ORANGE); }

    static JTextField field(String placeholder, int cols) {
        JTextField f = new JTextField(cols);
        f.putClientProperty("JTextField.placeholderText", placeholder);
        return f;
    }

    static JTable table(String[] columns) {
        JTable t = new JTable(new DefaultTableModel(columns, 0));
        t.setRowHeight(30);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        return t;
    }

    static JPanel borderPanel(String title, int pad) {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));
        if (title != null) {
            JLabel lbl = new JLabel(title);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
            p.add(lbl, BorderLayout.NORTH);
        }
        return p;
    }

    static JPanel header(String welcomeText, Color bg, JButton... buttons) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(bg);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lbl = new JLabel(welcomeText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(Color.WHITE);
        header.add(lbl, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        for (JButton b : buttons) right.add(b);
        header.add(right, BorderLayout.EAST);
        return header;
    }
}
