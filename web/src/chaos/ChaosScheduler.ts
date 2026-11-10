import { ChaosEventType, CHAOS_DEFINITIONS, isCompatibleChaosEvents } from './ChaosEventType';
import { ActiveChaosEvent } from './ActiveChaosEvent';
import { AnnouncerManager } from '../announcer/AnnouncerManager';
import { Fighter } from '../game/Fighter';
import { Armoured } from '../game/Armoured';
import { Projectile } from '../game/Projectile';

export class ChaosScheduler {
  public enabled: boolean = true;
  public firstEventDelay: number;
  public minInterval: number;
  public maxInterval: number;
  public nextEventTimer: number;
  public maxActiveEvents: number = 4;

  public activeEvents: ActiveChaosEvent[] = [];
  public lastTriggered: ChaosEventType | null = null;
  public history: ChaosEventType[] = [];

  constructor(firstDelay: number = 45.0, minInt: number = 18.0, maxInt: number = 35.0) {
    this.firstEventDelay = Math.max(1.0, firstDelay);
    this.minInterval = Math.max(5.0, minInt);
    this.maxInterval = Math.max(this.minInterval, maxInt);
    this.nextEventTimer = this.firstEventDelay;
  }

  public reset(engine?: any): void {
    if (engine && engine.player) {
      this.restorePlayerState(engine.player);
    }
    this.activeEvents = [];
    this.nextEventTimer = this.firstEventDelay;
    this.lastTriggered = null;
  }

  public update(dt: number, engine: any): void {
    if (!this.enabled || !engine || !engine.player || !engine.player.active) {
      return;
    }

    const currentLevel = engine.waveManager?.currentLevel ?? 1;

    // 1. Next event trigger countdown
    if (this.nextEventTimer > 0) {
      this.nextEventTimer -= dt;
      if (this.nextEventTimer <= 0 && this.activeEvents.length < this.maxActiveEvents) {
        const primary = this.pickEligibleEvent(currentLevel);
        if (primary) {
          const secondary = this.pickCompatibleSecondary(primary, currentLevel);
          if (secondary && (this.activeEvents.length + 2 <= this.maxActiveEvents)) {
            this.triggerDualChaos(primary, secondary, engine, 3.0);
          } else {
            this.triggerEvent(primary, engine, 3.0);
          }
        }
        this.nextEventTimer = this.minInterval + Math.random() * (this.maxInterval - this.minInterval);
      }
    }

    // 2. Active events update and effects
    for (let i = this.activeEvents.length - 1; i >= 0; i--) {
      const event = this.activeEvents[i];
      const wasWarning = event.isWarning();

      if (wasWarning && !event.warningAnnounced) {
        if (event.dualPartnerType) {
          const partner = this.activeEvents.find(e => e.type === event.dualPartnerType);
          const def1 = event.definition;
          const def2 = CHAOS_DEFINITIONS[event.dualPartnerType];
          const name2 = def2 ? def2.displayName : event.dualPartnerType;
          AnnouncerManager.getInstance().triggerChaosLine(
            `⚠️ DUAL ANOMALY ALERT! ${def1.displayName} + ${name2} ek saath shuru hone wale hain! Dono se bach ke rehna!`,
            'GENERAL_ROAST'
          );
          event.warningAnnounced = true;
          if (partner) partner.warningAnnounced = true;
        } else {
          event.warningAnnounced = true;
          AnnouncerManager.getInstance().triggerChaosLine(
            event.definition.warningLine,
            event.definition.category
          );
        }
      }

      const justActivated = event.update(dt);

      if (justActivated) {
        if (event.dualPartnerType) {
          if (!event.activationAnnounced) {
            const partner = this.activeEvents.find(e => e.type === event.dualPartnerType);
            const def1 = event.definition;
            const def2 = CHAOS_DEFINITIONS[event.dualPartnerType];
            const name2 = def2 ? def2.displayName : event.dualPartnerType;
            AnnouncerManager.getInstance().triggerChaosLine(
              `⚡ DUAL CHAOS CONVERGENCE! ${def1.displayName} aur ${name2} dono active! Ab survive karke dikha!`,
              'BOLLYWOOD'
            );
            event.activationAnnounced = true;
            if (partner) partner.activationAnnounced = true;
          }
        } else {
          AnnouncerManager.getInstance().triggerChaosLine(
            event.definition.activateLine,
            event.definition.category
          );
        }
        this.applyInitialActivationEffect(event, engine);
      }

      if (event.isActive()) {
        this.applyContinuousEffect(event, dt, engine);
      }

      if (event.isExpired()) {
        this.cleanupEventEffect(event, engine);
        this.activeEvents.splice(i, 1);
      }
    }
  }

