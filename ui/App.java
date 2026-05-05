package ui;

import com.formdev.flatlaf.FlatDarkLaf;
import db.DatabaseManager;

import javax.swing.*;
import java.awt.*;

public class App {
    public static void main(String[] args) {
        // Setup Professional Modern Theme
        FlatDarkLaf.setup();
        
        // Global UI adjustments
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("ProgressBar.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("TabbedPane.tabSeparatorsFullHeight", true);
        
        // Font adjustments
        Font defaultFont = new Font("Segoe UI", Font.PLAIN, 14);
        UIManager.put("defaultFont", defaultFont);

        // Initialize Database
        DatabaseManager.initializeDatabase();

        // Launch Login
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
