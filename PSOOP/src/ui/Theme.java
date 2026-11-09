package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

/**
 * Centralized Design System tokens and rendering utilities for AIRSTRIKE.
 * Serves as the single source of truth for Java Swing and matches web CSS tokens.
 */
public final class Theme {

    private Theme() {}

    // =========================================================================
    // 1. Color Palette
    // =========================================================================
    public static final Color BG_VOID         = new Color(2, 4, 9);
    public static final Color BG_DEEP         = new Color(7, 10, 20);
    public static final Color BG_PANEL        = new Color(15, 23, 42);
    public static final Color BG_CARD         = new Color(13, 19, 33, 235);
    public static final Color BG_CARD_OPAQUE  = new Color(13, 19, 33);
    public static final Color BG_GLASS        = new Color(17, 24, 39, 195);
    public static final Color BG_INPUT        = new Color(22, 31, 56);

    // Primary & Accent Colors
    public static final Color CYAN_PRIMARY    = new Color(0, 229, 255);
    public static final Color CYAN_GLOW       = new Color(0, 229, 255, 90);
    public static final Color CYAN_FAINT      = new Color(0, 229, 255, 30);
    public static final Color BLUE_ACCENT     = new Color(56, 189, 248);
    public static final Color BLUE_DARK       = new Color(0, 119, 182);

    // Specialty Accents
    public static final Color PURPLE_ACCENT   = new Color(168, 85, 247);
    public static final Color PURPLE_DARK     = new Color(36, 18, 48);
    public static final Color GOLD_ACCENT     = new Color(251, 191, 36);
    public static final Color GOLD_DARK       = new Color(40, 32, 16);
    public static final Color EMERALD_STATUS  = new Color(52, 211, 153);
    public static final Color EMERALD_DARK    = new Color(12, 50, 36);
    public static final Color CORAL_DANGER    = new Color(239, 68, 68);
    public static final Color CORAL_DARK      = new Color(48, 18, 22);

    // Typography Colors
    public static final Color TEXT_WHITE      = new Color(248, 250, 252);
    public static final Color TEXT_MUTED      = new Color(148, 163, 184);
    public static final Color TEXT_DIM        = new Color(100, 116, 139);

    // Border Colors
    public static final Color BORDER_SUBTLE   = new Color(30, 41, 59, 200);
    public static final Color BORDER_CYAN     = new Color(0, 229, 255, 95);
    public static final Color BORDER_EMERALD  = new Color(52, 211, 153, 95);

    // =========================================================================
    // 2. Typography
    // =========================================================================
    public static final Font TITLE_HERO       = new Font("SansSerif", Font.BOLD, 44);
    public static final Font HEADER_1         = new Font("SansSerif", Font.BOLD, 18);
    public static final Font HEADER_2         = new Font("SansSerif", Font.BOLD, 14);
    public static final Font BODY             = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font BODY_BOLD        = new Font("SansSerif", Font.BOLD, 13);
    public static final Font BUTTON_HERO      = new Font("SansSerif", Font.BOLD, 14);
    public static final Font BUTTON_TACTICAL  = new Font("SansSerif", Font.BOLD, 12);
    public static final Font BADGE_FONT       = new Font("SansSerif", Font.BOLD, 10);
    public static final Font MONO_FONT        = new Font("Monospaced", Font.BOLD, 12);
    public static final Font MONO_SMALL       = new Font("Monospaced", Font.PLAIN, 11);

    // =========================================================================
    // 3. Dimensions & Radii
    // =========================================================================
    public static final int RADIUS_CARD   = 14;
    public static final int RADIUS_BUTTON = 12;
    public static final int RADIUS_HERO   = 14;
    public static final int RADIUS_PILL   = 10;
    public static final int BUTTON_WIDTH  = 380;
    public static final int BUTTON_HEIGHT = 38;
    public static final int HERO_HEIGHT   = 44;

    // =========================================================================
    // 4. Common Borders & Decorations
    // =========================================================================
    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CYAN, 1, true),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        );
    }

    public static Border createFieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 56, 90), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        );
    }

    // =========================================================================
    // 5. Drawing Helpers (Graphics2D)
    // =========================================================================

    public static void enableQuality(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /**
     * Paints cosmic deep space gradient with subtle top nebula glow.
     */
    public static void renderCosmicBackground(Graphics2D g2, int w, int h) {
        // Base cosmic gradient
        GradientPaint bgGradient = new GradientPaint(
                0, 0, BG_DEEP,
                0, h, new Color(13, 17, 32)
        );
        g2.setPaint(bgGradient);
        g2.fillRect(0, 0, w, h);

        // Violet and cyan nebula core at top
        Point2D center = new Point2D.Float(w / 2.0f, 90.0f);
        float radius = Math.max(320, w / 2.2f);
        float[] dist = {0.0f, 0.45f, 1.0f};
        Color[] colors = {
                new Color(124, 58, 237, 50),
                new Color(14, 165, 233, 22),
                new Color(7, 10, 20, 0)
        };
        RadialGradientPaint nebula = new RadialGradientPaint(center, radius, dist, colors);
        g2.setPaint(nebula);
        g2.fillRect(0, 0, w, h);

        // Subtle cybernetic grid lines
        g2.setColor(new Color(30, 48, 80, 12));
        for (int x = 0; x < w; x += 44) g2.drawLine(x, 0, x, h);
        for (int y = 0; y < h; y += 44) g2.drawLine(0, y, w, y);

        // Tactical Corner Brackets
        renderCornerBrackets(g2, w, h, 14, 10, new Color(0, 229, 255, 60));
    }

    /**
     * Renders high-tech corner brackets on a rectangular area.
     */
    public static void renderCornerBrackets(Graphics2D g2, int w, int h, int bLen, int m, Color color) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.2f));
        // Top-left
        g2.drawLine(m, m, m + bLen, m);
        g2.drawLine(m, m, m, m + bLen);
        // Top-right
        g2.drawLine(w - m, m, w - m - bLen, m);
        g2.drawLine(w - m, m, w - m, m + bLen);
        // Bottom-left
        g2.drawLine(m, h - m, m + bLen, h - m);
        g2.drawLine(m, h - m, m, h - m - bLen);
        // Bottom-right
        g2.drawLine(w - m, h - m, w - m - bLen, h - m);
        g2.drawLine(w - m, h - m, w - m, h - m - bLen);
    }

    /**
     * Renders a multi-pass glowing text string centered horizontally.
     */
    public static void drawGlowTextCentered(Graphics2D g2, String text, int centerX, int y, Font font, Color glow, Color core) {
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int tx = centerX - (fm.stringWidth(text) / 2);

        // Faint outer bloom
        g2.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), 35));
        g2.drawString(text, tx - 2, y);
        g2.drawString(text, tx + 2, y);
        g2.drawString(text, tx, y - 2);
        g2.drawString(text, tx, y + 2);

        // Mid glow
        g2.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), 85));
        g2.drawString(text, tx - 1, y);
        g2.drawString(text, tx + 1, y);
        g2.drawString(text, tx, y - 1);
        g2.drawString(text, tx, y + 1);

        // Brilliant core
        g2.setColor(core);
        g2.drawString(text, tx, y);
    }
}
