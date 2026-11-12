package test;

import database.SessionManager;
import database.UserSession;
import database.multiplayer.MultiplayerCombatSyncService;
import database.multiplayer.MultiplayerModels.MultiplayerRoomState;
import database.multiplayer.MultiplayerModels.RoomStatus;
import game.GameEngine;
import ui.MainWindow;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Visual evidence generator for Java Swing Multiplayer 1v1 Combat & Debrief (Phase 12 Part 6).
 */
public class CapturePart6MultiplayerScreenshots {

    private static final String OUTPUT_DIR = "docs/screenshots";
    private static final String ARTIFACT_DIR = "C:/Users/lenovo/.gemini/antigravity/brain/e548191e-70dc-46a1-ac05-79371e515d36";

    public static void main(String[] args) {
        System.out.println("[PHASE 12 PART 6] Capturing Java Swing 1v1 Combat & Debrief Evidence...");

        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) dir.mkdirs();

        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    UserSession mockSession = new UserSession(
                            "host-uuid-001", "commander@starfleet.org", "STAR-COMMANDER",
                            "mock-jwt-token", "mock-refresh-token",
                            System.currentTimeMillis() + 3600000, true
                    );
                    SessionManager.getInstance().setSession(mockSession);

                    GameEngine engine = new GameEngine(800, 650);

                    MultiplayerRoomState room = new MultiplayerRoomState(
                            "room-uuid-part6", "SYNC88",
                            "host-uuid-001", "STAR-COMMANDER", true,
                            "guest-uuid-002", "VIPER-CADET", true,
                            42L, RoomStatus.ACTIVE, true, false,
                            "", "", java.time.Instant.now().toString(), 90, null, null, null
                    );

                    // 1. Capture Active 1v1 Combat Scene with Dual Pod HUD
                    System.out.println("[1/2] Capturing 1v1 Combat Screen with Synchronized Telemetry HUD...");
                    engine.startMultiplayerGame(42L, java.time.Instant.now().toString(), room);

                    // Populate peer telemetry
                    Map<String, Object> peerTelemetry = new HashMap<>();
                    peerTelemetry.put("roomId", room.getRoomId());
                    peerTelemetry.put("playerId", "guest-uuid-002");
                    peerTelemetry.put("callsign", "VIPER-CADET");
                    peerTelemetry.put("score", 1450);
                    peerTelemetry.put("health", 85);
                    peerTelemetry.put("shield", 70);
                    peerTelemetry.put("lives", 2);
                    peerTelemetry.put("wave", 2);
                    peerTelemetry.put("combo", 2);
                    peerTelemetry.put("overheated", false);
                    peerTelemetry.put("sequence", 15L);
                    peerTelemetry.put("timestamp", System.currentTimeMillis());
                    MultiplayerCombatSyncService.getInstance().handleInboundTelemetry(peerTelemetry);

                    // Add local score
                    engine.getScoreCalculator().addScore(1850); // Local leads by +400!

                    BufferedImage combatImg = new BufferedImage(800, 650, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = combatImg.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    engine.render(g2, 800, 650);
                    g2.dispose();

                    saveImage(combatImg, "java_mp_combat_hud.png");

                    // 2. Capture Match Debrief Results Screen
                    System.out.println("[2/2] Capturing 1v1 Score Attack Debrief Results Screen...");
                    engine.finishMultiplayerMatch();

                    BufferedImage debriefImg = new BufferedImage(800, 650, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D gDebrief = debriefImg.createGraphics();
                    gDebrief.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    gDebrief.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    engine.render(gDebrief, 800, 650);
                    gDebrief.dispose();

                    saveImage(debriefImg, "java_mp_match_results.png");

                    System.out.println("[SUCCESS] Java Swing Part 6 Screenshots captured successfully!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveImage(BufferedImage img, String filename) {
        try {
            File outFile = new File(OUTPUT_DIR, filename);
            ImageIO.write(img, "PNG", outFile);
            System.out.println("  ✓ Saved: " + outFile.getAbsolutePath());

            File artifactFile = new File(ARTIFACT_DIR, filename);
            if (artifactFile.getParentFile().exists()) {
                ImageIO.write(img, "PNG", artifactFile);
                System.out.println("  ✓ Mirrored to artifact dir: " + artifactFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("  ✗ Failed to save " + filename + ": " + e.getMessage());
        }
    }
}

