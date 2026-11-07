import { BoundingBox, EnemyType, PowerUpType } from '../types/game';
import { Player } from './Player';
import { Enemy } from './Enemy';
import { Projectile } from './Projectile';
import { PowerUp } from './PowerUp';
import { ParticleSystem } from './Particle';
import { ScoreCalculator } from './ScoreCalculator';
import { SoundService } from '../services/SoundService';

export class CollisionManager {
  public static checkAABB(b1: BoundingBox, b2: BoundingBox): boolean {
    return (
      b1.x < b2.x + b2.width &&
      b1.x + b1.width > b2.x &&
      b1.y < b2.y + b2.height &&
      b1.y + b1.height > b2.y
    );
  }

  public static handlePlayerProjectiles(
    bullets: Projectile[],
    enemies: Enemy[],
    powerUps: PowerUp[],
    particles: ParticleSystem,
    scoreCalc: ScoreCalculator,
    sounds: SoundService
  ): void {
    for (const bullet of bullets) {
      if (!bullet.active || !bullet.isPlayerBullet) continue;

      const bulletBounds = bullet.getBounds();

      for (const enemy of enemies) {
        if (!enemy.active) continue;

        if (this.checkAABB(bulletBounds, enemy.getBounds())) {
          bullet.active = false;
          particles.spawnSparks(bullet.x, bullet.y, bullet.colorCore, 5);

          const destroyed = enemy.takeDamage(bullet.damage);
          if (destroyed) {
            const isBoss = enemy.enemyType === 'BOSS';
            if (isBoss) {
              sounds.playBossExplosion();
              particles.spawnExplosion(enemy.x + enemy.width / 2, enemy.y + enemy.height / 2, true, '#FF144B');
            } else {
              sounds.playExplosion();
              particles.spawnExplosion(enemy.x + enemy.width / 2, enemy.y + enemy.height / 2, false, '#FF7814');
            }

            const { addedPoints, multiplier } = scoreCalc.addKill(enemy.scoreValue);
            particles.spawnFloatingText(
              enemy.x + enemy.width / 2 - 10,
              enemy.y,
              `+${addedPoints}${multiplier > 1 ? ` (x${multiplier})` : ''}`,
              '#FFD700'
            );

            // 22% drop rate for power-up
            if (Math.random() < 0.22) {
              powerUps.push(new PowerUp(enemy.x + enemy.width / 2 - 13, enemy.y + enemy.height / 2 - 13));
            }
          }
          break; // Bullet hit one enemy
        }
      }
    }
  }

  public static handleEnemyProjectiles(
    bullets: Projectile[],
    player: Player,
    particles: ParticleSystem,
    scoreCalc: ScoreCalculator,
    sounds: SoundService
  ): void {
    if (!player.active || player.invulnerableTimer > 0) return;

    const playerBounds = player.getBounds();

    for (const bullet of bullets) {
      if (!bullet.active || bullet.isPlayerBullet) continue;

      if (this.checkAABB(bullet.getBounds(), playerBounds)) {
        bullet.active = false;
        const shieldWasActive = player.shield > 0;
        player.takeDamage(bullet.damage, false);
        scoreCalc.onPlayerDamaged();

        if (shieldWasActive) {
          sounds.playShieldHit();
          particles.spawnSparks(bullet.x, bullet.y, '#00E5FF', 10);
        } else {
          sounds.playExplosion();
          particles.spawnSparks(bullet.x, bullet.y, '#FF3232', 12);
        }

        if (!player.active) {
          sounds.playBossExplosion();
          particles.spawnExplosion(player.x + 24, player.y + 28, true, '#00E5FF');
        }
        break;
      }
    }
  }

  public static handleShipRamming(
    player: Player,
    enemies: Enemy[],
    particles: ParticleSystem,
    scoreCalc: ScoreCalculator,
    sounds: SoundService
  ): void {
    if (!player.active || player.invulnerableTimer > 0) return;

    const playerBounds = player.getBounds();

    for (const enemy of enemies) {
      if (!enemy.active) continue;

      if (this.checkAABB(playerBounds, enemy.getBounds())) {
        const shieldWasActive = player.shield > 0;
        const ramDamage = (enemy.enemyType === EnemyType.RAMMER) ? 45 : 35;
        player.takeDamage(ramDamage, true);
        const destroyed = enemy.takeDamage(60);
        scoreCalc.onPlayerDamaged();

        particles.spawnExplosion((player.x + enemy.x) / 2 + 16, (player.y + enemy.y) / 2 + 16, false, '#FF9600');

        if (shieldWasActive) {
          sounds.playShieldHit();
        } else {
          sounds.playExplosion();
        }

        if (destroyed) {
          const { addedPoints } = scoreCalc.addKill(enemy.scoreValue);
          particles.spawnFloatingText(enemy.x, enemy.y, `+${addedPoints}`, '#FFD700');
        }

        if (!player.active) {
          sounds.playBossExplosion();
          particles.spawnExplosion(player.x + 24, player.y + 28, true, '#00E5FF');
        }
        break;
      }
    }
  }

  public static handlePowerUps(
    powerUps: PowerUp[],
    player: Player,
    particles: ParticleSystem,
    scoreCalc: ScoreCalculator,
    sounds: SoundService
  ): void {
    if (!player.active) return;

    const playerBounds = player.getBounds();

    for (const p of powerUps) {
      if (!p.active) continue;

      if (this.checkAABB(playerBounds, p.getBounds())) {
        p.active = false;
        sounds.playPowerUp();
        scoreCalc.addBonus(150);

        let label = '+150 PTS';
        let color = '#FFD700';

        switch (p.type) {
          case PowerUpType.HEALTH_PACK:
            player.heal(40);
            label = 'HULL REPAIRED +40';
            color = '#32DC50';
            break;
          case PowerUpType.SHIELD_BOOST:
            player.rechargeShield(50);
            label = 'SHIELD OVERCHARGED +50';
            color = '#3CA0FF';
            break;
          case PowerUpType.WEAPON_UPGRADE:
            player.upgradeWeapon();
            label = `WEAPON SYS LVL ${player.weaponLevel}`;
            color = '#FFB428';
            break;
          case PowerUpType.SPEED_BOOST:
            player.boostSpeed(25);
            label = 'THRUSTER BOOST +25';
            color = '#DC46FF';
            break;
        }

        particles.spawnSparks(p.x + 13, p.y + 13, color, 14);
        particles.spawnFloatingText(p.x - 20, p.y - 10, label, color);
      }
    }
  }
}

