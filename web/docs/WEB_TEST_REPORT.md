# AIRSTRIKE Web Edition — Comprehensive QA & Gameplay Verification Report

**Document**: `web/docs/WEB_TEST_REPORT.md`  
**Phase**: 7.2C.1 — Browser QA and Gameplay Verification  
**Date of Verification**: November 7, 2026  
**Test Engines**:  
- Real Browser Automation: Google Chrome & Microsoft Edge via `puppeteer-core`
- Web Unit & Integration Tests: `tsx` + `node:test` (Node.js v24.18.0)
- Desktop Unified Academic Test Suite: `test_all.bat` (Java 26, MSVC C++17, NASM 32-bit x86, FreeGLUT)  
**Total Automated Web Tests**: 37 / 37 Passed (100%)  
**Real Browser Verification Checks**: 17 / 17 Passed on Chrome (100%) & 17 / 17 Passed on Edge (100%)  
**Desktop Academic Test Baseline**: 302 / 302 Passed (100%) Across 4 Subject Modules  

---

## 1. Real Browser Gameplay Verification (Chrome & Edge)

Automated end-to-end browser tests were conducted against production build assets served over `http://127.0.0.1:4173/` using installed browser binaries:
- **Google Chrome**: `C:\Program Files\Google\Chrome\Application\chrome.exe`
- **Microsoft Edge**: `C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe`

| Verification Check | Google Chrome | Microsoft Edge | Observed Behavior & Assertion |
| :--- | :---: | :---: | :--- |
| **Main Menu Rendering** | **PASS** | **PASS** | Title `"AIRSTRIKE"`, Subtitle `"ORBITAL DEFENSE INITIATIVE"` rendered with zero layout shift |
| **Pilot Status Capsule** | **PASS** | **PASS** | Displays `"STATUS: CADET (GUEST)"` with `"LOGIN / REGISTER"` button |
| **Leaderboard Modal** | **PASS** | **PASS** | Opens glassmorphic dialog; populates 10 academy records with medal ranks (`🥇 #1`, `🥈 #2`, `🥉 #3`) |
| **Leaderboard Error Handling**| **PASS** | **PASS** | Captures Supabase HTTP 401 code `42501` gracefully; displays Academy Standby Records notice |
| **Flight Logs Modal** | **PASS** | **PASS** | Opens flight logs table; displays guest flight records from local memory |
| **Auth Tabs (Sign In / Register)**| **PASS** | **PASS** | Toggling tabs dynamically displays/hides Pilot Callsign field (`#field-callsign-group`) |
| **Mission Launch** | **PASS** | **PASS** | Clicking `"LAUNCH MISSION"` dismisses menu; transitions `GameEngine.state` to `'PLAYING'` |
| **Initial Spacecraft Vitals** | **PASS** | **PASS** | Spawns at exact coordinates $(376.0, 540.0)$ with Hull $100$, Shield $100$, and $3$ lives |
| **Player Movement (WASD/Arrows)**| **PASS** | **PASS** | Arrow keys move spacecraft left ($x: 376 \rightarrow 307.2$) and right ($x \rightarrow 429.7$); boundaries clamped |
| **Primary Weapon Firing** | **PASS** | **PASS** | Pressing `Spacebar` spawns glowing cyan projectiles with $v_y = -560\text{ px/s}$ and $30\text{ dmg}$ |
| **Hostile Fleet Spawning** | **PASS** | **PASS** | Spawns wave hostiles within $1.2\text{ s}$; active hostile tracking confirmed |
| **Tactical Pause & Resume** | **PASS** | **PASS** | Pressing `P` suspends engine (`'PAUSED'`); clicking `"RESUME MISSION"` restores active gameplay |
| **Responsive Laptop (1366x768)**| **PASS** | **PASS** | Canvas integer scales with responsive CSS letterbox; zero viewport overflow |
| **Responsive Tablet (768x1024)**| **PASS** | **PASS** | Touch controls layer auto-activates; virtual D-pad and fire buttons visible |
| **Responsive Mobile (375x667)** | **PASS** | **PASS** | Full responsive letterboxing; virtual D-pad and fire zone interactive |
| **Virtual Touch Interaction** | **PASS** | **PASS** | Tapping `#touch-fire` triggers weapon firing via touch event handlers |
| **Game Over Sequence** | **PASS** | **PASS** | Zero lives triggers `'GAME_OVER'`; debrief card displays score, sector, duration, and retry button |
| **Deploy Again (Restart)** | **PASS** | **PASS** | Clicking `"DEPLOY AGAIN"` resets vitals ($100/100/3$), clears entities, and starts Sector 1 |
| **Audio Synthesizer Mute** | **PASS** | **PASS** | Clicking speaker toggle sets `SoundService.isMuted()` to `true` and updates icon to 🔇 |

