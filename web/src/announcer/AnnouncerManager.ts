import dialoguesData from './announcer_dialogues.json';
import { AnnouncerSettingsManager } from './AnnouncerSettingsManager';
import { AnnouncerEvent, DialogueCategory, DialogueEntry } from './types';

export class AnnouncerManager {
  private static instance: AnnouncerManager;

  private dialogues: DialogueEntry[] = [];
  private recentHistory: string[] = [];
  private readonly maxRecentHistory = 20;

  // Banner display state
  private activeText = '';
  private activeCategory: DialogueCategory = 'GENERAL_ROAST';
  private bannerTimer = 0;
  private bannerDuration = 4.8;
  private bannerAlpha = 0;

  // Timers
  private cooldownTimer = 0;
  private randomInterruptTimer = 16.0;

  // Telemetry trackers
  private lastHealth = -1;
  private lastShotsFired = 0;
  private lastShotsHit = 0;
  private consecutiveMisses = 0;
  private campingTimer = 0;
  private criticalHealthTriggered = false;
  private lastReportedKillStreak = 0;
  private bossSpawnTriggered = false;

  private constructor() {
    this.dialogues = (dialoguesData.dialogues as DialogueEntry[]) || [];
  }

  public static getInstance(): AnnouncerManager {
    if (!AnnouncerManager.instance) {
      AnnouncerManager.instance = new AnnouncerManager();
    }
    return AnnouncerManager.instance;
  }

  public getAllDialogues(): readonly DialogueEntry[] {
    return this.dialogues;
  }

  public getDialogueCount(): number {
    return this.dialogues.length;
  }

  public trigger(event: AnnouncerEvent, forcePriority = false): boolean {
    const settings = AnnouncerSettingsManager.getInstance().getSettings();
    if (!settings.enabled || !event) {
      return false;
    }

    const minCooldown = this.getMinCooldown();
    if (!forcePriority && this.cooldownTimer > 0) {
      return false;
    }
    if (forcePriority && this.cooldownTimer > minCooldown - 1.5) {
      return false;
    }

    const activeCat = settings.activeCategory;
    let candidates = this.dialogues.filter(d => {
      if (activeCat !== 'ALL_PACKS' && d.category !== activeCat) {
        return false;
      }
      return Array.isArray(d.triggers) && d.triggers.includes(event);
    });

    if (candidates.length === 0) {
      candidates = this.dialogues.filter(d => Array.isArray(d.triggers) && d.triggers.includes(event));
    }

    if (candidates.length === 0) {
      return false;
    }

    // Anti-repetition
    let freshCandidates = candidates.filter(c => !this.recentHistory.includes(c.id));
    if (freshCandidates.length === 0) {
      this.recentHistory.splice(0, Math.floor(this.maxRecentHistory / 2));
      freshCandidates = candidates;
    }

    const chosen = freshCandidates[Math.floor(Math.random() * freshCandidates.length)];

    this.showBanner(chosen.text, chosen.category, chosen.priority === 'HIGH');

    this.recentHistory.push(chosen.id);
    if (this.recentHistory.length > this.maxRecentHistory) {
      this.recentHistory.shift();
    }

    this.cooldownTimer = minCooldown;
    this.randomInterruptTimer = this.getRandomInterval() + (Math.random() * 6 - 3);

    if (settings.voiceEnabled) {
      this.speak(chosen.text);
    }

    return true;
  }

  public triggerChaosLine(text: string, category: DialogueCategory): void {
    const settings = AnnouncerSettingsManager.getInstance().getSettings();
    if (!settings.enabled || !text) return;
    this.showBanner(text, category || 'GENERAL_ROAST', true);
    this.cooldownTimer = Math.max(this.cooldownTimer, 5.0);
    if (settings.voiceEnabled) {
      this.speak(text);
    }
  }

