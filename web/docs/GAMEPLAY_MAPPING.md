# AIRSTRIKE: Desktop-to-Browser Gameplay Mechanics Mapping Specification

**Document**: `web/docs/GAMEPLAY_MAPPING.md`  
**Purpose**: Rigorous specification of all mathematical models, entity physics, rendering contracts, scoring logic, wave progressions, and backend schemas ported from the canonical Java desktop implementation (`PSOOP`) to the web browser client.  
**Source Baseline**: Java SE 26 / Swing / AWT Runtime  
**Target Runtime**: TypeScript / HTML5 Canvas 2D (`CanvasRenderingContext2D`)  

---

## 1. Physical World & Viewport Configuration

| Attribute | Desktop Canonical Value (`PSOOP`) | Web Browser Port |
| :--- | :--- | :--- |
| **Virtual Playfield Resolution** | $800 \times 650$ pixels | $800 \times 650$ base logical canvas with responsive integer/aspect-ratio scaling |
| **Minimum Viewport** | $640 \times 480$ pixels | CSS letterboxed canvas with max height 95vh / aspect ratio $800:650$ |
| **Physics Delta Time ($\Delta t$)** | Elapsed seconds between frames, clamped to $0.033\text{ s}$ | `performance.now()` accumulated delta: $\Delta t = \min(dt, 0.033)$ |
| **Target Frame Rate** | $60\text{ FPS}$ (Swing Timer $16\text{ ms}$) | $60\text{ FPS}$ (`requestAnimationFrame`) |
| **Coordinate System** | Origin $(0, 0)$ top-left, $+X$ right, $+Y$ down | Identical Cartesian top-left origin |

---

## 2. Player Spacecraft (`Player.java` $\rightarrow$ `Player.ts`)

### 2.1 Kinematic & Vital Constants
- **Hitbox Dimensions**: $\text{width} = 48\text{ px}$, $\text{height} = 56\text{ px}$
- **Maximum Hull (Health)**: $100$
- **Maximum Shield**: $100$
- **Initial Lives**: $3$
- **Base Speed**: $320.0\text{ px/s}$
- **Diagonal Normalization**: If $|v_x| > 0$ and $|v_y| > 0$, multiply by $1/\sqrt{2} \approx 0.70710678$
- **Playfield Boundary Clamping**: $0 \le x \le \text{width} - 48$, $0 \le y \le \text{height} - 56$
- **Respawn Invulnerability**: $2,500\text{ ms}$ duration with $100\text{ ms}$ blinking interval
- **Impact Invulnerability**: $400\text{ ms}$ after projectile hit, $800\text{ ms}$ after ramming hit
- **Initial Position**: $x = (800 - 48) / 2 = 376.0$, $y = 650 - 110 = 540.0$

### 2.2 Weapon Systems & Cannons
- **Base Shot Cooldown**: $180\text{ ms}$
- **Player Bullet Velocity**: $v_x = 0$, $v_y = -560.0\text{ px/s}$
- **Bullet Dimensions**: width $6\text{ px}$, height $16\text{ px}$ (cyan core `#B4FFFF`, outer glow `rgba(0, 200, 255, 0.47)`)
- **Weapon Levels**:
  - **Level 1 (Single Cannon)**: 1 projectile from nose tip $(x + 21, y - 4)$, $\text{damage} = 30$ ($25 + 5 \times 1$).
  - **Level 2 (Dual Cannons)**: 2 projectiles from wingtips $(x + 6, y + 4)$ and $(x + 37, y + 4)$, $\text{damage} = 35$ each ($25 + 5 \times 2$).
  - **Level 3 (Triple Spread Cannons)**: 3 projectiles:
    - Center nose: $v_x = 0$, $v_y = -560$, $\text{damage} = 45$
    - Left wing angled: $v_x = -110$, $v_y = -560$, $\text{damage} = 40$
    - Right wing angled: $v_x = 110$, $v_y = -560$, $\text{damage} = 40$

### 2.3 Vector Hull Geometry (8-Point Polygon)
Offsets relative to $(p_x, p_y)$:
1. Nose Tip: $(p_x + 24, p_y)$
2. Right Shoulder: $(p_x + 38, p_y + 36)$
3. Right Wingtip: $(p_x + 48, p_y + 48)$
4. Right Engine: $(p_x + 36, p_y + 52)$
5. Center Aft: $(p_x + 24, p_y + 44)$
6. Left Engine: $(p_x + 12, p_y + 52)$
7. Left Wingtip: $(p_x, p_y + 48)$
8. Left Shoulder: $(p_x + 10, p_y + 36)$
- Fill: Dark metallic blue `#1E2D4B`
- Stroke: Electric Cyan `#00D2FF` ($1.8\text{ px}$)
- Cockpit: Cyan oval at $(p_x + 20, p_y + 18)$, size $8 \times 16\text{ px}$
- Thruster Flame: Pulsing orange `#FF7814` and yellow `#FFF050` with $12 + 6 \sin(15t)$ length

