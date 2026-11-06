import { Entity } from './Entity';
import { PowerUpType } from '../types/game';

export class PowerUp extends Entity {
  public type: PowerUpType;
  public spawnX: number;
  private timeAlive: number = 0;

  constructor(x: number, y: number, type?: PowerUpType) {
    super(x, y, 26, 26);
    this.spawnX = x;
    this.vy = 85.0;

    if (type) {
      this.type = type;
    } else {
      const types = [
        PowerUpType.HEALTH_PACK,
        PowerUpType.SHIELD_BOOST,
        PowerUpType.WEAPON_UPGRADE,
        PowerUpType.SPEED_BOOST
      ];
      this.type = types[Math.floor(Math.random() * types.length)];
    }
  }

  public update(dt: number): void {
    this.timeAlive += dt;
    this.y += this.vy * dt;
    this.x = this.spawnX + 12.0 * Math.sin(this.timeAlive * 3.5);

    // Despawn off bottom screen
    if (this.y > 670) {
      this.active = false;
    }
  }

  public render(ctx: CanvasRenderingContext2D): void {
    ctx.save();

    let color = '#32DC50';
    let symbol = '+';

    switch (this.type) {
      case PowerUpType.HEALTH_PACK:
        color = '#32DC50';
        symbol = '+';
        break;
      case PowerUpType.SHIELD_BOOST:
        color = '#3CA0FF';
        symbol = 'S';
        break;
      case PowerUpType.WEAPON_UPGRADE:
        color = '#FFB428';
        symbol = 'W';
        break;
      case PowerUpType.SPEED_BOOST:
        color = '#DC46FF';
        symbol = '⚡';
        break;
    }

    const pulse = 1.0 + 0.15 * Math.sin(this.timeAlive * 6.0);
    const radius = (this.width / 2) * pulse;
    const centerX = this.x + this.width / 2;
    const centerY = this.y + this.height / 2;

    // Outer glowing capsule
    ctx.shadowBlur = 12;
    ctx.shadowColor = color;
    ctx.strokeStyle = color;
    ctx.lineWidth = 2.0;
    ctx.fillStyle = 'rgba(15, 23, 42, 0.85)';

    ctx.beginPath();
    ctx.arc(centerX, centerY, radius, 0, Math.PI * 2);
    ctx.fill();
    ctx.stroke();

    // Inner symbol
    ctx.fillStyle = color;
    ctx.font = 'bold 15px monospace';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(symbol, centerX, centerY);

    ctx.restore();
  }
}

