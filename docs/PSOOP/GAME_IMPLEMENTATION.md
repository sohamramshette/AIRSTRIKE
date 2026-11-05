# AIRSTRIKE: Java Game Implementation & OOP Architecture Manual

**Project**: AIRSTRIKE – 2D Space Shooter & Academic Demonstration System  
**Subject**: Principles of Object-Oriented Programming (PSOOP)  
**Implementation Phase**: Phase 2 Complete Java Game Development  
**Target Runtime**: Java SE 26 (Swing & AWT Desktop)  

---

## 1. Game Architecture & Subsystem Layout

AIRSTRIKE is structured following a decoupled Model-View-Controller (MVC) architecture with clear separation between simulation logic, hardware-accelerated 2D rendering, and user input handling.

```
+-----------------------------------------------------------------------------------+
|                                  MAIN WINDOW                                      |
|                               (CardLayout Frame)                                  |
+-----------------------------------------------------------------------------------+
                  |                                                 |
                  v                                                 v
+-----------------------------------+             +---------------------------------+
|             MENU PANEL            |             |           GAME CANVAS           |
|        (Swing / GridBagLayout)    |             |      (Double-Buffered JPanel)   |
+-----------------------------------+             +---------------------------------+
| - Title Branding                  |                             |
| - Launch Mission Button           |                             | Repaints on EDT
| - Pilot Manual (Instructions)     |                             v
| - System Settings Dialog          |             +---------------------------------+
| - Abort / Exit Application        |             |            GAME LOOP            |
+-----------------------------------+             |      (Swing Timer ~60 FPS)      |
                                                  +---------------------------------+
                                                                  |
                                                                  | Delta ticks (dt)
                                                                  v
                                                  +---------------------------------+
                                                  |           GAME ENGINE           |
                                                  |      (Central Simulation)       |
                                                  +---------------------------------+
                                                                  |
        +-----------------------+------------------------+--------+---------------+
        |                       |                        |                        |
        v                       v                        v                        v
+---------------+       +---------------+        +---------------+        +---------------+
|  MODEL LAYER  |       | COLLISION MGR |        | WAVE MANAGER  |        |  SCORE CALC   |
+---------------+       +---------------+        +---------------+        +---------------+
| - Player      |       | - AABB Tests  |        | - Spawn Rate  |        | - Multiplier  |
| - Enemies     |       | - Bullet Hits |        | - Level Steps |        | - Streaks     |
| - Projectiles |       | - Power-ups   |        | - Boss Waves  |        | - High Score  |
| - PowerUps    |       | - Explosions  |        | - Drop Rates  |        | - Overloading |
| - Particles   |       +---------------+        +---------------+        +---------------+
+---------------+
```

---

## 2. Class Responsibilities Matrix

