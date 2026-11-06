import { Entity } from './Entity';
import { Projectile } from './Projectile';

export class Player extends Entity {
  public maxHealth: number = 100;
  public health: number = 100;
  public maxShield: number = 100;
  public shield: number = 100;
  public lives: number = 3;
  public baseSpeed: number = 320.0;
  public speed: number = 320.0;
  public weaponLevel: number = 1;

  public shootCooldown: number = 0.18; // 180ms
  public shootTimer: number = 0;

  public invulnerableTimer: number = 0;
  public blinkTimer: number = 0;
  public isVisible: boolean = true;

  private animTime: number = 0;

  constructor(x: number = 376.0, y: number = 540.0) {
    super(x, y, 48, 56);
    this.reset();
  }

  public reset(): void {
    this.x = 376.0;
    this.y = 540.0;
    this.vx = 0;
    this.vy = 0;
    this.health = this.maxHealth;
    this.shield = this.maxShield;
    this.lives = 3;
    this.speed = this.baseSpeed;
    this.weaponLevel = 1;
    this.shootTimer = 0;
    this.invulnerableTimer = 2.5; // 2.5s respawn protection
    this.blinkTimer = 0;
    this.isVisible = true;
    this.active = true;
  }

  public respawn(): void {
    this.x = 376.0;
    this.y = 540.0;
    this.vx = 0;
    this.vy = 0;
    this.health = this.maxHealth;
    this.shield = this.maxShield;
    this.invulnerableTimer = 2.5;
    this.blinkTimer = 0;
    this.isVisible = true;
  }

  public updateMovement(inputX: number, inputY: number): void {
    let dx = inputX;
    let dy = inputY;

    if (dx !== 0 && dy !== 0) {
      const norm = 1.0 / Math.SQRT2; // 0.70710678
      dx *= norm;
      dy *= norm;
    }

    this.vx = dx * this.speed;
    this.vy = dy * this.speed;
  }

  public update(dt: number): void {
    this.animTime += dt;
    this.x += this.vx * dt;
    this.y += this.vy * dt;

    // Strict boundary clamping
    if (this.x < 0) this.x = 0;
    if (this.x > 800 - this.width) this.x = 800 - this.width;
    if (this.y < 0) this.y = 0;
    if (this.y > 650 - this.height) this.y = 650 - this.height;

    // Cooldown & invulnerability
    if (this.shootTimer > 0) {
      this.shootTimer -= dt;
    }

    if (this.invulnerableTimer > 0) {
      this.invulnerableTimer -= dt;
      this.blinkTimer += dt;
      if (this.blinkTimer >= 0.1) {
        this.isVisible = !this.isVisible;
        this.blinkTimer = 0;
      }
    } else {
      this.isVisible = true;
    }
  }

  public fire(): Projectile[] {
    if (this.shootTimer > 0 || !this.active) {
      return [];
    }

    this.shootTimer = this.shootCooldown;
    const bullets: Projectile[] = [];

    if (this.weaponLevel === 1) {
      // Single Cannon
      bullets.push(
        new Projectile(
          this.x + 21,
          this.y - 4,
          0,
          -560.0,
          30, // 25 + 5 * 1
          true,
          6,
          16,
          '#B4FFFF',
          'rgba(0, 210, 255, 0.7)'
        )
      );
    } else if (this.weaponLevel === 2) {
      // Dual Wingtip Cannons
      bullets.push(
        new Projectile(
          this.x + 6,
          this.y + 4,
          0,
          -560.0,
          35, // 25 + 5 * 2
          true,
          6,
          16,
          '#B4FFFF',
          'rgba(0, 210, 255, 0.7)'
        ),
        new Projectile(
          this.x + 37,
          this.y + 4,
          0,
          -560.0,
          35,
          true,
          6,
          16,
          '#B4FFFF',
          'rgba(0, 210, 255, 0.7)'
        )
      );
    } else {
      // Triple Spread Cannons (Level 3+)
      bullets.push(
        new Projectile(
          this.x + 21,
          this.y - 4,
          0,
          -560.0,
          45, // Center heavy
          true,
          6,
          18,
          '#FFFFFF',
          'rgba(0, 230, 255, 0.9)'
        ),
        new Projectile(
          this.x + 6,
          this.y + 4,
          -110.0,
          -560.0,
          40, // Left angled
          true,
          6,
          16,
          '#B4FFFF',
          'rgba(0, 210, 255, 0.7)'
        ),
        new Projectile(
          this.x + 37,
          this.y + 4,
          110.0,
          -560.0,
          40, // Right angled
          true,
          6,
          16,
          '#B4FFFF',
          'rgba(0, 210, 255, 0.7)'
        )
      );
    }

    return bullets;
  }

