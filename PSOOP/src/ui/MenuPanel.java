package ui;

import database.SessionManager;
import database.UserSession;
import service.AuthService;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/**
 * Modern, aesthetic landing page and main menu interface for AIRSTRIKE.
 *
 * Visual Features:
 * - Animated deep-space procedural starfield with twinkling alpha layers.
 * - Glassmorphic dark-slate aesthetic with glowing holographic cyan and neon accents.
 * - Multi-layer neon text glow on AIRSTRIKE hero branding.
 * - Sleek Pilot HUD Status Capsule with real-time clearance indicators.
 * - Custom vector-styled interactive command buttons with glow-on-hover effects.
 * - Full integration with Supabase Authentication and Player Session Management.
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
        setBackground(new Color(7, 10, 19));
        setLayout(new GridBagLayout());

        // Initialize background stars
        this.stars = new ArrayList<>();
        Random rand = new Random(42);
        for (int i = 0; i < 85; i++) {
            stars.add(new MenuStar(
                    rand.nextInt(850),
                    rand.nextInt(700),
                    0.2 + rand.nextDouble() * 0.9,
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

        // 1. Pilot Status HUD Capsule (below hero header space)
        gbc.gridy = 0;
        gbc.insets = new Insets(152, 12, 10, 12);
        JPanel pilotHud = buildPilotStatusCapsule();
        add(pilotHud, gbc);

        // 2. Primary Hero Button: LAUNCH MISSION
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 12, 8, 12);
        AestheticButton launchBtn = new AestheticButton(
                "▶   LAUNCH MISSION",
                new Color(0, 229, 255),
                new Color(0, 119, 182),
                true
        );
        launchBtn.setPreferredSize(new Dimension(380, 44));
        launchBtn.addActionListener(e -> mainWindow.startGame());
        add(launchBtn, gbc);

        // 3. Global Leaderboard
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 12, 3, 12);
        AestheticButton leaderboardBtn = new AestheticButton(
                "★   GLOBAL LEADERBOARD",
                new Color(255, 193, 7),
                new Color(40, 32, 16),
                false
        );
        leaderboardBtn.addActionListener(e -> mainWindow.showLeaderboardDialog());
        add(leaderboardBtn, gbc);

        // 4. Match History / Flight Logs
        gbc.gridy = 3;
        AestheticButton historyBtn = new AestheticButton(
                "◈   MATCH HISTORY (FLIGHT LOGS)",
                new Color(56, 189, 248),
                new Color(16, 32, 48),
                false
        );
        historyBtn.addActionListener(e -> mainWindow.showMatchHistoryDialog());
        add(historyBtn, gbc);

        // 5. Starfleet Pilot Registry (Account)
        gbc.gridy = 4;
        AestheticButton registryBtn = new AestheticButton(
                "✦   STARFLEET PILOT REGISTRY",
                new Color(168, 85, 247),
                new Color(36, 18, 48),
                false
        );
        registryBtn.addActionListener(e -> mainWindow.showAuthDialog());
        add(registryBtn, gbc);

        // 6. Pilot Manual (Controls)
        gbc.gridy = 5;
        AestheticButton manualBtn = new AestheticButton(
                "❖   PILOT MANUAL (CONTROLS)",
                new Color(52, 211, 153),
                new Color(16, 40, 30),
                false
        );
        manualBtn.addActionListener(e -> mainWindow.showInstructions());
        add(manualBtn, gbc);

        // 7. System Settings
        gbc.gridy = 6;
        AestheticButton settingsBtn = new AestheticButton(
                "⚙   SYSTEM SETTINGS",
                new Color(251, 146, 60),
                new Color(44, 28, 16),
                false
        );
        settingsBtn.addActionListener(e -> mainWindow.showSettings());
        add(settingsBtn, gbc);

        // 8. Exit / Abort Button
        gbc.gridy = 7;
        gbc.insets = new Insets(6, 12, 14, 12);
        AestheticButton exitBtn = new AestheticButton(
                "✕   ABORT TO DESKTOP",
                new Color(248, 113, 113),
                new Color(48, 18, 22),
                false
        );
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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Dark glassmorphic background
                g2.setColor(new Color(13, 19, 33, 220));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 16, 16));

                // Glowing border
                boolean auth = SessionManager.getInstance().isLoggedIn();
                Color borderColor = auth ? new Color(52, 211, 153, 110) : new Color(56, 189, 248, 90);
                g2.setColor(borderColor);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, 16, 16));

                g2.dispose();
                super.paintComponent(g);
            }
        };

        capsule.setOpaque(false);
        capsule.setPreferredSize(new Dimension(380, 38));
        capsule.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 10));

        // Left indicator container (BoxLayout ensures it never wraps onto a second line)
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new javax.swing.BoxLayout(infoPanel, javax.swing.BoxLayout.X_AXIS));
        infoPanel.setOpaque(false);

        pilotBeaconDot = new JLabel("●");
        pilotBeaconDot.setFont(new Font("SansSerif", Font.BOLD, 12));
        pilotBeaconDot.setForeground(new Color(56, 189, 248));
        infoPanel.add(pilotBeaconDot);

        infoPanel.add(javax.swing.Box.createHorizontalStrut(6));

        pilotStatusLabel = new JLabel("PILOT: GUEST / OFFLINE");
        pilotStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        pilotStatusLabel.setForeground(new Color(203, 213, 225));
        infoPanel.add(pilotStatusLabel);

        capsule.add(infoPanel, BorderLayout.WEST);

        // Right button pill panel (BoxLayout ensures clean horizontal alignment)
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new javax.swing.BoxLayout(btnPanel, javax.swing.BoxLayout.X_AXIS));
        btnPanel.setOpaque(false);

        pilotLoginBtn = createPillButton("LOGIN", new Color(0, 160, 230), new Color(14, 40, 70), 58);
        pilotLoginBtn.addActionListener(e -> mainWindow.showAuthDialog("LOGIN"));
        btnPanel.add(pilotLoginBtn);

        btnPanel.add(javax.swing.Box.createHorizontalStrut(6));

        pilotRegisterBtn = createPillButton("REGISTER", new Color(16, 185, 129), new Color(12, 50, 36), 72);
        pilotRegisterBtn.addActionListener(e -> mainWindow.showAuthDialog("REGISTER"));
        btnPanel.add(pilotRegisterBtn);

        pilotLogoutBtn = createPillButton("LOGOUT", new Color(239, 68, 68), new Color(60, 18, 24), 66);
        pilotLogoutBtn.addActionListener(e -> {
            authService.logout();
            mainWindow.showAuthDialog("LOGIN");
        });
        btnPanel.add(pilotLogoutBtn);

        capsule.add(btnPanel, BorderLayout.EAST);
        return capsule;
    }

    private JButton createPillButton(String text, Color accent, Color bg, int btnWidth) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(hovered ? accent.darker() : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 10, 10));

                g2.setColor(hovered ? accent.brighter() : accent);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, 10, 10));

                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(getText())) / 2;
                int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), tx, ty);

                g2.dispose();
            }
        };

        btn.setFont(new Font("SansSerif", Font.BOLD, 10));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btnWidth, 24));
        btn.setMaximumSize(new Dimension(btnWidth, 24));
        return btn;
    }

    public void updatePilotStatus() {
        UserSession session = SessionManager.getInstance().getCurrentSession();
        if (session.isAuthenticated()) {
            pilotStatusLabel.setText("PILOT: " + session.getUsername().toUpperCase());
            pilotStatusLabel.setForeground(new Color(52, 211, 153)); // Neon Emerald
            pilotBeaconDot.setForeground(new Color(52, 211, 153));
            pilotLoginBtn.setVisible(false);
            pilotRegisterBtn.setVisible(false);
            pilotLogoutBtn.setVisible(true);
        } else {
            pilotStatusLabel.setText("PILOT: GUEST / OFFLINE");
            pilotStatusLabel.setForeground(new Color(186, 230, 253)); // Ice Blue
            pilotBeaconDot.setForeground(new Color(56, 189, 248));
            pilotLoginBtn.setVisible(true);
            pilotRegisterBtn.setVisible(true);
            pilotLogoutBtn.setVisible(false);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Deep Space Cosmic Gradient
        GradientPaint bgGradient = new GradientPaint(
                0, 0, new Color(7, 10, 20),
                0, h, new Color(13, 17, 32)
        );
        g2d.setPaint(bgGradient);
        g2d.fillRect(0, 0, w, h);

        // 2. Cosmic Nebula Radial Glow at Top Center
        Point2D center = new Point2D.Float(w / 2.0f, 90.0f);
        float radius = Math.max(300, w / 2.2f);
        float[] dist = {0.0f, 0.45f, 1.0f};
        Color[] colors = {
                new Color(124, 58, 237, 55),  // Glowing Violet core
                new Color(14, 165, 233, 25),  // Electric Cyan ring
                new Color(7, 10, 20, 0)       // Transparent fade
        };
        RadialGradientPaint nebula = new RadialGradientPaint(center, radius, dist, colors);
        g2d.setPaint(nebula);
        g2d.fillRect(0, 0, w, h);

        // 3. Subtle Cybernetic Perspective Grid (ultra-delicate texture)
        g2d.setColor(new Color(30, 48, 80, 14));
        for (int x = 0; x < w; x += 44) {
            g2d.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += 44) {
            g2d.drawLine(0, y, w, y);
        }

        // 4. Procedural Drifting Stars
        for (MenuStar s : stars) {
            float tw = (float) (0.45 + 0.55 * Math.sin(s.twinkle));
            int alpha = (int) Math.min(255, Math.max(30, 230 * tw));
            g2d.setColor(new Color(220, 240, 255, alpha));
            g2d.fillOval((int) s.x, (int) s.y, s.size, s.size);

            // Soft glow halo on brightest stars
            if (s.size > 2) {
                g2d.setColor(new Color(0, 229, 255, alpha / 3));
                g2d.drawOval((int) s.x - 1, (int) s.y - 1, s.size + 2, s.size + 2);
            }
        }

        // 5. Tech Corner Brackets on outer viewport (neatly tucked in outer frame)
        g2d.setColor(new Color(0, 229, 255, 60));
        int bLen = 14;
        int m = 10;
        // Top-left
        g2d.drawLine(m, m, m + bLen, m);
        g2d.drawLine(m, m, m, m + bLen);
        // Top-right
        g2d.drawLine(w - m, m, w - m - bLen, m);
        g2d.drawLine(w - m, m, w - m, m + bLen);
        // Bottom-left
        g2d.drawLine(m, h - m, m + bLen, h - m);
        g2d.drawLine(m, h - m, m, h - m - bLen);
        // Bottom-right
        g2d.drawLine(w - m, h - m, w - m - bLen, h - m);
        g2d.drawLine(w - m, h - m, w - m, h - m - bLen);

        // 6. HERO TITLE: AIRSTRIKE (Multi-layer Neon Bloom)
        String title = "A I R S T R I K E";
        Font titleFont = new Font("SansSerif", Font.BOLD, 46);
        g2d.setFont(titleFont);
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleX = (w - fmTitle.stringWidth(title)) / 2;
        int titleY = 78;

        // Outer cyan glow layer
        g2d.setColor(new Color(0, 229, 255, 35));
        g2d.drawString(title, titleX - 2, titleY);
        g2d.drawString(title, titleX + 2, titleY);
        g2d.drawString(title, titleX, titleY - 2);
        g2d.drawString(title, titleX, titleY + 2);

        // Mid glow layer
        g2d.setColor(new Color(0, 229, 255, 90));
        g2d.drawString(title, titleX - 1, titleY);
        g2d.drawString(title, titleX + 1, titleY);
        g2d.drawString(title, titleX, titleY - 1);
        g2d.drawString(title, titleX, titleY + 1);

        // Core brilliant white text
        g2d.setColor(new Color(248, 250, 252));
        g2d.drawString(title, titleX, titleY);

        // Decorative Wings with tech notches
        g2d.setColor(new Color(0, 229, 255, 150));
        int wingLen = 64;
        int wingY = titleY - 14;
        g2d.drawLine(titleX - 18 - wingLen, wingY, titleX - 18, wingY);
        g2d.drawLine(titleX + fmTitle.stringWidth(title) + 18, wingY,
                titleX + fmTitle.stringWidth(title) + 18 + wingLen, wingY);
        g2d.fillRect(titleX - 18 - wingLen - 3, wingY - 2, 4, 5);
        g2d.fillRect(titleX + fmTitle.stringWidth(title) + 18 + wingLen, wingY - 2, 4, 5);

        // 7. Subtitle: Tactical Space Combat & Academic Suite
        String subtitle = "TACTICAL COMBAT SIMULATOR & ACADEMIC DEMONSTRATION SYSTEM";
        g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fmSub = g2d.getFontMetrics();
        int subX = (w - fmSub.stringWidth(subtitle)) / 2;
        int subY = 104;

        g2d.setColor(new Color(148, 163, 184)); // Slate 400
        g2d.drawString(subtitle, subX, subY);

        // 8. Cloud Connection & System Badge
        String badge = "● STARFLEET CLOUD LINK ACTIVE  •  SUPABASE POSTGREST 2.2";
        g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
        FontMetrics fmBadge = g2d.getFontMetrics();
        int badgeW = fmBadge.stringWidth(badge) + 20;
        int badgeX = (w - badgeW) / 2;
        int badgeY = 114;

        // Badge pill background
        g2d.setColor(new Color(16, 24, 44, 180));
        g2d.fill(new RoundRectangle2D.Float(badgeX, badgeY, badgeW, 19, 10, 10));
        g2d.setColor(new Color(56, 189, 248, 70));
        g2d.draw(new RoundRectangle2D.Float(badgeX + 0.5f, badgeY + 0.5f, badgeW - 1f, 18, 10, 10));

        g2d.setColor(new Color(52, 211, 153)); // Active Emerald
        g2d.drawString(badge, badgeX + 10, badgeY + 13);

        // 9. Footer Module Bar
        renderFooterDeck(g2d, w, h);

        g2d.dispose();
    }

    private void renderFooterDeck(Graphics2D g2d, int w, int h) {
        int footerY = h - 24;

        // Left module indicator tags - positioned safely clear of corner brackets (x=48)
        g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2d.setColor(new Color(100, 116, 139));
        String modules = "ACADEMIC SUITE:  [PSOOP Java]  •  [PL C++17]  •  [COA 80386]  •  [CGL OpenGL]";
        g2d.drawString(modules, 48, footerY);

        // Right record display - positioned safely clear of corner brackets (recX padded by 48)
        int highScore = mainWindow.getEngine().getScoreCalculator().getHighScore();
        String recordStr = "RECORD: " + String.format("%,d", highScore) + " PTS";
        FontMetrics fm = g2d.getFontMetrics();
        int recX = w - fm.stringWidth(recordStr) - 48;

        g2d.setColor(new Color(251, 191, 36)); // Gold
        g2d.drawString(recordStr, recX, footerY);
    }

    // =========================================================================
    // Modern Aesthetic Vector Button Component
    // =========================================================================

    private static class AestheticButton extends JButton {
        private final Color accentColor;
        private final Color baseBg;
        private final boolean isHero;
        private boolean isHovered = false;

        public AestheticButton(String text, Color accentColor, Color baseBg, boolean isHero) {
            super(text);
            this.accentColor = accentColor;
            this.baseBg = baseBg;
            this.isHero = isHero;

            setFont(new Font("SansSerif", Font.BOLD, isHero ? 14 : 12));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(380, isHero ? 44 : 37));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = isHero ? 14 : 12;

            if (isHero) {
                // Hero Button: Rich Cyan-to-Azure Gradient
                Color c1 = isHovered ? new Color(0, 245, 255) : new Color(0, 215, 255);
                Color c2 = isHovered ? new Color(0, 145, 230) : new Color(0, 110, 190);
                GradientPaint heroGrad = new GradientPaint(0, 0, c1, w, 0, c2);
                g2.setPaint(heroGrad);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

                // Top specular highlight sheen
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 110), 0, h / 2f, new Color(255, 255, 255, 0)));
                g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, (h / 2f) - 1, arc, arc));

                // Glowing outer stroke
                g2.setColor(new Color(255, 255, 255, isHovered ? 200 : 120));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, arc, arc));

                // Crisp dark drop shadow + text
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(getText())) / 2;
                int ty = (h - fm.getHeight()) / 2 + fm.getAscent();

                g2.setColor(new Color(0, 30, 60, 140));
                g2.drawString(getText(), tx + 1, ty + 1);

                g2.setColor(Color.WHITE);
                g2.drawString(getText(), tx, ty);

            } else {
                // Tactical Command Buttons: Gradient glass with glowing accent tab
                if (isHovered) {
                    Color tintLeft = new Color(
                            Math.min(255, baseBg.getRed() + 30),
                            Math.min(255, baseBg.getGreen() + 30),
                            Math.min(255, baseBg.getBlue() + 45),
                            225
                    );
                    Color tintRight = new Color(17, 24, 39, 210);
                    g2.setPaint(new GradientPaint(0, 0, tintLeft, w, 0, tintRight));
                } else {
                    Color normalLeft = new Color(baseBg.getRed(), baseBg.getGreen(), baseBg.getBlue(), 160);
                    Color normalRight = new Color(13, 19, 33, 185);
                    g2.setPaint(new GradientPaint(0, 0, normalLeft, w, 0, normalRight));
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

                // 1px Border: Highlights accent on hover
                Color border = isHovered ? accentColor : new Color(45, 60, 90, 140);
                g2.setColor(border);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, arc, arc));

                // Left Glowing Accent Indicator Bar
                if (isHovered) {
                    g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 75));
                    g2.fill(new RoundRectangle2D.Float(1, 2, 6, h - 4, 4, 4));
                }
                g2.setColor(accentColor);
                g2.fill(new RoundRectangle2D.Float(2, 4, 4, h - 8, 4, 4));

                // Text
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = 22; // Left-aligned with comfortable padding
                int ty = (h - fm.getHeight()) / 2 + fm.getAscent();

                g2.setColor(isHovered ? Color.WHITE : new Color(226, 232, 240));
                g2.drawString(getText(), tx, ty);

                // Subtle chevron arrow on the right
                String arrow = "›";
                g2.setFont(new Font("SansSerif", Font.BOLD, 15));
                g2.setColor(isHovered ? accentColor : new Color(100, 116, 139));
                g2.drawString(arrow, w - 24, ty);
            }

            g2.dispose();
        }
    }

    // Star model for background animation
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
