import { Enemy } from './Enemy';
import { Projectile } from './Projectile';
import { EnemyType } from '../types/game';

export class Fighter extends Enemy {
  public timeAlive: number = 0;
  public shootCooldown: number = 1.2; // 1200ms
  public shootTimer: number = 0.6; // initial offset

  constructor(x: number, y: number = -50) {
    super(x, y, 42, 44, 60, 20, 250, EnemyType.FIGHTER);
    this.vy = 86.25;
  }

  public update(dt: number): void {
    this.timeAlive += dt;
    this.vx = 45.0 * Math.cos(this.timeAlive * 2.0);
    this.x += this.vx * dt;
    this.y += this.vy * dt;

    // Keep within playfield lateral bounds
    if (this.x < 10) this.x = 10;
    if (this.x > 800 - this.width - 10) this.x = 800 - this.width - 10;

    if (this.shootTimer > 0) {
      this.shootTimer -= dt;
    }

    if (this.hitFlashTimer > 0) {
      this.hitFlashTimer -= dt;
    }

    // Despawn off bottom
    if (this.y > 680) {
      this.active = false;
    }
  }

  public fire(): Projectile[] {
    if (this.shootTimer > 0 || !this.active) {
      return [];
    }

    // Only fire while in active combat altitude
    if (this.y < 20 || this.y > 450) {
      return [];
    }

    this.shootTimer = this.shootCooldown;
    const bullets: Projectile[] = [];

    // Dual downward lasers from wingtips
    bullets.push(
      new Projectile(
        this.x + 4,
        this.y + 36,
        0,
        340.0,
        15,
        false,
        6,
        14,
        '#FFA0E6',
        'rgba(230, 40, 180, 0.7)'
      ),
      new Projectile(
        this.x + 32,
        this.y + 36,
        0,
        340.0,
        15,
        false,
        6,
        14,
        '#FFA0E6',
        'rgba(230, 40, 180, 0.7)'
      )
    );

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
      ctx.fillStyle = '#41144B';
      ctx.strokeStyle = '#E628B4';
    }

    ctx.lineWidth = 1.8;
    ctx.shadowBlur = 10;
    ctx.shadowColor = '#E628B4';

    // Swept-forward wing hull matching Java desktop implementation:
    // (21, 44) -> (38, 32) -> (42, 12) -> (28, 0) -> (21, 10) -> (14, 0) -> (0, 12) -> (4, 32)
    ctx.beginPath();
    ctx.moveTo(px + 21, py + 44);
    ctx.lineTo(px + 38, py + 32);
    ctx.lineTo(px + 42, py + 12);
    ctx.lineTo(px + 28, py);
    ctx.lineTo(px + 21, py + 10);
    ctx.lineTo(px + 14, py);
    ctx.lineTo(px, py + 12);
    ctx.lineTo(px + 4, py + 32);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();

    // Gold cockpit
    ctx.fillStyle = '#FFC832';
    ctx.beginPath();
    ctx.ellipse(px + 21, py + 22, 3, 6, 0, 0, Math.PI * 2);
    ctx.fill();

    // Shield outline if shield > 0
    if (this.shield > 0) {
      ctx.strokeStyle = 'rgba(230, 40, 180, 0.5)';
      ctx.lineWidth = 1.5;
      ctx.beginPath();
      ctx.ellipse(px + 21, py + 22, 24, 26, 0, 0, Math.PI * 2);
      ctx.stroke();
    }

    ctx.restore();
  }
}

