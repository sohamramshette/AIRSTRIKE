import { Enemy } from './Enemy';
import { Drone } from './Drone';
import { Fighter } from './Fighter';
import { Boss } from './Boss';
import { Armoured } from './Armoured';
import { Interceptor } from './Interceptor';
import { Morphing } from './Morphing';
import { FailSafe } from './FailSafe';
import { Rammer } from './Rammer';

export class WaveManager {
  public currentLevel: number = 1;
  public spawnTimer: number = 0;
  public waveClearTimer: number = 0;
  public waveEnemiesRemaining: number = 0;
  public isBossWave: boolean = false;
  public bossActive: boolean = false;
  public bannerText: string | null = null;

  constructor() {
    this.startLevel(1);
  }

  public reset(): void {
    this.startLevel(1);
  }

  public startLevel(level: number): void {
    this.currentLevel = level;
    this.isBossWave = level % 3 === 0;
    this.bossActive = false;
    this.waveClearTimer = 0;
    this.bannerText = `SECTOR ${this.currentLevel}: ${this.isBossWave ? 'FLAGSHIP INCOMING' : 'INVASION FLEET'}`;

    if (this.isBossWave) {
      this.waveEnemiesRemaining = 1; // The boss
      this.spawnTimer = 1.0; // Short delay before boss descends
    } else {
      this.waveEnemiesRemaining = 8 + level * 4;
      this.spawnTimer = 0.5;
    }
  }

  public update(
    dt: number,
    activeEnemiesCount: number,
    onSpawnEnemy: (enemy: Enemy) => void,
    onWaveCleared: (level: number) => void
  ): void {
    // Wave clear banner delay
    if (this.waveClearTimer > 0) {
      this.waveClearTimer -= dt;
      if (this.waveClearTimer <= 0) {
        this.bannerText = null;
        this.startLevel(this.currentLevel + 1);
        onWaveCleared(this.currentLevel);
      }
      return;
    }

    // Spawn logic
    if (this.isBossWave) {
      if (!this.bossActive && this.spawnTimer > 0) {
        this.spawnTimer -= dt;
        if (this.spawnTimer <= 0) {
          const boss = new Boss(this.currentLevel);
          this.bossActive = true;
          onSpawnEnemy(boss);
        }
      } else if (this.bossActive && activeEnemiesCount === 0) {
        // Boss defeated!
        this.bossActive = false;
        this.bannerText = `SECTOR ${this.currentLevel} SECURED!`;
        this.waveClearTimer = 2.5;
      }
    } else {
      // Standard wave
      if (this.waveEnemiesRemaining > 0) {
        this.spawnTimer -= dt;
        if (this.spawnTimer <= 0) {
          const interval = Math.max(0.7, 2.2 - this.currentLevel * 0.2);
          this.spawnTimer = interval;

          const spawnX = 50 + Math.random() * 680;
          const roll = Math.random() * 100;

          if (this.currentLevel === 1) {
            // Level 1: Baseline introduction (70% Drone, 30% Fighter)
            if (roll < 70) {
              onSpawnEnemy(new Drone(spawnX));
            } else {
              onSpawnEnemy(new Fighter(spawnX));
            }
          } else if (this.currentLevel === 2) {
            // Level 2: Tactical speed & armor (25% Drone, 25% Fighter, 25% Armoured, 25% Interceptor)
            if (roll < 25) {
              onSpawnEnemy(new Drone(spawnX));
            } else if (roll < 50) {
              onSpawnEnemy(new Fighter(spawnX));
            } else if (roll < 75) {
              onSpawnEnemy(new Armoured(spawnX));
            } else {
              onSpawnEnemy(new Interceptor(spawnX));
            }
          } else if (this.currentLevel === 4) {
            // Level 4: Adaptive tactics (20% Drone, 20% Fighter, 20% Armoured, 20% Interceptor, 20% Morphing)
            if (roll < 20) {
              onSpawnEnemy(new Drone(spawnX));
            } else if (roll < 40) {
              onSpawnEnemy(new Fighter(spawnX));
            } else if (roll < 60) {
              onSpawnEnemy(new Armoured(spawnX));
            } else if (roll < 80) {
              onSpawnEnemy(new Interceptor(spawnX));
            } else {
              onSpawnEnemy(new Morphing(spawnX));
            }
          } else {
            // Level 5+: Full combined fleet
            if (roll < 18) {
              onSpawnEnemy(new Drone(spawnX));
            } else if (roll < 36) {
              onSpawnEnemy(new Fighter(spawnX));
            } else if (roll < 50) {
              onSpawnEnemy(new Armoured(spawnX));
            } else if (roll < 65) {
              onSpawnEnemy(new Interceptor(spawnX));
            } else if (roll < 77) {
              onSpawnEnemy(new Morphing(spawnX));
            } else if (roll < 88) {
              onSpawnEnemy(new FailSafe(spawnX));
            } else {
              onSpawnEnemy(new Rammer(spawnX));
            }
          }

          this.waveEnemiesRemaining -= 1;
        }
      } else if (activeEnemiesCount === 0 && this.waveClearTimer <= 0) {
        // All wave enemies destroyed!
        this.bannerText = `SECTOR ${this.currentLevel} SECURED!`;
        this.waveClearTimer = 2.5;
      }
    }
  }
}

