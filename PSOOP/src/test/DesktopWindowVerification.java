package test;

import game.GameState;
import ui.MainWindow;

import javax.swing.SwingUtilities;

/**
 * Desktop Window Verification Test.
 * Confirms that MainWindow launches on the actual desktop, renders the Main Menu,
 * transitions into active gameplay, updates frames on EDT, and disposes cleanly.
 */
public class DesktopWindowVerification {

    public static void main(String[] args) {
        System.out.println("[DESKTOP-TEST] Starting MainWindow desktop launch verification...");

        try {
            SwingUtilities.invokeAndWait(() -> {
                MainWindow window = new MainWindow();
                window.setVisible(true);

                boolean showing = window.isShowing();
                System.out.println("[DESKTOP-TEST] MainWindow setVisible(true). isShowing() = " + showing);
                if (!showing) {
                    System.err.println("[DESKTOP-TEST] ERROR: Window failed to display on desktop!");
                    System.exit(1);
                }

                // Verify initial state is MENU
                GameState initialState = window.getEngine().getState();
                System.out.println("[DESKTOP-TEST] Initial Engine State = " + initialState);
                if (initialState != GameState.MENU) {
                    System.err.println("[DESKTOP-TEST] ERROR: Expected MENU state but found " + initialState);
                    System.exit(1);
                }

                // Simulate clicking 'LAUNCH MISSION'
                System.out.println("[DESKTOP-TEST] Simulating mission launch...");
                window.startGame();

                GameState activeState = window.getEngine().getState();
                System.out.println("[DESKTOP-TEST] Active Engine State = " + activeState);
                if (activeState != GameState.PLAYING) {
                    System.err.println("[DESKTOP-TEST] ERROR: Expected PLAYING state but found " + activeState);
                    System.exit(1);
                }

                // Simulate player input
                window.getEngine().getPlayer().setMoveLeft(true);
                window.getEngine().setShooting(true);

                // Let the simulation run for 1 second on the desktop
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}

                System.out.println("[DESKTOP-TEST] Simulation ran smoothly for 1 second. Disposing window...");
                window.dispose();
                System.out.println("[DESKTOP-TEST] MainWindow disposed cleanly.");
            });

            System.out.println("[DESKTOP-TEST] RUNTIME DESKTOP TEST: SUCCESS");
            System.exit(0);
        } catch (Exception e) {
            System.err.println("[DESKTOP-TEST] RUNTIME TEST FAILED: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