  /**
   * Triggered when the player clears a sector.
   * Schedules an advance warning identifying a DUAL CHAOS CONVERGENCE for the next sector,
   * announces it via the Announcer, and applies both anomalies simultaneously!
   */
  public onSectorCleared(clearedSector: number, engine: any): ActiveChaosEvent | null {
    if (!this.enabled || !engine) return null;
    const nextSector = clearedSector + 1;

    const primary = this.pickEligibleEvent(nextSector);
    if (!primary) return null;

    const secondary = this.pickCompatibleSecondary(primary, nextSector);
    const warningDuration = 4.0;

    let event: ActiveChaosEvent;
    if (secondary) {
      event = this.triggerDualChaos(primary, secondary, engine, warningDuration);
    } else {
      event = this.triggerEvent(primary, engine, warningDuration)!;
    }

    // Push next interval so periodic timer does not collide
    this.nextEventTimer = this.minInterval + Math.random() * (this.maxInterval - this.minInterval);
    return event;
  }

  /**
   * Picks a compatible, non-conflicting secondary chaos event for Dual Chaos Convergence.
   */
  public pickCompatibleSecondary(primary: ChaosEventType, currentLevel: number): ChaosEventType | null {
    const allTypes = Object.values(ChaosEventType);
    let candidates = allTypes.filter(t => {
      if (t === primary) return false;
      if (t === this.lastTriggered) return false;
      const def = CHAOS_DEFINITIONS[t];
      if (!def || currentLevel < def.minDifficulty) return false;
      if (!isCompatibleChaosEvents(primary, t)) return false;

      for (const active of this.activeEvents) {
        if (!isCompatibleChaosEvents(t, active.type)) return false;
      }
      return true;
    });

    if (candidates.length === 0) {
      candidates = allTypes.filter(t => {
        if (t === primary) return false;
        if (!isCompatibleChaosEvents(primary, t)) return false;
        for (const active of this.activeEvents) {
          if (!isCompatibleChaosEvents(t, active.type)) return false;
        }
        return true;
      });
    }

    if (candidates.length === 0) return null;
    return candidates[Math.floor(Math.random() * candidates.length)];
  }

  public pickEligibleEvent(currentLevel: number): ChaosEventType | null {
    const allTypes = Object.values(ChaosEventType);
    let candidates = allTypes.filter(t => {
      if (t === this.lastTriggered) return false;
      const def = CHAOS_DEFINITIONS[t];
      if (!def || currentLevel < def.minDifficulty) return false;

      for (const active of this.activeEvents) {
        if (!isCompatibleChaosEvents(t, active.type)) return false;
      }
      return true;
    });

    if (candidates.length === 0) {
      candidates = allTypes.filter(t => {
        for (const active of this.activeEvents) {
          if (!isCompatibleChaosEvents(t, active.type)) return false;
        }
        return true;
      });
    }

    if (candidates.length === 0) return null;
    return candidates[Math.floor(Math.random() * candidates.length)];
  }

  public triggerDualChaos(
    primary: ChaosEventType,
    secondary: ChaosEventType,
    _engine?: any,
    warningDuration: number = 4.0
  ): ActiveChaosEvent {
    const def1 = CHAOS_DEFINITIONS[primary];
    const def2 = CHAOS_DEFINITIONS[secondary];

    const event1 = new ActiveChaosEvent(primary, def1?.defaultDuration ?? 10.0, warningDuration);
    const event2 = new ActiveChaosEvent(secondary, def2?.defaultDuration ?? 10.0, warningDuration);

    event1.dualPartnerType = secondary;
    event2.dualPartnerType = primary;

    this.activeEvents.push(event1);
    this.activeEvents.push(event2);

    this.lastTriggered = secondary;
    this.history.push(primary);
    this.history.push(secondary);
    while (this.history.length > 20) this.history.shift();

    return event1;
  }

