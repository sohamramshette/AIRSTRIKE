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
        if (this.onGameOverCallback) {
          this.onGameOverCallback({
            score: this.scoreCalc.getScore(),
            levelReached: this.waveManager.currentLevel,
            durationSeconds: Math.floor(this.matchDuration),
            enemiesDestroyed: this.enemiesDestroyed
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

  private renderHUD(ctx: CanvasRenderingContext2D, w: number, _h: number): void {
    ctx.save();

    // --- Top Bar Background Ribbon ---
    ctx.fillStyle = 'rgba(7, 10, 20, 0.75)';
    ctx.fillRect(0, 0, w, 44);
    ctx.strokeStyle = 'rgba(0, 229, 255, 0.25)';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(0, 44);
    ctx.lineTo(w, 44);
    ctx.stroke();

    // --- Left: Hull (Health) & Shield Gauges ---
    // Hull gauge
    ctx.font = 'bold 10px monospace';
    ctx.fillStyle = '#94A3B8';
    ctx.fillText('HULL', 14, 16);

    const healthPct = Math.max(0, this.player.health / this.player.maxHealth);
    ctx.fillStyle = '#1E293B';
    ctx.fillRect(48, 8, 90, 10);
    ctx.fillStyle = healthPct > 0.4 ? '#22C55E' : '#EF4444';
    ctx.fillRect(48, 8, 90 * healthPct, 10);
    ctx.strokeStyle = '#334155';
    ctx.strokeRect(48, 8, 90, 10);

    // Shield gauge
    ctx.fillStyle = '#94A3B8';
    ctx.fillText('SHIELD', 14, 34);

    const shieldPct = Math.max(0, this.player.shield / this.player.maxShield);
    ctx.fillStyle = '#1E293B';
    ctx.fillRect(58, 26, 80, 10);
    ctx.fillStyle = '#00E5FF';
    ctx.shadowBlur = 8;
    ctx.shadowColor = '#00E5FF';
    ctx.fillRect(58, 26, 80 * shieldPct, 10);
    ctx.shadowBlur = 0;
    ctx.strokeStyle = '#334155';
    ctx.strokeRect(58, 26, 80, 10);

    // Vessel Lives
    ctx.fillStyle = '#00E5FF';
    for (let i = 0; i < this.player.lives; i++) {
      const lx = 152 + i * 16;
      ctx.beginPath();
      ctx.moveTo(lx + 6, 12);
      ctx.lineTo(lx + 12, 28);
      ctx.lineTo(lx, 28);
      ctx.closePath();
      ctx.fill();
    }

    // --- Center: Sector / Boss Bar ---
    const boss = this.enemies.find(e => e.enemyType === 'BOSS') as Enemy | undefined;
    if (boss && boss.active) {
      // Boss Health Bar at top center
      const bw = 280;
      const bx = (w - bw) / 2;
      const bHealthPct = Math.max(0, boss.health / boss.maxHealth);

      ctx.fillStyle = '#FF5A28';
      ctx.font = 'bold 11px monospace';
      ctx.textAlign = 'center';
      ctx.fillText('FLAGSHIP: LEVIATHAN', w / 2, 14);

      ctx.fillStyle = '#1E293B';
      ctx.fillRect(bx, 20, bw, 12);
      ctx.fillStyle = '#DC2626';
      ctx.fillRect(bx, 20, bw * bHealthPct, 12);
      ctx.strokeStyle = '#FF5A28';
      ctx.strokeRect(bx, 20, bw, 12);

      // Boss shield bar if active
      if (boss.shield > 0) {
        const bShieldPct = boss.shield / boss.maxShield;
        ctx.fillStyle = '#00E5FF';
        ctx.fillRect(bx, 34, bw * bShieldPct, 3);
      }
    } else {
      ctx.font = 'bold 13px monospace';
      ctx.fillStyle = '#BAE6FD';
      ctx.textAlign = 'center';
      ctx.fillText(`SECTOR ${this.waveManager.currentLevel}`, w / 2, 26);
    }

    // --- Right: Score & Streak Multiplier ---
    ctx.textAlign = 'right';
    ctx.font = 'bold 14px monospace';
    ctx.fillStyle = '#F8FAFC';
    ctx.fillText(`${this.scoreCalc.getScore()}`, w - 16, 20);

    ctx.font = '10px monospace';
    ctx.fillStyle = '#64748B';
    ctx.fillText(`HI: ${this.scoreCalc.getHighScore()}`, w - 16, 36);

    // Multiplier Badge
    const mult = this.scoreCalc.getMultiplier();
    if (mult > 1) {
      ctx.font = 'bold 12px monospace';
      ctx.fillStyle = mult === 4 ? '#FF5A28' : mult === 3 ? '#FFB400' : '#00E5FF';
      ctx.fillText(`x${mult} COMBO`, w - 120, 20);

      // Combo timer meter
      const comboRatio = this.scoreCalc.getComboProgress();
      ctx.fillStyle = 'rgba(255, 255, 255, 0.2)';
      ctx.fillRect(w - 180, 26, 50, 4);
      ctx.fillStyle = '#00E5FF';
      ctx.fillRect(w - 180, 26, 50 * comboRatio, 4);
    }

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