  public takeDamage(amount: number, isRamming: boolean = false): boolean {
    if (this.invulnerableTimer > 0 || !this.active) {
      return false;
    }

    // Shield absorption logic matching Ship.java
    let remainingDamage = amount;
    if (this.shield > 0) {
      if (this.shield >= remainingDamage) {
        this.shield -= remainingDamage;
        remainingDamage = 0;
      } else {
        remainingDamage -= this.shield;
        this.shield = 0;
      }
    }

    if (remainingDamage > 0) {
      this.health = Math.max(0, this.health - remainingDamage);
    }

    // Set impact invulnerability: 0.4s for laser, 0.8s for ramming
    this.invulnerableTimer = isRamming ? 0.8 : 0.4;
    this.blinkTimer = 0;

    if (this.health <= 0) {
      this.lives -= 1;
      if (this.lives > 0) {
        this.respawn();
      } else {
        this.active = false;
      }
    }

    return true;
  }

  public heal(amount: number): void {
    this.health = Math.min(this.maxHealth, this.health + amount);
  }

  public rechargeShield(amount: number): void {
    this.shield = Math.min(this.maxShield, this.shield + amount);
  }

  public upgradeWeapon(): void {
    if (this.weaponLevel < 3) {
      this.weaponLevel += 1;
    }
  }

  public boostSpeed(amount: number = 25.0): void {
    this.speed += amount;
  }

  public render(ctx: CanvasRenderingContext2D): void {
    if (!this.active || !this.isVisible) return;

    ctx.save();
    const px = this.x;
    const py = this.y;

    // Thruster engine flames (left & right engine nozzles)
    const flameLen = 12.0 + 6.0 * Math.sin(this.animTime * 18.0);
    ctx.fillStyle = '#FF7814';
    ctx.shadowBlur = 10;
    ctx.shadowColor = '#FF5A28';

    // Left flame
    ctx.beginPath();
    ctx.moveTo(px + 8, py + 52);
    ctx.lineTo(px + 12, py + 52 + flameLen);
    ctx.lineTo(px + 16, py + 52);
    ctx.fill();

    // Right flame
    ctx.beginPath();
    ctx.moveTo(px + 32, py + 52);
    ctx.lineTo(px + 36, py + 52 + flameLen);
    ctx.lineTo(px + 40, py + 52);
    ctx.fill();

    // Inner bright flame core
    ctx.fillStyle = '#FFF050';
    ctx.beginPath();
    ctx.moveTo(px + 10, py + 52);
    ctx.lineTo(px + 12, py + 52 + flameLen * 0.6);
    ctx.lineTo(px + 14, py + 52);
    ctx.fill();

    ctx.beginPath();
    ctx.moveTo(px + 34, py + 52);
    ctx.lineTo(px + 36, py + 52 + flameLen * 0.6);
    ctx.lineTo(px + 38, py + 52);
    ctx.fill();

    // 8-Point Hull Polygon matching Java desktop implementation
    ctx.shadowBlur = 12;
    ctx.shadowColor = '#00D2FF';
    ctx.fillStyle = '#1E2D4B';
    ctx.strokeStyle = '#00D2FF';
    ctx.lineWidth = 1.8;

    ctx.beginPath();
    ctx.moveTo(px + 24, py);           // 1. Nose Tip
    ctx.lineTo(px + 38, py + 36);      // 2. Right Shoulder
    ctx.lineTo(px + 48, py + 48);      // 3. Right Wingtip
    ctx.lineTo(px + 36, py + 52);      // 4. Right Engine
    ctx.lineTo(px + 24, py + 44);      // 5. Center Aft
    ctx.lineTo(px + 12, py + 52);      // 6. Left Engine
    ctx.lineTo(px, py + 48);           // 7. Left Wingtip
    ctx.lineTo(px + 10, py + 36);      // 8. Left Shoulder
    ctx.closePath();
    ctx.fill();
    ctx.stroke();

    // Cockpit Canopy
    ctx.fillStyle = '#00E5FF';
    ctx.shadowBlur = 6;
    ctx.shadowColor = '#00E5FF';
    ctx.beginPath();
    ctx.ellipse(px + 24, py + 26, 4, 8, 0, 0, Math.PI * 2);
    ctx.fill();

    // Shield Aura if active shield > 0
    if (this.shield > 0) {
      const shieldAlpha = Math.min(0.6, (this.shield / this.maxShield) * 0.5 + 0.1);
      ctx.strokeStyle = `rgba(0, 229, 255, ${shieldAlpha})`;
      ctx.lineWidth = 2.0;
      ctx.shadowBlur = 14;
      ctx.shadowColor = '#00E5FF';
      ctx.beginPath();
      ctx.ellipse(px + 24, py + 28, 28, 34, 0, 0, Math.PI * 2);
      ctx.stroke();
    }

    ctx.restore();
  }
}