---

## 2. Desktop (`PSOOP`) vs Browser (`web/`) Parity Comparison

Every kinematic, mathematical, and algorithmic parameter was audited between `PSOOP/src/` (Java) and `web/src/` (TypeScript):

| Parameter | Java Desktop Implementation (`PSOOP`) | TypeScript Web Implementation (`web/src/`) | Parity Status / Detailed Analysis |
| :--- | :--- | :--- | :--- |
| **Player Base Speed** | $320.0\text{ px/s}$ (`Player.java:28`) | $320.0\text{ px/s}$ (`Player.ts:9`) | **Identical**: Exact float value scaled by frame $\Delta t$. |
| **Diagonal Normalization** | $v \times 0.70710678$ (`Player.java:53`) | $v \times (1 / \sqrt{2}) \approx 0.70710678$ (`Player.ts:46`) | **Identical**: Multiplies vector components to eliminate hypotenuse hyperspeed. |
| **Weapon Cooldown** | $180\text{ ms}$ (`Player.java:28`) | $0.18\text{ s}$ ($180\text{ ms}$) (`Player.ts:13`) | **Identical**: Exact cooldown timer. |
| **Weapon Level 1** | 1 bullet, $30\text{ dmg}$ ($25 + 5 \times 1$) (`Player.java:93`) | 1 bullet, $30\text{ dmg}$, center nose (`Player.ts:89`) | **Identical**: Projectile $(x+21, y-4)$, velocity $v_y = -560\text{ px/s}$. |
| **Weapon Level 2** | 2 wingtip bullets, $35\text{ dmg}$ each (`Player.java:97`) | 2 wingtip bullets, $35\text{ dmg}$ each (`Player.ts:107`) | **Identical**: Wingtips $(x+6, y+4)$ and $(x+37, y+4)$. |
| **Weapon Level 3** | Center $45\text{ dmg}$, wingtips $40\text{ dmg}$, $v_x = \pm 110$ | Center $45\text{ dmg}$, wingtips $40\text{ dmg}$, $v_x = \pm 110$ (`Player.ts:132`) | **Identical**: Angle spread and velocities match desktop exactly. |
| **Shield Absorption** | Shield absorbs first; excess to hull (`Ship.java:46`) | Shield absorbs first; excess to hull (`Player.ts:182`) | **Identical**: Mathematical model identical to `takeDamage(amount, bypass)`. |
| **Drone Kinematics** | $v_y = 200$, $x(t) = x_0 + 35\sin(4.5t)$, $100\text{ pts}$ | $v_y = 200$, $x(t) = x_0 + 35\sin(4.5t)$, $100\text{ pts}$ (`Drone.ts:24`) | **Identical**: Sine-wave amplitude ($35\text{ px}$) and frequency ($4.5$). |
| **Fighter Kinematics** | $v_y = 86.25$, $\Delta x = 45\cos(2t)\Delta t$, $250\text{ pts}$ | $v_y = 86.25$, $v_x = 45\cos(2t)$, $250\text{ pts}$ (`Fighter.ts:20`) | **Identical**: Lateral banking matches desktop integral. |
| **Fighter Attack** | Dual lasers, $v_y = 340$, $15\text{ dmg}$, $1.2\text{ s}$ cooldown | Dual lasers, $v_y = 340$, $15\text{ dmg}$, $1.2\text{ s}$ cooldown (`Fighter.ts:40`) | **Identical**: Fires while altitude $20 < y < 450$. |
| **Boss Flags & Hull** | Health $550$, Shield $150$, Bounds $30 \le x \le 650$, $2500\text{ pts}$ | Health $550$, Shield $150$, Bounds $30 \le x \le 650$, $2500\text{ pts}$ (`Boss.ts:15`) | **Identical**: Speed $80\text{ px/s}$, descent ceiling $y = 45\text{ px}$ at $40\text{ px/s}$. |
| **Boss Attack Phases** | Phase 0 (5-way spread), Phase 1 (dual heavy), Phase 2 (targeted) | Phase 0 (5-way spread), Phase 1 (dual heavy), Phase 2 (targeted) (`Boss.ts:60`) | **Identical**: 4.0s phase timers, velocities ($v_y = 310, 360, 330$) match. |
| **Power-Up Types** | Health ($+40$), Shield ($+50$), Weapon ($+1$), Speed ($+25$) | Health ($+40$), Shield ($+50$), Weapon ($+1$), Speed ($+25$) (`CollisionManager.ts:160`)| **Identical**: All 4 types and stat deltas match. |
| **Power-Up Drop Rate**| $22\%$ (`WaveManager.java:109`) | $22\%$ (`CollisionManager.ts:58`) | **Identical**: Random roll $< 0.22$. |
| **Score Streak Window**| $3.5\text{ s}$ (`ScoreCalculator.java:53`) | $3.5\text{ s}$ (`ScoreCalculator.ts:7`) | **Identical**: Streak resets if gap $> 3.5\text{ s}$. |
| **Multiplier Tiers** | $1\text{--}2$ ($1\times$), $3\text{--}7$ ($2\times$), $8\text{--}14$ ($3\times$), $15+$ ($4\times$) | $1\text{--}2$ ($1\times$), $3\text{--}7$ ($2\times$), $8\text{--}14$ ($3\times$), $15+$ ($4\times$) (`ScoreCalculator.ts:77`) | **Identical**: Tier boundaries and multipliers match. |
| **Streak Break** | Reset to 0 on player damage (`ScoreCalculator.java:70`)| Reset to 0 on player damage (`ScoreCalculator.ts:69`) | **Identical**: Multiplier drops to $1\times$ immediately upon hit. |
| **Wave Scaling** | Total $8 + 4L$, Interval $\max(0.7, 2.2 - 0.2L)$ | Total $8 + 4L$, Interval $\max(0.7, 2.2 - 0.2L)$ (`WaveManager.ts:38`) | **Identical**: Sector enemy count and dynamic spawn pacing match. |
| **Boss Wave Cycle** | Every 3rd Sector ($L \pmod 3 == 0$) (`WaveManager.java:59`)| Every 3rd Sector ($L \pmod 3 == 0$) (`WaveManager.ts:23`) | **Identical**: Standard spawns halt, Leviathan spawns, $2.5\text{ s}$ clear banner. |

