package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JPanel;

/**
 * Reusable glassmorphic card container with futuristic starfleet border styling.
 */
public class CyberCard extends JPanel {

    private final Color accentColor;
    private final int radius;
    private final boolean showTopSheen;

    public CyberCard() {
        this(null, Theme.RADIUS_CARD, true);
    }

    public CyberCard(Color accentColor) {
        this(accentColor, Theme.RADIUS_CARD, true);
    }

    public CyberCard(Color accentColor, int radius, boolean showTopSheen) {
        super();
        this.accentColor = accentColor != null ? accentColor : Theme.CYAN_PRIMARY;
        this.radius = radius;
        this.showTopSheen = showTopSheen;
        setOpaque(false);
    }

    public CyberCard(LayoutManager layout) {
        this(layout, null, Theme.RADIUS_CARD, true);
    }

    public CyberCard(LayoutManager layout, Color accentColor) {
        this(layout, accentColor, Theme.RADIUS_CARD, true);
    }

    public CyberCard(LayoutManager layout, Color accentColor, int radius, boolean showTopSheen) {
        super(layout);
        this.accentColor = accentColor != null ? accentColor : Theme.CYAN_PRIMARY;
        this.radius = radius;
        this.showTopSheen = showTopSheen;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.enableQuality(g2);

        int w = getWidth();
        int h = getHeight();

        // 1. Dark Glassmorphic background
        g2.setColor(Theme.BG_CARD);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, radius, radius));

        // 2. Subtle 1px boundary stroke
        g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 75));
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, radius, radius));

        // 3. Top Glowing Hairline Accent
        if (showTopSheen && w > 40) {
            int sheenW = Math.max(40, (int) (w * 0.7));
            int sx = (w - sheenW) / 2;
            GradientPaint sheen = new GradientPaint(
                    sx, 1f, new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 30),
                    sx + sheenW, 1f, accentColor
            );
            g2.setPaint(sheen);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawLine(sx, 1, sx + sheenW, 1);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
