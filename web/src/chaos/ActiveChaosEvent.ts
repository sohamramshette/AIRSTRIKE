import { ChaosEventType, CHAOS_DEFINITIONS, ChaosEventDefinition } from './ChaosEventType';

export class ActiveChaosEvent {
  public type: ChaosEventType;
  public totalDuration: number;
  public warningTimer: number;
  public durationRemaining: number;
  public warningAnnounced: boolean = false;
  public activated: boolean = false;
  public activationAnnounced: boolean = false;
  public dualPartnerType: ChaosEventType | null = null;

  // Cached state for guaranteed restoration
  public cachedPlayerSpeed: number = -1;
  public cachedShootCooldown: number = -1;
  public cachedDamageMultiplier: number = 1.0;

  constructor(type: ChaosEventType, duration?: number, warningDuration: number = 3.0) {
    this.type = type;
    const def = CHAOS_DEFINITIONS[type];
    this.totalDuration = Math.max(2.0, duration ?? (def ? def.defaultDuration : 10.0));
    this.durationRemaining = this.totalDuration;
    this.warningTimer = Math.max(0.0, warningDuration);
  }

  public get definition(): ChaosEventDefinition {
    return CHAOS_DEFINITIONS[this.type];
  }

  public update(dt: number): boolean {
    if (this.warningTimer > 0) {
      this.warningTimer -= dt;
      if (this.warningTimer <= 0) {
        this.warningTimer = 0;
        this.activated = true;
        return true;
      }
      return false;
    } else if (this.durationRemaining > 0) {
      this.durationRemaining -= dt;
      if (this.durationRemaining <= 0) {
        this.durationRemaining = 0;
      }
    }
    return false;
  }

  public isWarning(): boolean {
    return this.warningTimer > 0;
  }

  public isActive(): boolean {
    return this.warningTimer <= 0 && this.durationRemaining > 0;
  }

  public isExpired(): boolean {
    return this.warningTimer <= 0 && this.durationRemaining <= 0;
  }

  public getProgress(): number {
    if (this.totalDuration <= 0) return 0;
    return Math.max(0, Math.min(1, this.durationRemaining / this.totalDuration));
  }
}
