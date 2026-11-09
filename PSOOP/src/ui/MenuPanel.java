package ui;

import database.SessionManager;
import database.UserSession;
import service.AuthService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Modern, aesthetic landing page and main menu interface for AIRSTRIKE.
 * Implements the Phase 9 Design System with dark navy glassmorphism,
 * glowing cyan/purple accents, and full integration with all 4 Academic Modules.
 */
public class MenuPanel extends JPanel {

    private final MainWindow mainWindow;
    private final AuthService authService;

    // Pilot HUD components
    private JLabel pilotBeaconDot;
    private JLabel pilotStatusLabel;
    private JButton pilotLoginBtn;
    private JButton pilotRegisterBtn;
    private JButton pilotLogoutBtn;

    // Starfield animation
    private final List<MenuStar> stars;
    private final Timer animationTimer;
    private float pulsePhase = 0.0f;

    public MenuPanel(MainWindow mainWindow) {
        this(mainWindow, new AuthService());
    }

    public MenuPanel(MainWindow mainWindow, AuthService authService) {
        this.mainWindow = mainWindow;
        this.authService = authService;

        setPreferredSize(new Dimension(800, 650));
        setBackground(Theme.BG_DEEP);
        setLayout(new GridBagLayout());

        // Initialize background stars
        this.stars = new ArrayList<>();
        Random rand = new Random(42);
        for (int i = 0; i < 90; i++) {
            stars.add(new MenuStar(
                    rand.nextInt(1920),
                    rand.nextInt(1080),
                    0.25 + rand.nextDouble() * 0.95,
                    1 + rand.nextInt(3),
                    rand.nextDouble() * Math.PI * 2
            ));
        }

        buildUI();

        // Listen for authentication changes from SessionManager
        SessionManager.getInstance().addListener((oldSession, newSession) -> {
            SwingUtilities.invokeLater(this::updatePilotStatus);
        });

        updatePilotStatus();

        // Background starfield & pulse animation timer (~35 FPS)
        this.animationTimer = new Timer(28, e -> {
            updateAnimation();
            repaint();
        });
        this.animationTimer.start();
    }

    private void updateAnimation() {
        pulsePhase += 0.04f;
        if (pulsePhase > Math.PI * 2) {
            pulsePhase -= (float) (Math.PI * 2);
        }

        int h = getHeight() > 0 ? getHeight() : 650;
        int w = getWidth() > 0 ? getWidth() : 800;

        for (MenuStar s : stars) {
            s.y += s.speed;
            s.twinkle += 0.05;
            if (s.y > h + 10) {
                s.y = -10;
                s.x = Math.random() * w;
            }
        }
    }

    private void buildUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // 1. Pilot Status HUD Capsule (top header space)
        gbc.gridy = 0;
        gbc.insets = new Insets(136, 12, 10, 12);
        JPanel pilotHud = buildPilotStatusCapsule();
        add(pilotHud, gbc);

