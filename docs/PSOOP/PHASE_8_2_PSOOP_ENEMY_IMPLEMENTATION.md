# AIRSTRIKE Phase 8.2: PSOOP Enemy Implementation & Academic CO Verification Report

**Project Title**: AIRSTRIKE – 2D Space Shooter & Academic Demonstration System  
**Phase**: Phase 8.2 — PSOOP Enemy System Implementation  
**Subject**: Principles of Software Object-Oriented Programming (PSOOP)  
**Modules Modified**: Java SE 26 (Swing / AWT Desktop) & TypeScript (HTML5 Canvas Browser)  
**Total Verification Assertions**: 169 Java Assertions + 44 Web Assertions (100% Pass Rate)  

---

## 1. Executive Summary

In Phase 8.2, five distinct enemy classes were implemented in both the Java desktop game (`PSOOP/src/model/`) and the corresponding browser client (`web/src/game/`). Each enemy demonstrates a verified concept from the **Principles of Software Object-Oriented Programming (PSOOP)** Course Outcomes (CO1 through CO4).

### Restrictions Compliance Checklist:
* **PL, COA, CGL Code Untouched**: Zero changes were made to C++17 Programming Laboratory (`PL/`), 80386 Assembly (`COA/`), or OpenGL/GLUT (`CGL/`).
* **No New Environments**: Combat is situated inside the existing space sector canvas.
* **No New Bosses Added**: The 5 implemented units are regular and tactical fleet combatants. `BossEnemy` ("Leviathan MK-X") remains the milestone encounter ($Level \pmod 3 == 0$).
* **Scoring & Database Intact**: Base scores and multipliers in `ScoreCalculator` and Supabase RLS policies remain untouched.
* **Existing Enemies Preserved**: Baseline `DroneEnemy` and `FighterEnemy` are preserved in both desktop and web runtimes.
* **Architecture Integrity**: Clean object-oriented extension with zero hacks or breaking modifications.

---

## 2. Master Academic Mapping & Enemy Specifications

### Enemy 1: Armoured Vanguard (`ArmouredEnemy.java` / `Armoured.ts`)
* **Subject & CO**: **PSOOP-CO1** — Object-Oriented Solutions & Principles
* **Syllabus Concept**: Encapsulation, Access Specifiers, and Inheritance.
* **Class & Interface Structure**:
  - Desktop: `public class ArmouredEnemy extends Enemy`
  - Browser: `export class Armoured extends Enemy`
* **Encapsulated State**:
  - `private int armorRating;` (8-point flat kinetic damage mitigation)
  - Controlled accessors: `public int getArmorRating()`, `public void setArmorRating(int rating)`
* **Controlled Damage Method**:
  ```java
  @Override
  public void takeDamage(int amount, boolean bypassShield) {
      if (amount <= 0 || !active) return;
      int remaining = amount;
      if (!bypassShield && shield > 0) {
          if (shield >= remaining) {
              shield -= remaining;
              remaining = 0;
          } else {
              remaining -= shield;
              shield = 0;
          }
      }
      if (remaining > 0) {
          int effectiveDamage = Math.max(1, remaining - armorRating);
          health = Math.max(0, health - effectiveDamage);
          if (health == 0) active = false;
      }
  }
  ```
* **Technical Explanation**: Demonstrates strict encapsulation by keeping `armorRating` private and protecting object invariants. When incoming damage strikes the craft, shields absorb damage first; any remaining damage penetrates to the hull where the armored plating dampens it by a flat amount ($\max(1, D_{in} - Armor)$).
* **Viva Explanation**: *"Armoured Vanguard demonstrates Encapsulation and Inheritance under PSOOP-CO1. Its armor rating is a private internal field inaccessible directly by outside classes. Rather than allowing callers to directly manipulate health, its overridden `takeDamage()` method enforces the business rules: shields absorb kinetic energy first, then the armored plating mitigates the remaining impact, ensuring state consistency."*

---

### Enemy 2: Interceptor Fighter (`InterceptorEnemy.java` / `Interceptor.ts`)
* **Subject & CO**: **PSOOP-CO1** — Object-Oriented Solutions & Principles
* **Syllabus Concept**: Polymorphism, Method Overriding, and Dynamic Method Dispatch.
* **Class & Interface Structure**:
  - Desktop: `public class InterceptorEnemy extends Enemy`
  - Browser: `export class Interceptor extends Enemy`
* **Polymorphic Methods Overridden**:
  - `public void update(double delta)`
  - `public void performAttack(double delta, List<Projectile> projectileList, Player player)`
