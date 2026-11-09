import { GameState, MatchSummary, CombatStats } from '../types/game';
import { Player } from './Player';
import { Enemy } from './Enemy';
import { Projectile } from './Projectile';
import { PowerUp } from './PowerUp';
import { ParticleSystem } from './Particle';
import { ScoreCalculator } from './ScoreCalculator';
import { WaveManager } from './WaveManager';
import { CollisionManager } from './CollisionManager';
import { InputManager } from './InputManager';
import { SoundService } from './../services/SoundService';
import { AnnouncerManager } from '../announcer/AnnouncerManager';
import { Starfield } from './Starfield';

export class GameEngine {
  private canvas: HTMLCanvasElement;
  private ctx: CanvasRenderingContext2D;

  public state: GameState = GameState.MENU;
  public player: Player;
  public enemies: Enemy[] = [];
  public projectiles: Projectile[] = [];
  public powerUps: PowerUp[] = [];
  public particles: ParticleSystem;
  public scoreCalc: ScoreCalculator;
  public waveManager: WaveManager;
  public inputManager: InputManager;
  public soundService: SoundService;
  public starfield: Starfield;

  private matchDuration: number = 0;
  private enemiesDestroyed: number = 0;
  private shotsFired: number = 0;
  private shotsHit: number = 0;

  private lastTime: number = 0;
  private animationFrameId: number | null = null;
  private onGameOverCallback?: (summary: MatchSummary) => void;
  private onStateChangeCallback?: (state: GameState) => void;

  constructor(canvas: HTMLCanvasElement) {
    this.canvas = canvas;
    const context = canvas.getContext('2d');
    if (!context) {
      throw new Error('Failed to acquire 2D canvas context');
    }
    this.ctx = context;

    this.player = new Player();
    this.particles = new ParticleSystem();
    this.scoreCalc = new ScoreCalculator();
    this.waveManager = new WaveManager();
    this.inputManager = new InputManager();
    this.soundService = SoundService.getInstance();
    this.starfield = new Starfield(canvas.width, canvas.height);

    this.inputManager.attachCanvas(canvas);
  }

  public setGameOverCallback(cb: (summary: MatchSummary) => void): void {
    this.onGameOverCallback = cb;
  }

  public setStateChangeCallback(cb: (state: GameState) => void): void {
    this.onStateChangeCallback = cb;
  }

  public start(): void {
    this.lastTime = performance.now();
    this.loop(this.lastTime);
  }

  public stop(): void {
    if (this.animationFrameId !== null) {
      cancelAnimationFrame(this.animationFrameId);
      this.animationFrameId = null;
    }
  }

  public startNewGame(): void {
    this.player.reset();
    this.enemies = [];
    this.projectiles = [];
    this.powerUps = [];
    this.particles.clear();
    this.scoreCalc.reset();
    this.waveManager.reset();

    this.matchDuration = 0;
    this.enemiesDestroyed = 0;
    this.shotsFired = 0;
    this.shotsHit = 0;

    AnnouncerManager.getInstance().reset();
    AnnouncerManager.getInstance().trigger('WAVE_START', false);

    this.setState(GameState.PLAYING);
  }

  public pauseGame(): void {
    if (this.state === GameState.PLAYING) {
      this.setState(GameState.PAUSED);
    }
  }

  public resumeGame(): void {
    if (this.state === GameState.PAUSED) {
      this.setState(GameState.PLAYING);
    }
  }

  public returnToMenu(): void {
    this.setState(GameState.MENU);
  }

  private setState(newState: GameState): void {
    if (this.state !== GameState.GAME_OVER && newState === GameState.GAME_OVER) {
      AnnouncerManager.getInstance().trigger('GAME_OVER', true);
    }
    this.state = newState;
    if (this.onStateChangeCallback) {
      this.onStateChangeCallback(newState);
    }
  }

  private loop = (timestamp: number): void => {
    const rawDt = (timestamp - this.lastTime) / 1000.0;
    this.lastTime = timestamp;

    // Delta time clamping to 33ms to protect against background tab lags
    const dt = Math.min(rawDt, 0.033);

    this.update(dt);
    this.render();

    this.animationFrameId = requestAnimationFrame(this.loop);
  };