---

## 3. Hostile Forces & Enemy Archetypes

### 3.1 Drone Scout (`DroneEnemy.java` $\rightarrow$ `Drone.ts`)
- **Dimensions**: $32 \times 32\text{ px}$
- **Health**: $30$ | **Shield**: $0$
- **Speed ($v_y$)**: $200.0\text{ px/s}$ downward
- **Trajectory**: Sine-wave lateral weave: $x(t) = x_0 + 35.0 \times \sin(4.5t)$
- **Weapon**: None (ramming attack trajectory)
- **Score Value**: $100\text{ points}$
- **Geometry**: Downward dart polygon: $(16, 32) \rightarrow (32, 8) \rightarrow (24, 0) \rightarrow (16, 6) \rightarrow (8, 0) \rightarrow (0, 8)$. Crimson hull `#8C191E`, orange stroke `#FF5A28`, red optic sensor.

### 3.2 Combat Fighter (`FighterEnemy.java` $\rightarrow$ `Fighter.ts`)
- **Dimensions**: $42 \times 44\text{ px}$
- **Health**: $60$ | **Shield**: $20$
- **Speed ($v_y$)**: $115 \times 0.75 = 86.25\text{ px/s}$ downward
- **Trajectory**: Lateral banking: $v_x(t) = 45.0 \times \cos(2.0t)$
- **Weapon Cooldown**: $1,200\text{ ms}$ (fires while $20 < y < 450$)
- **Projectiles**: Dual downward laser bolts from wingtips, $v_y = 340.0\text{ px/s}$, $\text{damage} = 15$ each
- **Score Value**: $250\text{ points}$
- **Geometry**: Swept-forward wing hull: $(21, 44) \rightarrow (38, 32) \rightarrow (42, 12) \rightarrow (28, 0) \rightarrow (21, 10) \rightarrow (14, 0) \rightarrow (0, 12) \rightarrow (4, 32)$. Deep purple hull `#41144B`, magenta stroke `#E628B4`, gold cockpit.

### 3.3 Flagship Dreadnought (`BossEnemy.java` $\rightarrow$ `Boss.ts`)
- **Dimensions**: $116 \times 78\text{ px}$
- **Health**: $550$ | **Shield**: $150$
- **Speed ($v_x$)**: $80.0\text{ px/s}$ patrol sweep
- **Descent**: Enters from $y = -80$ down to $y = 45$ at $40.0\text{ px/s}$
- **Patrol Bounds**: $30 \le x \le 650$ (reverses velocity upon hitting bounds)
- **Score Value**: $2,500\text{ points}$
- **Cyclic Attack Phases** (switches every $4.0\text{ s}$ across 3 phases, shot cooldown $900\text{ ms}$):
  - **Phase 0 (5-Way Spread Fan)**: Fires 5 projectiles from nose $(x + 58, y + 72)$ with velocities:
    $v_x \in \{-160, -80, 0, 80, 160\}$, $v_y = 310.0\text{ px/s}$, $\text{damage} = 15$ each.
  - **Phase 1 (Heavy Dual Cannons)**: Fires 2 heavy plasma bolts from sponsons $(x + 24)$ and $(x + 84)$, $v_x = \mp 20$, $v_y = 360.0$, size $8 \times 22\text{ px}$, $\text{damage} = 25$ each.
  - **Phase 2 (Targeted Barrage)**: Calculates normalized direction vector $(\Delta x, \Delta y)$ towards player center:
    $\vec{v} = \frac{\Delta \vec{p}}{\|\Delta \vec{p}\|} \times 330.0$, size $8 \times 18\text{ px}$, $\text{damage} = 20$.
- **HUD Boss Bar**: Displayed at top center ($x = (800 - 480) / 2$, $y = 32$, width $480\text{ px}$, height $14\text{ px}$). Red health fill + cyan shield line.

---

## 4. Damage, Shielding & Collision Rules

### 4.1 Shield Absorption Model (`Ship.java`)
```
takeDamage(amount, bypassShield):
    if (!bypassShield and shield > 0):
        if (shield >= amount):
            shield -= amount
            amount = 0
        else:
            amount -= shield
            shield = 0
    if (amount > 0):
        health = max(0, health - amount)
```

