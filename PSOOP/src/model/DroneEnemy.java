// ==============================================================================
// File      : PSOOP/src/model/DroneEnemy.java
// Subject   : PSOOP-CO1 | Fast Ramming Scout Subclass
// Topics    : [TOPIC: Inheritance] [TOPIC: Polymorphism] [TOPIC: Method Overriding] [TOPIC: Kinematics]
// Game Role : Implements a fast, unshielded sine-weaving drone that attacks strictly by ramming.
// ==============================================================================
package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

/**
 * Fast, lightweight scout craft.
 */
public class DroneEnemy extends Enemy {

    private final double initialX;

    /**
     * [TOPIC: Inheritance]
     * Game Role: Spawns a rapid scout craft (HP 30, speed 200) anchored around its spawn X.
     */
    public DroneEnemy(double x, double y) {
        // width 32, height 32, health 30, shield 0, speed 200, cooldown 9999, score 100
        super(x, y, 32, 32, 30, 0, 200.0, 9999, 100);
        this.initialX = x;
        this.vy = speed;
    }

    /**
     * [TOPIC: Method Overriding] [TOPIC: Kinematics]
     * Game Role: Integrates fast downward travel combined with high-frequency sine weaving.
     */
    @Override
    public void update(double delta) {
        flightTimer += delta * 4.5;
        y += vy * delta;
        // WHY: Oscillates +/-35px around initialX so drones dodge straight player fire during descent.
        x = initialX + Math.sin(flightTimer) * 35.0;
    }

    /**
     * [TOPIC: Polymorphism] [TOPIC: Method Overriding]
     * Game Role: Intentional no-op because scouts threaten the player strictly via kinetic ramming.
     */
    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        // WHY: Drones carry no ranged weapons and rely on contact collision instead.
    }

    /** [TOPIC: Method Overriding] Game Role: No-op satisfying Ship's abstract weapon contract. */
    @Override
    public void shoot(List<Projectile> projectileList) {
        // No projectile firing
    }

    /**
     * [TOPIC: Method Overriding]
     * Game Role: Draws downward-pointing angular dart geometry and red optic sensor.
     */
    @Override
    public void render(Graphics2D g) {
        if (!active) return;

        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Angular dart shape pointing downwards
        Polygon dart = new Polygon();
        dart.addPoint(px + 16, py + 32); // Nose pointing down
        dart.addPoint(px + 32, py + 8);  // Right wing
        dart.addPoint(px + 24, py);      // Right aft
        dart.addPoint(px + 16, py + 6);  // Center engine
        dart.addPoint(px + 8, py);       // Left aft
        dart.addPoint(px, py + 8);       // Left wing

        // Red/Orange metallic body
        g.setColor(new Color(140, 25, 30));
        g.fillPolygon(dart);

        // Neon orange edge highlights
        g.setColor(new Color(255, 90, 40));
        g.setStroke(new BasicStroke(1.5f));
        g.drawPolygon(dart);

        // Glowing red optic sensor
        g.setColor(new Color(255, 40, 40));
        g.fillOval(px + 13, py + 16, 6, 6);
        g.setColor(Color.WHITE);
        g.fillOval(px + 15, py + 18, 2, 2);
    }
}

