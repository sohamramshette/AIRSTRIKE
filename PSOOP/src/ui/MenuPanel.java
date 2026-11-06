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
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * Main menu interface displaying game branding, pilot identity status,
 * mission launch, settings, and Supabase authentication access.
 */
public class MenuPanel extends JPanel {

    private final MainWindow mainWindow;
    private final AuthService authService;

    private JLabel pilotStatusLabel;
    private JButton pilotAuthBtn;

    public MenuPanel(MainWindow mainWindow) {
        this(mainWindow, new AuthService());
    }

    public MenuPanel(MainWindow mainWindow, AuthService authService) {
        this.mainWindow = mainWindow;
        this.authService = authService;

        setPreferredSize(new Dimension(800, 650));
        setBackground(new Color(10, 12, 22));
        setLayout(new GridBagLayout());

        buildUI();

        // Listen for authentication changes from SessionManager
        SessionManager.getInstance().addListener((oldSession, newSession) -> {
            SwingUtilities.invokeLater(this::updatePilotStatus);
        });

        updatePilotStatus();
    }

    private void buildUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Pilot Status Bar (positioned below title)
        gbc.gridy = 0;
        gbc.insets = new Insets(165, 12, 12, 12);
        JPanel pilotBar = buildPilotStatusBar();
        add(pilotBar, gbc);

        // 2. Play Button
        gbc.gridy = 1;
        gbc.insets = new Insets(8, 12, 8, 12);
        JButton playBtn = createStyledButton("LAUNCH MISSION", new Color(0, 180, 255));
        playBtn.addActionListener(e -> mainWindow.startGame());
        add(playBtn, gbc);

        // 3. Pilot Registry / Account Button
        gbc.gridy = 2;
        JButton registryBtn = createStyledButton("STARFLEET PILOT REGISTRY", new Color(130, 90, 240));
        registryBtn.addActionListener(e -> mainWindow.showAuthDialog());
        add(registryBtn, gbc);

        // 4. Instructions Button
        gbc.gridy = 3;
        JButton instrBtn = createStyledButton("PILOT MANUAL (CONTROLS)", new Color(60, 220, 120));
        instrBtn.addActionListener(e -> mainWindow.showInstructions());
        add(instrBtn, gbc);

        // 5. Settings Button
        gbc.gridy = 4;
        JButton settingsBtn = createStyledButton("SYSTEM SETTINGS", new Color(255, 170, 40));
        settingsBtn.addActionListener(e -> mainWindow.showSettings());
        add(settingsBtn, gbc);

        // 6. Exit Button
        gbc.gridy = 5;
        JButton exitBtn = createStyledButton("ABORT TO DESKTOP", new Color(240, 60, 60));
        exitBtn.addActionListener(e -> System.exit(0));
        add(exitBtn, gbc);
    }

    private JPanel buildPilotStatusBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(new Color(18, 24, 40));
        bar.setPreferredSize(new Dimension(320, 38));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 65, 110), 1),
                BorderFactory.createEmptyBorder(4, 12, 4, 8)
        ));

        pilotStatusLabel = new JLabel("PILOT: GUEST / OFFLINE", SwingConstants.LEFT);
        pilotStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        pilotStatusLabel.setForeground(new Color(160, 190, 230));
        bar.add(pilotStatusLabel, BorderLayout.CENTER);

        pilotAuthBtn = new JButton("LOGIN");
        pilotAuthBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        pilotAuthBtn.setForeground(Color.WHITE);
        pilotAuthBtn.setBackground(new Color(35, 55, 95));
        pilotAuthBtn.setFocusPainted(false);
        pilotAuthBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pilotAuthBtn.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        pilotAuthBtn.addActionListener(e -> handlePilotAuthAction());
        bar.add(pilotAuthBtn, BorderLayout.EAST);

        return bar;
    }

    private void handlePilotAuthAction() {
        if (SessionManager.getInstance().isLoggedIn()) {
            authService.logout();
        } else {
            mainWindow.showAuthDialog();
        }
    }

    public void updatePilotStatus() {
        UserSession session = SessionManager.getInstance().getCurrentSession();
        if (session.isAuthenticated()) {
            pilotStatusLabel.setText("PILOT: " + session.getUsername().toUpperCase());
            pilotStatusLabel.setForeground(new Color(80, 250, 120)); // Active Green
            pilotAuthBtn.setText("LOGOUT");
            pilotAuthBtn.setBackground(new Color(110, 35, 45));
        } else {
            pilotStatusLabel.setText("PILOT: GUEST / OFFLINE");
            pilotStatusLabel.setForeground(new Color(160, 190, 230)); // Cool Blue
            pilotAuthBtn.setText("LOGIN");
            pilotAuthBtn.setBackground(new Color(35, 55, 95));
        }
    }

    private JButton createStyledButton(String text, Color accentColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(25, 32, 50));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(320, 40));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));

        // Hover highlighting
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(accentColor.darker());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(25, 32, 50));
            }
        });

        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Deep space background gradient
        g2d.setColor(new Color(8, 10, 20));
        g2d.fillRect(0, 0, w, h);

        // Grid lines aesthetic
        g2d.setColor(new Color(20, 30, 50, 40));
        for (int x = 0; x < w; x += 40) g2d.drawLine(x, 0, x, h);
        for (int y = 0; y < h; y += 40) g2d.drawLine(0, y, w, y);

        // Title: AIRSTRIKE
        g2d.setColor(new Color(0, 210, 255));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 54));
        FontMetrics fm1 = g2d.getFontMetrics();
        String title = "AIRSTRIKE";
        int titleX = (w - fm1.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, 90);

        // Subtitle
        g2d.setColor(new Color(180, 210, 240));
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 15));
        FontMetrics fm2 = g2d.getFontMetrics();
        String subtitle = "2D Space Shooter & Academic Demonstration System";
        int subX = (w - fm2.stringWidth(subtitle)) / 2;
        g2d.drawString(subtitle, subX, 120);

        // Version badge
        g2d.setColor(new Color(100, 140, 180));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm3 = g2d.getFontMetrics();
        String ver = "v2.1.0 (Phase 6.2A Supabase Auth Edition)";
        g2d.drawString(ver, (w - fm3.stringWidth(ver)) / 2, 142);
    }
}
