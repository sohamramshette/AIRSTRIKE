import { DialogueCategory } from '../announcer/types';

export interface ChaosEventDefinition {
  id: string;
  displayName: string;
  courseOutcome: string;
  description: string;
  defaultDuration: number;
  cooldown: number;
  minDifficulty: number;
  warningLine: string;
  activateLine: string;
  category: DialogueCategory;
}

export enum ChaosEventType {
  REVERSE_CONTROLS = 'REVERSE_CONTROLS',
  GRAVITY_WELL = 'GRAVITY_WELL',
  SPEED_DEMON = 'SPEED_DEMON',
  EMERGENCY_SHIELD = 'EMERGENCY_SHIELD',
  STATE_CHAOS = 'STATE_CHAOS',
  TOTAL_CHAOS = 'TOTAL_CHAOS',
  FORMATION_SHUFFLE = 'FORMATION_SHUFFLE',
  DOUBLE_TROUBLE = 'DOUBLE_TROUBLE',
  BOSS_RUSH = 'BOSS_RUSH',
  MAGNETIC_BULLETS = 'MAGNETIC_BULLETS',
  HASH_FLUX = 'HASH_FLUX',
  QUANTUM_COLLAPSE = 'QUANTUM_COLLAPSE',
  RAPID_FIRE = 'RAPID_FIRE',
  CRITICAL_OVERDRIVE = 'CRITICAL_OVERDRIVE',
  WEAPON_JAM = 'WEAPON_JAM',
  GEOMETRIC_BARRAGE = 'GEOMETRIC_BARRAGE',
  MIRROR_ATTACK = 'MIRROR_ATTACK',
  BULLET_RAIN = 'BULLET_RAIN',
  TIME_DISTORTION = 'TIME_DISTORTION',
  STEALTH_CLOAK = 'STEALTH_CLOAK'
}

