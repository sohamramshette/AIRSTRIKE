// ==============================================================================
// File      : PSOOP/src/model/DroneAIState.java
// Subject   : PSOOP-CO2 | Enum State Pattern for Enemy AI
// Topics    : [TOPIC: FSM State Pattern] [TOPIC: Polymorphism] [TOPIC: Enum] [TOPIC: Method Overriding]
// Game Role : Encapsulates MorphingEnemy kinematics, firing, visuals, and transitions across 4 AI states.
// ==============================================================================
package model;

import java.awt.Color;
import java.util.List;

/**
 * State-pattern enum driving Adaptive Morphing Drone behaviors without nested conditionals.
 */
public enum DroneAIState {

    /** [TOPIC: FSM State Pattern] Game Role: Sweeping patrol until player enters 260px or HP < 40%. */
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
            // WHY: Survival transition takes priority over engagement when hull drops below 40%.
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

    /** [TOPIC: FSM State Pattern] Game Role: Tracks player X until overcharge range (<160px) or disengage (>350px). */
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
                // WHY: Hysteresis gap (enter <260px, exit >350px) prevents rapid PATROL/CHASE oscillation.
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

    /** [TOPIC: FSM State Pattern] Game Role: Withdraws laterally until safe distance (>300px) and HP >= 40%. */
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

    /** [TOPIC: FSM State Pattern] Game Role: Surges forward with rapid twin bursts until timer expires. */
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
            // WHY: Drops back into EVADE after overcharge burns out so the cooldown window stays safe.
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

    /** [TOPIC: Polymorphism] Game Role: Advances state-specific movement over delta seconds. */
    public abstract void update(MorphingEnemy drone, double delta, Player player);

    /** [TOPIC: Polymorphism] Game Role: Executes state-specific firing behavior. */
    public abstract void attack(MorphingEnemy drone, double delta, List<Projectile> projectiles, Player player);

    /** [TOPIC: FSM State Pattern] Game Role: Evaluates and returns the next valid AI state. */
    public abstract DroneAIState evaluateTransition(MorphingEnemy drone, Player player);

    /** Game Role: Supplies the HUD/hull tint representing the active AI state. */
    public abstract Color getAuraColor();
}
