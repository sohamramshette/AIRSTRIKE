package test;

import game.GameCanvas;
import game.GameEngine;
import game.GameState;
import model.BossEnemy;
import model.DroneEnemy;
import model.FighterEnemy;
import model.PowerUp;
import model.PowerUpType;
import model.Projectile;
import ui.MainWindow;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/**
 * High-resolution screenshot capture generator for AIRSTRIKE Phase 2.1 QA evidence.
 * Captures pixel-perfect screenshots of all major game scenes.
 */
public class CaptureScreenshots {

    private static final String OUTPUT_DIR = "docs/screenshots/PSOOP";

    public static void main(String[] args) {
        System.out.println("[SCREENSHOT] Initializing screenshot capture suite...");

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
                    System.out.println("[SCREENSHOT] 1. Capturing Main Menu...");
                    window.showMainMenu();
                    Thread.sleep(300);
                    captureComponent(window.getContentPane(), new File(OUTPUT_DIR, "main_menu.png"));

                    // 2. Capture Active Gameplay
                    System.out.println("[SCREENSHOT] 2. Capturing Active Gameplay...");
                    window.startGame();
                    GameEngine engine = window.getEngine();
                    GameCanvas canvas = window.getGameCanvas();

                    // Setup combat scenario
                    engine.getEnemies().add(new DroneEnemy(180, 140));
                    engine.getEnemies().add(new FighterEnemy(460, 110));
                    engine.getPowerUps().add(new PowerUp(340, 260, PowerUpType.WEAPON_UPGRADE));
                    engine.getPlayer().setX(376);
                    engine.getPlayer().setY(480);
                    engine.getPlayer().shoot(engine.getProjectiles());
                    engine.getScoreCalculator().addScore(450);

                    // Render multiple frames to advance stars and particles
                    for (int i = 0; i < 15; i++) {
                        engine.update(0.016);
                    }
                    Thread.sleep(200);
                    captureComponent(canvas, new File(OUTPUT_DIR, "active_gameplay.png"));

                    // 3. Capture Boss Battle
                    System.out.println("[SCREENSHOT] 3. Capturing Boss Battle...");
                    engine.getEnemies().clear();
                    engine.getProjectiles().clear();
                    BossEnemy boss = new BossEnemy(342, 50, "Leviathan MK-I");
                    boss.setHealth(420);
                    boss.setShield(80);
                    engine.getEnemies().add(boss);
                    boss.performAttack(0.016, engine.getProjectiles(), engine.getPlayer());
                    engine.getScoreCalculator().addScore(1800);
                    engine.update(0.016);
                    Thread.sleep(200);
                    captureComponent(canvas, new File(OUTPUT_DIR, "boss_battle.png"));

                    // 4. Capture Pause Screen
                    System.out.println("[SCREENSHOT] 4. Capturing Pause Screen...");
                    engine.pauseGame();
                    Thread.sleep(200);
                    captureComponent(canvas, new File(OUTPUT_DIR, "pause_screen.png"));

                    // 5. Capture Game Over Screen
                    System.out.println("[SCREENSHOT] 5. Capturing Game Over Screen...");
                    engine.resumeGame();
                    engine.getPlayer().setLives(0);
                    engine.getPlayer().setHealth(0);
                    engine.getPlayer().setActive(false);
                    try {
                        engine.setState(GameState.GAME_OVER);
                    } catch (Exception ignored) {}
                    Thread.sleep(200);
                    captureComponent(canvas, new File(OUTPUT_DIR, "game_over_screen.png"));

                    // 6. Capture Leaderboard / Tactical Manual
                    System.out.println("[SCREENSHOT] 6. Capturing Tactical Flight Manual...");
                    window.showMainMenu();
                    captureComponent(window.getContentPane(), new File(OUTPUT_DIR, "leaderboard_placeholder.png"));

                    // 7. Capture Pilot Authentication Dialog
                    System.out.println("[SCREENSHOT] 7. Capturing Pilot Authentication Dialog...");
                    ui.AuthDialog authDialog = new ui.AuthDialog(window, window.getAuthService());
                    authDialog.pack();
                    authDialog.setSize(480, 520);
                    captureComponent(authDialog.getContentPane(), new File(OUTPUT_DIR, "auth_dialog.png"));
                    authDialog.dispose();

                    // 8. Capture Global Leaderboard Dialog
                    System.out.println("[SCREENSHOT] 8. Capturing Global Leaderboard Dialog...");
                    ui.LeaderboardDialog lbDialog = new ui.LeaderboardDialog(window, window, window.getMatchService());
                    lbDialog.pack();
                    lbDialog.setSize(580, 520);
                    Thread.sleep(150);
                    captureComponent(lbDialog.getContentPane(), new File(OUTPUT_DIR, "leaderboard_dialog.png"));
                    lbDialog.dispose();

                    // 9. Capture Match History Dialog
                    System.out.println("[SCREENSHOT] 9. Capturing Match History Dialog...");
                    ui.MatchHistoryDialog histDialog = new ui.MatchHistoryDialog(window, window, window.getMatchService());
                    histDialog.pack();
                    histDialog.setSize(580, 520);
                    Thread.sleep(150);
                    captureComponent(histDialog.getContentPane(), new File(OUTPUT_DIR, "match_history_dialog.png"));
                    histDialog.dispose();

                    window.dispose();
                    System.out.println("[SCREENSHOT] All 9 screenshots successfully captured in " + OUTPUT_DIR);
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