| Class Name | Package | Inheritance / Interface | Key Responsibility |
| :--- | :--- | :--- | :--- |
| `Main` | `main` | Entry point | Launches `MainWindow` on the Swing Event Dispatch Thread (EDT). |
| `MainWindow` | `ui` | `JFrame` | Top-level window hosting `CardLayout` to toggle between Menu and Game. |
| `MenuPanel` | `ui` | `JPanel` | Main menu displaying arcade title, mission launch, settings, and controls. |
| `GameCanvas` | `game` | `JPanel`, `KeyListener` | Double-buffered custom painting surface handling keyboard inputs. |
| `GameLoop` | `game` | `ActionListener` | 60 FPS / UPS loop using `javax.swing.Timer` executing safely on the EDT. |
| `GameEngine` | `game` | Central Coordinator | Coordinates entity collections, state machine, and calls render/update. |
| `GameState` | `game` | `enum` | Discrete system states (`MENU`, `PLAYING`, `PAUSED`, `GAME_OVER`). |
| `CollisionManager` | `game` | Subsystem | Axis-Aligned Bounding Box (AABB) intersection tests and particle triggers. |
| `Entity` | `model` | `abstract`, `Renderable`, `Collidable` | Root physics object containing coordinates, dimensions, and kinematics. |
| `Ship` | `model` | `abstract Entity` | Base class for combat craft containing health, shields, and weapon cooldowns. |
| `Player` | `model` | `Ship` | Player spacecraft with multidirectional movement, boundary clamping, and weapons. |
| `Enemy` | `model` | `abstract Ship` | Hostile craft template with score value and polymorphic `performAttack()`. |
| `DroneEnemy` | `model` | `Enemy` | Fast scout craft with sinusoidal trajectory and low health. |
| `FighterEnemy` | `model` | `Enemy` | Combat ship firing targeted downward lasers while banking laterally. |
| `BossEnemy` | `model` | `Enemy` | Heavy dreadnought with 3 cycling attack patterns, patrol AI, and boss bar. |
| `Projectile` | `model` | `Entity` | High-velocity laser bolts with glowing visual aura and boundary cleanup. |
| `PowerUp` | `model` | `Entity` | Floating capsules granting Health, Shields, Plasma upgrades, or Speed. |
| `Particle` | `model` | `Entity` | Physics particles creating expanding explosion rings and thruster sparks. |
| `ScoreCalculator`| `service`| Utility / Service | Accumulates score, evaluates kill-streaks, and manages combo multipliers. |
| `WaveManager` | `service`| Utility / Service | Manages wave difficulty scaling, enemy spawn rates, and Boss milestones. |
| `AssetManager` | `service`| Procedural Utility | Multi-layered parallax scrolling starfield rendered with pure Java2D. |
| `AirstrikeException` | `exception` | `Exception` | Checked domain base exception for robust error handling. |
| `InvalidGameStateException` | `exception`| `AirstrikeException` | Checked exception preventing illegal state transitions (e.g. Menu to Paused). |

---

## 3. Game Loop Architecture (60 FPS / UPS)

The simulation runs on a high-precision `javax.swing.Timer` executing at approximately 16.6 milliseconds per tick (~60 FPS):

1. **Delta Time Calculation**:
   $$\Delta t = \frac{\text{now} - \text{lastTime}}{10^9} \quad (\text{clamped to } \Delta t \le 0.05\text{s})$$
2. **Deterministic Kinematics**:
   All entity displacements use delta time:
   $$x_{new} = x + v_x \cdot \Delta t, \quad y_{new} = y + v_y \cdot \Delta t$$
3. **EDT Thread Safety**:
   Because `javax.swing.Timer` invokes action listeners on the Event Dispatch Thread (EDT), UI state mutation and `repaint()` calls never suffer from race conditions or screen tearing.

---

## 4. Object-Oriented Programming (OOP) Implementation Mapping

This section documents the explicit code locations satisfying the core OOP syllabus requirements:

### 4.1. Classes and Objects
- **Instantiated Objects**: Concrete instances of `Player`, `DroneEnemy`, `FighterEnemy`, `BossEnemy`, `Projectile`, `PowerUp`, `Particle`, and `Star` are created and managed during gameplay.
- **Location**: `GameEngine.java`, `WaveManager.java`, `CollisionManager.java`.

### 4.2. Encapsulation & Data Hiding
- **Private Fields with Invariant Validation**:
  - `Entity.java`: Coordinates `x, y`, dimensions `width, height`, and kinematics `vx, vy` are protected/private, validated against non-positive bounds.
  - `Ship.java`: `health` and `shield` fields are clamped between `0` and `maxHealth`/`maxShield`.
  - `Player.java`: Private control flags (`moveLeft`, `moveRight`) mutated through dedicated accessors.
- **Location**: `Entity.java` (lines 10-65), `Ship.java` (lines 14-85), `Player.java` (lines 18-40).

### 4.3. Inheritance & Hierarchy
- Clean 3-tier inheritance tree:
  $$\text{Entity} \longrightarrow \text{Ship} \longrightarrow \text{Player}$$
  $$\text{Entity} \longrightarrow \text{Ship} \longrightarrow \text{Enemy} \longrightarrow \{\text{DroneEnemy}, \text{FighterEnemy}, \text{BossEnemy}\}$$
  $$\text{Entity} \longrightarrow \{\text{Projectile}, \text{PowerUp}, \text{Particle}\}$$
