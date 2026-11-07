package model;

import java.awt.Color;
import java.util.List;

/**
 * State pattern implementation for Adaptive Morphing Drone (PSOOP-CO2).
 *
 * Demonstrates: Solutions to Reduce Complexity through State Pattern.
 * Rather than dispersing complex nested conditionals across the entity update loop,
 * each state encapsulates its own kinematic behavior, offensive patterns,
 * aura presentation, and transition rules.
 */
public enum DroneAIState {

    /**
     * Standard cruising state: weaves laterally while descending at moderate pace.
     */
    PATROL {
        @Override
        public void update(MorphingEnemy drone, double delta, Player player) {
            drone.patrolUpdate(delta);
        }

        @Override
        public void attack(MorphingEnemy drone, double delta, List<Projectile> projectiles, Player player) {
            drone.patrolAttack(delta, projectiles);
        }

        @Override
        public DroneAIState evaluateTransition(MorphingEnemy drone, Player player) {
            if (drone.getHealth() < drone.getMaxHealth() * 0.40) {
                return EVADE;
            }
            if (player != null && player.isActive()) {
                double dist = Math.hypot(player.getCenterX() - drone.getCenterX(), player.getCenterY() - drone.getCenterY());
                if (dist < 260.0) {
                    return CHASE;
                }
            }
            return this;
        }

        @Override
        public Color getAuraColor() {
            return new Color(255, 150, 0); // Amber
        }
    },

    /**
     * Aggressive pursuit state: tracks player horizontal corridor and accelerates attack cadence.
     */
    CHASE {
        @Override
        public void update(MorphingEnemy drone, double delta, Player player) {
            drone.chaseUpdate(delta, player);
        }

        @Override
        public void attack(MorphingEnemy drone, double delta, List<Projectile> projectiles, Player player) {
            drone.chaseAttack(delta, projectiles, player);
        }

        @Override
        public DroneAIState evaluateTransition(MorphingEnemy drone, Player player) {
            if (drone.getHealth() < drone.getMaxHealth() * 0.35) {
                return EVADE;
            }
            if (player != null && player.isActive()) {
                double dist = Math.hypot(player.getCenterX() - drone.getCenterX(), player.getCenterY() - drone.getCenterY());
                if (dist < 160.0 && drone.canOvercharge()) {
                    return OVERCHARGE;
                }
                if (dist > 350.0) {
                    return PATROL;
                }
            } else {
                return PATROL;
            }
            return this;
        }

        @Override
        public Color getAuraColor() {
            return new Color(255, 30, 60); // Crimson
        }
    },

    /**
     * Evasive survival state: veers away from player to preserve hull integrity.
     */
    EVADE {
        @Override
        public void update(MorphingEnemy drone, double delta, Player player) {
            drone.evadeUpdate(delta, player);
        }

        @Override
        public void attack(MorphingEnemy drone, double delta, List<Projectile> projectiles, Player player) {
            drone.evadeAttack(delta, projectiles);
        }

        @Override
        public DroneAIState evaluateTransition(MorphingEnemy drone, Player player) {
            if (player != null && player.isActive()) {
                double dist = Math.hypot(player.getCenterX() - drone.getCenterX(), player.getCenterY() - drone.getCenterY());
                if (dist > 300.0 && drone.getHealth() >= drone.getMaxHealth() * 0.40) {
                    return PATROL;
                }
            }
            return this;
        }

        @Override
        public Color getAuraColor() {
            return new Color(180, 0, 255); // Violet
        }
    },

    /**
     * Overcharge state: high-speed surge and rapid micro-burst barrage before cooldown exhaustion.
     */
    OVERCHARGE {
        @Override
        public void update(MorphingEnemy drone, double delta, Player player) {
            drone.overchargeUpdate(delta);
        }

        @Override
        public void attack(MorphingEnemy drone, double delta, List<Projectile> projectiles, Player player) {
            drone.overchargeAttack(delta, projectiles);
        }

        @Override
        public DroneAIState evaluateTransition(MorphingEnemy drone, Player player) {
            if (drone.getHealth() < drone.getMaxHealth() * 0.35 || drone.isOverchargeExpired()) {
                return EVADE;
            }
            return this;
        }

        @Override
        public Color getAuraColor() {
            return new Color(255, 255, 80); // Electric Yellow
        }
    };

    public abstract void update(MorphingEnemy drone, double delta, Player player);
    public abstract void attack(MorphingEnemy drone, double delta, List<Projectile> projectiles, Player player);
    public abstract DroneAIState evaluateTransition(MorphingEnemy drone, Player player);
    public abstract Color getAuraColor();
}