* **Attack & Pursuit Behaviour**:
  - Scans for player horizontal alignment: $|player.getCenterX() - getCenterX()| \le 75.0$.
  - Triggers a high-speed dash: surges from $v_y = 110.0$ to $v_y = 260.0 \text{ px/s}$ for $0.85$ seconds with trailing thruster exhaust.
  - Fires staggered twin micro-darts: discharges the left wing projectile immediately, queues the second shot, and discharges the right wing projectile after a $140\text{ ms}$ delay.
* **Technical Explanation**: In `GameEngine.java`, the game loop iterates through `List<Enemy> enemies` without knowing the concrete subtype of each craft. Dynamic method dispatch at runtime binds calls to `e.performAttack()` and `e.update()` directly to `InterceptorEnemy`'s implementation.
* **Viva Explanation**: *"Interceptor Fighter demonstrates Runtime Polymorphism and Dynamic Dispatch. The GameEngine maintains a polymorphic collection `List<Enemy>` and executes `e.performAttack()`. The JVM uses its virtual method table (vtable) to resolve `InterceptorEnemy`'s specialized alignment-detection and staggered twin attacks at runtime without type-casting."*

---

### Enemy 3: Adaptive Morphing Drone (`MorphingEnemy.java` / `Morphing.ts`)
* **Subject & CO**: **PSOOP-CO2** — Solutions to Reduce Complexity
* **Syllabus Concept**: Software Design Patterns (State Pattern) & High Cohesion / Low Coupling.
* **Class & Interface Structure**:
  - Desktop: `public class MorphingEnemy extends Enemy`, driven by `public enum DroneAIState`
  - Browser: `export class Morphing extends Enemy`, driven by `export type DroneState`
* **Four Encapsulated States**:
  1. `PATROL`: Default cruising state. Sinusoidal lateral weave while descending at $95\text{ px/s}$. Transitions to `CHASE` when player distance $< 260\text{ px}$, or `EVADE` if health $< 40\%$.
  2. `CHASE`: Aggressive pursuit. Aligns with player X corridor ($v_x = \text{sign}(dx) \cdot 110$) and fires targeted plasma needles. Transitions to `OVERCHARGE` when in close proximity ($dist < 150\text{ px}$), or `EVADE` if health $< 35\%$.
  3. `EVADE`: Evasive retreat. Veers away from player ($v_x = -\text{sign}(dx) \cdot 130$) with reduced firing frequency to preserve hull integrity.
  4. `OVERCHARGE`: Temporary overload state. Velocity surges to $180\text{ px/s}$, emits pulsing yellow warning aura, and discharges rapid twin micro-bursts before transitioning to `EVADE` upon timer expiration.
* **Technical Explanation**: Replaces monolithic nested `if-else` blocks with the Gang of Four State Pattern. In Java, each `DroneAIState` constant defines its own polymorphic `update()`, `attack()`, and `evaluateTransition()` methods, achieving high cohesion and eliminating code smells.
* **Viva Explanation**: *"Adaptive Morphing Drone demonstrates Complexity Reduction under PSOOP-CO2. Rather than using large, error-prone nested switch statements, we implement the State Pattern via `DroneAIState`. Each state encapsulates its own movement, firing, and transition rules, dramatically reducing cyclomatic complexity."*

---

### Enemy 4: Fail-Safe Sentry (`FailSafeEnemy.java` / `FailSafe.ts`)
* **Subject & CO**: **PSOOP-CO3** — Critical Thinking & Problem Solving
* **Syllabus Concept**: Defensive Programming, Invariant Checking, and Exception Resilience.
* **Class & Interface Structure**:
  - Desktop: `public class FailSafeEnemy extends Enemy`
  - Browser: `export class FailSafe extends Enemy`
* **Defensive Mechanisms**:
  - **Single-Use Emergency Barrier Latch**: Critical health is detected when $health \le \lceil maxHealth \times 0.30 \rceil$ ($21\text{ HP}$). Activates an emergency barrier ($3.0\text{ s}$ duration) and recharges shields to $\ge 35\text{ points}$. Latched boolean `emergencyBarrierTriggered` strictly prevents infinite or repeated barrier loops.
  - **Boundary Clamping**: Coordinates are clamped within arena margins ($10 \le x \le 750$).
  - **Defensive Invariant Validation**: Guarded against invalid or NaN frame deltas (`if (delta <= 0 || Double.isNaN(delta)) return;`) and rejects negative damage inputs.
* **Technical Explanation**: Exemplifies defensive programming by establishing class invariants at construction and enforcing precondition checks in all mutator methods. The emergency barrier logic prevents invalid negative health and clamps state recovery within safe limits.
* **Viva Explanation**: *"Fail-Safe Sentry demonstrates Defensive Programming under PSOOP-CO3. It monitors its internal state for critical health thresholds ($30\%$). Upon breach, it triggers a single-use emergency barrier that recharges shields and mitigates damage by 50%. A latched boolean flag ensures that the emergency protocol cannot be exploited repeatedly."*