  public showBanner(text: string, category: DialogueCategory, highPriority: boolean): void {
    this.activeText = text;
    this.activeCategory = category;
    this.bannerDuration = highPriority ? 3.4 : 2.8;
    this.bannerTimer = this.bannerDuration;
    this.bannerAlpha = 0;
  }

  private speak(text: string): void {
    if (typeof window === 'undefined' || !('speechSynthesis' in window)) {
      return;
    }

    try {
      window.speechSynthesis.cancel();
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.rate = 1.05;
      utterance.pitch = 1.0;
      utterance.volume = AnnouncerSettingsManager.getInstance().getSettings().volume;

      const voices = window.speechSynthesis.getVoices();
      const desiVoice = voices.find(v => v.lang === 'hi-IN' || v.lang === 'en-IN' || v.lang.startsWith('hi'));
      if (desiVoice) {
        utterance.voice = desiVoice;
      }

      window.speechSynthesis.speak(utterance);
    } catch {
      // Audio speech ignored gracefully
    }
  }

  private getMinCooldown(): number {
    const freq = AnnouncerSettingsManager.getInstance().getSettings().frequency;
    switch (freq) {
      case 'HIGH': return 14.0;
      case 'LOW': return 45.0;
      default: return 26.0;
    }
  }

  private getRandomInterval(): number {
    const freq = AnnouncerSettingsManager.getInstance().getSettings().frequency;
    switch (freq) {
      case 'HIGH': return 28.0;
      case 'LOW': return 90.0;
      default: return 55.0;
    }
  }

