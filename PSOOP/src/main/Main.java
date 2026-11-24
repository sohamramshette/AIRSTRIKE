// ==============================================================================
// File      : PSOOP/src/main/Main.java
// Subject   : PSOOP-CO4 | Application Bootstrap & Swing EDT
// Topics    : [TOPIC: Entry Point] [TOPIC: Swing EDT] [TOPIC: Defensive Programming]
// Game Role : Parses launch flags, sets look-and-feel safely, and starts MainWindow on the Swing EDT.
// ==============================================================================
package main;

import ui.MainWindow;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main application entry point for the AIRSTRIKE 2D Space Shooter.
 */
public class Main {

    /**
     * [TOPIC: Entry Point] [TOPIC: Swing EDT]
     * Game Role: Bootstraps the desktop window and optionally launches the enemy showcase mode.
     */
    public static void main(String[] args) {
        boolean demoFlag = false;
        // WHY: Null-safe CLI argument scan supports headless/no-arg invocation without throwing.
        if (args != null) {
            for (String arg : args) {
                if ("--demo".equalsIgnoreCase(arg) || "--enemies".equalsIgnoreCase(arg) || "-e".equalsIgnoreCase(arg)) {
                    demoFlag = true;
                    break;
                }
            }
        }
        final boolean launchDemo = demoFlag;

        // WHY: Gracefully degrade to Swing's default cross-platform L&F if native theme setup fails.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // WHY: Swing components are not thread-safe and must be created and shown on the EDT.
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
            if (launchDemo) {
                window.startEnemyDemo();
            }
        });
    }
}

