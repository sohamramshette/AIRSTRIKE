package ui;

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
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Main menu interface displaying game branding, mission launch, settings, and controls navigation.
 */
public class MenuPanel extends JPanel {

    private final MainWindow mainWindow;

    public MenuPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setPreferredSize(new Dimension(800, 650));
        setBackground(new Color(10, 12, 22));
        setLayout(new GridBagLayout());

        buildUI();
    }

    private void buildUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Spacer below title
        gbc.gridy = 1;
        gbc.insets = new Insets(140, 12, 12, 12); // Pushes buttons below custom painted title

        // 1. Play Button
        JButton playBtn = createStyledButton("LAUNCH MISSION", new Color(0, 180, 255));
        playBtn.addActionListener(e -> mainWindow.startGame());
        add(playBtn, gbc);

        // 2. Instructions Button
        gbc.gridy = 2;
        gbc.insets = new Insets(8, 12, 8, 12);
        JButton instrBtn = createStyledButton("PILOT MANUAL (CONTROLS)", new Color(60, 220, 120));
        instrBtn.addActionListener(e -> mainWindow.showInstructions());
        add(instrBtn, gbc);

        // 3. Settings Button
        gbc.gridy = 3;
        JButton settingsBtn = createStyledButton("SYSTEM SETTINGS", new Color(255, 170, 40));
        settingsBtn.addActionListener(e -> mainWindow.showSettings());
        add(settingsBtn, gbc);

        // 4. Exit Button
        gbc.gridy = 4;
        JButton exitBtn = createStyledButton("ABORT TO DESKTOP", new Color(240, 60, 60));
        exitBtn.addActionListener(e -> System.exit(0));
        add(exitBtn, gbc);
    }

    private JButton createStyledButton(String text, Color accentColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(25, 32, 50));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(280, 44));
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
        g2d.drawString(title, titleX, 100);

        // Subtitle
        g2d.setColor(new Color(180, 210, 240));
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 15));
        FontMetrics fm2 = g2d.getFontMetrics();
        String subtitle = "2D Space Shooter & Academic Demonstration System";
        int subX = (w - fm2.stringWidth(subtitle)) / 2;
        g2d.drawString(subtitle, subX, 130);

        // Version badge
        g2d.setColor(new Color(100, 140, 180));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm3 = g2d.getFontMetrics();
        String ver = "v2.0.0 (Phase 2 Java Swing/AWT Edition)";
        g2d.drawString(ver, (w - fm3.stringWidth(ver)) / 2, 152);
    }
}