### Documented Technical Platform Differences
1. **Rendering Subsystem**:
   - Desktop Java renders via `java.awt.Graphics2D` / `JPanel` paint pipeline on a Swing Timer.
   - Browser Web renders via HTML5 Canvas 2D (`CanvasRenderingContext2D`) driven by browser `requestAnimationFrame` with delta time clamping ($\Delta t \le 0.033\text{ s}$).
2. **Audio Generation**:
   - Desktop Java uses silent fallbacks/mocks for cross-platform portability.
   - Browser Web uses a procedural Web Audio API synthesizer (`AudioContext`) that synthesizes retro laser chirps, explosions, and chimes in memory with zero external assets.
3. **Session High Score Persistence**:
   - Desktop Java maintains high score in-memory per JVM run.
   - Browser Web persists session high score across reloads using `localStorage.getItem('airstrike_high_score')`.
4. **Input Modality**:
   - Desktop Java supports physical keyboard only.
   - Browser Web supports physical keyboard, mouse click/drag, and responsive on-screen touch D-Pad and FIRE buttons for mobile/tablet devices.

---

## 3. Automated Web Test Suites Breakdown (37 / 37 Passed)

Executed via `npm test --prefix web`:

| Suite File | Test Assertion Description | Status | Execution Time |
| :--- | :--- | :---: | :---: |
| `tests/collision.test.ts` | AABB collision correctly detects overlapping bounding boxes | **PASS** | 0.55 ms |
| `tests/collision.test.ts` | AABB collision correctly detects separated bounding boxes | **PASS** | 0.19 ms |
| `tests/collision.test.ts` | AABB collision handles touching edges as non-penetrating | **PASS** | 0.17 ms |
| `tests/enemies.test.ts` | Drone Scout properties and sine-wave trajectory | **PASS** | 0.67 ms |
| `tests/enemies.test.ts` | Combat Fighter properties, banking, and projectile attacks | **PASS** | 0.25 ms |
| `tests/enemies.test.ts` | Flagship Dreadnought Boss properties and cyclic attack phases | **PASS** | 0.73 ms |
| `tests/enemies.test.ts` | Flagship patrol sweep stays within 30 to 650 boundary | **PASS** | 0.15 ms |
| `tests/player.test.ts` | Initial kinematics and vital parameters match specification | **PASS** | 0.73 ms |
| `tests/player.test.ts` | Movement vector normalization prevents diagonal hyperspeed | **PASS** | 0.19 ms |
| `tests/player.test.ts` | Playfield boundaries clamp spacecraft within 800x650 viewport | **PASS** | 0.12 ms |
| `tests/player.test.ts` | Weapon Level 1 spawns single cannon projectile with 30 damage | **PASS** | 0.18 ms |
| `tests/player.test.ts` | Weapon Level 2 spawns dual wingtip cannons with 35 damage each | **PASS** | 0.10 ms |
| `tests/player.test.ts` | Weapon Level 3 spawns triple spread cannons (center 45, angled 40) | **PASS** | 0.12 ms |
| `tests/player.test.ts` | Shield absorption absorbs damage completely when shield >= damage | **PASS** | 0.12 ms |
| `tests/player.test.ts` | Shield absorption spills over into hull when damage > shield | **PASS** | 0.08 ms |
| `tests/player.test.ts` | Lethal damage decrements lives and triggers respawn | **PASS** | 0.18 ms |
| `tests/player.test.ts` | Power-up enhancements apply correctly to player state | **PASS** | 0.18 ms |
| `tests/score.test.ts` | Initial state has score 0 and multiplier 1x | **PASS** | 0.87 ms |
| `tests/score.test.ts` | Kill streak multipliers scale: 1-2 (1x), 3-7 (2x), 8-14 (3x), 15+ (4x) | **PASS** | 0.33 ms |
| `tests/score.test.ts` | Combo timer window expires after 3.5 seconds of inactivity | **PASS** | 0.22 ms |
| `tests/score.test.ts` | Player taking damage immediately breaks kill-streak combo | **PASS** | 0.17 ms |
| `tests/score.test.ts` | Bonus points add directly to active score without affecting combo streak | **PASS** | 0.18 ms |
| `tests/services.test.ts` | LeaderboardService returns formatted offline leaderboard entries sorted descending | **PASS** | 383.23 ms |
| `tests/services.test.ts` | MatchService prevents duplicate consecutive submissions | **PASS** | 2.53 ms |
| `tests/services.test.ts` | MatchService guest matches persist locally | **PASS** | 0.55 ms |
| `tests/wave.test.ts` | Sector 1 initializes with 12 wave enemies (8 + 1*4) | **PASS** | 0.52 ms |
| `tests/wave.test.ts` | Sector 2 initializes with 16 wave enemies (8 + 2*4) | **PASS** | 0.11 ms |
| `tests/wave.test.ts` | Sector 3 triggers Flagship Boss Encounter | **PASS** | 0.08 ms |
| `tests/wave.test.ts` | Sector 6 and 9 also trigger Boss Encounters | **PASS** | 0.09 ms |
| `tests/wave.test.ts` | Enemy spawning and wave clear transitions | **PASS** | 0.27 ms |
| `tests/backend_integration.test.ts` | Leaderboard captures Supabase 401/42501 error and serves standby records | **PASS** | 551.53 ms |
| `tests/backend_integration.test.ts` | Auth validation rejects invalid email and weak passwords locally | **PASS** | 0.46 ms |
| `tests/backend_integration.test.ts` | Auth login with invalid credentials parses Supabase error response | **PASS** | 192.00 ms |
| `tests/backend_integration.test.ts` | Guest matches are strictly isolated and never uploaded to Supabase | **PASS** | 2.51 ms |
| `tests/backend_integration.test.ts` | Match submission guards against duplicate concurrent submissions | **PASS** | 0.51 ms |
| `tests/backend_integration.test.ts` | Callsign resolution falls back to formatted email username when profile empty | **PASS** | 142.58 ms |
| `tests/backend_integration.test.ts` | Session state clears completely on logout | **PASS** | 0.56 ms |