  private update(dt: number): void {
    // Starfield animates in all states
    this.starfield.update(dt);

    // Global shortcut handles
    if (this.inputManager.consumeMute()) {
      this.soundService.toggleMute();
    }

    if (this.state === GameState.PLAYING) {
      if (this.inputManager.consumePause()) {
        this.pauseGame();
        return;
      }

      this.matchDuration += dt;

      // Desi Toxic Announcer AI telemetry and live commentary
      AnnouncerManager.getInstance().update(dt, this);

      // 1. Player input & movement
      const move = this.inputManager.getMovementVector();
      this.player.updateMovement(move.x, move.y);
      this.player.update(dt);

      // Player weapons
      if (this.inputManager.isFiring()) {
        const bullets = this.player.fire();
        if (bullets.length > 0) {
          this.soundService.playLaser();
          this.shotsFired += bullets.length;
          this.projectiles.push(...bullets);
        }
      }

      // 2. Wave manager update & enemy spawning
      this.waveManager.update(
        dt,
        this.enemies.length,
        (enemy: Enemy) => {
          this.enemies.push(enemy);
          if (enemy.enemyType === 'BOSS') {
            this.soundService.playBossWarning();
          }
        },
        (_clearedLevel: number) => {
          // Level cleared bonus
          this.scoreCalc.addBonus(500);
          this.particles.spawnFloatingText(400, 300, 'SECTOR CLEAR +500', '#00E5FF');
          AnnouncerManager.getInstance().trigger('WAVE_CLEARED', true);
        }
      );

      // 3. Update Enemies & their firing
      for (let i = this.enemies.length - 1; i >= 0; i--) {
        const enemy = this.enemies[i];
        enemy.update(dt, this.player.x, this.player.y);

        if (enemy.active) {
          const enemyBullets = enemy.fire(this.player.x, this.player.y);
          if (enemyBullets.length > 0) {
            if (enemy.enemyType === 'BOSS') {
              this.soundService.playBossLaser();
            } else {
              this.soundService.playEnemyLaser();
            }
            this.projectiles.push(...enemyBullets);
          }
        } else {
          this.enemies.splice(i, 1);
          this.enemiesDestroyed += 1;
        }
      }

      // 4. Update Projectiles
      for (let i = this.projectiles.length - 1; i >= 0; i--) {
        const p = this.projectiles[i];
        p.update(dt);
        if (!p.active) {
          this.projectiles.splice(i, 1);
        }
      }

      // 5. Update Power-Ups
      for (let i = this.powerUps.length - 1; i >= 0; i--) {
        const pu = this.powerUps[i];
        pu.update(dt);
        if (!pu.active) {
          this.powerUps.splice(i, 1);
        }
      }

      // 6. Collision Resolution
      CollisionManager.handlePlayerProjectiles(
        this.projectiles,
        this.enemies,
        this.powerUps,
        this.particles,
        this.scoreCalc,
        this.soundService
      );

      CollisionManager.handleEnemyProjectiles(
        this.projectiles,
        this.player,
        this.particles,
        this.scoreCalc,
        this.soundService
      );

      CollisionManager.handleShipRamming(
        this.player,
        this.enemies,
        this.particles,
        this.scoreCalc,
        this.soundService
      );

      CollisionManager.handlePowerUps(
        this.powerUps,
        this.player,
        this.particles,
        this.scoreCalc,
        this.soundService
      );

      // 7. Update Score & Combo Timers
      this.scoreCalc.update(dt);

      // 8. Update Particles
      this.particles.update(dt);

      // 9. Check Game Over
      if (!this.player.active && this.player.lives <= 0) {
        this.setState(GameState.GAME_OVER);
        const accuracy = this.shotsFired > 0
          ? Math.min(100, Math.round(((this.enemiesDestroyed * 2) / Math.max(1, this.shotsFired)) * 100))
          : 0;
        if (this.onGameOverCallback) {
          this.onGameOverCallback({
            score: this.scoreCalc.getScore(),
            levelReached: this.waveManager.currentLevel,
            durationSeconds: Math.floor(this.matchDuration),
            enemiesDestroyed: this.enemiesDestroyed,
            accuracy: accuracy
          });
        }
      }
    } else if (this.state === GameState.PAUSED) {
      if (this.inputManager.consumePause()) {
        this.resumeGame();
      }
    } else if (this.state === GameState.GAME_OVER) {
      if (this.inputManager.consumeRestart()) {
        this.startNewGame();
      }
    }
  }

  private render(): void {
    const ctx = this.ctx;
    const w = this.canvas.width;
    const h = this.canvas.height;

    // 1. Draw Parallax Space Background
    this.starfield.render(ctx);

    // 2. Render Gameplay Entities if active or paused
    if (this.state === GameState.PLAYING || this.state === GameState.PAUSED || this.state === GameState.GAME_OVER) {
      // Power-ups
      for (const pu of this.powerUps) {
        pu.render(ctx);
      }

      // Enemies
      for (const enemy of this.enemies) {
        enemy.render(ctx);
      }

      // Projectiles
      for (const p of this.projectiles) {
        p.render(ctx);
      }

      // Player
      this.player.render(ctx);

      // Particle explosions & floating damage numbers
      this.particles.render(ctx);

      // In-game Canvas HUD
      this.renderHUD(ctx, w, h);

      // Sector clear banner
      if (this.waveManager.bannerText) {
        this.renderBanner(ctx, w, h, this.waveManager.bannerText);
      }
    }
  }

