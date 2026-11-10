import { Enemy } from './Enemy';
import { Projectile } from './Projectile';
import { EnemyType } from '../types/game';

export type DroneState = 'PATROL' | 'CHASE' | 'EVADE' | 'OVERCHARGE';

/**
 * Adaptive Morphing Drone (PSOOP-CO2 Browser Implementation).
 * Demonstrates: Solutions to Reduce Complexity via State Pattern.
 *
 * Implements four decoupled states (PATROL, CHASE, EVADE, OVERCHARGE)
 * with structured transitions and color-coded HUD rendering.
 */
export class Morphing extends Enemy {
  public currentState: DroneState = 'PATROL';
  public timeAlive: number = 0;
  public stateTimer: number = 0;
  public overchargeTimer: number = 0;
  public overchargeCooldown: number = 0;
  public shootCooldown: number = 1.3;
  public shootTimer: number = 0.6;

  private static readonly OVERCHARGE_DURATION = 1.8;

  constructor(x: number, y: number = -40) {
    // x, y, width 36, height 36, health 50, shield 15, score 300, type MORPHING
    super(x, y, 36, 36, 50, 15, 300, EnemyType.MORPHING);
    this.vy = 90.0;
  }

  public update(dt: number, playerX: number = 400, playerY: number = 550): void {
    this.timeAlive += dt;
    this.stateTimer += dt;

    if (this.overchargeCooldown > 0) {
      this.overchargeCooldown -= dt;
    }
    if (this.shootTimer > 0) {
      this.shootTimer -= dt;
    }
    if (this.hitFlashTimer > 0) {
      this.hitFlashTimer -= dt;
    }

    // State kinematics
    const centerX = this.x + this.width / 2;
    const centerY = this.y + this.height / 2;
    const dx = playerX + 24 - centerX;
    const dy = playerY + 28 - centerY;
    const dist = Math.sqrt(dx * dx + dy * dy);

    switch (this.currentState) {
      case 'PATROL':
        this.vy = 95.0;
        this.x += Math.cos(this.timeAlive * 2.2) * 45.0 * dt;
        break;

      case 'CHASE':
        this.vy = 125.0;
        this.x += Math.sign(dx) * Math.min(Math.abs(dx), 110.0 * dt);
        break;

      case 'EVADE':
        this.vy = 65.0;
        this.x -= Math.sign(dx) * 130.0 * dt;
        break;

      case 'OVERCHARGE':
        this.vy = 180.0;
        this.overchargeTimer -= dt;
        break;
    }

    this.y += this.vy * dt;

    // Evaluate transitions
    this.evaluateTransitions(dist);

    if (this.y > this.playfieldHeight + 30) {
      this.active = false;
    }
  }

  private evaluateTransitions(dist: number): void {
    const healthRatio = this.health / this.maxHealth;

    switch (this.currentState) {
      case 'PATROL':
        if (healthRatio < 0.4) {
          this.transitionTo('EVADE');
        } else if (dist < 260.0) {
          this.transitionTo('CHASE');
        }
        break;

      case 'CHASE':
        if (healthRatio < 0.35) {
          this.transitionTo('EVADE');
        } else if (dist < 150.0 && this.overchargeCooldown <= 0) {
          this.transitionTo('OVERCHARGE');
        } else if (dist > 350.0) {
          this.transitionTo('PATROL');
        }
        break;

      case 'EVADE':
        if (dist > 300.0 && healthRatio >= 0.4) {
          this.transitionTo('PATROL');
        }
        break;

      case 'OVERCHARGE':
        if (healthRatio < 0.35 || this.overchargeTimer <= 0) {
          this.overchargeCooldown = 8.0;
          this.transitionTo('EVADE');
        }
        break;
    }
  }

  public transitionTo(nextState: DroneState): void {
    this.currentState = nextState;
    this.stateTimer = 0;
    if (nextState === 'OVERCHARGE') {
      this.overchargeTimer = Morphing.OVERCHARGE_DURATION;
    }
  }

  public fire(playerX: number = 400, playerY: number = 550): Projectile[] {
    if (!this.active || this.y < 20 || this.y > 480) {
      return [];
    }

    const bullets: Projectile[] = [];

    if (this.currentState === 'OVERCHARGE') {
      if (this.shootTimer <= 0) {
        this.shootTimer = 0.35; // Rapid fire during overcharge
        bullets.push(
          new Projectile(this.x + 6, this.y + this.height, -40.0, 370.0, 12, false, 5, 12, '#FFFF50', 'rgba(255, 255, 0, 0.7)'),
          new Projectile(this.x + this.width - 10, this.y + this.height, 40.0, 370.0, 12, false, 5, 12, '#FFFF50', 'rgba(255, 255, 0, 0.7)')
        );
      }
    } else if (this.shootTimer <= 0) {
      this.shootTimer = this.shootCooldown;

      if (this.currentState === 'CHASE') {
        const centerX = this.x + this.width / 2;
        const dx = playerX + 24 - centerX;
        const dy = playerY + 28 - (this.y + this.height);
        const dist = Math.sqrt(dx * dx + dy * dy);
        const vx = dist > 0.001 ? (dx / dist) * 330.0 : 0;
        const vy = dist > 0.001 ? (dy / dist) * 330.0 : 330.0;

        bullets.push(new Projectile(this.x + 15, this.y + this.height, vx, vy, 15, false, 6, 14, '#FF1E3C', 'rgba(255, 30, 60, 0.7)'));
      } else {
        bullets.push(new Projectile(this.x + 16, this.y + this.height, 0, 300.0, 14, false, 5, 14, '#FFA028', 'rgba(255, 160, 40, 0.7)'));
      }
    }

    return bullets;
  }

  public render(ctx: CanvasRenderingContext2D): void {
    if (!this.active) return;

    ctx.save();
    const px = this.x;
    const py = this.y;

    let auraColor = '#FF9600'; // PATROL: Amber
    if (this.currentState === 'CHASE') auraColor = '#FF1E3C'; // Crimson
    if (this.currentState === 'EVADE') auraColor = '#B400FF'; // Violet
    if (this.currentState === 'OVERCHARGE') auraColor = '#FFFF50'; // Electric Yellow

    if (this.hitFlashTimer > 0) {
      ctx.fillStyle = '#FFFFFF';
      ctx.strokeStyle = '#FFFFFF';
    } else {
      ctx.fillStyle = '#1E1E2D';
      ctx.strokeStyle = auraColor;
    }

    ctx.lineWidth = 1.8;
    ctx.shadowBlur = 10;
    ctx.shadowColor = auraColor;

    // Geometric hexagon diamond
    ctx.beginPath();
    ctx.moveTo(px + 18, py + 36);
    ctx.lineTo(px + 36, py + 22);
    ctx.lineTo(px + 30, py + 4);
    ctx.lineTo(px + 18, py);
    ctx.lineTo(px + 6, py + 4);
    ctx.lineTo(px, py + 22);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();

    // Core sensor
    ctx.fillStyle = auraColor;
    ctx.beginPath();
    ctx.arc(px + 18, py + 18, 5, 0, Math.PI * 2);
    ctx.fill();

    // Shield aura
    if (this.shield > 0) {
      ctx.strokeStyle = auraColor;
      ctx.lineWidth = 1.2;
      ctx.beginPath();
      ctx.arc(px + 18, py + 18, 22, 0, Math.PI * 2);
      ctx.stroke();
    }

    ctx.restore();
  }
}