---

## 4. Desktop Baseline Regression Verification (302 / 302 Passed)

Running `test_all.bat` confirms that the entire multi-subject desktop suite continues to pass with zero regressions:
- **PSOOP (Java 26 + Swing + Supabase)**: 143 / 143 Passed (Core game unit tests, Auth test suite, Match debrief & Leaderboard tests)
- **PL (C++17 Data Structures & Algos)**: 53 / 53 Passed (Spatial Grid, Enemy BST, QuadTree, Weapon Factory, Graph Patrol)
- **COA (Intel 80386 32-bit Assembly)**: 41 / 41 Passed (Booth Multiplier, Bitwise Collision, Fixed-Point Math, Matrix Traversal)
- **CGL (OpenGL / FreeGLUT Raster & Lighting)**: 65 / 65 Passed (Bresenham, Midpoint Circles, Projections, Cohen-Sutherland, Phong)
- **Total Academic Assertions**: **302 / 302 PASSED (100%)**

---

## 5. Security Inspection Findings

An exhaustive audit of local files, git history, and build artifacts was conducted:
1. **Confidential Credentials**:
   - `web/.env` contains only `VITE_SUPABASE_URL` and `VITE_SUPABASE_PUBLISHABLE_KEY` (`sb_publishable_...`).
   - `web/.env` is strictly verified as ignored by git (`git check-ignore web/.env` returns `0`).
   - `.gitignore` actively excludes `.env`, `web/.env`, `web/.env.local`, `dist/`, `release/`, and `node_modules/`.
   - `git grep -i "service_role"` confirmed that zero administrative `service_role` or secret keys exist in the codebase. All references to `service_role` in the repository are explicit runtime checks in `SupabaseConfig.java` and tests that deliberately throw `SecurityException` if detected.
