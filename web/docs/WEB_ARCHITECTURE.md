# AIRSTRIKE Web Client — Architecture & Technical Design Specification

**Document**: `web/docs/WEB_ARCHITECTURE.md`  
**Target Platform**: Modern Web Browsers (Chrome, Edge, Firefox, Safari, Mobile Browsers)  
**Language / Framework**: TypeScript 5.6.3 / HTML5 Canvas 2D / Vite 5.4.10  
**Backend Integration**: Supabase JS v2.49.1  

---

## 1. Directory Structure

```
web/
├── .env.example                # Sample environment configuration
├── docs/
│   ├── GAMEPLAY_MAPPING.md     # Desktop-to-browser mathematical & kinematic mapping
│   ├── WEB_ARCHITECTURE.md    # Architecture and technical design document
│   └── WEB_TEST_REPORT.md      # Automated test execution report
├── index.html                  # Responsive HTML5 canvas & overlay layout
├── package.json                # Dependencies, scripts, and build configuration
├── src/
│   ├── game/                   # Core 2D game engine & entity models
│   │   ├── Boss.ts             # Leviathan flagship dreadnought boss
│   │   ├── CollisionManager.ts # AABB intersection, shield absorption & event routing
│   │   ├── Drone.ts            # High-speed sine-wave ramming drone
│   │   ├── Enemy.ts            # Abstract base class for hostile forces
│   │   ├── Entity.ts           # Base spatial entity (x, y, vx, vy, bounds)
│   │   ├── Fighter.ts          # Combat fighter with lateral banking & dual lasers
│   │   ├── GameEngine.ts       # Master game coordinator, state machine & rendering
│   │   ├── InputManager.ts     # Multi-device keyboard, mouse, and touch listener
│   │   ├── Particle.ts         # Spark, explosion, exhaust, and floating text particles
│   │   ├── Player.ts           # Spacecraft kinematics, 3 weapon levels, shields & lives
│   │   ├── PowerUp.ts          # 4 collectible capsule types with bobbing motion
│   │   ├── Projectile.ts       # Directed glowing plasma bolts & lasers
│   │   ├── ScoreCalculator.ts  # Kill-streak combo timer, multipliers & high scores
│   │   ├── Starfield.ts        # 3-layer parallax starfield & space nebula
│   │   ├── Vector2D.ts         # 2D mathematical vector utilities
│   │   └── WaveManager.ts      # Level spawner, difficulty curves & boss waves
│   ├── services/               # Supabase backend and audio services
│   │   ├── AuthService.ts      # Session management, signup, signin, profile sync
│   │   ├── LeaderboardService.ts # Global top-10 fetching with offline fallback
│   │   ├── MatchService.ts     # Match debrief saving, history & duplicate guards
│   │   ├── SoundService.ts     # Web Audio API procedural synthesizer
│   │   └── SupabaseClient.ts   # Safe Supabase JS client initializer
│   ├── styles/
│   │   └── main.css            # Cyberpunk & Deep-Space sci-fi design system
│   ├── types/
│   │   ├── game.ts             # Enums and interfaces for gameplay structures
│   │   └── supabase.ts         # Database record contracts & auth session types
│   ├── ui/
│   │   └── UIManager.ts        # Overlay menus, auth dialogs, tables & toasts
│   ├── main.ts                 # Application entry point
│   └── vite-env.d.ts           # TypeScript ambient environment declarations
├── tests/                      # Automated unit test suites (30 tests)
│   ├── collision.test.ts       # Bounding box mathematics & overlap checks
│   ├── enemies.test.ts         # Drone, Fighter, and Boss behaviors & bounds
│   ├── player.test.ts          # Kinematics, clamping, weapons, and shield math
│   ├── score.test.ts           # Combo streak multipliers, windows, and damage resets
│   ├── services.test.ts        # Supabase duplicate prevention & fallback validation
│   └── wave.test.ts            # Wave sizes, spawn intervals, and boss sectors
├── tsconfig.json               # Vite bundler TypeScript configuration
├── tsconfig.test.json          # Node test runner configuration
└── vite.config.ts              # Vite production bundler settings
```

---

## 2. Core Game Loop & Synchronization

The game loop utilizes browser `requestAnimationFrame` with high-resolution performance timers:
$$\Delta t = \min\left(\frac{t_{\text{now}} - t_{\text{prev}}}{1000}, 0.033\right)$$
Delta time clamping guarantees physics stability even if the player minimizes the browser tab or switches windows.

### State Machine
```
   [MENU]  ──(Launch Mission)──>  [PLAYING]
      ▲                               │
      │                               ├──(P / Esc)──> [PAUSED]
      │                               │                  │
      │                               │<──(Resume)───────┘
      │                               │
      │                               └──(Zero Lives)──> [GAME OVER]
      │                                                    │
      └───────(Return to HQ)───────────────────────────────┘
```

---

## 3. Rendering Pipeline & Vector Geometry

To ensure crisp rendering on 4K monitors and Retina displays without blurring, entity hulls are rendered as procedural geometric vectors directly on `<canvas>`:
- **Player Hull**: An 8-point Cartesian polygon with metallic gradient fill, electric cyan outline ($1.8\text{ px}$), glowing cockpit canopy, and dual animated thruster flames.
- **Enemies**:
  - Drones: 6-point dart polygon with crimson fill and ruby central sensor.
  - Fighters: 8-point swept-forward polygon with magenta stroke and gold cockpit.
  - Boss: 10-point command flagship with sponson weapon ports, pulse-phase reactor core, and command tower.
- **Starfield**: 85 stars rendered across 3 distinct parallax depth layers with twinkling sine phases and a soft radial nebula glow.

---

## 4. Audio Synthesis Architecture

Rather than loading large audio files that may fail over college Wi-Fi networks, `SoundService` generates retro arcade sound effects procedurally using the Web Audio API (`AudioContext`):
- **Player Lasers**: High-frequency sawtooth oscillator sweeping $880\text{ Hz} \rightarrow 180\text{ Hz}$ in $80\text{ ms}$.
- **Enemy Lasers**: Triangle oscillator sweeping $420\text{ Hz} \rightarrow 140\text{ Hz}$ in $120\text{ ms}$.
- **Boss Plasma**: Low square wave sweeping $260\text{ Hz} \rightarrow 60\text{ Hz}$ in $200\text{ ms}$.
- **Explosions**: White noise buffer routed through a low-pass biquad filter with exponential decay.
- **Shield Impacts**: High resonant sine chirps ($900\text{ Hz} \rightarrow 450\text{ Hz}$).
- **Power-Ups**: Arpeggiated C-major melodic chord ($523\text{ Hz} \rightarrow 659\text{ Hz} \rightarrow 784\text{ Hz} \rightarrow 1046\text{ Hz}$).

---

## 5. Backend Integration & Resilience

The web client communicates with the existing Supabase project:
- **Authentication**: `POST /auth/v1/signup` and `POST /auth/v1/token?grant_type=password` using `@supabase/supabase-js`.
- **Match Logging**: `POST /rest/v1/matches`.
  - **Duplicate Prevention**: Hashes match parameters and maintains an in-flight submission lock.
  - **Guest Isolation**: Guest matches remain in local memory/`localStorage` and are never uploaded to the database, fulfilling strict project safety guidelines.
- **Leaderboard Telemetry**: `GET /rest/v1/leaderboard`.
  - **Graceful Fallback**: If anonymous RLS permissions return HTTP 401 (pending backend approval Phase 7.2A), the service detects status 401 / code `42501` and seamlessly presents the Academy Standby Records with an informative notice.

