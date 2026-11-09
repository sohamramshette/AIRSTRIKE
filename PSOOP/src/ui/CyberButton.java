package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;

/**
 * High-performance, vector-rendered tactical command button for AIRSTRIKE.
 * Implements Hero, Tactical, Danger, and Pill variants matching the Phase 9 Design System.
 */
public class CyberButton extends JButton {
    private static final long serialVersionUID = 1L;

    public enum ButtonStyle {
        HERO,
        TACTICAL,
        PILL,
        DANGER
    }

    private final ButtonStyle style;
    private final Color accentColor;
    private final Color baseBg;
    private boolean isHovered = false;
    private boolean isPressed = false;

    public CyberButton(String text, ButtonStyle style, Color accentColor, Color baseBg) {
        super(text);
        this.style = style;
        this.accentColor = accentColor != null ? accentColor : Theme.CYAN_PRIMARY;
        this.baseBg = baseBg != null ? baseBg : Theme.BG_PANEL;

        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        switch (style) {
            case HERO -> {
                setFont(Theme.BUTTON_HERO);
                setPreferredSize(new Dimension(Theme.BUTTON_WIDTH, Theme.HERO_HEIGHT));
            }
            case PILL -> {
                setFont(Theme.BADGE_FONT);
                setPreferredSize(new Dimension(72, 24));
            }
            case DANGER -> {
                setFont(Theme.BUTTON_TACTICAL);
                setPreferredSize(new Dimension(Theme.BUTTON_WIDTH, Theme.BUTTON_HEIGHT));
            }
            case TACTICAL -> {
                setFont(Theme.BUTTON_TACTICAL);
                setPreferredSize(new Dimension(Theme.BUTTON_WIDTH, Theme.BUTTON_HEIGHT));
            }
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                isPressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public static CyberButton hero(String text) {
        return new CyberButton(text, ButtonStyle.HERO, Theme.CYAN_PRIMARY, Theme.BLUE_DARK);
    }

    public static CyberButton tactical(String text, Color accent, Color bg) {
        return new CyberButton(text, ButtonStyle.TACTICAL, accent, bg);
    }

    public static CyberButton danger(String text) {
        return new CyberButton(text, ButtonStyle.DANGER, Theme.CORAL_DANGER, Theme.CORAL_DARK);
    }

    public static CyberButton pill(String text, Color accent, Color bg, int width) {
        CyberButton btn = new CyberButton(text, ButtonStyle.PILL, accent, bg);
        btn.setPreferredSize(new Dimension(width, 24));
        btn.setMaximumSize(new Dimension(width, 24));
        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.enableQuality(g2);

        int w = getWidth();
        int h = getHeight();

        switch (style) {
            case HERO -> paintHero(g2, w, h);
            case PILL -> paintPill(g2, w, h);
            case DANGER -> paintDanger(g2, w, h);
            case TACTICAL -> paintTactical(g2, w, h);
        }

        g2.dispose();
    }

    private void paintHero(Graphics2D g2, int w, int h) {
        int arc = Theme.RADIUS_HERO;
        Color c1 = isHovered ? new Color(0, 245, 255) : new Color(0, 215, 255);
        Color c2 = isHovered ? new Color(0, 155, 240) : new Color(0, 110, 190);
        if (isPressed) {
            c1 = c1.darker();
            c2 = c2.darker();
        }

        // Vibrant gradient fill
        g2.setPaint(new GradientPaint(0, 0, c1, w, 0, c2));
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        // Specular top highlight
        g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 110), 0, h / 2f, new Color(255, 255, 255, 0)));
        g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, (h / 2f) - 1, arc, arc));

        // Glowing outer stroke
        g2.setColor(new Color(255, 255, 255, isHovered ? 210 : 130));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, arc, arc));

        // Text with drop shadow
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(getText())) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();

        g2.setColor(new Color(0, 30, 60, 150));
        g2.drawString(getText(), tx + 1, ty + 1);

        g2.setColor(Color.WHITE);
        g2.drawString(getText(), tx, ty);
    }

    private void paintTactical(Graphics2D g2, int w, int h) {
        int arc = Theme.RADIUS_BUTTON;

        // Background Glass
        if (isHovered) {
            Color tintLeft = new Color(
                    Math.min(255, baseBg.getRed() + 28),
                    Math.min(255, baseBg.getGreen() + 28),
                    Math.min(255, baseBg.getBlue() + 42),
                    230
            );
            Color tintRight = new Color(17, 24, 39, 215);
            g2.setPaint(new GradientPaint(0, 0, tintLeft, w, 0, tintRight));
        } else {
            Color normalLeft = new Color(baseBg.getRed(), baseBg.getGreen(), baseBg.getBlue(), 170);
            Color normalRight = new Color(13, 19, 33, 195);
            g2.setPaint(new GradientPaint(0, 0, normalLeft, w, 0, normalRight));
        }
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        // 1px Border highlighting on hover
        Color border = isHovered ? accentColor : new Color(45, 60, 90, 140);
        g2.setColor(border);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, arc, arc));

        // Left Glowing Accent Indicator Bar (4px)
        if (isHovered) {
            g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 80));
            g2.fill(new RoundRectangle2D.Float(1, 2, 6, h - 4, 4, 4));
        }
        g2.setColor(accentColor);
        g2.fill(new RoundRectangle2D.Float(2, 4, 4, h - 8, 4, 4));

        // Text
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int tx = 24;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();

        g2.setColor(isHovered ? Theme.TEXT_WHITE : new Color(226, 232, 240));
        g2.drawString(getText(), tx, ty);

        // Right Chevron
        String arrow = "›";
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.setColor(isHovered ? accentColor : new Color(100, 116, 139));
        g2.drawString(arrow, w - 24, ty);
    }

    private void paintDanger(Graphics2D g2, int w, int h) {
        int arc = Theme.RADIUS_BUTTON;
        Color tintLeft = isHovered ? new Color(85, 20, 26, 230) : new Color(50, 18, 22, 180);
        Color tintRight = new Color(17, 24, 39, 200);
        g2.setPaint(new GradientPaint(0, 0, tintLeft, w, 0, tintRight));
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        Color border = isHovered ? Theme.CORAL_DANGER : new Color(90, 35, 45, 140);
        g2.setColor(border);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, arc, arc));

        // Left indicator bar
        g2.setColor(Theme.CORAL_DANGER);
        g2.fill(new RoundRectangle2D.Float(2, 4, 4, h - 8, 4, 4));

        // Text
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int tx = 24;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(isHovered ? Color.WHITE : new Color(248, 180, 180));
        g2.drawString(getText(), tx, ty);

        // Right chevron
        String arrow = "›";
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.setColor(isHovered ? Theme.CORAL_DANGER : new Color(140, 70, 75));
        g2.drawString(arrow, w - 24, ty);
    }

    private void paintPill(Graphics2D g2, int w, int h) {
        int arc = Theme.RADIUS_PILL;
        g2.setColor(isHovered ? accentColor.darker() : baseBg);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        g2.setColor(isHovered ? accentColor.brighter() : accentColor);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, arc, arc));

        g2.setColor(Color.WHITE);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(getText())) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), tx, ty);
    }
}