- Constructor chaining using `super(...)` passes physical dimensions, maximum health, speed, and cooldowns up the inheritance chain.
- **Location**: `Ship.java`, `Player.java`, `Enemy.java`, `DroneEnemy.java`, `FighterEnemy.java`, `BossEnemy.java`.

### 4.4. Abstract Classes & Interfaces
- **Interfaces**:
  - `Renderable`: Declares `void render(Graphics2D g)` contract.
  - `Collidable`: Declares `Rectangle getBounds()` and `boolean intersects(Collidable other)`.
- **Abstract Classes**:
  - `Entity`: Implements `Renderable` and `Collidable`, provides common bounding box logic, declares `abstract void update(double delta)`.
  - `Ship`: Inherits `Entity`, provides shielding and damage absorption, declares `abstract void shoot(List<Projectile> list)`.
  - `Enemy`: Inherits `Ship`, declares `abstract void performAttack(double delta, List<Projectile> list, Player player)`.
- **Location**: `Renderable.java`, `Collidable.java`, `Entity.java`, `Ship.java`, `Enemy.java`.

### 4.5. Runtime Polymorphism (Dynamic Method Dispatch)
- **Heterogeneous Collections**: `GameEngine` holds a single `List<Enemy>` containing mixed `DroneEnemy`, `FighterEnemy`, and `BossEnemy` instances.
- **Virtual Dispatch**: Iterating through `for (Enemy e : enemies)` executes the overridden `update()`, `performAttack()`, and `render()` belonging to the actual runtime subclass without casting or `instanceof` checks.
- **Location**: `GameEngine.java` (lines 125-132, 178-181).

### 4.6. Method Overriding (`@Override`)
- `Player.java`: Overrides `update()` for directional vector calculation, `shoot()` for weapon tier spread patterns, and `render()` for procedural fighter graphics.
- `DroneEnemy.java` / `FighterEnemy.java` / `BossEnemy.java`: Override `performAttack()` with completely distinct combat behaviors (kamikaze dive, aimed lasers, 5-way spread fan).
- `GameCanvas.java`: Overrides `paintComponent(Graphics g)` for custom 2D rendering.
- **Location**: `Player.java` (lines 35, 65, 130), `FighterEnemy.java` (lines 32, 41, 52), `GameCanvas.java` (lines 80-92).

### 4.7. Method Overloading (Compile-Time Polymorphism)
- **Damage Infliction**:
  - `Ship.takeDamage(int amount)`: Standard damage where shield absorbs damage first.
  - `Ship.takeDamage(int amount, boolean bypassShield)`: Overloaded variant allowing armor-piercing or hazard damage that ignores shielding.
- **Score Calculation**:
  - `ScoreCalculator.addScore(int baseScore)`: Calculates points using dynamic combo streak multipliers.
  - `ScoreCalculator.addScore(int baseScore, int customMultiplier)`: Overloaded variant applying explicit bonus multipliers.
- **Location**: `Ship.java` (lines 35-65), `ScoreCalculator.java` (lines 28-50).

### 4.8. Exception Handling Architecture
- Custom domain checked exception hierarchy (`AirstrikeException`, `InvalidGameStateException`, `ResourceLoadException`).
- `GameEngine.setState(GameState newState)` throws `InvalidGameStateException` if an invalid state transition (such as jumping directly from `MENU` to `PAUSED`) is requested.
- Defensive try-catch blocks and boundary handling protect game loop stability.
- **Location**: `AirstrikeException.java`, `InvalidGameStateException.java`, `GameEngine.java` (lines 55-65).

### 4.9. Java Collections Framework
- `ArrayList<Enemy>`, `ArrayList<Projectile>`, `ArrayList<PowerUp>`, and `ArrayList<Particle>` manage dynamic game elements.
- Clean functional entity pruning via `removeIf(e -> !e.isActive())` prevents memory leaks and stale references.
- **Location**: `GameEngine.java` (lines 26-30, 155-160).

