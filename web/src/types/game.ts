export enum GameState {
  MENU = 'MENU',
  PLAYING = 'PLAYING',
  PAUSED = 'PAUSED',
  GAME_OVER = 'GAME_OVER'
}

export enum PowerUpType {
  HEALTH_PACK = 'HEALTH_PACK',
  SHIELD_BOOST = 'SHIELD_BOOST',
  WEAPON_UPGRADE = 'WEAPON_UPGRADE',
  SPEED_BOOST = 'SPEED_BOOST'
}

export enum EnemyType {
  DRONE = 'DRONE',
  FIGHTER = 'FIGHTER',
  BOSS = 'BOSS',
  ARMOURED = 'ARMOURED',
  INTERCEPTOR = 'INTERCEPTOR',
  MORPHING = 'MORPHING',
  FAIL_SAFE = 'FAIL_SAFE',
  RAMMER = 'RAMMER'
}

export interface BoundingBox {
  x: number;
  y: number;
  width: number;
  height: number;
}

export interface MatchSummary {
  score: number;
  levelReached: number;
  durationSeconds: number;
  enemiesDestroyed: number;
}

export interface CombatStats {
  kills: number;
  shotsFired: number;
  shotsHit: number;
  maxCombo: number;
  powerUpsCollected: number;
}

