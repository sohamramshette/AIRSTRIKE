package test;

import game.GameCanvas;
import game.GameEngine;
import game.GameState;
import model.DroneEnemy;
import model.FighterEnemy;
import model.PowerUp;
import model.PowerUpType;
import ui.*;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/**
 * Phase 9 Screenshot Capture Generator for Java Swing UI.
 * Saves high-resolution visual evidence to docs/phase9_screenshots/
 */
public class CapturePhase9Screenshots {

    private static final String OUTPUT_DIR = "docs/phase9_screenshots";

    public static void main(String[] args) {
        System.out.println("[PHASE 9] Generating Java Swing Screenshot Evidence...");

        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            SwingUtilities.invokeAndWait(() -> {
                MainWindow window = new MainWindow();
                window.setSize(800, 650);
                window.setVisible(true);

                try {
                    // 1. Capture Main Menu
                    System.out.println("[SCREENSHOT 1/10] Main Menu...");
                    window.showMainMenu();
                    Thread.sleep(300);
                    captureComponent(window.getContentPane(), new File(OUTPUT_DIR, "java_main_menu.png"));

                    // Multi-resolution main menu captures
                    captureAtResolution(window, 1280, 720, new File(OUTPUT_DIR, "java_main_menu_1280x720.png"));
                    captureAtResolution(window, 1366, 768, new File(OUTPUT_DIR, "java_main_menu_1366x768.png"));
                    captureAtResolution(window, 1920, 1080, new File(OUTPUT_DIR, "java_main_menu_1920x1080.png"));

                    // Restore standard window size
                    window.setSize(800, 650);
                    window.validate();

                    // 2. Capture Active Gameplay HUD
                    System.out.println("[SCREENSHOT 2/10] Active Gameplay HUD...");
                    window.startGame();
                    GameEngine engine = window.getEngine();
                    GameCanvas canvas = window.getGameCanvas();

                    engine.getEnemies().add(new DroneEnemy(200, 150));
                    engine.getEnemies().add(new FighterEnemy(500, 120));
                    engine.getPowerUps().add(new PowerUp(380, 280, PowerUpType.SHIELD_BOOST));
                    engine.getPlayer().setX(376);
                    engine.getPlayer().setY(480);
                    engine.getPlayer().shoot(engine.getProjectiles());
                    engine.getScoreCalculator().addScore(1250);

                    for (int i = 0; i < 20; i++) {
                        engine.update(0.016);
                    }
                    Thread.sleep(200);
                    captureComponent(canvas, new File(OUTPUT_DIR, "java_hud.png"));

                    // 3. Capture Tactical Pause Screen
                    System.out.println("[SCREENSHOT 3/10] Tactical Pause Screen...");
                    engine.pauseGame();
                    Thread.sleep(200);
                    captureComponent(canvas, new File(OUTPUT_DIR, "java_pause.png"));

                    // 4. Capture Game Over Screen
                    System.out.println("[SCREENSHOT 4/10] Game Over Screen...");
                    engine.resumeGame();
                    engine.getPlayer().setLives(0);
                    engine.getPlayer().setHealth(0);
                    engine.getPlayer().setActive(false);
                    try {
                        engine.setState(GameState.GAME_OVER);
                    } catch (Exception ignored) {}
                    Thread.sleep(200);
                    captureComponent(canvas, new File(OUTPUT_DIR, "java_game_over.png"));

                    // Return to Main Menu for dialog captures
                    window.showMainMenu();

                    // 5. Capture Subject & Course Outcomes Dialog (19 COs)
                    System.out.println("[SCREENSHOT 5/10] Subject & Course Outcomes Modal...");
                    SubjectCODialog coDialog = new SubjectCODialog(window, window);
                    coDialog.pack();
                    coDialog.setSize(740, 560);
                    Thread.sleep(200);
                    captureComponent(coDialog.getContentPane(), new File(OUTPUT_DIR, "java_subject_co.png"));
                    coDialog.dispose();

                    // 6. Capture Pilot Profile & Settings Dialog
                    System.out.println("[SCREENSHOT 6/10] Pilot Profile & Settings Modal...");
                    ProfileSettingsDialog profileDialog = new ProfileSettingsDialog(window, window, window.getEngine(), window.getAuthService());
                    profileDialog.pack();
                    profileDialog.setSize(580, 540);
                    Thread.sleep(200);
                    captureComponent(profileDialog.getContentPane(), new File(OUTPUT_DIR, "java_profile_settings.png"));
                    profileDialog.dispose();

                    // 7. Capture Pilot Authentication Dialog
                    System.out.println("[SCREENSHOT 7/10] Pilot Authentication Modal...");
                    AuthDialog authDialog = new AuthDialog(window, window.getAuthService());
                    authDialog.pack();
                    authDialog.setSize(480, 500);
                    Thread.sleep(200);
                    captureComponent(authDialog.getContentPane(), new File(OUTPUT_DIR, "java_auth.png"));
                    authDialog.dispose();

                    // 8. Capture Global Leaderboard Dialog
                    System.out.println("[SCREENSHOT 8/10] Global Leaderboard Modal...");
                    LeaderboardDialog lbDialog = new LeaderboardDialog(window, window, window.getMatchService());
                    lbDialog.pack();
                    lbDialog.setSize(580, 500);
                    Thread.sleep(200);
                    captureComponent(lbDialog.getContentPane(), new File(OUTPUT_DIR, "java_leaderboard.png"));
                    lbDialog.dispose();

                    // 9. Capture Match History Flight Log Dialog
                    System.out.println("[SCREENSHOT 9/10] Match History Modal...");
                    MatchHistoryDialog histDialog = new MatchHistoryDialog(window, window, window.getMatchService());
                    histDialog.pack();
                    histDialog.setSize(580, 500);
                    Thread.sleep(200);
                    captureComponent(histDialog.getContentPane(), new File(OUTPUT_DIR, "java_match_history.png"));
                    histDialog.dispose();

                    // 10. Capture Pilot Instructions / Flight Manual
                    System.out.println("[SCREENSHOT 10/10] Pilot Flight Manual Modal...");
                    InstructionsDialog instDialog = new InstructionsDialog(window);
                    instDialog.pack();
                    instDialog.setSize(520, 540);
                    Thread.sleep(200);
                    captureComponent(instDialog.getContentPane(), new File(OUTPUT_DIR, "java_instructions.png"));
                    instDialog.dispose();

                    window.dispose();
                    System.out.println("[SUCCESS] All 10 Java Swing evidence screenshots captured successfully!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void captureAtResolution(MainWindow window, int width, int height, File targetFile) {
        window.setSize(width, height);
        window.validate();
        try {
            Thread.sleep(150);
        } catch (InterruptedException ignored) {}
        captureComponent(window.getContentPane(), targetFile);
    }

    private static void captureComponent(Component comp, File targetFile) {
        int w = Math.max(1, comp.getWidth());
        int h = Math.max(1, comp.getHeight());

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        comp.paint(g2);
        g2.dispose();

        try {
            ImageIO.write(image, "png", targetFile);
            System.out.println("  [SAVED] " + targetFile.getName() + " (" + w + "x" + h + " px)");
        } catch (Exception e) {
            System.err.println("  [ERROR] Failed to save " + targetFile.getName() + ": " + e.getMessage());
        }
    }
}