---

## 5. Pilot Controls Reference

| Action | Primary Key Binding | Secondary Key Binding | Notes |
| :--- | :--- | :--- | :--- |
| **Move Up / Forward** | `W` | `UP ARROW` | Normalized diagonal speed. |
| **Move Down / Backward** | `S` | `DOWN ARROW` | Clamped to screen boundary. |
| **Move Left** | `A` | `LEFT ARROW` | Clamped to screen boundary. |
| **Move Right** | `D` | `RIGHT ARROW` | Clamped to screen boundary. |
| **Fire Plasma Cannons** | `SPACEBAR` | *None* | Hold for continuous rapid fire. |
| **Tactical Pause / Resume**| `P` | `ESCAPE` | Pauses simulation and displays pause overlay. |
| **Redeploy / Restart** | `R` | *None* | Instantly restarts mission session. |
| **Main Menu Return** | `M` | *None* | Returns to Main Menu from Pause / Game Over. |

---

## 6. Verification and Automated Testing Results

All 11 functional gameplay areas were verified through the automated test suite [`PSOOP/src/test/GameUnitTest.java`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PSOOP/src/test/GameUnitTest.java):

```
========================================================
        AIRSTRIKE PHASE 2 AUTOMATED TEST SUITE          
========================================================
  [PASS] Player Move Left
  [PASS] Player Move Right
  [PASS] Player Move Up
  [PASS] Player Move Down
  [PASS] Player Boundary Left/Top
  [PASS] Player Boundary Right/Bottom
  [PASS] Shooting Projectile Added
  [PASS] Projectile Properties
  [PASS] Shooting Cooldown
  [PASS] Enemy Spawning
  [PASS] Enemy Properties
  [PASS] Bullet Consumed on Hit
  [PASS] Enemy Damaged on Hit
  [PASS] Enemy Shield Absorption
  [PASS] Enemy Destruction
  [PASS] Player Shield Absorption
  [PASS] Player Hull Damage
  [PASS] Player Heal
  [PASS] Player Shield Recharge
  [PASS] Base Score Calculation
  [PASS] Streak Multiplier
  [PASS] Overloaded AddScore
  [PASS] Reset Streak
  [PASS] Initial Level
  [PASS] Level 3 Advance
  [PASS] Boss Spawning on Milestone
  [PASS] Engine Start Playing
  [PASS] Engine Pause
  [PASS] Engine Resume
  [PASS] Game Over Trigger
========================================================
TEST RESULTS: 30 / 30 PASSED
========================================================
```

Desktop display verification was executed using [`PSOOP/src/test/DesktopWindowVerification.java`](file:///c:/Users/lenovo/OneDrive/Desktop/Projects/AirStrike11/PSOOP/src/test/DesktopWindowVerification.java):
- Confirmed `MainWindow` set visible on Windows desktop (`isShowing() == true`).
- Verified initial `MENU` card rendering and button interaction.
- Verified transition to `PLAYING` on combat canvas.
- Verified active frame simulation and clean disposal without memory leaks.

---

## 7. Procedural Visual Assets & Rendering

AIRSTRIKE requires zero external bitmap or audio dependencies:
- **Player Spaceship**: Geometric interceptor with dark metallic wings, cyan neon edge glow, cockpit canopy, and dual animated thruster exhaust flames.
- **Drone Enemy**: Compact crimson dart shape with glowing red optic core.
- **Fighter Enemy**: Swept-forward wing fighter with purple/crimson hull and wingtip laser pods.
- **Boss Dreadnought**: Massive heavy obsidian command vessel with gold trim, pulsing core power reactor, dual wing cannons, and on-screen health bar.
- **Parallax Starfield**: Multi-layered depth simulation with background, midground, and foreground stars scrolling at varying speeds with subtle cosmic nebula dust.
- **HUD**: Antialiased health bar, shield capacitor bar, vessel lives count, weapon tier, current score, high score, sector level, and active streak multipliers.
