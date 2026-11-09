# AIRSTRIKE — PHASE 9: UNIFIED DESIGN SYSTEM
**Document Version:** 1.0.0  
**Target Clients:** Java Swing Desktop (`PSOOP`) & TypeScript/HTML5 Browser (`web`)  
**Design Paradigm:** Modern Futuristic Space-Combat / Tactical Military Starfleet  
**Status:** Canonical Visual Reference  

---

## 1. Visual Identity & Aesthetics

The AIRSTRIKE visual language is engineered to deliver the immersive feel of a high-tech tactical starfleet combat console. It avoids the flat, unstyled appearance of legacy desktop forms while preserving deterministic performance across both Java Swing Graphics2D and HTML5 Canvas / CSS environments.

### Key Visual Pillars
1. **Cosmic Depth**: Multi-layer dark navy backgrounds (`#070A14`) paired with subtle radial nebula gradients in electric violet (`#7C3AED`) and deep cyan.
2. **Holographic Glassmorphism**: Translucent card panels (`#0D1321`, 85–95% alpha) with crisp 1px borders, subtle inner shadows, and glowing accent hairline tops.
3. **Tactical Accent System**:
   - **Primary Action**: Electric Cyan (`#00E5FF`) for primary launch buttons, shield bars, and active system focus.
   - **Secondary Accent**: Celestial Blue (`#38BDF8`) for secondary controls, navigational tabs, and flight logs.
   - **Academic & Special**: Neon Purple (`#A855F7`) for Course Outcome demonstration arenas and research modules.
   - **Honor & Score**: Imperial Gold (`#F59E0B` / `#FBBF24`) for global leaderboards, ace medals, and multipliers.
   - **System Status**: Neon Emerald (`#10B981` / `#34D399`) for online cloud telemetry and secure authorization.
   - **Combat Hazard**: Coral Red (`#EF4444`) for lethal hull breach alerts, enemy warnings, and abort actions.
4. **Precision Typography**: Clean, high-contrast sans-serif hierarchy for tactical labels with monospaced readouts for numbers, coordinates, and flight metrics.

---

## 2. Design Tokens Matrix

| Token Name | Hex Code | RGB / RGBA | Swing `Color` | CSS Custom Variable | Semantic Usage |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `BG_VOID` | `#020409` | `rgb(2, 4, 9)` | `new Color(2, 4, 9)` | `--bg-void` | Deepest viewport canvas background |
| `BG_DEEP` | `#070A14` | `rgb(7, 10, 20)` | `new Color(7, 10, 20)` | `--bg-deep` | Root window and backdrop gradient start |
| `BG_PANEL` | `#0F172A` | `rgb(15, 23, 42)` | `new Color(15, 23, 42)` | `--bg-panel` | Header, footer bars, and card containers |
| `BG_CARD` | `#0D1321` | `rgba(13, 19, 33, 0.92)` | `new Color(13, 19, 33, 235)` | `--bg-card` | Floating modal dialogs and overlay cards |
| `BG_GLASS` | `#111827` | `rgba(17, 24, 39, 0.75)` | `new Color(17, 24, 39, 190)` | `--bg-glass` | In-game HUD pods and status capsules |
| `BG_INPUT` | `#161F38` | `rgb(22, 31, 56)` | `new Color(22, 31, 56)` | `--bg-input` | Text field and input backgrounds |
| `CYAN_PRIMARY` | `#00E5FF` | `rgb(0, 229, 255)` | `new Color(0, 229, 255)` | `--cyan-primary` | Hero button gradients, active focus, branding |
| `CYAN_GLOW` | — | `rgba(0, 229, 255, 0.35)` | `new Color(0, 229, 255, 90)` | `--cyan-glow` | Multi-layer glow on titles and active strokes |
| `BLUE_ACCENT` | `#38BDF8` | `rgb(56, 189, 248)` | `new Color(56, 189, 248)` | `--blue-accent` | Secondary tactical buttons and information tags |
| `BLUE_DARK` | `#0077B6` | `rgb(0, 119, 182)` | `new Color(0, 119, 182)` | `--blue-dark` | Hero button gradient baseline |
| `PURPLE_ACCENT`| `#A855F7` | `rgb(168, 85, 247)` | `new Color(168, 85, 247)` | `--purple-accent` | Academic modules, Course Outcome badges |
| `GOLD_ACCENT` | `#FBBF24` | `rgb(251, 191, 36)` | `new Color(251, 191, 36)` | `--gold-accent` | Leaderboard #1 rank, record scores |
| `EMERALD_STATUS`|`#34D399` | `rgb(52, 211, 153)` | `new Color(52, 211, 153)` | `--emerald-status`| Active authentication, hull integrity high |
| `CORAL_DANGER` | `#EF4444` | `rgb(239, 68, 68)` | `new Color(239, 68, 68)` | `--coral-danger` | Destruction alerts, hull integrity critical |
| `TEXT_WHITE` | `#F8FAFC` | `rgb(248, 250, 252)` | `new Color(248, 250, 252)` | `--text-white` | Primary titles, button labels, high-visibility text |
| `TEXT_MUTED` | `#94A3B8` | `rgb(148, 163, 184)` | `new Color(148, 163, 184)` | `--text-muted` | Subtitles, helper text, inactive labels |
| `BORDER_SUBTLE`| `#1E293B` | `rgba(30, 41, 59, 0.8)` | `new Color(30, 41, 59, 200)` | `--border-subtle` | Container separators and inactive outlines |
| `BORDER_CYAN` | — | `rgba(0, 229, 255, 0.4)` | `new Color(0, 229, 255, 100)`| `--border-cyan` | Active card borders and focused form inputs |

---

## 3. Typography Hierarchy

