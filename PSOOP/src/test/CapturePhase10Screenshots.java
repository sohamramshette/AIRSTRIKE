package test;

import announcer.AnnouncerManager;
import announcer.DialogueCategory;
import game.GameEngine;
import model.BossEnemy;
import model.DroneEnemy;
import model.FighterEnemy;
import ui.ProfileSettingsDialog;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class CapturePhase10Screenshots {

    public static void main(String[] args) {
        System.out.println("========================================================");
        System.out.println("   CAPTURING PHASE 10 DESI ANNOUNCER SCREENSHOTS        ");
        System.out.println("========================================================");

        File dir = new File("docs/phase10_screenshots");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        captureJavaCombatAnnouncer();
        captureJavaBossAnnouncer();
        captureJavaSettingsDialog();

        System.out.println("[SUCCESS] Java Announcer screenshots captured successfully in docs/phase10_screenshots/");
    }

    private static void captureJavaCombatAnnouncer() {
        int width = 1024;
        int height = 720;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GameEngine engine = new GameEngine(width, height);
        engine.startNewGame();

        // Spawn a couple enemies
        engine.getEnemies().add(new DroneEnemy(width / 2.0 - 80, 160));
        engine.getEnemies().add(new FighterEnemy(width / 2.0 + 80, 190));

        // Trigger Announcer Banner
        AnnouncerManager.getInstance().showBanner(
            "AREY BHAI BHAI BHAI! Ye kya kar diya?",
            DialogueCategory.DESI_MEME,
            true
        );
        AnnouncerManager.getInstance().update(0.4, engine); // Advance animation

        // Render Game Canvas
        engine.render(g, width, height);

        g.dispose();

        try {
            ImageIO.write(img, "png", new File("docs/phase10_screenshots/java_announcer_combat_toast.png"));
            System.out.println("  [SAVED] docs/phase10_screenshots/java_announcer_combat_toast.png");
        } catch (Exception e) {
            System.err.println("  [ERROR] " + e.getMessage());
        }
    }

    private static void captureJavaBossAnnouncer() {
        int width = 1024;
        int height = 720;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GameEngine engine = new GameEngine(width, height);
        engine.startNewGame();

        // Spawn Boss
        engine.getEnemies().add(new BossEnemy((width - 128) / 2.0, 100, "LEVIATHAN"));

        // Trigger Bollywood Boss Announcer Banner
        AnnouncerManager.getInstance().showBanner(
            "Gabbar bhi teri accuracy dekh ke hans raha hai!",
            DialogueCategory.BOLLYWOOD,
            true
        );
        AnnouncerManager.getInstance().update(0.4, engine);

        engine.render(g, width, height);
        g.dispose();

        try {
            ImageIO.write(img, "png", new File("docs/phase10_screenshots/java_announcer_boss_toast.png"));
            System.out.println("  [SAVED] docs/phase10_screenshots/java_announcer_boss_toast.png");
        } catch (Exception e) {
            System.err.println("  [ERROR] " + e.getMessage());
        }
    }

    private static void captureJavaSettingsDialog() {
        try {
            javax.swing.SwingUtilities.invokeAndWait(() -> {
                try {
                    int width = 620;
                    int height = 660;

                    ProfileSettingsDialog dialog = new ProfileSettingsDialog(null, null, new GameEngine(1024, 720), new service.AuthService());
                    dialog.pack();
                    dialog.setSize(width, height);
                    dialog.validate();
                    Thread.sleep(200);

                    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = img.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    dialog.getContentPane().paint(g);
                    g.dispose();
                    dialog.dispose();

                    ImageIO.write(img, "png", new File("docs/phase10_screenshots/java_announcer_settings_modal.png"));
                    System.out.println("  [SAVED] docs/phase10_screenshots/java_announcer_settings_modal.png (" + width + "x" + height + " px)");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("  [ERROR] " + e.getMessage());
        }
    }
}
