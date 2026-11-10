import { Entity } from './Entity';
import { Projectile } from './Projectile';
import { EnemyType } from '../types/game';

export abstract class Enemy extends Entity {
  public health: number;
  public maxHealth: number;
  public shield: number;
  public maxShield: number;
  public scoreValue: number;
  public enemyType: EnemyType;
  public hitFlashTimer: number = 0;
  public playfieldWidth: number = 800;
  public playfieldHeight: number = 650;

  public setPlayfieldBounds(w: number, h: number): void {
    this.playfieldWidth = w;
    this.playfieldHeight = h;
  }

  constructor(
    x: number,
    y: number,
    width: number,
    height: number,
    health: number,
    shield: number,
    scoreValue: number,
    enemyType: EnemyType
  ) {
    super(x, y, width, height);
    this.health = health;
    this.maxHealth = health;
    this.shield = shield;
    this.maxShield = shield;
    this.scoreValue = scoreValue;
    this.enemyType = enemyType;
  }

  public takeDamage(amount: number): boolean {
    if (!this.active) return false;

    // Shield absorption logic matching Ship.java
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
    }

    this.hitFlashTimer = 0.08; // Flash white for 80ms on hit

    if (this.health <= 0) {
      this.active = false;
      return true; // Destroyed
    }

    return false;
  }

  public override update(dt: number, _playerX?: number, _playerY?: number): void {
    this.y += this.vy * dt;
    this.x += this.vx * dt;
  }

  public abstract fire(playerX: number, playerY: number): Projectile[];
}