  public update(dt: number, engine: any): void {
    if (this.cooldownTimer > 0) {
      this.cooldownTimer -= dt;
    }

    // Banner presentation fade
    if (this.bannerTimer > 0) {
      this.bannerTimer -= dt;
      const elapsed = this.bannerDuration - this.bannerTimer;
      if (elapsed < 0.35) {
        this.bannerAlpha = elapsed / 0.35;
      } else if (this.bannerTimer < 0.6) {
        this.bannerAlpha = Math.max(0, this.bannerTimer / 0.6);
      } else {
        this.bannerAlpha = 1.0;
      }
    } else {
      this.bannerAlpha = 0;
    }

    if (!engine || !engine.player) {
      return;
    }

    const player = engine.player;

    // 1. Critical Health (< 25%)
    const healthRatio = player.health / Math.max(1, player.maxHealth);
    if (healthRatio <= 0.25 && !this.criticalHealthTriggered && player.active) {
      this.criticalHealthTriggered = true;
      this.trigger('CRITICAL_HEALTH', true);
    } else if (healthRatio > 0.35) {
      this.criticalHealthTriggered = false;
    }

    // 2. Significant Damage Taken (shield depleted or substantial hull damage)
    if (this.lastHealth >= 0 && (this.lastHealth - player.health >= 20 || (player.shield <= 0 && this.lastHealth - player.health >= 15))) {
      if (healthRatio > 0.25) {
        this.trigger('PLAYER_DAMAGE', false);
      }
    }
    this.lastHealth = player.health;

    // 3. Missed Shots Tracking (only when player is spraying repeatedly)
    const shotsFired = engine.shotsFired || 0;
    const shotsHit = engine.shotsHit || 0;
    const newFired = shotsFired - this.lastShotsFired;
    const newHits = shotsHit - this.lastShotsHit;

    if (newFired > 0) {
      if (newHits === 0) {
        this.consecutiveMisses += newFired;
        if (this.consecutiveMisses >= 28 && shotsFired >= 35) {
          this.trigger('MISSED_SHOTS', false);
          this.consecutiveMisses = 0;
        }
      } else {
        this.consecutiveMisses = 0;
      }
    }
    this.lastShotsFired = shotsFired;
    this.lastShotsHit = shotsHit;

    // 4. Low Accuracy Roast (evaluated infrequently)
    if (shotsFired >= 50 && shotsFired % 50 === 0) {
      const acc = shotsHit / shotsFired;
      if (acc < 0.20) {
        this.trigger('LOW_ACCURACY', false);
      } else if (acc > 0.80) {
        this.trigger('HIGH_ACCURACY', false);
      }
    }

    // 5. Corner Camping Detection (only when squatting for extended duration)
    const screenW = engine.width || 800;
    if (player.x < 70 || player.x > screenW - 120) {
      this.campingTimer += dt;
      if (this.campingTimer >= 8.5) {
        this.trigger('CAMPING', false);
        this.campingTimer = 0;
      }
    } else {
      this.campingTimer = Math.max(0, this.campingTimer - dt * 1.5);
    }

    // 6. Kill Streak Tracking (rewards destroying multiple enemies in succession)
    const streak = engine.scoreCalc
      ? (typeof engine.scoreCalc.getStreak === 'function'
          ? engine.scoreCalc.getStreak()
          : (typeof engine.scoreCalc.getCurrentStreak === 'function'
              ? engine.scoreCalc.getCurrentStreak()
              : 0))
      : 0;
    if (streak >= 5 && streak !== this.lastReportedKillStreak && streak % 5 === 0) {
      this.lastReportedKillStreak = streak;
      this.trigger('KILL_STREAK', false);
    } else if (streak === 0) {
      this.lastReportedKillStreak = 0;
    }

    // 7. Surrounded By Enemies
    if (Array.isArray(engine.enemies) && engine.enemies.length >= 3 && player.active) {
      let left = 0, right = 0, top = 0;
      const px = player.x + player.width / 2;
      const py = player.y + player.height / 2;

      for (const e of engine.enemies) {
        if (!e.active) continue;
        const ex = e.x + (e.width || 32) / 2;
        const ey = e.y + (e.height || 32) / 2;
        const distY = py - ey;
        if (distY > 0 && distY < 240) {
          if (ex < px - 40 && ex > px - 220) left++;
          if (ex > px + 40 && ex < px + 220) right++;
          if (Math.abs(ex - px) <= 60) top++;
        }
      }
      if (left >= 1 && right >= 1 && top >= 1) {
        this.trigger('SURROUNDED', false);
      }
    }

    // 8. Boss Presence
    const isBossNow = typeof engine.isBossActive === 'function' ? engine.isBossActive() : false;
    if (isBossNow && !this.bossSpawnTriggered) {
      this.bossSpawnTriggered = true;
      this.trigger('BOSS_APPEARS', true);
    } else if (!isBossNow && this.bossSpawnTriggered) {
      this.bossSpawnTriggered = false;
      this.trigger('BOSS_DEFEATED', true);
    }

    // 9. Random Interruption
    this.randomInterruptTimer -= dt;
    if (this.randomInterruptTimer <= 0) {
      this.trigger('RANDOM_INTERRUPT', false);
    }
  }