  private roundRect(ctx: CanvasRenderingContext2D, x: number, y: number, w: number, h: number, r: number): void {
    ctx.beginPath();
    ctx.moveTo(x + r, y);
    ctx.lineTo(x + w - r, y);
    ctx.quadraticCurveTo(x + w, y, x + w, y + r);
    ctx.lineTo(x + w, y + h - r);
    ctx.quadraticCurveTo(x + w, y + h, x + w - r, y + h);
    ctx.lineTo(x + r, y + h);
    ctx.quadraticCurveTo(x, y + h, x, y + h - r);
    ctx.lineTo(x, y + r);
    ctx.quadraticCurveTo(x, y, x + r, y);
    ctx.closePath();
  }

  private renderHUD(ctx: CanvasRenderingContext2D, w: number, _h: number): void {
    ctx.save();

    // --- 1. Top-Left Vitals Pod (Matching Java Swing Theme) ---
    const leftW = 280;
    const leftH = 76;
    const leftX = 20;
    const leftY = 20;

    ctx.fillStyle = 'rgba(13, 19, 33, 0.88)';
    this.roundRect(ctx, leftX, leftY, leftW, leftH, 12);
    ctx.fill();
    ctx.strokeStyle = 'rgba(0, 229, 255, 0.35)';
    ctx.lineWidth = 1;
    this.roundRect(ctx, leftX, leftY, leftW, leftH, 12);
    ctx.stroke();

    ctx.strokeStyle = '#00E5FF';
    ctx.beginPath();
    ctx.moveTo(leftX + 20, leftY);
    ctx.lineTo(leftX + leftW - 20, leftY);
    ctx.stroke();

    const barW = 150;
    const barH = 10;
    const barX = leftX + 115;

    // Health Bar
    ctx.font = 'bold 10px Rajdhani, sans-serif';
    ctx.fillStyle = '#94A3B8';
    ctx.textAlign = 'left';
    ctx.fillText('HULL INTEGRITY', leftX + 14, leftY + 20);

    const healthPct = Math.max(0, Math.min(1, this.player.health / this.player.maxHealth));
    ctx.fillStyle = '#161F38';
    this.roundRect(ctx, barX, leftY + 11, barW, barH, 4);
    ctx.fill();

    const hCol = healthPct > 0.35 ? '#34D399' : '#EF4444';
    ctx.fillStyle = hCol;
    if (healthPct > 0) {
      this.roundRect(ctx, barX, leftY + 11, barW * healthPct, barH, 4);
      ctx.fill();
    }
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.15)';
    this.roundRect(ctx, barX, leftY + 11, barW, barH, 4);
    ctx.stroke();

    // Shield Bar
    ctx.fillStyle = '#94A3B8';
    ctx.fillText('SHIELD CAPACITOR', leftX + 14, leftY + 40);

    const shieldPct = Math.max(0, Math.min(1, this.player.shield / this.player.maxShield));
    ctx.fillStyle = '#161F38';
    this.roundRect(ctx, barX, leftY + 31, barW, barH, 4);
    ctx.fill();

    ctx.fillStyle = '#00E5FF';
    if (shieldPct > 0) {
      this.roundRect(ctx, barX, leftY + 31, barW * shieldPct, barH, 4);
      ctx.fill();
    }
    ctx.strokeStyle = 'rgba(0, 229, 255, 0.4)';
    this.roundRect(ctx, barX, leftY + 31, barW, barH, 4);
    ctx.stroke();

    // Vessels & Weapon Tier
    ctx.fillStyle = '#FBBF24';
    ctx.font = 'bold 11px Rajdhani, sans-serif';
    ctx.fillText(`VESSELS: ${this.player.lives}`, leftX + 14, leftY + 62);

    ctx.fillStyle = '#38BDF8';
    const wpnTier = this.player.weaponLevel === 2 ? 'DUAL CANNONS' : this.player.weaponLevel >= 3 ? 'TRIPLE SPREAD' : 'SINGLE CANNON';
    ctx.fillText(`ORDNANCE: ${wpnTier}`, leftX + 115, leftY + 62);

    // --- 2. Top-Right Mission Pod ---
    const rightW = 230;
    const rightH = 92;
    const rightX = w - rightW - 20;
    const rightY = 20;

    ctx.fillStyle = 'rgba(13, 19, 33, 0.88)';
    this.roundRect(ctx, rightX, rightY, rightW, rightH, 12);
    ctx.fill();
    ctx.strokeStyle = 'rgba(0, 229, 255, 0.35)';
    ctx.lineWidth = 1;
    this.roundRect(ctx, rightX, rightY, rightW, rightH, 12);
    ctx.stroke();

