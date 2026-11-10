import { Enemy } from './Enemy';
import { Projectile } from './Projectile';
import { EnemyType } from '../types/game';

/**
 * Interceptor Fighter (PSOOP-CO1 Browser Implementation).
 * Demonstrates: Polymorphism & Dynamic Dispatch.
 *
 * Scans player horizontal alignment, initiates high-speed forward dashes,
 * and executes staggered twin projectile discharges.
 */
export class Interceptor extends Enemy {
  public timeAlive: number = 0;
  public dashing: boolean = false;
  public dashTimer: number = 0;
  public shootCooldown: number = 1.1; // 1100ms
  public shootTimer: number = 0.5;

  private pendingStaggerShot: boolean = false;
  private staggerTimer: number = 0;
  private static readonly STAGGER_DELAY = 0.14; // 140ms
  private static readonly NORMAL_VY = 110.0;
  private static readonly DASH_VY = 260.0;

  constructor(x: number, y: number = -40) {
    // x, y, width 38, height 40, health 45, shield 10, score 200, type INTERCEPTOR
    super(x, y, 38, 40, 45, 10, 200, EnemyType.INTERCEPTOR);
    this.vy = Interceptor.NORMAL_VY;
  }

  public update(dt: number): void {
    this.timeAlive += dt;

    if (this.dashing) {
      this.dashTimer -= dt;
      this.vy = Interceptor.DASH_VY;
      if (this.dashTimer <= 0) {
        this.dashing = false;
        this.vy = Interceptor.NORMAL_VY;
      }
    } else {
      this.vy = Interceptor.NORMAL_VY;
      this.x += Math.cos(this.timeAlive * 3.0) * 55.0 * dt;
    }

    this.y += this.vy * dt;

    if (this.shootTimer > 0) {
      this.shootTimer -= dt;
    }
    if (this.hitFlashTimer > 0) {
      this.hitFlashTimer -= dt;
    }

    if (this.y > this.playfieldHeight + 30) {
      this.active = false;
    }
  }

  public fire(playerX: number = 400, _playerY: number = 550): Projectile[] {
    const bullets: Projectile[] = [];

    // Discharge pending staggered shot
    if (this.pendingStaggerShot) {
      this.staggerTimer -= 0.016; // approximate delta tick
      if (this.staggerTimer <= 0) {
        this.pendingStaggerShot = false;
        // Right wing staggered dart
        bullets.push(
          new Projectile(
            this.x + this.width - 8,
            this.y + this.height - 6,
            0,
            360.0,
            12,
            false,
            4,
            12,
            '#00FFFF',
            'rgba(0, 255, 255, 0.7)'
          )
        );
      }
    }

    if (!this.active || this.y < 30 || this.y > 450) {
      return bullets;
    }

    // Check player horizontal corridor alignment
    const enemyCenterX = this.x + this.width / 2;
    const dx = Math.abs(playerX + 24 - enemyCenterX);
    const isAligned = dx <= 75.0;

    if (isAligned) {
      if (!this.dashing && this.timeAlive > 0.8) {
        this.dashing = true;
        this.dashTimer = 0.85;
      }

      if (this.shootTimer <= 0) {
        this.shootTimer = this.shootCooldown;
        // Left wing immediate shot
        bullets.push(
          new Projectile(
            this.x + 4,
            this.y + this.height - 6,
            0,
            360.0,
            12,
            false,
            4,
            12,
            '#00FFFF',
            'rgba(0, 255, 255, 0.7)'
          )
        );
        this.pendingStaggerShot = true;
        this.staggerTimer = Interceptor.STAGGER_DELAY;
      }
    }

    return bullets;
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
      ctx.fillStyle = '#143C4B';
      ctx.strokeStyle = this.dashing ? '#FFE650' : '#32DCFF';
    }

    ctx.lineWidth = this.dashing ? 2.2 : 1.5;
    ctx.shadowBlur = this.dashing ? 14 : 8;
    ctx.shadowColor = this.dashing ? '#FFE650' : '#32DCFF';

    // Sleek forward-swept delta needle
    ctx.beginPath();
    ctx.moveTo(px + 19, py + 40);
    ctx.lineTo(px + 38, py + 22);
    ctx.lineTo(px + 32, py + 4);
    ctx.lineTo(px + 23, py);
    ctx.lineTo(px + 19, py + 10);
    ctx.lineTo(px + 15, py);
    ctx.lineTo(px + 6, py + 4);
    ctx.lineTo(px, py + 22);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();

    // Afterburner flare during dash
    if (this.dashing) {
      ctx.fillStyle = '#FF781E';
      ctx.beginPath();
      ctx.ellipse(px + 19, py - 4, 5, 8, 0, 0, Math.PI * 2);
      ctx.fill();
    }

    // Cockpit optic
    ctx.fillStyle = '#00FFC8';
    ctx.beginPath();
    ctx.arc(px + 19, py + 18, 3, 0, Math.PI * 2);
    ctx.fill();

    // Shield aura
    if (this.shield > 0) {
      ctx.strokeStyle = 'rgba(0, 255, 255, 0.4)';
      ctx.lineWidth = 1.2;
      ctx.beginPath();
      ctx.ellipse(px + 19, py + 20, 22, 23, 0, 0, Math.PI * 2);
      ctx.stroke();
    }

    ctx.restore();
  }
}