  public triggerEvent(type: ChaosEventType, _engine?: any, warningDuration: number = 3.0): ActiveChaosEvent | null {
    if (!type) return null;

    if (type === ChaosEventType.TOTAL_CHAOS) {
      const totalEvent = new ActiveChaosEvent(ChaosEventType.TOTAL_CHAOS, 12.0, warningDuration);
      this.activeEvents.push(totalEvent);
      this.lastTriggered = ChaosEventType.TOTAL_CHAOS;

      const secondaryPool = [
        ChaosEventType.REVERSE_CONTROLS,
        ChaosEventType.SPEED_DEMON,
        ChaosEventType.EMERGENCY_SHIELD,
        ChaosEventType.MAGNETIC_BULLETS,
        ChaosEventType.CRITICAL_OVERDRIVE,
        ChaosEventType.RAPID_FIRE
      ];
      const secondary = secondaryPool[Math.floor(Math.random() * secondaryPool.length)];
      this.activeEvents.push(new ActiveChaosEvent(secondary, 12.0, warningDuration));
      return totalEvent;
    }

    const def = CHAOS_DEFINITIONS[type];
    const event = new ActiveChaosEvent(type, def?.defaultDuration ?? 10.0, warningDuration);
    this.activeEvents.push(event);
    this.lastTriggered = type;
    this.history.push(type);
    if (this.history.length > 20) this.history.shift();

    return event;
  }

  private applyInitialActivationEffect(event: ActiveChaosEvent, engine: any): void {
    const player = engine.player;
    const enemies = engine.enemies || [];
    const projectiles = engine.projectiles || [];

    switch (event.type) {
      case ChaosEventType.REVERSE_CONTROLS:
        if (player) player.controlsInverted = true;
        break;

      case ChaosEventType.SPEED_DEMON:
        if (player) {
          event.cachedPlayerSpeed = player.speed;
          player.speed *= 1.7;
        }
        break;

      case ChaosEventType.RAPID_FIRE:
        if (player) {
          event.cachedShootCooldown = player.shootCooldown;
          player.shootCooldown = 0.06; // 3x faster
        }
        break;

      case ChaosEventType.WEAPON_JAM:
        if (player) {
          event.cachedShootCooldown = player.shootCooldown;
          player.shootCooldown = player.shootCooldown * 3.5; // Pipeline stall
        }
        break;

      case ChaosEventType.CRITICAL_OVERDRIVE:
        if (player) {
          event.cachedDamageMultiplier = player.damageMultiplier || 1.0;
          player.damageMultiplier = 2.0;
        }
        break;

      case ChaosEventType.EMERGENCY_SHIELD:
        if (player) {
          player.invulnerableTimer = event.totalDuration;
        }
        break;

      case ChaosEventType.QUANTUM_COLLAPSE:
        let count = 0;
        for (const p of projectiles) {
          if (p.active && !p.isPlayer) {
            if (count % 2 === 0) p.active = false;
            count++;
          }
        }
        break;

      case ChaosEventType.DOUBLE_TROUBLE:
        let clones = 0;
        const toAdd: any[] = [];
        for (const e of enemies) {
          if (e.active && e.type !== 'boss' && clones < 4) {
            const nx = Math.max(40, Math.min(740, e.x + 45));
            toAdd.push(new Fighter(nx, e.y));
            clones++;
          }
        }
        enemies.push(...toAdd);
        break;

      case ChaosEventType.BOSS_RUSH:
        enemies.push(new Armoured(376, 40));
        break;

      case ChaosEventType.GEOMETRIC_BARRAGE:
        const cx = 400;
        const cy = 90;
        const spd = 220.0;
        for (let i = 0; i < 12; i++) {
          const angle = (i * 2.0 * Math.PI) / 12.0;
          const vx = Math.cos(angle) * spd;
          const vy = Math.sin(angle) * spd;
          projectiles.push(new Projectile(cx, cy, vx, vy, 12, false, 6, 6, '#FF5588', 'rgba(255, 40, 100, 0.7)'));
        }
        break;

      case ChaosEventType.BULLET_RAIN:
        for (let i = 1; i <= 8; i++) {
          const sx = (800 / 9) * i;
          projectiles.push(new Projectile(sx, 20, 0, 260.0, 10, false, 5, 14, '#FFAA00', 'rgba(255, 170, 0, 0.7)'));
        }
        break;

      default:
        break;
    }
  }

