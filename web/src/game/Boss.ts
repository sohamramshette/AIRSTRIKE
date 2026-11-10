import { Enemy } from './Enemy';
import { Projectile } from './Projectile';
import { EnemyType } from '../types/game';

export class Boss extends Enemy {
  public phase: number = 0; // 0, 1, 2
  public phaseTimer: number = 0;
  public shootCooldown: number = 0.9; // 900ms
  public shootTimer: number = 0.5;
  public isEntering: boolean = true;
  public targetY: number = 45.0;
  public bossName: string;

  constructor(level: number = 3) {
    super(342.0, -90.0, 116, 78, 550, 150, 2500, EnemyType.BOSS);
    this.bossName = `LEVIATHAN MK-${Math.floor(level / 3) || 1}`;
    this.vx = 80.0;
    this.vy = 40.0;
  }

  public update(dt: number): void {
    if (this.isEntering) {
      this.y += this.vy * dt;
      if (this.y >= this.targetY) {
        this.y = this.targetY;
        this.vy = 0;
        this.isEntering = false;
      }
    } else {
      // Horizontal patrol
      this.x += this.vx * dt;
      const maxPatrol = Math.min(650, Math.max(30, this.playfieldWidth - this.width - 20));
      if (this.x <= 30) {
        this.x = 30;
        this.vx = Math.abs(this.vx);
      } else if (this.x >= maxPatrol) {
        this.x = maxPatrol;
        this.vx = -Math.abs(this.vx);
      }

      // Attack Phase cycle (4.0s per phase)
      this.phaseTimer += dt;
      if (this.phaseTimer >= 4.0) {
        this.phase = (this.phase + 1) % 3;
        this.phaseTimer = 0;
      }

      if (this.shootTimer > 0) {
        this.shootTimer -= dt;
      }
    }

    if (this.hitFlashTimer > 0) {
      this.hitFlashTimer -= dt;
    }
  }

  public fire(playerX: number = 400, playerY: number = 550): Projectile[] {
    if (this.isEntering || this.shootTimer > 0 || !this.active) {
      return [];
    }

    this.shootTimer = this.shootCooldown;
    const bullets: Projectile[] = [];

    if (this.phase === 0) {
      // Phase 0: 5-way spread fan
      const spreadVx = [-160, -80, 0, 80, 160];
      for (const vx of spreadVx) {
        bullets.push(
          new Projectile(
            this.x + 58,
            this.y + 72,
            vx,
            310.0,
            15,
            false,
            6,
            14,
            '#FFA028',
            'rgba(255, 120, 20, 0.7)'
          )
        );
      }
    } else if (this.phase === 1) {
      // Phase 1: Heavy dual plasma cannons
      bullets.push(
        new Projectile(
          this.x + 24,
          this.y + 68,
          -20.0,
          360.0,
          25,
          false,
          8,
          22,
          '#FF3250',
          'rgba(255, 30, 60, 0.8)'
        ),
        new Projectile(
          this.x + 84,
          this.y + 68,
          20.0,
          360.0,
          25,
          false,
          8,
          22,
          '#FF3250',
          'rgba(255, 30, 60, 0.8)'
        )
      );
    } else {
      // Phase 2: Targeted barrage towards player
      const bossCenterX = this.x + 58;
      const bossCenterY = this.y + 70;
      const dx = playerX + 24 - bossCenterX;
      const dy = playerY + 28 - bossCenterY;
      const dist = Math.sqrt(dx * dx + dy * dy);

      const speed = 330.0;
      const vx = dist > 0.001 ? (dx / dist) * speed : 0;
      const vy = dist > 0.001 ? (dy / dist) * speed : speed;

      bullets.push(
        new Projectile(
          bossCenterX - 4,
          bossCenterY,
          vx,
          vy,
          20,
          false,
          8,
          18,
          '#E600FF',
          'rgba(180, 0, 255, 0.8)'
        )
      );
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
      ctx.fillStyle = '#141428';
      ctx.strokeStyle = '#FF3250';
    }

    ctx.lineWidth = 2.5;
    ctx.shadowBlur = 16;
    ctx.shadowColor = '#FF1E3C';

    // Massive flagship hull polygon
    ctx.beginPath();
    ctx.moveTo(px + 58, py + 78);   // Main forward nose tip
    ctx.lineTo(px + 90, py + 60);   // Right weapon sponson
    ctx.lineTo(px + 116, py + 40);  // Right outer wing
    ctx.lineTo(px + 104, py + 10);  // Right aft flank
    ctx.lineTo(px + 78, py);        // Right engine cowl
    ctx.lineTo(px + 58, py + 12);   // Center engine notch
    ctx.lineTo(px + 38, py);        // Left engine cowl
    ctx.lineTo(px + 12, py + 10);   // Left aft flank
    ctx.lineTo(px, py + 40);        // Left outer wing
    ctx.lineTo(px + 26, py + 60);   // Left weapon sponson
    ctx.closePath();
    ctx.fill();
    ctx.stroke();

    // Command Bridge
    ctx.fillStyle = '#FFB400';
    ctx.shadowBlur = 8;
    ctx.shadowColor = '#FFA000';
    ctx.beginPath();
    ctx.ellipse(px + 58, py + 34, 12, 6, 0, 0, Math.PI * 2);
    ctx.fill();

    // Sponson Cannon Ports
    ctx.fillStyle = '#FF144B';
    ctx.fillRect(px + 22, py + 58, 6, 12);
    ctx.fillRect(px + 88, py + 58, 6, 12);

    // Glowing Core Reactor (pulses with current phase color)
    let coreColor = '#FF9600';
    if (this.phase === 1) coreColor = '#FF2850';
    if (this.phase === 2) coreColor = '#C800FF';

    ctx.fillStyle = coreColor;
    ctx.shadowBlur = 14;
    ctx.shadowColor = coreColor;
    ctx.beginPath();
    ctx.arc(px + 58, py + 48, 8, 0, Math.PI * 2);
    ctx.fill();

    // Shield Aura if boss shield > 0
    if (this.shield > 0) {
      ctx.strokeStyle = 'rgba(0, 229, 255, 0.65)';
      ctx.lineWidth = 2.0;
      ctx.shadowBlur = 18;
      ctx.shadowColor = '#00E5FF';
      ctx.beginPath();
      ctx.ellipse(px + 58, py + 39, 64, 45, 0, 0, Math.PI * 2);
      ctx.stroke();
    }

    ctx.restore();
  }
}