    ctx.strokeStyle = '#00E5FF';
    ctx.beginPath();
    ctx.moveTo(rightX + 20, rightY);
    ctx.lineTo(rightX + rightW - 20, rightY);
    ctx.stroke();

    // Pilot Tag
    ctx.font = 'bold 11px Rajdhani, sans-serif';
    ctx.fillStyle = '#34D399';
    const pilotName = (typeof window !== 'undefined' && (window as any).__PILOT_CALLSIGN__) || 'CADET';
    ctx.fillText(`● PILOT: ${String(pilotName).toUpperCase()}`, rightX + 14, rightY + 20);

    // Score
    ctx.fillStyle = '#F8FAFC';
    ctx.font = 'bold 16px Rajdhani, sans-serif';
    ctx.fillText(`SCORE: ${this.scoreCalc.getScore().toLocaleString()}`, rightX + 14, rightY + 42);

    // High Score
    ctx.fillStyle = '#94A3B8';
    ctx.font = '12px Rajdhani, sans-serif';
    ctx.fillText(`RECORD: ${this.scoreCalc.getHighScore().toLocaleString()}`, rightX + 14, rightY + 60);

    // Sector & Multiplier
    ctx.fillStyle = '#FBBF24';
    ctx.font = 'bold 12px Rajdhani, sans-serif';
    ctx.fillText(`SECTOR: ${this.waveManager.currentLevel}`, rightX + 14, rightY + 80);

    const mult = this.scoreCalc.getMultiplier();
    if (mult > 1) {
      ctx.fillStyle = '#EF4444';
      ctx.fillText(`${mult}x STREAK!`, rightX + 120, rightY + 80);
    }

    // --- 3. Center Pause Hint Pill ---
    const pillW = 86;
    const pillH = 22;
    const pillX = (w - pillW) / 2;
    const pillY = 16;
    ctx.fillStyle = 'rgba(13, 19, 33, 0.8)';
    this.roundRect(ctx, pillX, pillY, pillW, pillH, 10);
    ctx.fill();
    ctx.strokeStyle = 'rgba(30, 41, 59, 0.8)';
    this.roundRect(ctx, pillX, pillY, pillW, pillH, 10);
    ctx.stroke();
    ctx.fillStyle = '#94A3B8';
    ctx.font = 'bold 10px Rajdhani, sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText('[P] PAUSE', w / 2, pillY + 15);

    // --- 4. Boss Health Bar (if active) ---
    const boss = this.enemies.find(e => e.enemyType === 'BOSS') as Enemy | undefined;
    if (boss && boss.active) {
      const bw = 280;
      const bx = (w - bw) / 2;
      const bHealthPct = Math.max(0, boss.health / boss.maxHealth);

      ctx.fillStyle = '#FF5A28';
      ctx.font = 'bold 11px Rajdhani, sans-serif';
      ctx.textAlign = 'center';
      ctx.fillText('FLAGSHIP: LEVIATHAN', w / 2, 48);

      ctx.fillStyle = '#161F38';
      ctx.fillRect(bx, 54, bw, 10);
      ctx.fillStyle = '#DC2626';
      ctx.fillRect(bx, 54, bw * bHealthPct, 10);
      ctx.strokeStyle = '#FF5A28';
      ctx.strokeRect(bx, 54, bw, 10);
    }

    // --- 5. Center Tactical Desi Announcer Banner ---
    AnnouncerManager.getInstance().renderHUD(ctx, w, _h);

    ctx.restore();
  }

  private renderBanner(ctx: CanvasRenderingContext2D, w: number, h: number, text: string): void {
    ctx.save();
    ctx.fillStyle = 'rgba(7, 10, 20, 0.85)';
    ctx.fillRect(0, h / 2 - 40, w, 80);
    ctx.strokeStyle = '#00E5FF';
    ctx.lineWidth = 2;
    ctx.strokeRect(0, h / 2 - 40, w, 80);

    ctx.font = 'bold 24px monospace';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillStyle = '#00E5FF';
    ctx.shadowBlur = 14;
    ctx.shadowColor = '#00E5FF';
    ctx.fillText(text, w / 2, h / 2);
    ctx.restore();
  }

  public isBossActive(): boolean {
    return this.enemies.some(e => e.active && e.enemyType === 'BOSS');
  }

  public getCombatStats(): CombatStats {
    return {
      kills: this.enemiesDestroyed,
      shotsFired: this.shotsFired,
      shotsHit: this.shotsHit,
      maxCombo: this.scoreCalc.getMaxStreak(),
      powerUpsCollected: 0
    };
  }
}

