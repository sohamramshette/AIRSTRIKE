import { Enemy } from './Enemy';
import { Projectile } from './Projectile';
import { EnemyType } from '../types/game';

/**
 * Armoured Vanguard Heavy Fighter (PSOOP-CO1 Browser Implementation).
 * Demonstrates: Encapsulation & Inheritance.
 *
 * Encapsulated armor rating mitigates incoming projectile damage
 * after shield absorption, matching desktop ArmouredEnemy.java.
 */
export class Armoured extends Enemy {
  private armorRating: number = 8;
  public timeAlive: number = 0;
  public shootCooldown: number = 1.6; // 1600ms
  public shootTimer: number = 0.8;

  constructor(x: number, y: number = -45) {
    // x, y, width 46, height 44, health 90, shield 30, score 350, type ARMOURED
    super(x, y, 46, 44, 90, 30, 350, EnemyType.ARMOURED);
    this.vy = 52.5; // Steady heavy forward advance
  }

  /**
   * Controlled damage handling method overriding Enemy.takeDamage().
   */
  public override takeDamage(amount: number): boolean {
    if (!this.active || amount <= 0) return false;

    let remaining = amount;

    // 1. Shield layer
    if (this.shield > 0) {
      if (this.shield >= remaining) {
        this.shield -= remaining;
        remaining = 0;
      } else {
        remaining -= this.shield;
        this.shield = 0;
      }
    }

    // 2. Encapsulated Armor mitigation layer
    if (remaining > 0) {
      const effectiveDamage = Math.max(1, remaining - this.armorRating);
      this.health = Math.max(0, this.health - effectiveDamage);
    }

    this.hitFlashTimer = 0.08;

    if (this.health <= 0) {
      this.active = false;
      return true;
    }
    return false;
  }

  public getArmorRating(): number {
    return this.armorRating;
  }

  public setArmorRating(rating: number): void {
    this.armorRating = Math.max(0, rating);
  }

  public update(dt: number): void {
    this.timeAlive += dt;
    this.y += this.vy * dt;
    this.x += Math.sin(this.timeAlive * 1.5) * 20.0 * dt;

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

  public fire(): Projectile[] {
    if (this.shootTimer > 0 || !this.active || this.y < 20 || this.y > 460) {
      return [];
    }

    this.shootTimer = this.shootCooldown;
    // Central prow heavy plasma slug
    return [
      new Projectile(
        this.x + this.width / 2 - 4,
        this.y + this.height,
        0,
        280.0,
        20,
        false,
        8,
        18,
        '#FF9632',
        'rgba(255, 120, 0, 0.7)'
      )
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
      ctx.fillStyle = '#283241';
      ctx.strokeStyle = '#00C8DC';
    }

    ctx.lineWidth = 2.0;
    ctx.shadowBlur = 8;
    ctx.shadowColor = '#00C8DC';

    // Heavy reinforced hexagonal chine
    ctx.beginPath();
    ctx.moveTo(px + 23, py + 44);
    ctx.lineTo(px + 44, py + 30);
    ctx.lineTo(px + 46, py + 10);
    ctx.lineTo(px + 34, py);
    ctx.lineTo(px + 23, py + 8);
    ctx.lineTo(px + 12, py);
    ctx.lineTo(px, py + 10);
    ctx.lineTo(px + 2, py + 30);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();

    // Rivet studs
    ctx.fillStyle = '#A0D2F0';
    ctx.fillRect(px + 14, py + 16, 3, 3);
    ctx.fillRect(px + 29, py + 16, 3, 3);
    ctx.fillRect(px + 21, py + 26, 4, 4);

    // Shield aura
    if (this.shield > 0) {
      ctx.strokeStyle = 'rgba(0, 220, 255, 0.4)';
      ctx.lineWidth = 1.5;
      ctx.beginPath();
      ctx.ellipse(px + 23, py + 22, 27, 26, 0, 0, Math.PI * 2);
      ctx.stroke();
    }

    ctx.restore();
  }
}
