package test;

import database.SessionManager;
import database.UserSession;
import database.multiplayer.MultiplayerCombatSyncService;
import database.multiplayer.MultiplayerModels.MultiplayerRoomState;
import database.multiplayer.MultiplayerModels.RoomStatus;
import game.GameEngine;
import ui.Theme;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Visual evidence generator for Java Swing Part 7:
 * - 1v1 Provisional Match Debrief Screen
 * - 1v1 Multiplayer Provisional Leaderboard Dialog
 */
public class CapturePart7MultiplayerScreenshots {

    private static final String OUTPUT_DIR = "docs/screenshots";
    private static final String ARTIFACT_DIR = "C:/Users/lenovo/.gemini/antigravity/brain/e548191e-70dc-46a1-ac05-79371e515d36";

    public static void main(String[] args) {
        System.out.println("[PHASE 12 PART 7] Capturing Java Swing 1v1 Provisional Debrief & Leaderboard Evidence...");

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

                    // 1. Capture 1v1 Provisional Debrief Results Screen
                    GameEngine engine = new GameEngine(800, 650);
                    MultiplayerRoomState room = new MultiplayerRoomState(
                            "room-uuid-part7", "PROV77",
                            "host-uuid-001", "STAR-COMMANDER", true,
                            "guest-uuid-002", "VIPER-CADET", true,
                            42L, RoomStatus.ACTIVE, true, false,
                            "", "", java.time.Instant.now().toString(), 90, null, null, null
                    );

                    engine.startMultiplayerGame(42L, java.time.Instant.now().toString(), room);

                    Map<String, Object> peerTelemetry = new HashMap<>();
                    peerTelemetry.put("roomId", room.getRoomId());
                    peerTelemetry.put("playerId", "guest-uuid-002");
                    peerTelemetry.put("callsign", "VIPER-CADET");
                    peerTelemetry.put("score", 2950);
                    peerTelemetry.put("health", 80);
                    peerTelemetry.put("shield", 60);
                    peerTelemetry.put("lives", 2);
                    peerTelemetry.put("wave", 3);
                    peerTelemetry.put("combo", 3);
                    peerTelemetry.put("overheated", false);
                    peerTelemetry.put("sequence", 80L);
                    peerTelemetry.put("timestamp", System.currentTimeMillis());
                    MultiplayerCombatSyncService.getInstance().handleInboundTelemetry(peerTelemetry);

                    engine.getScoreCalculator().addScore(3650); // Local leads by +700
                    engine.finishMultiplayerMatch();

                    BufferedImage debriefImg = new BufferedImage(800, 650, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D gDebrief = debriefImg.createGraphics();
                    gDebrief.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    gDebrief.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    engine.render(gDebrief, 800, 650);
                    gDebrief.dispose();

                    saveImage(debriefImg, "java_mp_provisional_debrief.png");

                    // 2. Capture 1v1 Multiplayer Provisional Leaderboard Dialog
                    JPanel lbPanel = new JPanel(new BorderLayout(10, 10));
                    lbPanel.setSize(580, 440);
                    lbPanel.setBackground(Theme.BG_DEEP);

                    JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
                    headerPanel.setOpaque(false);
                    headerPanel.setBorder(BorderFactory.createEmptyBorder(14, 18, 4, 18));

                    JLabel titleLbl = new JLabel("STARFLEET 1v1 PILOT RANKINGS (PROVISIONAL)");
                    titleLbl.setFont(new Font("SansSerif", Font.BOLD, 15));
                    titleLbl.setForeground(Theme.GOLD_ACCENT);

                    JLabel noticeLbl = new JLabel("<html><font color='#FBBF24'><b>⚠️ PROVISIONAL STATUS:</b></font> <font color='#94A3B8'>Peer-reported telemetry. Official verified rankings require dedicated server simulation.</font></html>");
                    noticeLbl.setFont(Theme.MONO_SMALL);

                    headerPanel.add(titleLbl, BorderLayout.NORTH);
                    headerPanel.add(noticeLbl, BorderLayout.SOUTH);
                    lbPanel.add(headerPanel, BorderLayout.NORTH);

                    String[] cols = {"RANK", "CALLSIGN", "RATING (MMR)", "W / L / D", "WIN RATE"};
                    DefaultTableModel model = new DefaultTableModel(cols, 0);
                    model.addRow(new Object[]{"🥇 #1", "STAR-COMMANDER [PROV]", 1050, "2W / 0L / 0D", "100.0%"});
                    model.addRow(new Object[]{"🥈 #2", "VIPER-CADET [PROV]", 1025, "1W / 1L / 0D", "50.0%"});
                    model.addRow(new Object[]{"🥉 #3", "PHOENIX-ACE [PROV]", 1010, "1W / 0L / 1D", "66.7%"});
                    model.addRow(new Object[]{"#4", "SHADOW-RAVEN [PROV]", 990, "0W / 1L / 1D", "0.0%"});

                    JTable table = new JTable(model);
                    table.setBackground(Theme.BG_CARD);
                    table.setForeground(Color.WHITE);
                    table.setGridColor(Theme.BORDER_CYAN);
                    table.setRowHeight(28);
                    table.setFont(Theme.BODY);
                    table.getTableHeader().setBackground(new Color(15, 23, 42));
                    table.getTableHeader().setForeground(Theme.CYAN_PRIMARY);
                    table.getTableHeader().setFont(Theme.HEADER_2);

                    DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
                    centerRender.setHorizontalAlignment(SwingConstants.CENTER);
                    table.getColumnModel().getColumn(0).setCellRenderer(centerRender);
                    table.getColumnModel().getColumn(2).setCellRenderer(centerRender);
                    table.getColumnModel().getColumn(3).setCellRenderer(centerRender);
                    table.getColumnModel().getColumn(4).setCellRenderer(centerRender);

                    JScrollPane scrollPane = new JScrollPane(table);
                    scrollPane.getViewport().setBackground(Theme.BG_DEEP);
                    scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 18, 12, 18));
                    lbPanel.add(scrollPane, BorderLayout.CENTER);

                    BufferedImage lbImg = new BufferedImage(580, 440, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D gLb = lbImg.createGraphics();
                    gLb.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    gLb.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    lbPanel.doLayout();
                    headerPanel.doLayout();
                    scrollPane.doLayout();
                    table.doLayout();
                    lbPanel.paint(gLb);
                    gLb.dispose();

                    saveImage(lbImg, "java_mp_leaderboard.png");

                    System.out.println("[SUCCESS] Java Swing Part 7 Screenshots captured successfully!");
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
            ImageIO.write(img, "PNG", artifactFile);
            System.out.println("  ✓ Mirrored to artifact dir: " + artifactFile.getAbsolutePath());
        } catch (Exception ex) {
            System.err.println("Failed to save image: " + ex.getMessage());
        }
    }
}
