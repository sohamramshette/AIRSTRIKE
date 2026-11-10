package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

/**
 * The player's combat spacecraft.
 * Manages multidirectional movement, keyboard input integration, weapon upgrades,
 * shield depletion, and invulnerability frames.
 */
public class Player extends Ship {

    private int lives;
    private int weaponLevel;
    private boolean moveLeft;
    private boolean moveRight;
    private boolean moveUp;
    private boolean moveDown;
    private boolean invulnerable;
    private long invulnerableEndTime;
    private double enginePulse;
    private boolean controlsInverted;
    private double damageMultiplier;

    // Weapon Overheat & Thermal Throttling System (COA-CO4 / PSOOP-CO3)
    private double weaponHeat = 0.0;
    private final double maxHeat = 100.0;
    private boolean isOverheated = false;
    private final double heatPerShot = 11.0;
    private final double heatCoolRate = 32.0;
    private final double overheatCoolRate = 25.0;

    public Player(double x, double y) {
        // Player stats: width 48, height 56, maxHealth 100, maxShield 100, speed 320, cooldown 180ms
        super(x, y, 48, 56, 100, 100, 320.0, 180);
        this.lives = 2; // Hardcore 2-vessel tactical limit
        this.weaponLevel = 1;
        this.moveLeft = false;
        this.moveRight = false;
        this.moveUp = false;
        this.moveDown = false;
        this.invulnerable = false;
        this.invulnerableEndTime = 0;
        this.enginePulse = 0.0;
        this.controlsInverted = false;
        this.damageMultiplier = 1.0;
    }

    @Override
    public void update(double delta) {
        // Kinematic vector calculation based on active inputs
        // Damage-Induced Engine Degradation (PSOOP-CO4): speed slowed down by 30% when health <= 35%
        double currentSpeed = speed;
        if (health <= maxHealth * 0.35) {
            currentSpeed *= 0.70;
        }

        vx = 0.0;
        vy = 0.0;

        if (moveLeft && !moveRight) vx = -currentSpeed;
        if (moveRight && !moveLeft) vx = currentSpeed;
        if (moveUp && !moveDown) vy = -currentSpeed;
        if (moveDown && !moveUp) vy = currentSpeed;

        if (controlsInverted) {
            vx = -vx;
            vy = -vy;
        }

        // Diagonal normalization to prevent faster speed on diagonal
        if (vx != 0.0 && vy != 0.0) {
            double invSqrt2 = 0.70710678;
            vx *= invSqrt2;
            vy *= invSqrt2;
        }

        x += vx * delta;
        y += vy * delta;

        // Weapon Heat Dissipation & Thermal Recovery (COA-CO4 / PSOOP-CO3)
        if (isOverheated) {
            weaponHeat -= overheatCoolRate * delta;
            if (weaponHeat <= 0.0) {
                weaponHeat = 0.0;
                isOverheated = false;
            }
        } else if (weaponHeat > 0.0) {
            weaponHeat = Math.max(0.0, weaponHeat - heatCoolRate * delta);
        }

        // Invulnerability timer expiration
        if (invulnerable && System.currentTimeMillis() >= invulnerableEndTime) {
            invulnerable = false;
        }

        enginePulse += delta * 15.0;
    }

    /**
     * Enforces playfield boundaries, clamping coordinates strictly within viewport bounds.
     */
    public void clampToBounds(int minX, int maxX, int minY, int maxY) {
        if (x < minX) x = minX;
        if (x + width > maxX) x = maxX - width;
        if (y < minY) y = minY;
        if (y + height > maxY) y = maxY - height;
    }

    @Override
    public void shoot(List<Projectile> projectileList) {
        if (!canShoot() || !active || isOverheated) return;

        recordShot();

        // Weapon Heat Accumulation
        weaponHeat = Math.min(maxHeat, weaponHeat + heatPerShot);
        if (weaponHeat >= maxHeat) {
            isOverheated = true;
        }

        double bulletSpeed = -560.0; // Moving upwards
        int damage = (int) Math.round((25 + (weaponLevel * 5)) * damageMultiplier);

        double centerX = getCenterX();
        double noseY = y - 4;

        switch (weaponLevel) {
            case 1 -> {
                // Single focused cannon
                projectileList.add(new Projectile(centerX - 3, noseY, 6, 16, 0, bulletSpeed, true, damage));
            }
            case 2 -> {
                // Dual wingtip cannons
                projectileList.add(new Projectile(x + 6, noseY + 8, 5, 15, 0, bulletSpeed, true, damage));
                projectileList.add(new Projectile(x + width - 11, noseY + 8, 5, 15, 0, bulletSpeed, true, damage));
            }
            default -> {
                // Triple spread cannons
                projectileList.add(new Projectile(centerX - 3, noseY, 6, 16, 0, bulletSpeed, true, damage + 5));
                projectileList.add(new Projectile(x + 4, noseY + 8, 5, 15, -110, bulletSpeed, true, damage));
                projectileList.add(new Projectile(x + width - 9, noseY + 8, 5, 15, 110, bulletSpeed, true, damage));
            }
        }
    }

    @Override
    public void takeDamage(int amount, boolean bypassShield) {
        if (invulnerable) return; // Immune during i-frames
        super.takeDamage(amount, bypassShield);
    }