        // 2. Primary Hero Button: LAUNCH MISSION
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 12, 6, 12);
        CyberButton launchBtn = CyberButton.hero("▶   LAUNCH MISSION");
        launchBtn.addActionListener(e -> mainWindow.startGame());
        add(launchBtn, gbc);

        // 3. Academic Curriculum: 19 Course Outcomes (All 4 Subjects)
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 12, 3, 12);
        CyberButton subjectCoBtn = CyberButton.tactical(
                "✦   COURSE OUTCOMES & SUBJECTS (19 COs)",
                Theme.PURPLE_ACCENT,
                Theme.PURPLE_DARK
        );
        subjectCoBtn.addActionListener(e -> mainWindow.showSubjectCODialog());
        add(subjectCoBtn, gbc);

        // 4. Global Leaderboard
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 12, 3, 12);
        CyberButton leaderboardBtn = CyberButton.tactical(
                "★   GLOBAL LEADERBOARD (HALL OF FAME)",
                Theme.GOLD_ACCENT,
                Theme.GOLD_DARK
        );
        leaderboardBtn.addActionListener(e -> mainWindow.showLeaderboardDialog());
        add(leaderboardBtn, gbc);

        // 5. Pilot Profile & System Settings
        gbc.gridy = 4;
        CyberButton profileSettingsBtn = CyberButton.tactical(
                "◈   PILOT PROFILE & SYSTEM SETTINGS",
                Theme.BLUE_ACCENT,
                new Color(16, 32, 54)
        );
        profileSettingsBtn.addActionListener(e -> mainWindow.showProfileSettingsDialog());
        add(profileSettingsBtn, gbc);

        // 6. Match History / Flight Logs
        gbc.gridy = 5;
        CyberButton historyBtn = CyberButton.tactical(
                "⟡   MATCH HISTORY (FLIGHT LOGS)",
                new Color(56, 189, 248),
                new Color(16, 32, 48)
        );
        historyBtn.addActionListener(e -> mainWindow.showMatchHistoryDialog());
        add(historyBtn, gbc);

        // 7. Pilot Manual (Controls)
        gbc.gridy = 6;
        CyberButton manualBtn = CyberButton.tactical(
                "❖   PILOT MANUAL (FLIGHT CONTROLS)",
                Theme.EMERALD_STATUS,
                Theme.EMERALD_DARK
        );
        manualBtn.addActionListener(e -> mainWindow.showInstructions());
        add(manualBtn, gbc);

        // 8. Abort to Desktop Button
        gbc.gridy = 7;
        gbc.insets = new Insets(6, 12, 14, 12);
        CyberButton exitBtn = CyberButton.danger("✕   ABORT TO DESKTOP");
        exitBtn.addActionListener(e -> System.exit(0));
        add(exitBtn, gbc);
    }

    /**
     * Builds the floating Glassmorphic Pilot HUD Capsule.
     */
    private JPanel buildPilotStatusCapsule() {
        JPanel capsule = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.enableQuality(g2);

                int w = getWidth();
                int h = getHeight();

                // Dark glassmorphic background
                g2.setColor(Theme.BG_GLASS);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));

                // Glowing border
                boolean auth = SessionManager.getInstance().isLoggedIn();
                Color borderColor = auth ? Theme.BORDER_EMERALD : Theme.BORDER_CYAN;
                g2.setColor(borderColor);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, 14, 14));

                g2.dispose();
                super.paintComponent(g);
            }
        };

        capsule.setOpaque(false);
        capsule.setPreferredSize(new Dimension(Theme.BUTTON_WIDTH, 38));
        capsule.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 10));

        // Left indicator container
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));
        infoPanel.setOpaque(false);

        pilotBeaconDot = new JLabel("●");
        pilotBeaconDot.setFont(Theme.BODY_BOLD);
        pilotBeaconDot.setForeground(Theme.BLUE_ACCENT);
        infoPanel.add(pilotBeaconDot);

        infoPanel.add(Box.createHorizontalStrut(6));

        pilotStatusLabel = new JLabel("PILOT: CADET (LOCAL GUEST)");
        pilotStatusLabel.setFont(Theme.BODY_BOLD);
        pilotStatusLabel.setForeground(Theme.TEXT_WHITE);
        infoPanel.add(pilotStatusLabel);

        capsule.add(infoPanel, BorderLayout.WEST);

        // Right button pill panel
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.X_AXIS));
        btnPanel.setOpaque(false);

        pilotLoginBtn = CyberButton.pill("LOGIN", Theme.CYAN_PRIMARY, Theme.BG_PANEL, 58);
        pilotLoginBtn.addActionListener(e -> mainWindow.showAuthDialog("LOGIN"));
        btnPanel.add(pilotLoginBtn);

        btnPanel.add(Box.createHorizontalStrut(6));

        pilotRegisterBtn = CyberButton.pill("REGISTER", Theme.EMERALD_STATUS, Theme.EMERALD_DARK, 74);
        pilotRegisterBtn.addActionListener(e -> mainWindow.showAuthDialog("REGISTER"));
        btnPanel.add(pilotRegisterBtn);

        pilotLogoutBtn = CyberButton.pill("LOGOUT", Theme.CORAL_DANGER, Theme.CORAL_DARK, 66);
        pilotLogoutBtn.addActionListener(e -> {
            authService.logout();
            mainWindow.showAuthDialog("LOGIN");
        });
        btnPanel.add(pilotLogoutBtn);

        capsule.add(btnPanel, BorderLayout.EAST);
        return capsule;
    }

    public void updatePilotStatus() {
        UserSession session = SessionManager.getInstance().getCurrentSession();
        if (session.isAuthenticated()) {
            pilotStatusLabel.setText("PILOT: " + session.getUsername().toUpperCase());
            pilotStatusLabel.setForeground(Theme.EMERALD_STATUS);
            pilotBeaconDot.setForeground(Theme.EMERALD_STATUS);
            pilotLoginBtn.setVisible(false);
            pilotRegisterBtn.setVisible(false);
            pilotLogoutBtn.setVisible(true);
        } else {
            pilotStatusLabel.setText("PILOT: CADET (LOCAL GUEST)");
            pilotStatusLabel.setForeground(new Color(186, 230, 253));
            pilotBeaconDot.setForeground(Theme.BLUE_ACCENT);
            pilotLoginBtn.setVisible(true);
            pilotRegisterBtn.setVisible(true);
            pilotLogoutBtn.setVisible(false);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        Theme.enableQuality(g2d);

        int w = getWidth();
        int h = getHeight();

        // 1. Cosmic Background and Nebula Glow
        Theme.renderCosmicBackground(g2d, w, h);

        // 2. Procedural Drifting Stars
        for (MenuStar s : stars) {
            float tw = (float) (0.45 + 0.55 * Math.sin(s.twinkle));
            int alpha = (int) Math.min(255, Math.max(30, 230 * tw));
            g2d.setColor(new Color(220, 240, 255, alpha));
            g2d.fillOval((int) s.x, (int) s.y, s.size, s.size);

            if (s.size > 2) {
                g2d.setColor(new Color(0, 229, 255, alpha / 3));
                g2d.drawOval((int) s.x - 1, (int) s.y - 1, s.size + 2, s.size + 2);
            }
        }

        // 3. HERO TITLE: A I R S T R I K E
        String title = "A I R S T R I K E";
        int titleY = 78;
        Theme.drawGlowTextCentered(g2d, title, w / 2, titleY, Theme.TITLE_HERO, Theme.CYAN_PRIMARY, Theme.TEXT_WHITE);

        // Decorative Wings with tech notches
        g2d.setFont(Theme.TITLE_HERO);
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleW = fmTitle.stringWidth(title);
        int titleX = (w - titleW) / 2;
        int wingLen = 64;
        int wingY = titleY - 14;

        g2d.setColor(new Color(0, 229, 255, 150));
        g2d.drawLine(titleX - 18 - wingLen, wingY, titleX - 18, wingY);
        g2d.drawLine(titleX + titleW + 18, wingY, titleX + titleW + 18 + wingLen, wingY);
        g2d.fillRect(titleX - 18 - wingLen - 3, wingY - 2, 4, 5);
        g2d.fillRect(titleX + titleW + 18 + wingLen, wingY - 2, 4, 5);

        // 4. Subtitle
        String subtitle = "TACTICAL COMBAT SIMULATOR & ACADEMIC DEMONSTRATION SYSTEM";
        g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fmSub = g2d.getFontMetrics();
        int subX = (w - fmSub.stringWidth(subtitle)) / 2;
        int subY = 104;
        g2d.setColor(Theme.TEXT_MUTED);
        g2d.drawString(subtitle, subX, subY);

        // 5. Cloud Connection & System Badge
        String badge = "● STARFLEET CLOUD LINK ACTIVE  •  SUPABASE POSTGREST 2.2";
        g2d.setFont(Theme.BADGE_FONT);
        FontMetrics fmBadge = g2d.getFontMetrics();
        int badgeW = fmBadge.stringWidth(badge) + 20;
        int badgeX = (w - badgeW) / 2;
        int badgeY = 114;

        g2d.setColor(new Color(16, 24, 44, 200));
        g2d.fill(new RoundRectangle2D.Float(badgeX, badgeY, badgeW, 19, 10, 10));
        g2d.setColor(Theme.BORDER_CYAN);
        g2d.draw(new RoundRectangle2D.Float(badgeX + 0.5f, badgeY + 0.5f, badgeW - 1f, 18, 10, 10));

        g2d.setColor(Theme.EMERALD_STATUS);
        g2d.drawString(badge, badgeX + 10, badgeY + 13);

        // 6. Footer Module Bar
        renderFooterDeck(g2d, w, h);

        g2d.dispose();
    }

    private void renderFooterDeck(Graphics2D g2d, int w, int h) {
        int footerY = h - 22;

        g2d.setFont(Theme.BODY_BOLD);
        g2d.setColor(Theme.TEXT_DIM);
        String modules = "ACADEMIC SUITE:  [PSOOP Java]  •  [PL C++17]  •  [COA 80386]  •  [CGL OpenGL]";
        g2d.drawString(modules, 48, footerY);

        int highScore = mainWindow.getEngine().getScoreCalculator().getHighScore();
        String recordStr = "RECORD: " + String.format("%,d", highScore) + " PTS";
        FontMetrics fm = g2d.getFontMetrics();
        int recX = w - fm.stringWidth(recordStr) - 48;

        g2d.setColor(Theme.GOLD_ACCENT);
        g2d.drawString(recordStr, recX, footerY);
    }

    private static class MenuStar {
        double x, y;
        double speed;
        int size;
        double twinkle;

        MenuStar(double x, double y, double speed, int size, double twinkle) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.size = size;
            this.twinkle = twinkle;
        }
    }
}