2. **Score Integrity Status**:
   - **Score integrity is NOT yet declared secure on the server side** in Phase 7.2C.1.
   - Client-side duplicate prevention is active in `MatchService.ts`, and guest matches are strictly quarantined locally.
   - Server-side cryptographic HMAC-SHA256 signature verification and maximum score rate limits remain planned for **Phase 7.2A** pending teacher approval.

---

## 6. Bugs Discovered & Fixes Applied During QA

| Bug / Observation | Root Cause | Fix Applied | Verification Result |
| :--- | :--- | :--- | :---: |
| **Modal Close Hitbox** | Small hit target for `.modal-close-btn` on high-DPI and mobile displays. | Enlarged close button hit area to $36 \times 36\text{ px}$ with flex centering, hover halo, and border radius. | Modal close clicks succeeded across all viewports. |
| **Input Key Compatibility** | `InputManager` originally checked only `e.code` (`KeyP`, `Space`). Synthetic keyboard events in certain browser configurations emit `e.key`. | Updated `InputManager.ts` to check both `e.code` and `e.key` (e.g. `e.code === 'KeyP' \|\| e.key === 'p'`). | Tactical pause and keyboard controls pass across both Chrome and Edge. |
| **Missing Favicon 404** | Browser automatically requested `/favicon.ico`, producing a console 404 error. | Added inline SVG data URI favicon `<link rel="icon" type="image/svg+xml" ...>` inside `<head>`. | Clean console logs with zero 404 errors during page load. |
| **Headless LocalStorage** | Headless Node.js test runners lacked `window.localStorage` on `globalThis`, printing warnings during headless test runs. | Added `typeof localStorage !== 'undefined'` checks with in-memory fallback array in `MatchService`, `ScoreCalculator`, and `SoundService`. | Tests execute cleanly in both headless Node and live browser environments. |

---

## 7. Remaining Blockers & Next Phase Readiness

- **Current Blocker**: None within the browser client.
- **Backend Blocker**: Phase 7.2A SQL Migration (`20261107_phase7_backend_correction.sql`) remains unexecuted awaiting teacher review and approval. Once approved, executing the migration will activate public anonymous SELECT on `public.leaderboard` and establish the server-side score validation RPC (`submit_match_score`).
