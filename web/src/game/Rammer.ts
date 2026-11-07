import { Enemy } from './Enemy';
import { Projectile } from './Projectile';
import { EnemyType } from '../types/game';

/**
 * Kinetic Battering Ram (PSOOP-CO4 Browser Implementation).
 * Demonstrates: Logic-Building for Real-Life Problems via Kinematics Simulation and OOP Collision Response.
 *
 * Implements accelerating non-uniform motion, forward player corridor locking,
 * and high-impact kinetic ramming prow damage.
 */
export class Rammer extends Enemy {
  public timeAlive: number = 0;
  public charging: boolean = false;
  public targetLockX: number;
  public currentAcceleration: number = 260.0;
  public maxRamSpeed: number = 380.0;
  public prowImpactDamage: number = 45;

  constructor(x: number, y: number = -45) {
    // x, y, width 38, height 46, health 85, shield 0, score 320, type RAMMER
    super(x, y, 38, 46, 85, 0, 320, EnemyType.RAMMER);
    this.targetLockX = x;
    this.vy = 70.0;
  }

  public update(dt: number, playerX: number = 400, playerY: number = 550): void {
    if (dt <= 0 || isNaN(dt)) return;

    this.timeAlive += dt;

    // Detect player corridor to ignite charging thrusters
    const centerX = this.x + this.width / 2;
    const dx = Math.abs(playerX + 24 - centerX);
    if (!this.charging && dx <= 110.0 && this.y < playerY - 30.0) {
      this.charging = true;
      this.targetLockX = playerX + 24;
    }

    if (this.charging) {
      // Non-uniform acceleration: vy += a * dt
      this.vy = Math.min(this.maxRamSpeed, this.vy + this.currentAcceleration * dt);

      // Fine corridor trim
      const offsetX = this.targetLockX - centerX;
      this.x += Math.sign(offsetX) * Math.min(Math.abs(offsetX), 80.0 * dt);
    } else {
      this.x += Math.sin(this.timeAlive * 2.0) * 30.0 * dt;
    }

    this.y += this.vy * dt;

    if (this.hitFlashTimer > 0) {
      this.hitFlashTimer -= dt;
    }

    if (this.y > 680) {
      this.active = false;
    }
  }

  public fire(): Projectile[] {
    // Rammer fires no projectiles; reactor energy is dedicated to prow kinetic thrusters
    return [];
  }

  public render(ctx: CanvasRenderingContext2D): void {
    if (!this.active) return;

    ctx.save();
    const px = this.x;
    const py = this.y;

    if (this.hitFlashTimer > 0) {
      ctx.fillStyle = '#FFFFFF';
      ctx.strokeStyle = '#FFFFFF';
    } else {
      ctx.fillStyle = '#2D2D32';
      ctx.strokeStyle = this.charging ? '#FF3C14' : '#FFAA00';
    }

    ctx.lineWidth = 2.2;
    ctx.shadowBlur = this.charging ? 14 : 8;
    ctx.shadowColor = this.charging ? '#FF3C14' : '#FFAA00';

    // Heavy tungsten battering arrowhead
    ctx.beginPath();
    ctx.moveTo(px + 19, py + 46);
    ctx.lineTo(px + 38, py + 26);
    ctx.lineTo(px + 32, py);
    ctx.lineTo(px + 19, py + 8);
    ctx.lineTo(px + 6, py);
    ctx.lineTo(px, py + 26);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();

    // Hazard chevron stripes on prow
    ctx.strokeStyle = '#FFC81E';
    ctx.lineWidth = 2.0;
    ctx.beginPath();
    ctx.moveTo(px + 12, py + 22);
    ctx.lineTo(px + 19, py + 30);
    ctx.lineTo(px + 26, py + 22);
    ctx.stroke();

    // Blazing thruster plume when charging
    if (this.charging) {
      ctx.fillStyle = '#FF5A00';
      ctx.beginPath();
      ctx.ellipse(px + 19, py - 6, 6, 12, 0, 0, Math.PI * 2);
      ctx.fill();

      ctx.fillStyle = '#FFF064';
      ctx.beginPath();
      ctx.ellipse(px + 19, py - 3, 4, 7, 0, 0, Math.PI * 2);
      ctx.fill();
    } else {
      ctx.fillStyle = '#FF781E';
      ctx.beginPath();
      ctx.arc(px + 19, py - 2, 4, 0, Math.PI * 2);
      ctx.fill();
    }

    ctx.restore();
  }
}