### Java Swing Standard Fonts
- **Title (Hero)**: `Font("SansSerif", Font.BOLD, 44)` with 4-pass multi-layer glow rendering.
- **Header 1**: `Font("SansSerif", Font.BOLD, 18)` (Dialog headers, Card titles).
- **Header 2**: `Font("SansSerif", Font.BOLD, 14)` (Section titles, Hero buttons).
- **Body / Button**: `Font("SansSerif", Font.BOLD, 12)` (Standard command buttons, form labels).
- **Small / Badge**: `Font("SansSerif", Font.BOLD, 10)` (Pill badges, status indicators).
- **Monospace Readout**: `Font("Monospaced", Font.BOLD, 12)` (Coordinates, scores, telemetry).

### Browser Standard Fonts
- **Display**: `'Rajdhani', -apple-system, sans-serif` (Letter-spacing: `2px` to `6px`).
- **Monospace**: `'JetBrains Mono', 'Fira Code', Consolas, monospace`.

---

## 4. Reusable UI Components

### 4.1 CyberButton
The standard interactive button across both platforms:
- **Hero Mode**: Vibrant cyan-to-azure gradient fill (`#00E5FF` to `#0077B6`), specular top sheen, glowing white stroke, bold 14px uppercase label.
- **Tactical Mode**: Dark glassmorphic background with a 4px glowing left accent bar, subtle 1px border that highlights on hover, left-aligned text (24px padding), and right-aligned chevron arrow (`›`).
- **Pill Mode**: Compact 24px-height rounded capsule button used for Login/Logout, tabs, and quick actions.

### 4.2 CyberCard
Glassmorphic modal and screen panel container:
- Background: `#0D1321` with 92% opacity.
- Border radius: 12px standard (16px outer cards).
- 1px border: `rgba(0, 229, 255, 0.25)` normal, glowing cyan top accent hairline.
- Padding: 20px to 32px based on dialog density.

### 4.3 In-Game Tactical HUD Pods
During active space combat, the HUD must never obstruct gameplay action:
- **Top-Left Vitals Pod**:
  - Size: 280px × 76px, positioned at `(20, 20)`.
  - Glassmorphic rounded rectangle with subtle cyan border.
  - Hull Integrity bar (Green/Red dynamic color transition based on percentage).
  - Shield Capacitor bar (Bright Cyan `#00E5FF`).
  - Vessel count (Remaining lives) and Weapon level tier indicator.
- **Top-Right Mission Pod**:
  - Size: 220px × 90px, positioned at `(screenWidth - 240, 20)`.
  - Authenticated pilot callsign tag.
  - Active Score and High Score readout.
  - Current Sector / Wave indicator.
  - Kill Streak multiplier badge (`2x`, `3x`, `4x STREAK!`) with pulsing glow.

### 4.4 Academic Subject & Course Outcome (CO) Center
A comprehensive modal interface showcasing the complete academic curriculum:
1. **Four Subject Panels**:
   - `PSOOP` (Principles of Object-Oriented Programming — Java 26)
   - `PL` (Programming Laboratory — C++17 Data Structures & Algorithms)
   - `COA` (Computer Organization & Architecture — 32-bit 80386 Assembly)
   - `CGL` (Computer Graphics Laboratory — C++17 / OpenGL / GLUT)
2. **All 19 Course Outcomes**:
   - PL-CO1 to PL-CO5 (Arrays, Lists, Stacks/Queues, Search/Sort, Hash Tables)
   - COA-CO1 to COA-CO4 (80386 ISA, Assembly Logic, Booth Arithmetic, Pipelining)
   - CGL-CO1 to CGL-CO6 (Primitives, Transformations, 2D/3D Math, Projections, Clipping, Phong Shading)
   - PSOOP-CO1 to PSOOP-CO4 (Encapsulation, State Machine, Exception Handling, Logic/UI)
3. **Interactive Demonstrations**:
   - Direct launch of the PSOOP 5-Enemy showcase arena.
   - Command triggers and status verifications for PL, COA, and CGL native runners.

---

## 5. Screen Layout Specifications

| Screen | Dimensions (Standard) | Key Elements |
| :--- | :--- | :--- |
| **Main Menu** | 800 × 650 (centered in 1280/1366/1920) | Animated Starfield, Glow Title, Pilot Capsule, 8 Command Buttons, Cloud Badge, Academic Footer Bar |
| **Auth Dialog** | 480 × 560 (Modal) | Starfleet Header, Tab Switcher, Callsign / Email / Password Inputs, Status Banner, Guest Bypass |
| **Subject & CO Center**| 720 × 580 (Modal) | Subject Selector Tabs (PSOOP, PL, COA, CGL), 19 CO Matrix with Description & File Paths, Demo Trigger |
| **Gameplay HUD** | Viewport overlay | Top-Left Vitals Pod, Top-Right Mission Pod, Sector Banner |
| **Pause Screen** | 440 × 380 (Modal / Canvas Overlay)| Tactical Pause Title, Resume, Restart, Settings, Main Menu, Abort Buttons |
| **Game Over Screen** | 480 × 440 (Modal / Canvas Overlay)| Vessel Destroyed Alert, Final Score, High Score, Wave Reached, Accuracy, Redeploy, Menu Buttons |
| **Leaderboard** | 600 × 520 (Modal) | Gold/Silver/Bronze Medals, Top 10 Pilot Table, Supabase Telemetry Notice, Async Refresh |
| **Profile & Settings** | 560 × 520 (Modal) | Pilot Dossier, Callsign, Threat Level Dropdown, Audio/SFX Sliders, Match Sync, Logout Button |
| **Flight Logs** | 600 × 520 (Modal) | Mission History Table (Sector, Score, Duration, Timestamp), Guest Local Storage Advisory |
