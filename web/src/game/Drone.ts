import { Enemy } from './Enemy';
import { Projectile } from './Projectile';
import { EnemyType } from '../types/game';

export class Drone extends Enemy {
  public spawnX: number;
  public timeAlive: number = 0;

  constructor(x: number, y: number = -35) {
    super(x, y, 32, 32, 30, 0, 100, EnemyType.DRONE);
    this.spawnX = x;
    this.vy = 200.0;
  }

  public update(dt: number): void {
    this.timeAlive += dt;
    this.y += this.vy * dt;
    this.x = this.spawnX + 35.0 * Math.sin(this.timeAlive * 4.5);

    if (this.hitFlashTimer > 0) {
      this.hitFlashTimer -= dt;
    }

    // Despawn off bottom
    if (this.y > 680) {
      this.active = false;
    }
  }

  public fire(): Projectile[] {
    // Drones perform ramming runs only
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
      ctx.fillStyle = '#8C191E';
      ctx.strokeStyle = '#FF5A28';
    }

    ctx.lineWidth = 1.5;
    ctx.shadowBlur = 8;
    ctx.shadowColor = '#FF3232';

    // Downward dart polygon matching Java desktop implementation
    // (16, 32) -> (32, 8) -> (24, 0) -> (16, 6) -> (8, 0) -> (0, 8)
    ctx.beginPath();
    ctx.moveTo(px + 16, py + 32);
    ctx.lineTo(px + 32, py + 8);
    ctx.lineTo(px + 24, py);
    ctx.lineTo(px + 16, py + 6);
    ctx.lineTo(px + 8, py);
    ctx.lineTo(px, py + 8);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();

    // Red optic sensor core
    ctx.fillStyle = '#FF1E1E';
    ctx.beginPath();
    ctx.arc(px + 16, py + 18, 3.5, 0, Math.PI * 2);
    ctx.fill();

    ctx.restore();
  }
}
