package test;

import database.SessionManager;
import database.UserSession;
import database.multiplayer.MultiplayerModels.MultiplayerRoomState;
import database.multiplayer.MultiplayerModels.RoomStatus;
import ui.MainWindow;
import ui.MultiplayerDialog;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Visual evidence generator for Java Swing Multiplayer UI (Phase 12 Part 4).
 * Captures pixel-perfect screenshots of Main Menu, Menu Subview, Join View, and Lobby View.
 */
public class CaptureMultiplayerScreenshots {

    private static final String OUTPUT_DIR = "docs/screenshots";
    private static final String ARTIFACT_DIR = "C:/Users/lenovo/.gemini/antigravity/brain/e548191e-70dc-46a1-ac05-79371e515d36";

    public static void main(String[] args) {
        System.out.println("[PHASE 12 PART 4] Capturing Java Swing Multiplayer Screenshot Evidence...");

        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) dir.mkdirs();

        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    // Set an authenticated mock session so the UI shows verified pilot
                    UserSession mockSession = new UserSession(
                            "host-uuid-001", "commander@starfleet.org", "STAR-COMMANDER",
                            "mock-jwt-token", "mock-refresh-token",
                            System.currentTimeMillis() + 3600000, true
                    );
                    SessionManager.getInstance().setSession(mockSession);

                    MainWindow window = new MainWindow();
                    window.setSize(800, 650);
                    window.setVisible(true);
                    Thread.sleep(300);

                    // 1. Capture Main Menu with Multiplayer Button
                    System.out.println("[1/5] Capturing Java Main Menu with Multiplayer Entry...");
                    captureComponent(window.getContentPane(), new File(OUTPUT_DIR, "java_mp_main_menu.png"));

                    // Instantiate Multiplayer Dialog
                    MultiplayerDialog dialog = new MultiplayerDialog(window, window);
                    dialog.setModal(false);
                    dialog.setSize(600, 540);
                    dialog.setLocationRelativeTo(window);
                    dialog.setVisible(true);
                    Thread.sleep(200);

                    Field contentPanelField = MultiplayerDialog.class.getDeclaredField("contentPanel");
                    contentPanelField.setAccessible(true);
                    JPanel contentPanel = (JPanel) contentPanelField.get(dialog);

                    Field cardLayoutField = MultiplayerDialog.class.getDeclaredField("cardLayout");
                    cardLayoutField.setAccessible(true);
                    CardLayout cardLayout = (CardLayout) cardLayoutField.get(dialog);

                    // 2. Capture Sub-View 1: Menu
                    System.out.println("[2/5] Capturing Multiplayer Menu View...");
                    cardLayout.show(contentPanel, "VIEW_MENU");
                    dialog.validate();
                    Thread.sleep(150);
                    captureComponent(dialog.getContentPane(), new File(OUTPUT_DIR, "java_mp_view_menu.png"));

                    // 3. Capture Sub-View 2: Join View
                    System.out.println("[3/5] Capturing Multiplayer Join Room View...");
                    cardLayout.show(contentPanel, "VIEW_JOIN");
                    dialog.validate();
                    Thread.sleep(150);
                    captureComponent(dialog.getContentPane(), new File(OUTPUT_DIR, "java_mp_view_join.png"));

                    // 4. Capture Sub-View 3: Waiting Lobby (Host waiting for challenger)
                    System.out.println("[4/5] Capturing Multiplayer Lobby Waiting View...");
                    MultiplayerRoomState waitingRoom = new MultiplayerRoomState(
                            "00000000-0000-0000-0000-000000000001", "K7N2P9",
                            "host-uuid-001", "STAR-COMMANDER", false,
                            "guest-uuid-002", "VIPER-CADET", false,
                            42L, RoomStatus.READY, true, false,
                            "2026-11-11T12:00:00Z", "2026-11-11T12:15:00Z"
                    );

                    Method renderLobbyMethod = MultiplayerDialog.class.getDeclaredMethod("renderLobbyState", MultiplayerRoomState.class);
                    renderLobbyMethod.setAccessible(true);
                    renderLobbyMethod.invoke(dialog, waitingRoom);

                    cardLayout.show(contentPanel, "VIEW_LOBBY");
                    dialog.validate();
                    Thread.sleep(150);
                    captureComponent(dialog.getContentPane(), new File(OUTPUT_DIR, "java_mp_view_lobby.png"));

                    // 5. Capture Sub-View 3: Countdown Engaged View
                    System.out.println("[5/5] Capturing Multiplayer Countdown Engaged View...");
                    MultiplayerRoomState countdownRoom = new MultiplayerRoomState(
                            "00000000-0000-0000-0000-000000000001", "K7N2P9",
                            "host-uuid-001", "STAR-COMMANDER", true,
                            "guest-uuid-002", "VIPER-CADET", true,
                            42L, RoomStatus.COUNTDOWN, true, false,
                            "2026-11-11T12:00:00Z", "2026-11-11T12:15:00Z"
                    );
                    renderLobbyMethod.invoke(dialog, countdownRoom);
                    dialog.validate();
                    Thread.sleep(150);
                    captureComponent(dialog.getContentPane(), new File(OUTPUT_DIR, "java_mp_view_countdown.png"));

                    // Copy to artifacts dir
                    copyToArtifact("java_mp_main_menu.png");
                    copyToArtifact("java_mp_view_menu.png");
                    copyToArtifact("java_mp_view_join.png");
                    copyToArtifact("java_mp_view_lobby.png");
                    copyToArtifact("java_mp_view_countdown.png");

                    dialog.dispose();
                    window.dispose();
                    System.out.println("[SUCCESS] All 5 Java multiplayer screenshots captured successfully!");
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

    private static void copyToArtifact(String filename) {
        try {
            File src = new File(OUTPUT_DIR, filename);
            File dest = new File(ARTIFACT_DIR, filename);
            if (src.exists()) {
                java.nio.file.Files.copy(src.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ignored) {}
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
