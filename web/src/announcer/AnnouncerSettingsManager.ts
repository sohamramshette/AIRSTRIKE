import { AnnouncerSettings, CommentaryFrequency, DialogueCategory } from './types';

const STORAGE_KEY = 'airstrike_announcer_settings';

export class AnnouncerSettingsManager {
  private static instance: AnnouncerSettingsManager;

  private settings: AnnouncerSettings = {
    enabled: true,
    activeCategory: 'ALL_PACKS',
    frequency: 'NORMAL',
    voiceEnabled: false,
    volume: 0.8
  };

  private constructor() {
    this.loadFromStorage();
  }

  public static getInstance(): AnnouncerSettingsManager {
    if (!AnnouncerSettingsManager.instance) {
      AnnouncerSettingsManager.instance = new AnnouncerSettingsManager();
    }
    return AnnouncerSettingsManager.instance;
  }

  private loadFromStorage(): void {
    try {
      if (typeof localStorage !== 'undefined') {
        const raw = localStorage.getItem(STORAGE_KEY);
        if (raw) {
          const parsed = JSON.parse(raw);
          this.settings = { ...this.settings, ...parsed };
        }
      }
    } catch {
      // Storage unavailable or blocked
    }
  }

  public saveToStorage(): void {
    try {
      if (typeof localStorage !== 'undefined') {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(this.settings));
      }
    } catch {
      // Storage unavailable
    }
  }

  public getSettings(): Readonly<AnnouncerSettings> {
    return this.settings;
  }

  public setEnabled(enabled: boolean): void {
    this.settings.enabled = enabled;
    this.saveToStorage();
  }

  public setActiveCategory(category: DialogueCategory): void {
    this.settings.activeCategory = category;
    this.saveToStorage();
  }

  public setFrequency(freq: CommentaryFrequency): void {
    this.settings.frequency = freq;
    this.saveToStorage();
  }

  public setVoiceEnabled(voiceEnabled: boolean): void {
    this.settings.voiceEnabled = voiceEnabled;
    this.saveToStorage();
  }

  public setVolume(volume: number): void {
    this.settings.volume = Math.max(0, Math.min(1, volume));
    this.saveToStorage();
  }
}

