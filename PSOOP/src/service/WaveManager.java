package service;

import model.BossEnemy;
import model.DroneEnemy;
import model.Enemy;
import model.FighterEnemy;
import model.PowerUp;
import model.PowerUpType;

import java.util.List;
import java.util.Random;

/**
 * Service orchestrating wave difficulty, enemy spawn timers, and Boss encounters.
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

    public WaveManager() {
        this.random = new Random();
        resetToLevel(1);
    }

    public void resetToLevel(int level) {
        this.currentLevel = Math.max(1, level);
        this.spawnTimer = 0.0;
        this.spawnInterval = Math.max(0.7, 2.2 - (currentLevel * 0.2));
        this.enemiesSpawnedInWave = 0;
        this.totalEnemiesForWave = 8 + (currentLevel * 4);
        this.bossSpawned = false;
        this.waveClearNotice = false;
        this.noticeTimer = 0.0;
    }

    /**
     * Updates wave timers and spawns new enemies into the active enemy collection.
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

        // Check for Boss Encounter wave (every 3rd level)
        boolean isBossLevel = (currentLevel % 3 == 0);

        if (isBossLevel) {
            if (!bossSpawned && enemyList.isEmpty()) {
                // Spawn the sector Boss flagship
                double bossX = (screenWidth - 116) / 2.0;
                enemyList.add(new BossEnemy(bossX, -80, "Leviathan MK-" + (currentLevel / 3)));
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

    private void spawnRegularEnemy(List<Enemy> enemyList, int screenWidth) {
        int margin = 60;
        double spawnX = margin + random.nextInt(Math.max(10, screenWidth - margin * 2));
        double spawnY = -40;

        // At level 1, mostly Drones. At level 2+, Fighters appear more frequently.
        int fighterChance = Math.min(65, currentLevel * 15);
        if (random.nextInt(100) < fighterChance) {
            enemyList.add(new FighterEnemy(spawnX, spawnY));
        } else {
            enemyList.add(new DroneEnemy(spawnX, spawnY));
        }
    }

    /**
     * Rolls random chance to drop a power-up capsule when an enemy is destroyed.
     */
    public void evaluatePowerUpDrop(double x, double y, List<PowerUp> powerUpList) {
        // 20% drop rate
        if (random.nextInt(100) < 22) {
            PowerUpType[] types = PowerUpType.values();
            PowerUpType chosen = types[random.nextInt(types.length)];
            powerUpList.add(new PowerUp(x, y, chosen));
        }
    }

    public int getCurrentLevel() { return currentLevel; }
    public boolean isBossSpawned() { return bossSpawned; }
    public boolean isWaveClearNotice() { return waveClearNotice; }
}
