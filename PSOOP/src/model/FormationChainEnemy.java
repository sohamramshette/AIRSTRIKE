// ==============================================================================
// File      : PSOOP/src/model/FormationChainEnemy.java
// Subject   : PSOOP-CO1 / PL-CO2 | Doubly-Linked Squadron Chain Node
// Topics    : [TOPIC: Inheritance] [TOPIC: Method Overriding] [TOPIC: Doubly Linked List] [TOPIC: Kinematics]
// Game Role : Forms a linked convoy whose interior nodes splice in O(1) time upon destruction.
// ==============================================================================
package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Formation Squadron Chain node linking adjacent enemies via prev/next references.
 */
public class FormationChainEnemy extends Enemy {

    private FormationChainEnemy prev;
    private FormationChainEnemy next;
    private final int segmentIndex;

    /**
     * [TOPIC: Inheritance]
     * Game Role: Spawns one indexed node of a doubly-linked squadron formation.
     */
    public FormationChainEnemy(double x, double y, int segmentIndex) {
        // x, y, width 38, height 36, maxHealth 70, maxShield 15, speed 100.0, shootCooldown 1500ms, scoreValue 340
        super(x, y, 38, 36, 70, 15, 100.0, 1500, 340);
        this.segmentIndex = segmentIndex;
        this.prev = null;
        this.next = null;
        this.vy = speed * 0.8;
    }

    public void setPrev(FormationChainEnemy prev) { this.prev = prev; }
    public void setNext(FormationChainEnemy next) { this.next = next; }
    public FormationChainEnemy getPrev() { return prev; }
    public FormationChainEnemy getNext() { return next; }
    public int getSegmentIndex() { return segmentIndex; }

    /**
     * [TOPIC: Doubly Linked List]
     * Game Role: Unlinks this destroyed segment from neighbors in O(1) time without shifting.
     */
    public void splice() {
        // WHY: Direct O(1) pointer bridge splits/relinks the chain without rebuilding the squadron list.
        if (prev != null) {
            prev.setNext(next);
        }
        if (next != null) {
            next.setPrev(prev);
        }
        // WHY: Clears own references so destroyed nodes cannot retain stale neighbor pointers.
        this.prev = null;
        this.next = null;
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Doubly Linked List]
     * Game Role: Relays damage to Ship and splices the node out when health reaches zero.
     */
    @Override
    public void takeDamage(int amount, boolean bypassShield) {
        super.takeDamage(amount, bypassShield);
        if (health == 0) {
            splice();
        }
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Kinematics]
     * Game Role: Integrates downward motion with a segment-indexed phase offset for snake undulation.
     */
    @Override
    public void update(double delta) {
        flightTimer += delta * 2.0;
        y += vy * delta;
        // WHY: Phase offset proportional to segmentIndex creates a trailing serpentine wave across nodes.
        double offset = segmentIndex * 0.45;
        x += Math.sin(flightTimer + offset) * 35.0 * delta;
    }

    /** [TOPIC: Polymorphism] [TOPIC: Method Overriding] Game Role: Fires within active vertical combat zone. */
    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        attackTimer += delta;
        if (canShoot() && y > 25 && y < 450) {
            shoot(projectileList);
        }
    }

    /** [TOPIC: Method Overriding] Game Role: Fires a single downward bolt from node center. */
    @Override
    public void shoot(List<Projectile> projectileList) {
        recordShot();
        double bulletSpeed = 290.0;
        int damage = 12;
        projectileList.add(new Projectile(x + width / 2.0 - 3, y + height, 6, 12, 0, bulletSpeed, false, damage));
    }

    /** [TOPIC: Method Overriding] Game Role: Draws tractor beam links to next node, chevron hull, and node ID. */
    @Override
    public void render(Graphics2D g) {
        if (!active) return;
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Draw tractor beam links to adjacent doubly-linked nodes
        if (next != null && next.isActive()) {
            g.setColor(new Color(0, 190, 255, 160));
            g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{4, 4}, 0));
            g.drawLine(px + width / 2, py + height / 2, (int) Math.round(next.getX() + next.getWidth() / 2.0), (int) Math.round(next.getY() + next.getHeight() / 2.0));
        }

        // Segment hull (Diamond / Chevron)
        int[] xs = { px + width / 2, px + width - 4, px + width / 2, px + 4 };
        int[] ys = { py + 2, py + height / 2, py + height - 2, py + height / 2 };
        g.setColor(new Color(20, 40, 70));
        g.fillPolygon(xs, ys, 4);
        g.setColor(new Color(0, 210, 255));
        g.setStroke(new BasicStroke(1.5f));
        g.drawPolygon(xs, ys, 4);

        // Node ID label
        g.setColor(Color.WHITE);
        g.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 9));
        g.drawString("N" + segmentIndex, px + width / 2 - 6, py + height / 2 + 3);

        renderVitals(g, px, py);
    }

    private void renderVitals(Graphics2D g, int px, int py) {
        int barW = width;
        int barH = 3;
        int barY = py - 6;
        g.setColor(new Color(30, 30, 30, 180));
        g.fillRect(px, barY, barW, barH);
        float hpRatio = (float) health / maxHealth;
        g.setColor(new Color(0, 200, 255));
        g.fillRect(px, barY, (int) (barW * hpRatio), barH);
    }
}