export const CHAOS_DEFINITIONS: Record<ChaosEventType, ChaosEventDefinition> = {
  [ChaosEventType.REVERSE_CONTROLS]: {
    id: 'CHAOS_REVERSE_CONTROLS',
    displayName: 'REVERSE CONTROLS',
    courseOutcome: 'PSOOP-CO4',
    description: 'Navigational gyro inverted: directional axes reversed (Left <-> Right, Up <-> Down)',
    defaultDuration: 9.0,
    cooldown: 40.0,
    minDifficulty: 2,
    warningLine: 'ALERT! Navigational gyro failure! Controls reverse ho gaye!',
    activateLine: 'Bhai, controls ulte ho gaye! Ab reverse gear mein spaceship chala!',
    category: 'GENERAL_ROAST'
  },
  [ChaosEventType.GRAVITY_WELL]: {
    id: 'CHAOS_GRAVITY_WELL',
    displayName: 'GRAVITY WELL',
    courseOutcome: 'CGL-CO5',
    description: 'Micro-singularity forms near sector center, exerting dynamic gravitational pull and Cohen-Sutherland boundary clipping on ships and projectiles',
    defaultDuration: 10.0,
    cooldown: 45.0,
    minDifficulty: 2,
    warningLine: 'WARNING! Micro-singularity detected! Gravity spike incoming!',
    activateLine: 'ALERT! Gravity ne resignation de diya hai! Center of gravity sabko kheench rahi hai!',
    category: 'DESI_MEME'
  },
  [ChaosEventType.SPEED_DEMON]: {
    id: 'CHAOS_SPEED_DEMON',
    displayName: 'SPEED DEMON',
    courseOutcome: 'PSOOP-CO4',
    description: 'Hyperdrive overclock increases player and enemy movement velocities by 60%',
    defaultDuration: 10.0,
    cooldown: 35.0,
    minDifficulty: 1,
    warningLine: 'WARNING! Engine warp coil overclocking in 3... 2... 1...',
    activateLine: 'Nitrous on! Fast & Furious: Space Drift shuru ho chuka hai! Thok mat dena!',
    category: 'INSTAGRAM_GENZ'
  },
  [ChaosEventType.EMERGENCY_SHIELD]: {
    id: 'CHAOS_EMERGENCY_SHIELD',
    displayName: 'EMERGENCY SHIELD',
    courseOutcome: 'PSOOP-CO3',
    description: 'Defensive invariant protection engages an impenetrable hexagonal barrier absorbing 100% damage',
    defaultDuration: 8.0,
    cooldown: 55.0,
    minDifficulty: 1,
    warningLine: 'TACTICAL DEFENSE: Emergency protective barrier initializing!',
    activateLine: 'Kavach kundal active! Ab enemy ka baap bhi damage nahi de sakta!',
    category: 'BOLLYWOOD'
  },
  [ChaosEventType.STATE_CHAOS]: {
    id: 'CHAOS_STATE_CHAOS',
    displayName: 'STATE CHAOS',
    courseOutcome: 'PSOOP-CO2',
    description: 'Hostile fleet AI state machines undergo rapid, chaotic state transitions between patrol, chase, and overcharge',
    defaultDuration: 10.0,
    cooldown: 45.0,
    minDifficulty: 2,
    warningLine: 'SYSTEM ANOMALY: Enemy AI behavior state matrices destabilizing!',
    activateLine: 'AI pagal ho gaya! Koi bhaag raha hai, koi gusse mein fire kar raha hai!',
    category: 'GENERAL_ROAST'
  },
  [ChaosEventType.TOTAL_CHAOS]: {
    id: 'CHAOS_TOTAL_CHAOS',
    displayName: 'TOTAL CHAOS',
    courseOutcome: 'PSOOP-CO1',
    description: 'Polymorphic anomaly surge: two compatible non-conflicting Chaos Protocols trigger simultaneously',
    defaultDuration: 12.0,
    cooldown: 75.0,
    minDifficulty: 4,
    warningLine: 'CRITICAL WARNING: MULTI-SYSTEM COLLAPSE! DUAL PROTOCOL IMMINENT!',
    activateLine: 'TOTAL CHAOS PROTOCOL! Sab system fail ho rahe hain! Survive karke dikha!',
    category: 'GENERAL_ROAST'
  },
  [ChaosEventType.FORMATION_SHUFFLE]: {
    id: 'CHAOS_FORMATION_SHUFFLE',
    displayName: 'FORMATION SHUFFLE',
    courseOutcome: 'PL-CO1',
    description: 'Hostile squadrons reorder into a transposed spatial grid using row-major address calculation',
    defaultDuration: 8.0,
    cooldown: 40.0,
    minDifficulty: 2,
    warningLine: 'WARNING: Enemy fleet executing rapid 2D grid matrix transposition!',
    activateLine: 'Musical chairs chal raha hai space mein! Formation shuffle ho gayi!',
    category: 'DESI_MEME'
  },
  [ChaosEventType.DOUBLE_TROUBLE]: {
    id: 'CHAOS_DOUBLE_TROUBLE',
    displayName: 'DOUBLE TROUBLE',
    courseOutcome: 'PL-CO2',
    description: 'Hostile squadron nodes clone dynamically via doubly linked list splicing, creating mirror squadrons',
    defaultDuration: 12.0,
    cooldown: 60.0,
    minDifficulty: 2,
    warningLine: 'RADAR ALERT: Fleet squadron nodes duplicating on dynamic link channels!',
    activateLine: 'Arey bhai bhai bhai! Ek dushman kam tha jo double aa gaye?',
    category: 'DESI_MEME'
  },
  [ChaosEventType.BOSS_RUSH]: {
    id: 'CHAOS_BOSS_RUSH',
    displayName: 'BOSS RUSH',
    courseOutcome: 'PL-CO3',
    description: 'High-priority heavy capital dreadnought dispatched from the circular wave queue ahead of schedule',
    defaultDuration: 14.0,
    cooldown: 70.0,
    minDifficulty: 4,
    warningLine: 'EMERGENCY PRIORITY: Circular queue dispatched high-tier hostile dreadnought!',
    activateLine: 'BOSS RUSH! Asli talent dikhane ka waqt aa gaya, bhidu!',
    category: 'BOLLYWOOD'
  },
  [ChaosEventType.MAGNETIC_BULLETS]: {
    id: 'CHAOS_MAGNETIC_BULLETS',
    displayName: 'MAGNETIC BULLETS',
    courseOutcome: 'PL-CO4',
    description: 'Player projectiles acquire magnetic guidance via priority target search tracking',
    defaultDuration: 10.0,
    cooldown: 50.0,
    minDifficulty: 2,
    warningLine: 'FIRE CONTROL: Target acquisition algorithm locked to nearest threats!',
    activateLine: 'Smart magnetic bullets active! Ab toh miss karke dikha!',
    category: 'INSTAGRAM_GENZ'
  },
  [ChaosEventType.HASH_FLUX]: {
    id: 'CHAOS_HASH_FLUX',
    displayName: 'HASH FLUX',
    courseOutcome: 'PL-CO5',
    description: 'Hostile fleet shields oscillate hash buckets; open-addressing resonance causes 3x vulnerability',
    defaultDuration: 11.0,
    cooldown: 50.0,
    minDifficulty: 3,
    warningLine: 'SPECTRAL SCAN: Enemy shield modulation re-hashing across frequency tables!',
    activateLine: 'Hash key match karo! Sahi frequency pe maro, dushman ek shot mein gayab!',
    category: 'GENERAL_ROAST'
  },
  [ChaosEventType.QUANTUM_COLLAPSE]: {
    id: 'CHAOS_QUANTUM_COLLAPSE',
    displayName: 'QUANTUM COLLAPSE',
    courseOutcome: 'COA-CO1',
    description: '80386 Zero Flag status evaluation unleashes a tactical EMP pulse clearing 50% of hostile projectiles',
    defaultDuration: 8.0,
    cooldown: 60.0,
    minDifficulty: 3,
    warningLine: 'CORE TELEMETRY: EFLAGS condition code monitor initializing tactical EMP capacitor!',
    activateLine: 'Zero Flag set! EMP blast triggered! Saare hostile laser saaf!',
    category: 'VICTORY_PRAISE'
  },
  [ChaosEventType.RAPID_FIRE]: {
    id: 'CHAOS_RAPID_FIRE',
    displayName: 'RAPID FIRE',
    courseOutcome: 'COA-CO2',
    description: 'Weapon discharge cadenced to assembly Fibonacci intervals, tripling player rate-of-fire',
    defaultDuration: 9.0,
    cooldown: 45.0,
    minDifficulty: 1,
    warningLine: 'POWER SURGE: Capacitor timing synchronized to Fibonacci series pulses!',
    activateLine: 'Unlimited ammo mode activated! Bhai ne trigger daba ke chhod diya!',
    category: 'INSTAGRAM_GENZ'
  },
  [ChaosEventType.CRITICAL_OVERDRIVE]: {
    id: 'CHAOS_CRITICAL_OVERDRIVE',
    displayName: 'CRITICAL OVERDRIVE',
    courseOutcome: 'COA-CO3',
    description: 'Fire control computer executes Booth two\'s-complement arithmetic multiplier, scaling shot damage by 2x',
    defaultDuration: 9.0,
    cooldown: 45.0,
    minDifficulty: 2,
    warningLine: 'WEAPON ARITHMETIC: Booth signed multiplier loaded into ordnance register!',
    activateLine: 'Booth algorithm ne damage 2x kar diya! Ab enemy ko steamroll karo!',
    category: 'BOLLYWOOD'
  },
  [ChaosEventType.WEAPON_JAM]: {
    id: 'CHAOS_WEAPON_JAM',
    displayName: 'WEAPON JAM',
    courseOutcome: 'COA-CO4',
    description: 'Weapon feed experiences 5-stage CPU pipeline hazard stalls; firing cooldown temporarily tripled',
    defaultDuration: 8.0,
    cooldown: 50.0,
    minDifficulty: 3,
    warningLine: 'HARDWARE FAULT: RAW Data hazard in weapon pipeline! Inserting stall cycles!',
    activateLine: 'Bhai, pipeline stall ho gaya! Hathiyar overheat, thoda sambhal ke!',
    category: 'GENERAL_ROAST'
  },
  [ChaosEventType.GEOMETRIC_BARRAGE]: {
    id: 'CHAOS_GEOMETRIC_BARRAGE',
    displayName: 'GEOMETRIC BARRAGE',
    courseOutcome: 'CGL-CO1',
    description: 'Capital ship discharges expanding 8-sided regular polygon projectile rings in OpenGL vertex fan pattern',
    defaultDuration: 8.0,
    cooldown: 40.0,
    minDifficulty: 2,
    warningLine: 'BALLISTIC WARNING: Geometric regular polygon projectile ring expanding!',
    activateLine: 'Geometric explosion! Maths pass kiya hota toh dodge kar leta!',
    category: 'DESI_MEME'
  },
  [ChaosEventType.MIRROR_ATTACK]: {
    id: 'CHAOS_MIRROR_ATTACK',
    displayName: 'MIRROR ATTACK',
    courseOutcome: 'CGL-CO2',
    description: 'Elite interceptor units reflect the player\'s horizontal maneuvering via affine transformation reflection',
    defaultDuration: 10.0,
    cooldown: 45.0,
    minDifficulty: 3,
    warningLine: 'SENSORS DETECT: Interceptors slaved to player reflection transformation matrix!',
    activateLine: 'Mirror mirror on the wall... dushman teri hi copy maar raha hai!',
    category: 'BOLLYWOOD'
  },
  [ChaosEventType.BULLET_RAIN]: {
    id: 'CHAOS_BULLET_RAIN',
    displayName: 'BULLET RAIN',
    courseOutcome: 'CGL-CO3',
    description: 'Hostile fleet unleashes an algorithmic curtain of plasma bolts following integer Bresenham slopes',
    defaultDuration: 8.0,
    cooldown: 50.0,
    minDifficulty: 3,
    warningLine: 'WEATHER WARNING: Bresenham integer-stepped projectile grid incoming!',
    activateLine: 'Mausam vibhag ki warning: Aaj space mein plasma bullets ki barish hogi!',
    category: 'DESI_MEME'
  },
  [ChaosEventType.TIME_DISTORTION]: {
    id: 'CHAOS_TIME_DISTORTION',
    displayName: 'TIME DISTORTION',
    courseOutcome: 'CGL-CO4',
    description: 'Relativistic chronal field induces perspective depth dilation, slowing enemy movement and shots by 50%',
    defaultDuration: 10.0,
    cooldown: 50.0,
    minDifficulty: 1,
    warningLine: 'OPTICAL ANOMALY: Perspective projection depth warp dilating enemy chronal field!',
    activateLine: 'The Matrix mode on! Bullets slow motion mein aa rahi hain, ab mat miss karna!',
    category: 'BOLLYWOOD'
  },
  [ChaosEventType.STEALTH_CLOAK]: {
    id: 'CHAOS_STEALTH_CLOAK',
    displayName: 'STEALTH CLOAK',
    courseOutcome: 'CGL-CO6',
    description: 'Hostile hulls engage optical camouflage with 25% Gouraud alpha blending and glowing exhaust cues',
    defaultDuration: 11.0,
    cooldown: 45.0,
    minDifficulty: 3,
    warningLine: 'OPTICAL SENSORS: Hostile fleet activating stealth camouflage shrouds!',
    activateLine: 'Dushman Mr. India ban gaya! Bas uski red eyes dikh rahi hain!',
    category: 'BOLLYWOOD'
  }
};

export function isCompatibleChaosEvents(a: ChaosEventType, b: ChaosEventType): boolean {
  if (a === b) return false;
  if (a === ChaosEventType.TOTAL_CHAOS || b === ChaosEventType.TOTAL_CHAOS) return false;

  // Speed conflicts
  if ((a === ChaosEventType.SPEED_DEMON && b === ChaosEventType.TIME_DISTORTION) ||
      (a === ChaosEventType.TIME_DISTORTION && b === ChaosEventType.SPEED_DEMON)) {
    return false;
  }

  // Weapon conflicts
  if ((a === ChaosEventType.RAPID_FIRE && b === ChaosEventType.WEAPON_JAM) ||
      (a === ChaosEventType.WEAPON_JAM && b === ChaosEventType.RAPID_FIRE)) {
    return false;
  }

  // Control conflicts
  if (a === ChaosEventType.REVERSE_CONTROLS && (b === ChaosEventType.GRAVITY_WELL || b === ChaosEventType.TIME_DISTORTION)) {
    return false;
  }
  if (b === ChaosEventType.REVERSE_CONTROLS && (a === ChaosEventType.GRAVITY_WELL || a === ChaosEventType.TIME_DISTORTION)) {
    return false;
  }

  return true;
}
