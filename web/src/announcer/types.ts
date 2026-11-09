export type AnnouncerEvent =
  | 'MISSED_SHOTS'
  | 'PLAYER_DAMAGE'
  | 'CRITICAL_HEALTH'
  | 'PLAYER_DEATH'
  | 'GAME_OVER'
  | 'BOSS_APPEARS'
  | 'BOSS_DEFEATED'
  | 'KILL_STREAK'
  | 'HIGH_COMBO'
  | 'HIGH_ACCURACY'
  | 'LOW_ACCURACY'
  | 'SURROUNDED'
  | 'CAMPING'
  | 'DODGE_STREAK'
  | 'CLUTCH_SURVIVAL'
  | 'POWERUP_COLLECTED'
  | 'SPECIAL_ENEMY_KILLED'
  | 'WAVE_START'
  | 'WAVE_CLEARED'
  | 'WAVE_FLAWLESS'
  | 'WAVE_SPEED_RUN'
  | 'STREAK_WAVES'
  | 'HIGH_SCORE_BEATEN'
  | 'RANDOM_INTERRUPT';

export type DialogueCategory =
  | 'ALL_PACKS'
  | 'DESI_MEME'
  | 'INSTAGRAM_GENZ'
  | 'BOLLYWOOD'
  | 'GENERAL_ROAST'
  | 'VICTORY_PRAISE'
  | 'RANDOM_INTERRUPT';

export type CommentaryFrequency = 'LOW' | 'NORMAL' | 'HIGH';

export interface DialogueEntry {
  id: string;
  category: DialogueCategory;
  triggers: AnnouncerEvent[];
  text: string;
  priority?: 'HIGH' | 'NORMAL';
}

export interface AnnouncerSettings {
  enabled: boolean;
  activeCategory: DialogueCategory;
  frequency: CommentaryFrequency;
  voiceEnabled: boolean;
  volume: number;
}

