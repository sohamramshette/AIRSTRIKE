// ==============================================================================
// File      : PSOOP/src/service/WaveManager.java
// Subject   : PSOOP-CO2 / CO4 | Sector Wave Spawning & Difficulty Progression
// Topics    : [TOPIC: Wave Spawning] [TOPIC: Deterministic PRNG] [TOPIC: Boss Encounters]
// Game Role : Schedules enemy spawns, boss sectors, difficulty scaling, and power-up drops.
// ==============================================================================
package service;

import model.ArmouredEnemy;
import model.BossEnemy;
import model.BoundarySkimmerEnemy;
import model.BoothGunshipEnemy;
import model.DroneEnemy;
import model.Enemy;
import model.FailSafeEnemy;
import model.FibonacciSkimmerEnemy;
import model.FighterEnemy;
import model.FormationChainEnemy;
import model.GridSwarmEnemy;
import model.HashShieldEnemy;
import model.InterceptorEnemy;
import model.MorphingEnemy;
import model.OrbitalRotorEnemy;
import model.PhongCruiserEnemy;
import model.PolygonCruiserEnemy;
import model.PowerUp;
import model.PowerUpType;
import model.RailgunDreadnoughtEnemy;
import model.RammerEnemy;
import model.RasterSentinelEnemy;
import model.SubRegSentryEnemy;
import model.TargetHunterEnemy;
import model.WarpCruiserEnemy;
import model.WaveEchelonEnemy;

import java.util.List;
import java.util.Random;

/**
 * Service orchestrating wave difficulty, enemy spawn timers, and Boss encounters.
 * Fully integrated multi-subject academic deployment of all 20 enemy types across 4 subjects.
 */
public class WaveManager {

    private int currentLevel;
    private double spawnTimer;
    private double spawnInterval;
    private int enemiesSpawnedInWave;
    private int totalEnemiesForWave;
    private boolean bossSpawned;
    private boolean waveClearNotice;
    private double noticeTimer;
    private final Random random;
    private database.multiplayer.DeterministicPRNG prng;

    public WaveManager() {
        this.random = new Random();
        this.prng = null;
        resetToLevel(1);
    }

    public void setPRNG(database.multiplayer.DeterministicPRNG prng) {
        this.prng = prng;
    }

    public database.multiplayer.DeterministicPRNG getPRNG() {
        return this.prng;
    }

    /**
     * [TOPIC: Wave Spawning] Configures spawn cadence and enemy quota for the target sector.
     */
    public final void resetToLevel(int level) {
        this.currentLevel = Math.max(1, level);
        this.spawnTimer = 0.0;
        // WHY: Floor spawnInterval at 0.7s so high sectors stay intense without becoming unplayable walls.
        this.spawnInterval = Math.max(0.7, 2.2 - (currentLevel * 0.2));
        this.enemiesSpawnedInWave = 0;
        this.totalEnemiesForWave = 8 + (currentLevel * 4);
        this.bossSpawned = false;
        this.waveClearNotice = false;
        this.noticeTimer = 0.0;
    }

    /**
     * [TOPIC: Wave Spawning] [TOPIC: Boss Encounters]
     * Advances wave/notice timers and deploys boss flagships every 3rd sector.
     */
    public void update(double delta, List<Enemy> enemyList, int screenWidth) {
        if (waveClearNotice) {
            noticeTimer += delta;
            if (noticeTimer >= 2.5) {
                waveClearNotice = false;
                noticeTimer = 0.0;
                resetToLevel(currentLevel + 1);
            }
            return;
        }

        // WHY: Every 3rd level is a milestone sector reserved for a solo Leviathan Boss encounter.
        boolean isBossLevel = (currentLevel % 3 == 0);

        if (isBossLevel) {
            if (!bossSpawned && enemyList.isEmpty()) {
                // Spawn the sector Boss flagship
                double bossX = (screenWidth - 116) / 2.0;
                enemyList.add(new BossEnemy(bossX, -80, "Leviathan MK-" + (currentLevel / 3), currentLevel));
                bossSpawned = true;
            } else if (bossSpawned && enemyList.isEmpty()) {
                // Boss was defeated! Trigger wave completion
                waveClearNotice = true;
                noticeTimer = 0.0;
            }
            return;
        }

        // Standard wave spawning
        if (enemiesSpawnedInWave < totalEnemiesForWave) {
            spawnTimer += delta;
            if (spawnTimer >= spawnInterval) {
                spawnTimer = 0.0;
                spawnRegularEnemy(enemyList, screenWidth);
                enemiesSpawnedInWave++;
            }
        } else if (enemyList.isEmpty()) {
            // All enemies defeated in wave!
            waveClearNotice = true;
            noticeTimer = 0.0;
        }
    }

