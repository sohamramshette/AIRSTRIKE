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
import { ChaosScheduler } from '../chaos/ChaosScheduler';
import { Starfield } from './Starfield';

export class GameEngine {
  private canvas: HTMLCanvasElement;
  private ctx: CanvasRenderingContext2D;

  public state: GameState = GameState.MENU;
  public width: number = 800;
  public height: number = 650;
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
  public chaosScheduler: ChaosScheduler;

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

    this.width = canvas.width || 800;
    this.height = canvas.height || 650;

    this.player = new Player();
    this.particles = new ParticleSystem();
    this.scoreCalc = new ScoreCalculator();
    this.waveManager = new WaveManager();
    this.inputManager = new InputManager();
    this.soundService = SoundService.getInstance();
    this.starfield = new Starfield(this.width, this.height);
    this.chaosScheduler = new ChaosScheduler();

    this.player.setPlayfieldBounds(this.width, this.height);
    this.waveManager.setPlayfieldBounds(this.width, this.height);

    this.inputManager.attachCanvas(canvas);
  }

  public resize(width: number, height: number): void {
    if (width <= 0 || height <= 0) return;
    this.width = width;
    this.height = height;
    this.canvas.width = width;
    this.canvas.height = height;

    this.starfield.resize(width, height);
    this.player.setPlayfieldBounds(width, height);
    this.waveManager.setPlayfieldBounds(width, height);

    for (const enemy of this.enemies) {
      enemy.setPlayfieldBounds(width, height);
    }
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
    this.chaosScheduler.reset(this);

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

      // Chaos Protocol scheduler & dynamic anomaly simulation (Phase 11)
      this.chaosScheduler.update(dt, this);

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
          enemy.setPlayfieldBounds(this.width, this.height);
          this.enemies.push(enemy);
          if (enemy.enemyType === 'BOSS') {
            this.soundService.playBossWarning();
          }
        },
        (clearedLevel: number) => {
          // Level cleared bonus
          this.scoreCalc.addBonus(500);
          this.particles.spawnFloatingText(400, 300, 'SECTOR CLEAR +500', '#00E5FF');
          AnnouncerManager.getInstance().trigger('WAVE_CLEARED', true);
          this.chaosScheduler.onSectorCleared(clearedLevel, this);
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

    if (w < 600) {
      // --- MOBILE COMPACT DUAL POD HUD (Fits any phone width cleanly) ---
      const topY = 10;
      const podW = Math.floor((w - 24) / 2);
      const podH = 64;
      const leftX = 8;
      const rightX = w - podW - 8;

      // 1. Mobile Left Pod: Vitals
      ctx.fillStyle = 'rgba(13, 19, 33, 0.92)';
      this.roundRect(ctx, leftX, topY, podW, podH, 8);
      ctx.fill();
      ctx.strokeStyle = 'rgba(0, 229, 255, 0.4)';
      ctx.lineWidth = 1;
      this.roundRect(ctx, leftX, topY, podW, podH, 8);
      ctx.stroke();

      const labelW = 38;
      const barW = Math.max(30, podW - labelW - 14);
      const barH = 6;
      const barX = leftX + labelW + 6;

      // Hull Bar
      ctx.font = 'bold 9px Rajdhani, sans-serif';
      ctx.fillStyle = '#94A3B8';
      ctx.textAlign = 'left';
      ctx.fillText('HULL', leftX + 6, topY + 16);

      const healthPct = Math.max(0, Math.min(1, this.player.health / this.player.maxHealth));
      ctx.fillStyle = '#161F38';
      this.roundRect(ctx, barX, topY + 10, barW, barH, 2);
      ctx.fill();
      ctx.fillStyle = healthPct > 0.35 ? '#34D399' : '#EF4444';
      if (healthPct > 0) {
        this.roundRect(ctx, barX, topY + 10, barW * healthPct, barH, 2);
        ctx.fill();
      }

      // Shield Bar
      ctx.fillStyle = '#94A3B8';
      ctx.fillText('SHIELD', leftX + 6, topY + 33);

      const shieldPct = Math.max(0, Math.min(1, this.player.shield / this.player.maxShield));
      ctx.fillStyle = '#161F38';
      this.roundRect(ctx, barX, topY + 27, barW, barH, 2);
      ctx.fill();
      ctx.fillStyle = '#00E5FF';
      if (shieldPct > 0) {
        this.roundRect(ctx, barX, topY + 27, barW * shieldPct, barH, 2);
        ctx.fill();
      }

      // Vessels & Ordnance
      ctx.fillStyle = '#FBBF24';
      ctx.font = 'bold 10px Rajdhani, sans-serif';
      ctx.fillText(`VESSELS: ${this.player.lives}`, leftX + 6, topY + 53);

      const wpnTier = this.player.weaponLevel === 2 ? 'T2' : this.player.weaponLevel >= 3 ? 'T3' : 'T1';
      ctx.fillStyle = '#38BDF8';
      ctx.fillText(`GUN: ${wpnTier}`, leftX + podW - 46, topY + 53);

      // 2. Mobile Right Pod: Score & Mission
      ctx.fillStyle = 'rgba(13, 19, 33, 0.92)';
      this.roundRect(ctx, rightX, topY, podW, podH, 8);
      ctx.fill();
      ctx.strokeStyle = 'rgba(0, 229, 255, 0.4)';
      ctx.lineWidth = 1;
      this.roundRect(ctx, rightX, topY, podW, podH, 8);
      ctx.stroke();

      // Pilot Tag
      ctx.font = 'bold 10px Rajdhani, sans-serif';
      ctx.fillStyle = '#34D399';
      const pilotName = (typeof window !== 'undefined' && (window as any).__PILOT_CALLSIGN__) || 'CADET';
      ctx.fillText(`● ${String(pilotName).toUpperCase()}`, rightX + 8, topY + 16);

      // Score
      ctx.fillStyle = '#F8FAFC';
      ctx.font = 'bold 13px Rajdhani, sans-serif';
      ctx.fillText(`SCORE: ${this.scoreCalc.getScore().toLocaleString()}`, rightX + 8, topY + 34);

      // Sector & Multiplier
      ctx.fillStyle = '#FBBF24';
      ctx.font = 'bold 10px Rajdhani, sans-serif';
      ctx.fillText(`SEC ${this.waveManager.currentLevel}`, rightX + 8, topY + 53);

      const mult = this.scoreCalc.getMultiplier();
      if (mult > 1) {
        ctx.fillStyle = '#EF4444';
        ctx.fillText(`${mult}x STREAK`, rightX + podW - 54, topY + 53);
      }
    } else {
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
    }

    // --- 4. Boss Health Bar (if active) ---
    const boss = this.enemies.find(e => e.enemyType === 'BOSS') as Enemy | undefined;
    if (boss && boss.active) {
      const bw = Math.min(280, w - 40);
      const bx = (w - bw) / 2;
      const by = w < 600 ? 80 : 48;
      const bHealthPct = Math.max(0, boss.health / boss.maxHealth);

      ctx.fillStyle = (boss as any).isEnraged ? '#FF0055' : '#FF5A28';
      ctx.font = 'bold 11px Rajdhani, sans-serif';
      ctx.textAlign = 'center';
      const label = (boss as any).bossName 
        ? `${(boss as any).bossName}${(boss as any).isEnraged ? ' [ENRAGED OVERDRIVE]' : ''}`
        : 'FLAGSHIP: LEVIATHAN';
      ctx.fillText(label, w / 2, by);

      ctx.fillStyle = '#161F38';
      ctx.fillRect(bx, by + 6, bw, 8);
      ctx.fillStyle = '#DC2626';
      ctx.fillRect(bx, by + 6, bw * bHealthPct, 8);
      ctx.strokeStyle = '#FF5A28';
      ctx.strokeRect(bx, by + 6, bw, 8);
    }

    // --- 5. Center Tactical Desi Announcer Banner ---
    AnnouncerManager.getInstance().renderHUD(ctx, w, _h);

    // --- 6. Top Chaos Protocol HUD Indicator (Phase 11) ---
    this.chaosScheduler.renderHUD(ctx, w);

    ctx.restore();
  }

  private renderBanner(ctx: CanvasRenderingContext2D, w: number, _h: number, text: string): void {
    ctx.save();

    // Smooth alpha fade based on waveManager timers
    const timer = this.waveManager.bannerTimer > 0 
      ? this.waveManager.bannerTimer 
      : this.waveManager.waveClearTimer;
    const fadeAlpha = Math.max(0.1, Math.min(1, timer * 2.5));

    const bannerW = Math.min(360, Math.max(260, w - 40));
    const bannerH = 40;
    const bannerX = (w - bannerW) / 2;
    // Position below HUD pods & announcer with clean breathing room
    const bannerY = w < 600 ? 148 : Math.round(_h * 0.25);

    // Sleek cyber glass capsule
    ctx.fillStyle = `rgba(8, 14, 28, ${0.82 * fadeAlpha})`;
    this.roundRect(ctx, bannerX, bannerY, bannerW, bannerH, 10);
    ctx.fill();

    const isVictory = text.includes('SECURED');
    const isBoss = text.includes('FLAGSHIP');
    const accentColor = isVictory 
      ? `rgba(52, 211, 153, ${0.95 * fadeAlpha})` 
      : isBoss 
        ? `rgba(239, 68, 68, ${0.95 * fadeAlpha})` 
        : `rgba(0, 229, 255, ${0.95 * fadeAlpha})`;

    ctx.strokeStyle = accentColor;
    ctx.lineWidth = 1.5;
    this.roundRect(ctx, bannerX, bannerY, bannerW, bannerH, 10);
    ctx.stroke();

    // Subtle top highlight line
    ctx.strokeStyle = accentColor;
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(bannerX + 16, bannerY);
    ctx.lineTo(bannerX + bannerW - 16, bannerY);
    ctx.stroke();

    const fontSize = w < 500 ? 14 : 16;
    ctx.font = `bold ${fontSize}px Rajdhani, monospace`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillStyle = accentColor;
    ctx.shadowBlur = 10 * fadeAlpha;
    ctx.shadowColor = accentColor;
    ctx.fillText(text, w / 2, bannerY + bannerH / 2);
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

