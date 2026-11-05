package main;

import ui.MainWindow;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main application entry point for the AIRSTRIKE 2D Space Shooter.
 */
public class Main {

    public static void main(String[] args) {
        // Set cross-platform look and feel or system look and feel defensively
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Launch main window on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
