import { Entity } from './Entity';

export class Projectile extends Entity {
  public damage: number;
  public isPlayerBullet: boolean;
  public colorCore: string;
  public colorGlow: string;

  constructor(
    x: number,
    y: number,
    vx: number,
    vy: number,
    damage: number,
    isPlayerBullet: boolean,
    width: number = 6,
    height: number = 16,
    colorCore: string = '#B4FFFF',
    colorGlow: string = 'rgba(0, 210, 255, 0.6)'
  ) {
    super(x, y, width, height);
    this.vx = vx;
    this.vy = vy;
    this.damage = damage;
    this.isPlayerBullet = isPlayerBullet;
    this.colorCore = colorCore;
    this.colorGlow = colorGlow;
  }

  public update(dt: number): void {
    this.x += this.vx * dt;
    this.y += this.vy * dt;

    // Despawn if outside screen boundaries (with 50px margin)
    if (this.y < -60 || this.y > 700 || this.x < -60 || this.x > 860) {
      this.active = false;
    }
  }

  public render(ctx: CanvasRenderingContext2D): void {
    ctx.save();

    // Outer glow
    ctx.shadowBlur = 10;
    ctx.shadowColor = this.colorGlow;

    // Outer rounded bolt
    ctx.fillStyle = this.colorGlow;
    ctx.beginPath();
    ctx.roundRect(this.x - 1, this.y - 1, this.width + 2, this.height + 2, 3);
    ctx.fill();

    // Inner bright core
    ctx.fillStyle = this.colorCore;
    ctx.beginPath();
    ctx.roundRect(this.x, this.y, this.width, this.height, 2);
    ctx.fill();

    ctx.restore();
  }
}