---

### Enemy 5: Kinetic Battering Ram (`RammerEnemy.java` / `Rammer.ts`)
* **Subject & CO**: **PSOOP-CO4** — Logic-Building for Real-Life Problems
* **Syllabus Concept**: Real-Time Delta-Time Simulation, Kinematics Integration, and 2D Collision Response.
* **Class & Interface Structure**:
  - Desktop: `public class RammerEnemy extends Enemy`
  - Browser: `export class Rammer extends Enemy`
* **Kinematics & Collision Mechanics**:
  - **Non-Uniform Acceleration Integration**: Simulates real-world kinematics over delta time ($\Delta t$):
    $$v_y(t + \Delta t) = \min(v_{max}, v_y(t) + a_y \cdot \Delta t)$$
    where $a_y = 260.0\text{ px/s}^2$ and $v_{max} = 380.0\text{ px/s}$.
  - **Corridor Acquisition**: Locks onto player presence within forward corridor ($|P_x - E_x| \le 110\text{ px}$) and engages prow thrusters (`charging = true`).
  - **Specialized Ramming Collision Response**: Delivers a devastating $45\text{-point}$ kinetic impact damage to player hull in `CollisionManager`, accompanied by mass recoil displacement.
* **Technical Explanation**: Embodies real-time logic building by coupling encapsulated physics state ($v_y, a_y, prowImpactDamage$) with semi-implicit Euler integration, cleanly decoupled from the presentation layer.
* **Viva Explanation**: *"Kinetic Battering Ram demonstrates Logic-Building for Real-Life Problems under PSOOP-CO4. It models real-time accelerating kinematics ($v = v_0 + at$) over frame deltas ($\Delta t$). Upon collision with the player, controlled class methods compute the kinetic momentum transfer and specialized prow damage ($45$ damage)."*

---

## 3. Wave Progression & Fleet Deployment Matrix

Spawning across sectors is governed by `WaveManager.java` and `WaveManager.ts` to ensure progressive difficulty and prevent overwhelming the player:

| Sector / Level | Hostile Fleet Composition | Difficulty & Tactical Purpose |
| :---: | :--- | :--- |
| **Level 1** | 70% Drone, 30% Fighter | Baseline introductory sector teaching player kinematics and firing. |
| **Level 2** | 25% Drone, 25% Fighter, 25% Armoured, 25% Interceptor | Introduces Armoured Vanguard (heavy damage absorption) and Interceptor (fast dashes). |
| **Level 3** | **Milestone Flagship Boss: Leviathan MK-1** | 3-phase dreadnought boss encounter with on-screen health bar. |
| **Level 4** | 20% Drone, 20% Fighter, 20% Armoured, 20% Interceptor, 20% Morphing | Introduces Adaptive Morphing Drone (4-state shifting combatant). |
| **Level 5+** | Combined Fleet: 18% Drone, 18% Fighter, 15% Armoured, 15% Interceptor, 12% Morphing, 11% FailSafe, 11% Rammer | Full combined hostile fleet including Fail-Safe Sentries and Kinetic Battering Rams. |

---

## 4. Automated Test Verification Summary

### Desktop Java Test Suite (`scripts/test_psoop.ps1` & `scripts/test_all.bat`):
* `GameUnitTest.java`: **66 / 66 PASSED** (includes 26 new assertions verifying the 5 enemies and multi-tier waves).
* `SupabaseAuthTest.java`: **53 / 53 PASSED**.
* `SupabaseMatchServiceTest.java`: **50 / 50 PASSED**.
* Total Java Assertions: **169 / 169 PASSED (100%)**.

### Web Browser Test Suite (`cd web; npm test`):
* `enemies.test.ts`: **9 / 9 PASSED** (verifies Drone, Fighter, Boss, Armoured, Interceptor, Morphing, FailSafe, Rammer).
* `wave.test.ts`: **6 / 6 PASSED** (verifies progressive multi-tier spawning).
* Full Browser Test Suites: **44 / 44 PASSED (100%)**.
* Web Production Build (`npm run build`): **0 errors, compiled in 549ms**.

### Repository Master Test Matrix (`test_all.bat`):
* **PSOOP**: 169 Assertions — **PASSED**
* **PL**: 53 Assertions — **PASSED**
* **COA**: 41 Assertions — **PASSED**
* **CGL**: 65 Assertions — **PASSED**
* **TOTAL**: **328 Desktop Assertions + 44 Web Assertions = 372 Tests (100% PASS)**