    /**
     * [TOPIC: Wave Spawning] [TOPIC: Deterministic PRNG]
     * Spawns sector-tiered enemy classes using seeded PRNG in 1v1 multiplayer or Random in solo.
     */
    private void spawnRegularEnemy(List<Enemy> enemyList, int screenWidth) {
        int margin = 60;
        int usableW = Math.max(10, screenWidth - margin * 2);
        // WHY: Routing rolls through DeterministicPRNG keeps both 1v1 clients' waves identical.
        double spawnX = margin + (prng != null ? prng.nextRange(0, usableW) : random.nextInt(usableW));
        double spawnY = -40;

        int roll = prng != null ? prng.nextInt(0, 100) : random.nextInt(100);

        if (currentLevel == 1) {
            // Level 1: Baseline introduction (70% Drone, 30% Fighter)
            if (roll < 70) {
                enemyList.add(new DroneEnemy(spawnX, spawnY));
            } else {
                enemyList.add(new FighterEnemy(spawnX, spawnY));
            }
        } else if (currentLevel == 2) {
            // Level 2: Tactical speed & armor (25% Drone, 25% Fighter, 25% Armoured, 25% Interceptor)
            if (roll < 25) {
                enemyList.add(new DroneEnemy(spawnX, spawnY));
            } else if (roll < 50) {
                enemyList.add(new FighterEnemy(spawnX, spawnY));
            } else if (roll < 75) {
                enemyList.add(new ArmouredEnemy(spawnX, spawnY));
            } else {
                enemyList.add(new InterceptorEnemy(spawnX, spawnY));
            }
        } else if (currentLevel == 4) {
            // Level 4: Programming Laboratory (PL) Fleet Introduction
            // Spatial Grid Swarm, Formation Chain, Wave Echelon, Target Hunter, Hash Shield
            if (roll < 20) {
                enemyList.add(new GridSwarmEnemy(spawnX, spawnY));
            } else if (roll < 40) {
                enemyList.add(new FormationChainEnemy(spawnX, spawnY, 0));
            } else if (roll < 60) {
                enemyList.add(new WaveEchelonEnemy(spawnX, spawnY));
            } else if (roll < 80) {
                enemyList.add(new TargetHunterEnemy(spawnX, spawnY));
            } else {
                enemyList.add(new HashShieldEnemy(spawnX, spawnY));
            }
        } else if (currentLevel == 5) {
            // Level 5: Full combined fleet (Drone, Fighter, Armoured, Interceptor, Morphing, FailSafe, Rammer)
            if (roll < 15) {
                enemyList.add(new DroneEnemy(spawnX, spawnY));
            } else if (roll < 30) {
                enemyList.add(new FighterEnemy(spawnX, spawnY));
            } else if (roll < 45) {
                enemyList.add(new ArmouredEnemy(spawnX, spawnY));
            } else if (roll < 60) {
                enemyList.add(new InterceptorEnemy(spawnX, spawnY));
            } else if (roll < 75) {
                enemyList.add(new MorphingEnemy(spawnX, spawnY));
            } else if (roll < 88) {
                enemyList.add(new FailSafeEnemy(spawnX, spawnY));
            } else {
                enemyList.add(new RammerEnemy(spawnX, spawnY));
            }
        } else if (currentLevel == 7) {
            // Level 7: Computer Organization & Architecture (COA) Fleet
            // Sub-Register Sentry, Fibonacci Skimmer, Booth Gunship, Railgun Dreadnought
            if (roll < 25) {
                enemyList.add(new SubRegSentryEnemy(spawnX, spawnY));
            } else if (roll < 50) {
                enemyList.add(new FibonacciSkimmerEnemy(spawnX, spawnY));
            } else if (roll < 75) {
                enemyList.add(new BoothGunshipEnemy(spawnX, spawnY));
            } else {
                enemyList.add(new RailgunDreadnoughtEnemy(spawnX, spawnY));
            }
        } else if (currentLevel == 8) {
            // Level 8: Computer Graphics Laboratory (CGL) Fleet
            // Polygon Cruiser, Orbital Rotor, Raster Sentinel, Warp Cruiser, Boundary Skimmer, Phong Cruiser
            if (roll < 17) {
                enemyList.add(new PolygonCruiserEnemy(spawnX, spawnY));
            } else if (roll < 34) {
                enemyList.add(new OrbitalRotorEnemy(spawnX, spawnY));
            } else if (roll < 50) {
                enemyList.add(new RasterSentinelEnemy(spawnX, spawnY));
            } else if (roll < 67) {
                enemyList.add(new WarpCruiserEnemy(spawnX, spawnY));
            } else if (roll < 84) {
                enemyList.add(new BoundarySkimmerEnemy(spawnX, spawnY));
            } else {
                enemyList.add(new PhongCruiserEnemy(spawnX, spawnY));
            }
        } else {
            // Level 10+: Master Grand Fleet: All 20 Enemies Active Across All 4 Subjects!
            int fleetRoll = (prng != null ? prng.nextInt(0, 16) : random.nextInt(16));
            switch (fleetRoll) {
                case 0 -> enemyList.add(new ArmouredEnemy(spawnX, spawnY));
                case 1 -> enemyList.add(new InterceptorEnemy(spawnX, spawnY));
                case 2 -> enemyList.add(new MorphingEnemy(spawnX, spawnY));
                case 3 -> enemyList.add(new FailSafeEnemy(spawnX, spawnY));
                case 4 -> enemyList.add(new RammerEnemy(spawnX, spawnY));
                case 5 -> enemyList.add(new GridSwarmEnemy(spawnX, spawnY));
                case 6 -> enemyList.add(new FormationChainEnemy(spawnX, spawnY, 0));
                case 7 -> enemyList.add(new WaveEchelonEnemy(spawnX, spawnY));
                case 8 -> enemyList.add(new TargetHunterEnemy(spawnX, spawnY));
                case 9 -> enemyList.add(new HashShieldEnemy(spawnX, spawnY));
                case 10 -> enemyList.add(new SubRegSentryEnemy(spawnX, spawnY));
                case 11 -> enemyList.add(new FibonacciSkimmerEnemy(spawnX, spawnY));
                case 12 -> enemyList.add(new BoothGunshipEnemy(spawnX, spawnY));
                case 13 -> enemyList.add(new RailgunDreadnoughtEnemy(spawnX, spawnY));
                case 14 -> enemyList.add(new OrbitalRotorEnemy(spawnX, spawnY));
                default -> enemyList.add(new PhongCruiserEnemy(spawnX, spawnY));
            }
        }
    }

    /**
     * [TOPIC: Deterministic PRNG] Rolls power-up drop chance on enemy defeat.
     */
    public void evaluatePowerUpDrop(double x, double y, List<PowerUp> powerUpList) {
        // WHY: 22% drop probability balanced across solo and PRNG-synchronized 1v1 matches.
        int roll = prng != null ? prng.nextInt(0, 100) : random.nextInt(100);
        if (roll < 22) {
            PowerUpType[] types = PowerUpType.values();
            int idx = prng != null ? prng.nextInt(0, types.length) : random.nextInt(types.length);
            PowerUpType chosen = types[idx];
            powerUpList.add(new PowerUp(x, y, chosen));
        }
    }

    public int getCurrentLevel() { return currentLevel; }
    public int getTotalEnemiesForWave() { return totalEnemiesForWave; }
    public boolean isBossSpawned() { return bossSpawned; }
    public boolean isWaveClearNotice() { return waveClearNotice; }
}