    /**
     * Activates invulnerability window (e.g. after respawn or hit).
     */
    public void triggerInvulnerability(long durationMs) {
        this.invulnerable = true;
        this.invulnerableEndTime = System.currentTimeMillis() + durationMs;
    }

    /**
     * Deducts one life and resets player status.
     * @return True if player still has lives remaining; false if Game Over.
     */
    public boolean loseLifeAndRespawn(int respawnX, int respawnY) {
        lives--;
        if (lives > 0) {
            this.x = respawnX;
            this.y = respawnY;
            this.health = maxHealth;
            this.shield = maxShield;
            this.weaponHeat = 0.0;
            this.isOverheated = false;
            this.active = true;
            triggerInvulnerability(2500); // 2.5 seconds safe respawn
            return true;
        }
        active = false;
        return false;
    }

    public void upgradeWeapon() {
        if (weaponLevel < 3) {
            weaponLevel++;
        }
    }

    public void resetFull(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.lives = 2; // Hardcore 2-vessel tactical limit
        this.weaponHeat = 0.0;
        this.isOverheated = false;
        this.health = maxHealth;
        this.shield = maxShield;
        this.weaponLevel = 1;
        this.active = true;
        this.invulnerable = false;
        this.controlsInverted = false;
        this.damageMultiplier = 1.0;
        this.moveLeft = false;
        this.moveRight = false;
        this.moveUp = false;
        this.moveDown = false;
    }

    @Override
    public void render(Graphics2D g) {
        if (!active) return;

        // Blinking effect during invulnerability
        if (invulnerable && (System.currentTimeMillis() / 100) % 2 == 0) {
            return;
        }

        int px = (int) Math.round(x);
        int py = (int) Math.round(y);

        // 1. Engine thruster flame
        int flameLen = (int) (12 + 6 * Math.sin(enginePulse));
        g.setColor(new Color(255, 120, 20, 200));
        g.fillPolygon(new int[]{px + 18, px + 24, px + 30}, new int[]{py + height - 4, py + height + flameLen, py + height - 4}, 3);
        g.setColor(new Color(255, 240, 80, 230));
        g.fillPolygon(new int[]{px + 20, px + 24, px + 28}, new int[]{py + height - 4, py + height + (flameLen / 2), py + height - 4}, 3);

        // 2. Spaceship wings and hull
        Polygon hull = new Polygon();
        hull.addPoint(px + 24, py);               // Nose tip
        hull.addPoint(px + 38, py + 36);          // Right wing shoulder
        hull.addPoint(px + 48, py + 48);          // Right wing tip
        hull.addPoint(px + 36, py + 52);          // Right engine
        hull.addPoint(px + 24, py + 44);          // Center aft
        hull.addPoint(px + 12, py + 52);          // Left engine
        hull.addPoint(px, py + 48);               // Left wing tip
        hull.addPoint(px + 10, py + 36);          // Left wing shoulder

        // Dark metallic body fill
        g.setColor(new Color(30, 45, 75));
        g.fillPolygon(hull);

        // Primary hull outline
        g.setColor(new Color(0, 210, 255));
        g.setStroke(new BasicStroke(1.8f));
        g.drawPolygon(hull);

        // Wing accents
        g.setColor(new Color(0, 160, 230));
        g.drawLine(px + 10, py + 36, px + 24, py + 22);
        g.drawLine(px + 38, py + 36, px + 24, py + 22);

        // 3. Cockpit canopy
        g.setColor(new Color(150, 240, 255, 220));
        g.fillOval(px + 20, py + 18, 8, 16);
        g.setColor(Color.WHITE);
        g.drawOval(px + 20, py + 18, 8, 16);

        // 4. Energy shield bubble (if shield active)
        if (shield > 0) {
            float shieldRatio = (float) shield / maxShield;
            int alpha = (int) (40 + 70 * shieldRatio);
            g.setColor(new Color(0, 180, 255, alpha));
            g.fillOval(px - 6, py - 6, width + 12, height + 12);
            g.setColor(new Color(120, 220, 255, (int) (140 * shieldRatio)));
            g.setStroke(new BasicStroke(1.2f));
            g.drawOval(px - 6, py - 6, width + 12, height + 12);
        }
    }

    // Input state mutators
    public void setMoveLeft(boolean val) { this.moveLeft = val; }
    public void setMoveRight(boolean val) { this.moveRight = val; }
    public void setMoveUp(boolean val) { this.moveUp = val; }
    public void setMoveDown(boolean val) { this.moveDown = val; }

    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = Math.max(0, lives); }
    public int getWeaponLevel() { return weaponLevel; }
    public void setWeaponLevel(int lvl) { this.weaponLevel = Math.max(1, Math.min(3, lvl)); }
    public boolean isInvulnerable() { return invulnerable; }
    public void setInvulnerable(boolean invulnerable) { this.invulnerable = invulnerable; }
    public boolean isControlsInverted() { return controlsInverted; }
    public void setControlsInverted(boolean inverted) { this.controlsInverted = inverted; }
    public double getDamageMultiplier() { return damageMultiplier; }
    public void setDamageMultiplier(double multiplier) { this.damageMultiplier = Math.max(0.1, multiplier); }

    public double getWeaponHeat() { return weaponHeat; }
    public void setWeaponHeat(double heat) { this.weaponHeat = Math.max(0.0, Math.min(maxHeat, heat)); }
    public boolean isOverheated() { return isOverheated; }
    public void setOverheated(boolean overheated) { this.isOverheated = overheated; }
}