  public renderHUD(ctx: CanvasRenderingContext2D, screenWidth: number, _screenHeight: number): void {
    if (this.bannerAlpha <= 0.01 || !this.activeText) {
      return;
    }

    ctx.save();

    let bannerW = Math.min(500, Math.max(340, screenWidth - 480));
    if (screenWidth < 980) {
      bannerW = Math.min(520, screenWidth - 20);
    }
    const bannerH = 56;
    const x = (screenWidth - bannerW) / 2;
    const y = screenWidth >= 980 ? 44 : (screenWidth < 600 ? 80 : 114);

    const bgAlpha = Math.min(1, 0.92 * this.bannerAlpha);
    const borderAlpha = Math.min(1, 0.8 * this.bannerAlpha);

    // Dark glass background
    ctx.fillStyle = `rgba(10, 16, 32, ${bgAlpha})`;
    this.roundRect(ctx, x, y, bannerW, bannerH, 12);
    ctx.fill();

    // Neon border
    const isDesi = this.activeCategory === 'BOLLYWOOD' || this.activeCategory === 'DESI_MEME';
    const borderColor = isDesi
      ? `rgba(245, 158, 11, ${borderAlpha})`
      : `rgba(0, 240, 255, ${borderAlpha})`;

    ctx.strokeStyle = borderColor;
    ctx.lineWidth = 1.2;
    this.roundRect(ctx, x, y, bannerW, bannerH, 12);
    ctx.stroke();

    // Top glowing accent line
    const grad = ctx.createLinearGradient(x + 20, y, x + bannerW - 20, y);
    grad.addColorStop(0, `rgba(0, 240, 255, ${0.9 * this.bannerAlpha})`);
    grad.addColorStop(1, `rgba(168, 85, 247, ${0.9 * this.bannerAlpha})`);
    ctx.strokeStyle = grad;
    ctx.lineWidth = 2.0;
    ctx.beginPath();
    ctx.moveTo(x + 18, y);
    ctx.lineTo(x + bannerW - 18, y);
    ctx.stroke();

    // Announcer Tag Badge
    const categoryTag = this.activeCategory ? this.activeCategory.replace('_', ' ') : 'AIRSTRIKE AI';
    const badge = `⚡ AIRSTRIKE AI • ${categoryTag}`;
    ctx.font = 'bold 10px Rajdhani, sans-serif';
    ctx.fillStyle = `rgba(0, 240, 255, ${0.95 * this.bannerAlpha})`;
    ctx.textAlign = 'left';
    ctx.fillText(badge, x + 14, y + 17);

    // Glowing dot
    ctx.fillStyle = `rgba(52, 211, 153, ${this.bannerAlpha})`;
    ctx.beginPath();
    ctx.arc(x + bannerW - 16, y + 13, 3, 0, Math.PI * 2);
    ctx.fill();

    // Main Dialogue Text
    ctx.font = 'bold 12px Rajdhani, sans-serif';
    ctx.fillStyle = `rgba(255, 255, 255, ${this.bannerAlpha})`;

    const maxTextWidth = bannerW - 28;
    const textMetrics = ctx.measureText(this.activeText);

    if (textMetrics.width <= maxTextWidth) {
      ctx.fillText(this.activeText, x + 14, y + 39);
    } else {
      // Two-line split for long dialogues
      const words = this.activeText.split(' ');
      let line1 = '';
      let line2 = '';
      let isLine2 = false;

      for (const w of words) {
        if (!isLine2) {
          if (ctx.measureText(line1 + w + ' ').width < maxTextWidth) {
            line1 += w + ' ';
          } else {
            isLine2 = true;
            line2 += w + ' ';
          }
        } else {
          line2 += w + ' ';
        }
      }

      ctx.font = 'bold 11px Rajdhani, sans-serif';
      ctx.fillText(line1.trim(), x + 14, y + 33);
      if (line2.trim()) {
        ctx.fillStyle = `rgba(226, 232, 240, ${0.95 * this.bannerAlpha})`;
        ctx.fillText(line2.trim(), x + 14, y + 48);
      }
    }

    ctx.restore();
  }

  public testAnnounce(): void {
    const settings = AnnouncerSettingsManager.getInstance().getSettings();
    const cat = settings.activeCategory;
    let pool = this.dialogues.filter(d => cat === 'ALL_PACKS' || d.category === cat);
    if (pool.length === 0) pool = this.dialogues;
    if (pool.length > 0) {
      const chosen = pool[Math.floor(Math.random() * pool.length)];
      this.showBanner(chosen.text, chosen.category, true);
      if (settings.voiceEnabled) {
        this.speak(chosen.text);
      }
    }
  }

  public reset(): void {
    this.cooldownTimer = 0;
    this.randomInterruptTimer = 35.0;
    this.lastHealth = -1;
    this.lastShotsFired = 0;
    this.lastShotsHit = 0;
    this.consecutiveMisses = 0;
    this.campingTimer = 0;
    this.criticalHealthTriggered = false;
    this.lastReportedKillStreak = 0;
    this.bossSpawnTriggered = false;
    this.bannerTimer = 0;
    this.bannerAlpha = 0;
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
}
