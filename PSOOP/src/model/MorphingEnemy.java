package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

/**
 * Adaptive Morphing Drone (PSOOP-CO2).
 * Demonstrates: Solutions to Reduce Complexity via State Pattern.
 *
 * Implements a 4-state Finite State Machine:
 * - PATROL: Standard sweeping cruise.
 * - CHASE: Active tracking towards player horizontal plane.
 * - EVADE: High-speed evasive withdrawal.
 * - OVERCHARGE: Pulsing temporary frenzy barrage.
 */
public class MorphingEnemy extends Enemy {

    private DroneAIState currentState;
    private double stateTimer;
    private double overchargeTimer;
    private static final double OVERCHARGE_DURATION = 1.8;
    private double overchargeCooldownTimer;

    // Cache latest player reference for decoupled state update
    private Player targetedPlayer;

    public MorphingEnemy(double x, double y) {
        // x, y, width 36, height 36, maxHealth 50, maxShield 15, speed 120.0, shootCooldown 1300ms, scoreValue 300
        super(x, y, 36, 36, 50, 15, 120.0, 1300, 300);
        this.currentState = DroneAIState.PATROL;
        this.stateTimer = 0.0;
        this.overchargeTimer = 0.0;
        this.overchargeCooldownTimer = 0.0;
        this.vy = 90.0;
    }

    @Override
    public void update(double delta) {
        stateTimer += delta;
        if (overchargeCooldownTimer > 0) {
            overchargeCooldownTimer -= delta;
        }

        // Delegate kinematics to current state object
        currentState.update(this, delta, targetedPlayer);

        // Evaluate state transitions in a decoupled, structured manner
        DroneAIState nextState = currentState.evaluateTransition(this, targetedPlayer);
        if (nextState != currentState) {
            transitionTo(nextState);
        }
    }

    @Override
    public void performAttack(double delta, List<Projectile> projectileList, Player player) {
        this.targetedPlayer = player;
        attackTimer += delta;

        // Delegate attack logic to current state object
        currentState.attack(this, delta, projectileList, player);
    }

    private void transitionTo(DroneAIState nextState) {
        this.currentState = nextState;
        this.stateTimer = 0.0;
        if (nextState == DroneAIState.OVERCHARGE) {
            this.overchargeTimer = OVERCHARGE_DURATION;
        }
    }

    // --- State-Specific Kinematic Callbacks ---

    public void patrolUpdate(double delta) {
        flightTimer += delta * 2.2;
        vy = 95.0;
        x += Math.cos(flightTimer) * 45.0 * delta;
        y += vy * delta;
    }

    public void chaseUpdate(double delta, Player player) {
        vy = 125.0;
        if (player != null) {
            double targetX = player.getCenterX() - (width / 2.0);
            double dx = targetX - x;
            x += Math.signum(dx) * Math.min(Math.abs(dx), 110.0 * delta);
        }
        y += vy * delta;
    }

    public void evadeUpdate(double delta, Player player) {
        vy = 65.0;
        if (player != null) {
            double targetX = player.getCenterX() - (width / 2.0);
            double dx = targetX - x;
            // Veer away from player
            x -= Math.signum(dx) * 130.0 * delta;
        }
        y += vy * delta;
    }

    public void overchargeUpdate(double delta) {
        overchargeTimer -= delta;
        vy = 180.0;
        y += vy * delta;
    }

    // --- State-Specific Attack Callbacks ---

    public void patrolAttack(double delta, List<Projectile> projectileList) {
        if (canShoot() && y > 20 && y < 450) {
            recordShot();
            projectileList.add(new Projectile(x + 16, y + height, 5, 14, 0, 310.0, false, 14));
        }
    }

    public void chaseAttack(double delta, List<Projectile> projectileList, Player player) {
        if (canShoot() && y > 20 && y < 460) {
            recordShot();
            double targetX = (player != null) ? player.getCenterX() : x + 18;
            double dx = targetX - (x + 18);
            double dy = ((player != null) ? player.getCenterY() : y + 300) - (y + height);
            double dist = Math.hypot(dx, dy);
            double vx = (dist > 0.001) ? (dx / dist) * 330.0 : 0.0;
            double vyProj = (dist > 0.001) ? (dy / dist) * 330.0 : 330.0;

            projectileList.add(new Projectile(x + 15, y + height, 6, 14, vx, vyProj, false, 15));
        }
    }

    public void evadeAttack(double delta, List<Projectile> projectileList) {
        // Reduced frequency during evasion
        if (canShoot() && y > 20 && y < 450 && attackTimer > 1.6) {
            recordShot();
            projectileList.add(new Projectile(x + 16, y + height, 5, 12, 0, 270.0, false, 10));
        }
    }

    public void overchargeAttack(double delta, List<Projectile> projectileList) {
        // High frequency firing during overcharge
        if (attackTimer >= 0.35 && y > 20 && y < 500) {
            attackTimer = 0.0;
            // Twin rapid burst
            projectileList.add(new Projectile(x + 6, y + height, 5, 12, -40.0, 370.0, false, 12));
            projectileList.add(new Projectile(x + width - 10, y + height, 5, 12, 40.0, 370.0, false, 12));
        }
    }

    @Override
    public void shoot(List<Projectile> projectileList) {
        projectileList.add(new Projectile(x + 16, y + height, 5, 14, 0, 300.0, false, 14));
    }

    @Override
    public void render(Graphics2D g) {
        if (!active) return;

        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // Dynamic geometric hexagon diamond
        Polygon morphHull = new Polygon();
        morphHull.addPoint(px + 18, py + 36); // Bottom tip
        morphHull.addPoint(px + 36, py + 22); // Right chine
        morphHull.addPoint(px + 30, py + 4);  // Right shoulder
        morphHull.addPoint(px + 18, py);      // Top tip
        morphHull.addPoint(px + 6, py + 4);   // Left shoulder
        morphHull.addPoint(px, py + 22);      // Left chine

        // Hull base
        g.setColor(new Color(30, 30, 45));
        g.fillPolygon(morphHull);

        // Outline colored by active state
        Color auraColor = currentState.getAuraColor();
        g.setColor(auraColor);
        g.setStroke(new BasicStroke(1.8f));
        g.drawPolygon(morphHull);

        // Core pulsating state node
        g.setColor(auraColor);
        g.fillOval(px + 13, py + 13, 10, 10);
        g.setColor(Color.WHITE);
        g.fillOval(px + 16, py + 16, 4, 4);

        // Shield aura
        if (shield > 0) {
            g.setColor(new Color(auraColor.getRed(), auraColor.getGreen(), auraColor.getBlue(), 75));
            g.drawOval(px - 3, py - 3, width + 6, height + 6);
        }
    }

    public boolean canOvercharge() {
        return overchargeCooldownTimer <= 0.0;
    }

    public boolean isOverchargeExpired() {
        if (overchargeTimer <= 0.0) {
            overchargeCooldownTimer = 8.0; // 8 second cooldown before next overcharge
            return true;
        }
        return false;
    }

    public DroneAIState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(DroneAIState state) {
        transitionTo(state);
    }
}
