import { Enemy } from './Enemy';
import { Projectile } from './Projectile';
import { EnemyType } from '../types/game';

/**
 * Fail-Safe Sentry (PSOOP-CO3 Browser Implementation).
 * Demonstrates: Critical Thinking & Problem Solving via Defensive Programming.
 *
 * Sentry craft that detects critical health (<= 30%) and triggers a single-use
 * emergency safety barrier, preventing invalid damage states and repeated triggers.
 */
export class FailSafe extends Enemy {
  public timeAlive: number = 0;
  public emergencyBarrierTriggered: boolean = false;
  public emergencyBarrierActive: boolean = false;
  public emergencyBarrierTimer: number = 0;
  public shootCooldown: number = 1.4;
  public shootTimer: number = 0.7;

  private static readonly CRITICAL_HEALTH_RATIO = 0.30;
  private static readonly BARRIER_DURATION = 3.0;

  constructor(x: number, y: number = -40) {
    // x, y, width 40, height 42, health 70, shield 15, score 280, type FAIL_SAFE
    super(x, y, 40, 42, 70, 15, 280, EnemyType.FAIL_SAFE);
    this.vy = 76.5;
  }

  /**
   * Defensive damage resolution enforcing invariants and critical fail-safe trigger.
   */
  public override takeDamage(amount: number): boolean {
    if (!this.active || amount <= 0) return false;

    // Emergency barrier absorbs 50% damage
    if (this.emergencyBarrierActive) {
      if (this.shield > 0) {
        this.shield = Math.max(0, this.shield - Math.floor(amount / 2));
      }
      this.hitFlashTimer = 0.08;
      return false;
    }

    let remaining = amount;
    if (this.shield > 0) {
      if (this.shield >= remaining) {
        this.shield -= remaining;
        remaining = 0;
      } else {
        remaining -= this.shield;
        this.shield = 0;
      }
    }

    if (remaining > 0) {
      this.health = Math.max(0, this.health - remaining);
      this.hitFlashTimer = 0.08;

      if (this.health <= 0) {
        this.active = false;
        this.emergencyBarrierActive = false;
        return true;
      }

      // Critical health check: single-use emergency barrier trigger
      const threshold = Math.ceil(this.maxHealth * FailSafe.CRITICAL_HEALTH_RATIO);
      if (this.health <= threshold && !this.emergencyBarrierTriggered) {
        this.activateEmergencyBarrier();
      }
    }

    return false;
  }

  private activateEmergencyBarrier(): void {
    this.emergencyBarrierTriggered = true;
    this.emergencyBarrierActive = true;
    this.emergencyBarrierTimer = FailSafe.BARRIER_DURATION;
    this.shield = Math.max(this.shield, 35);
  }

  public update(dt: number): void {
    if (dt <= 0 || isNaN(dt)) return;

    this.timeAlive += dt;
    this.y += this.vy * dt;
    this.x += Math.sin(this.timeAlive * 1.8) * 35.0 * dt;

    // Defensive playfield boundary clamping
    if (this.x < 10) this.x = 10;
    if (this.x > 750) this.x = 750;

    if (this.shootTimer > 0) {
      this.shootTimer -= dt;
    }
    if (this.hitFlashTimer > 0) {
      this.hitFlashTimer -= dt;
    }

    if (this.emergencyBarrierActive) {
      this.emergencyBarrierTimer -= dt;
      if (this.emergencyBarrierTimer <= 0) {
        this.emergencyBarrierActive = false;
      }
    }

    if (this.y > 680) {
      this.active = false;
    }
  }

  public fire(): Projectile[] {
    if (this.shootTimer > 0 || !this.active || this.y < 20 || this.y > 450) {
      return [];
    }

    this.shootTimer = this.shootCooldown;
    return [
      new Projectile(this.x + 6, this.y + this.height - 4, -15.0, 300.0, 16, false, 6, 14, '#00E68C', 'rgba(0, 230, 140, 0.7)'),
      new Projectile(this.x + this.width - 12, this.y + this.height - 4, 15.0, 300.0, 16, false, 6, 14, '#00E68C', 'rgba(0, 230, 140, 0.7)')
    ];
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
      ctx.fillStyle = '#143228';
      ctx.strokeStyle = '#00E68C';
    }

    ctx.lineWidth = 1.8;
    ctx.shadowBlur = 8;
    ctx.shadowColor = '#00E68C';

    // Fortified diamond bastion
    ctx.beginPath();
    ctx.moveTo(px + 20, py + 42);
    ctx.lineTo(px + 40, py + 26);
    ctx.lineTo(px + 34, py + 6);
    ctx.lineTo(px + 20, py);
    ctx.lineTo(px + 6, py + 6);
    ctx.lineTo(px, py + 26);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();

    // Core sensor
    ctx.fillStyle = this.emergencyBarrierActive ? '#FFF050' : '#00FFB4';
    ctx.beginPath();
    ctx.arc(px + 20, py + 21, 5, 0, Math.PI * 2);
    ctx.fill();

    // Emergency Barrier projection
    if (this.emergencyBarrierActive) {
      const pulse = 0.6 + 0.4 * Math.sin(Date.now() * 0.015);
      ctx.strokeStyle = `rgba(0, 255, 200, ${pulse * 0.9})`;
      ctx.lineWidth = 2.5;
      ctx.shadowBlur = 16;
      ctx.shadowColor = '#00FFC8';
      ctx.beginPath();
      ctx.arc(px + 20, py + 21, 28, 0, Math.PI * 2);
      ctx.stroke();
    } else if (this.shield > 0) {
      ctx.strokeStyle = 'rgba(0, 220, 180, 0.4)';
      ctx.lineWidth = 1.2;
      ctx.beginPath();
      ctx.arc(px + 20, py + 21, 23, 0, Math.PI * 2);
      ctx.stroke();
    }

    ctx.restore();
  }
}