  private applyContinuousEffect(event: ActiveChaosEvent, dt: number, engine: any): void {
    const player = engine.player;
    const enemies = engine.enemies || [];
    const projectiles = engine.projectiles || [];

    switch (event.type) {
      case ChaosEventType.GRAVITY_WELL:
        const cx = 400;
        const cy = 325;
        const pull = 130.0 * dt;

        if (player && player.active) {
          const dx = cx - (player.x + player.width / 2);
          const dy = cy - (player.y + player.height / 2);
          const dist = Math.max(30.0, Math.hypot(dx, dy));
          player.x += (dx / dist) * pull;
          player.y += (dy / dist) * pull;
        }

        for (const e of enemies) {
          if (e.active && e.type !== 'boss') {
            const dx = cx - (e.x + e.width / 2);
            e.x += Math.sign(dx) * Math.min(Math.abs(dx), pull * 0.7);
          }
        }
        break;

      case ChaosEventType.SPEED_DEMON:
        for (const e of enemies) {
          if (e.active) {
            e.y += (e.speed || 100) * 0.35 * dt;
          }
        }
        break;

      case ChaosEventType.MAGNETIC_BULLETS:
        for (const p of projectiles) {
          if (p.active && p.isPlayer) {
            let nearest: any = null;
            let minD2 = Infinity;
            for (const e of enemies) {
              if (e.active && e.y < p.y) {
                const dx = (e.x + e.width / 2) - p.x;
                const dy = (e.y + e.height / 2) - p.y;
                const d2 = dx * dx + dy * dy;
                if (d2 < minD2) {
                  minD2 = d2;
                  nearest = e;
                }
              }
            }
            if (nearest) {
              const targetX = nearest.x + nearest.width / 2;
              const dx = targetX - p.x;
              const sign = dx > 0 ? 1 : (dx < 0 ? -1 : 0);
              p.x += sign * Math.min(Math.abs(dx), 110.0 * dt);
            }
          }
        }
        break;

      case ChaosEventType.TIME_DISTORTION:
        for (const e of enemies) {
          if (e.active) {
            e.y -= (e.speed || 100) * 0.45 * dt;
          }
        }
        break;

      default:
        break;
    }
  }

  private cleanupEventEffect(event: ActiveChaosEvent, engine: any): void {
    const player = engine.player;
    if (!player) return;

    switch (event.type) {
      case ChaosEventType.REVERSE_CONTROLS:
        player.controlsInverted = false;
        break;

      case ChaosEventType.SPEED_DEMON:
        player.speed = event.cachedPlayerSpeed > 0 ? event.cachedPlayerSpeed : player.baseSpeed;
        break;

      case ChaosEventType.RAPID_FIRE:
      case ChaosEventType.WEAPON_JAM:
        player.shootCooldown = event.cachedShootCooldown > 0 ? event.cachedShootCooldown : 0.18;
        break;

      case ChaosEventType.CRITICAL_OVERDRIVE:
        player.damageMultiplier = event.cachedDamageMultiplier || 1.0;
        break;

      case ChaosEventType.EMERGENCY_SHIELD:
        player.invulnerableTimer = 0;
        break;

      default:
        break;
    }
  }

  private restorePlayerState(player: any): void {
    player.controlsInverted = false;
    player.speed = player.baseSpeed || 320.0;
    player.shootCooldown = 0.18;
    player.damageMultiplier = 1.0;
    player.invulnerableTimer = 0;
  }

  public renderHUD(ctx: CanvasRenderingContext2D, screenWidth: number): void {
    if (!this.enabled || this.activeEvents.length === 0) return;

    let hudY = screenWidth < 600 ? 196 : 16;
    for (const event of this.activeEvents) {
      const def = event.definition;
      const isWarn = event.isWarning();

      const badgeText = isWarn
        ? `⚠️ INCOMING ANOMALY: ${def.displayName.toUpperCase()} (${event.warningTimer.toFixed(1)}s)`
        : `⚡ ACTIVE ANOMALY: ${def.displayName.toUpperCase()} (${event.durationRemaining.toFixed(1)}s)`;

      const primaryCol = isWarn ? '#FBBF24' : '#00E5FF';
      const bgCol = isWarn ? 'rgba(30, 24, 12, 0.92)' : 'rgba(13, 25, 48, 0.92)';

      ctx.save();
      ctx.font = 'bold 11px sans-serif';
      const txtW = ctx.measureText(badgeText).width;
      const boxW = txtW + 36;
      const boxH = 26;
      const boxX = (screenWidth - boxW) / 2;

      // Container
      ctx.fillStyle = bgCol;
      ctx.strokeStyle = primaryCol;
      ctx.lineWidth = 1.3;

      ctx.beginPath();
      ctx.roundRect(boxX, hudY, boxW, boxH, 13);
      ctx.fill();
      ctx.stroke();

      // Progress line
      if (event.isActive()) {
        const fillW = (boxW - 16) * event.getProgress();
        ctx.strokeStyle = primaryCol;
        ctx.lineWidth = 2.0;
        ctx.beginPath();
        ctx.moveTo(boxX + 8, hudY + boxH - 2);
        ctx.lineTo(boxX + 8 + fillW, hudY + boxH - 2);
        ctx.stroke();
      }

      ctx.fillStyle = primaryCol;
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.fillText(badgeText, screenWidth / 2, hudY + boxH / 2);

      ctx.restore();
      hudY += boxH + 6;
    }
  }
}