### 4.2 Collision Scenarios (`CollisionManager.java`)
1. **Player Bullet vs Enemy**: Bullet destroyed; enemy damaged by bullet damage ($25\text{--}45$); spawns 4 sparks. If destroyed: awards enemy score value, spawns 20 explosion particles, and rolls power-up drop.
2. **Enemy Bullet vs Player**: If not invulnerable: bullet destroyed; player takes damage; grants $400\text{ ms}$ invulnerability; resets kill streak multiplier to $1\times$; spawns 8 sparks (cyan if shield active, red if hull damaged).
3. **Player Ship vs Enemy Ship (Ramming)**: If not invulnerable: player takes $35\text{ damage}$ ($800\text{ ms}$ invulnerability); enemy takes $60\text{ damage}$; resets kill streak multiplier to $1\times$; spawns 16 explosion particles.
4. **Player vs Power-Up**: Capsule absorbed; awards $+150\text{ pts}$ bonus score; spawns 12 colored sparkles.

---

## 5. Power-Up Enhancements (`PowerUp.java`)

- **Drop Rate**: $22\%$ random chance on enemy destruction.
- **Capsule Dimensions**: $26 \times 26\text{ px}$ with pulsing halo ($85.0\text{ px/s}$ descent, $\pm 12\text{ px}$ sine wave).
- **Types**:
  - `HEALTH_PACK` (`+`, Green `#32DC50`): Heals $+40\text{ health}$ (up to $100$).
  - `SHIELD_BOOST` (`S`, Blue `#3CA0FF`): Recharges $+50\text{ shield}$ (up to $100$).
  - `WEAPON_UPGRADE` (`W`, Orange `#FFB428`): Upgrades weapon level ($1 \rightarrow 2 \rightarrow 3$).
  - `SPEED_BOOST` (`^`, Purple `#DC46FF`): Increases player flight speed by $+25.0\text{ px/s}$.

---

## 6. Scoring & Kill-Streak Combo System (`ScoreCalculator.java`)

- **Combo Window**: $3.5\text{ seconds}$ ($3,500\text{ ms}$) between kills. If a kill occurs within $3.5\text{ s}$ of previous kill, streak increments by 1. Otherwise, streak resets to 1.
- **Multiplier Tiers**:
  - Streak $1\text{--}2$: $1\times$ Multiplier
  - Streak $3\text{--}7$: $2\times$ Multiplier
  - Streak $8\text{--}14$: $3\times$ Multiplier
  - Streak $15+$: $4\times$ Multiplier
- **Points Added**: $\text{Points} = \text{BaseScore} \times \text{Multiplier}$
- **Streak Break**: Multiplier resets to $1\times$ whenever the player takes any bullet or ramming damage.
- **High Score**: High score is dynamically updated whenever active score exceeds session record.

---

## 7. Wave Progression & Difficulty Curve (`WaveManager.java`)

- **Starting Level**: Sector 1
- **Spawn Interval**: $\text{interval} = \max(0.7\text{ s}, 2.2 - (\text{level} \times 0.2)\text{ s})$
- **Enemies per Standard Wave**: $\text{total} = 8 + (\text{level} \times 4)$
- **Enemy Type Distribution**:
  - Fighter probability: $P(\text{Fighter}) = \min(65\%, \text{level} \times 15\%)$
  - Drone probability: $100\% - P(\text{Fighter})$
- **Boss Encounter Waves**: Occurs every 3rd level ($\text{level} \pmod 3 == 0$):
  - Standard spawns halted.
  - Spawns Dreadnought flagship `"Leviathan MK-" + (level / 3)`.
  - When Boss is defeated, triggers Wave Clear Notice banner ($2.5\text{ s}$ delay) before Sector $\text{level} + 1$.
- **Sector Clear Transition**: Displays `"SECTOR SECURED"` banner for $2.5\text{ s}$, then advances to next level.

---

## 8. Supabase Cloud Contracts (`MatchService.java` & `AuthService.java`)

| Operation | HTTP Endpoint | Payload / Parameters | Auth Header |
| :--- | :--- | :--- | :--- |
| **Signup** | `POST /auth/v1/signup` | `{ email, password, data: { callsign } }` | `apikey: <publishable-key>` |
| **Login** | `POST /auth/v1/token?grant_type=password` | `{ email, password }` | `apikey: <publishable-key>` |
| **Get Profile**| `GET /rest/v1/profiles?user_id=eq.<id>&select=callsign` | None | `Authorization: Bearer <jwt>` |
| **Save Match** | `POST /rest/v1/matches` | `{ user_id, score, level_reached, duration_seconds }` | `Authorization: Bearer <jwt>`, `Prefer: return=representation` |
| **Get History**| `GET /rest/v1/matches?select=id,score,level_reached,duration_seconds,created_at&user_id=eq.<id>&order=created_at.desc` | None | `Authorization: Bearer <jwt>` |
| **Leaderboard**| `GET /rest/v1/leaderboard?select=username,best_score&order=best_score.desc&limit=10` | None | `apikey: <publishable-key>` |

