# AIRSTRIKE — PHASE 9.1: UI AUDIT REPORT
**Document Version:** 1.0.0  
**Phase:** 9.1 — Comprehensive Interface Audit (Java Swing Desktop & TypeScript/HTML5 Browser)  
**Date:** November 2026  
**Status:** COMPLETE  

---

## 1. Executive Summary

This audit evaluates the user interface architecture of both **AIRSTRIKE** deployment targets:
1. **Java Desktop Client** (Java 26, Swing/AWT, Double-Buffered Graphics2D)
2. **Browser Client** (TypeScript 5.6, HTML5 Canvas, Modern CSS, Vite 5.4)

The primary goal of Phase 9 is to establish a unified, modern, futuristic space-combat aesthetic with Java Swing as the primary visual reference, and ensure that the browser interface faithfully mirrors the desktop design in colors, typography, layout, component shapes, spacing, and interaction paradigms.

---

## 2. Java Desktop Client Audit

### 2.1 Existing Screens & Components

| Screen / Dialog | Primary Class | Role & Implementation |
| :--- | :--- | :--- |
| **Main Window** | `ui.MainWindow` | `JFrame` container (800x650 minimum 640x480) with `CardLayout` toggling between `"MENU"` and `"GAME"`. Coordinates auth and match services. |
| **Main Menu** | `ui.MenuPanel` | Custom `JPanel` with drifting starfield, multi-layer glowing title, floating pilot capsule, and stacked buttons. |
| **Authentication** | `ui.AuthDialog` | Modal `JDialog` with tabbed Login and Register forms, asynchronous Supabase auth, status banner, and Guest button. |
| **Gameplay HUD** | `game.GameEngine#renderHUD` | Painted directly in `GameEngine.render()` onto `GameCanvas`. Draws top-left health/shield pod and top-right score/wave pod. |
| **Pause Screen** | `game.GameEngine#renderPauseScreen` | Semi-transparent black overlay on `GameCanvas` with text banner. |
| **Game Over Screen** | `game.GameEngine#renderGameOverScreen` | Semi-transparent dark red overlay with text banner, score, high score, and key hints. |
| **Academic Arena** | `game.GameEngine#renderDemoBanner` | Interactive PSOOP 5-Enemy showcase arena with dynamic hotkey spawning ([1]-[5]). |
| **Global Leaderboard** | `ui.LeaderboardDialog` | Modal `JDialog` with `JTable` (Rank, Callsign, Best Score), gold/silver/bronze medals, and async refresh. |
| **Match History** | `ui.MatchHistoryDialog` | Modal `JDialog` with `JTable` (Sector, Score, Duration, Date) displaying pilot flight records. |
| **Mission Settings** | `ui.SettingsDialog` | Modal `JDialog` with threat level difficulty selector and renderer pipeline info. |
| **Pilot Manual** | `ui.InstructionsDialog` | Modal `JDialog` with `JTextPane` rendering HTML controls and combat rules. |

### 2.2 Problems and Deficiencies Identified (Desktop)

1. **Lack of Centralized Design System**: UI styling constants (colors, fonts, borders, paddings) are scattered across individual dialog classes, leading to minor color variances (e.g., `#070A14` vs `#0A0D18` vs `#0D111E`).
2. **Missing Comprehensive Subject & Course Outcome (CO) Browser**: While the Main Menu includes a button for the "PSOOP Enemy Showcase", it lacks a unified UI dialog displaying all 4 subjects (PSOOP Java, PL C++17, COA 80386 ASM, CGL OpenGL) and all 19 Course Outcomes with their academic definitions, implementation locations, and launch actions.
3. **Non-Interactive In-Game Overlays**:
   - The **Pause Screen** is currently drawn as static text on the canvas; it lacks interactive clickable buttons for **Resume**, **Restart**, **Settings**, **Main Menu**, and **Exit**.
   - The **Game Over Screen** is also static text; it lacks mouse-clickable **Play Again** and **Main Menu** buttons, and does not show enemies neutralized or wave reached in a structured tactical card.
4. **Scattered Profile and Settings**: Pilot callsign and authentication status are shown on the menu capsule and match history, but there is no cohesive Profile & Settings center to view clearance level, stats, sound/graphics toggles, and logout.
5. **Fixed Window Sizing**: The window is centered at 800x650; higher resolutions (1280x720, 1366x768, 1920x1080) need responsive centering and clean proportional scaling.

---

## 3. Browser Client Audit

### 3.1 Existing Screens & Components

| Screen / Overlay | Implementation | Role |
| :--- | :--- | :--- |
| **Main Menu** | `web/index.html#main-menu-overlay` | Center `.cyber-card` with Title, Subtitle, Pilot Pill, Cloud Link Pill, Launch, Leaderboard, History buttons. |
| **Pause Overlay** | `web/index.html#pause-overlay` | Center `.cyber-card` with Resume, Restart, Abort to Headquarters. |
| **Game Over Overlay**| `web/index.html#gameover-overlay` | Center `.cyber-card` with final score, sector, flight time, kills, Deploy Again, Return buttons. |
| **Auth Modal** | `web/index.html#auth-modal` | Modal dialog with Sign In / Register tabs, callsign input, email, password. |
| **Leaderboard Modal**| `web/index.html#leaderboard-modal` | Modal dialog with HTML table of top 10 pilots. |
| **History Modal** | `web/index.html#history-modal` | Modal dialog with HTML table of past missions. |
| **Diagnostics Modal**| `web/index.html#cloud-diag-modal` | Modal dialog with live Supabase ping and telemetry. |
| **In-Game Canvas HUD**| `web/src/game/GameEngine.ts#renderHUD` | Full-width top ribbon (height 44px) on HTML5 canvas. |

### 3.2 Inconsistencies with Java Desktop

1. **HUD Layout Mismatch**: Desktop uses two floating glassmorphic pods (top-left for Hull/Shield bars, top-right for Pilot/Score/Sector/Streak); Browser renders a full-width flat top ribbon across the entire canvas.
2. **Main Menu Structure Mismatch**: Desktop features a vertical tactical command list with glowing left accent tabs and chevron arrows; Browser uses simple centered flat rectangular buttons.
3. **Missing Subject & CO Selection**: Browser has no interface to view or select Course Outcomes or academic subjects (PSOOP, PL, COA, CGL).
4. **Missing Profile & Settings Screen**: Browser lacks a dedicated Settings & Pilot Profile modal matching the desktop application.
5. **Color & Design Token Drift**: Several CSS tokens differ slightly in saturation and alpha transparency from the Desktop Java rendering.

---

## 4. Reusable Component Strategy

### 4.1 Java Desktop Design Components

- **`Theme.java`**: Central repository for all design tokens:
  - Deep space backgrounds (`BG_DEEP`, `BG_PANEL`, `BG_GLASS`, `BG_HOVER`).
  - Neon accents (`CYAN_PRIMARY`, `CYAN_GLOW`, `BLUE_ACCENT`, `PURPLE_ACCENT`, `GOLD_ACCENT`, `EMERALD_ACCENT`, `CORAL_DANGER`).
  - Typography (`TITLE_FONT`, `HEADER_FONT`, `BODY_FONT`, `MONO_FONT`, `BADGE_FONT`).
  - Dimensions, stroke widths, and rounded corner radii (12px standard, 16px cards).
- **`CyberButton.java`**: Custom Swing button supporting:
  - Hero mode (vibrant gradient, top specular sheen, glowing stroke).
  - Tactical mode (dark glassmorphic gradient, left glowing accent indicator bar, right chevron arrow, hover glow).
  - Pill button mode (compact badges for auth, dialog headers, tabs).
- **`CyberCard.java`**: Floating glassmorphic card container with antialiased rounded borders, top accent hairline, and dark cosmic background.
- **`SubjectCODialog.java`**: Interactive modal displaying:
  - 4 Subject Cards: PSOOP (Java), PL (C++17), COA (80386 ASM), CGL (OpenGL).
  - 19 Course Outcomes (PL-CO1 to CO5, COA-CO1 to CO4, CGL-CO1 to CO6, PSOOP-CO1 to CO4) with exact academic descriptions and implementation paths.
  - Interactive demonstration triggers.
- **`ProfileSettingsDialog.java`**: Comprehensive pilot dossier + system configuration modal (audio, threat level, display, session management).
- **`InteractiveInGameOverlays`**: Interactive Pause and Game Over cards with hoverable, mouse-clickable buttons and keyboard shortcuts.

### 4.2 Browser Client Matching Strategy

- Mirror all `Theme.java` tokens into CSS custom properties (`:root`).
- Update `.cyber-btn` styling to include the left glowing accent bar and chevron indicator on hover.
- Re-architect `web/src/game/GameEngine.ts`'s `renderHUD()` to draw the exact twin glassmorphic pods (left vitals pod, right mission/score pod) instead of the full-width bar.
- Add **Subject & CO Selection Modal** (`#subject-co-modal`) with all 4 subjects and 19 Course Outcomes.
- Add **Profile & Settings Modal** (`#profile-settings-modal`) with difficulty, audio, stats, and pilot status.
- Align Pause and Game Over card layouts to match Java's typography, spacing, and buttons.

---

## 5. Files Requiring Modification

### Java Desktop
- `PSOOP/src/ui/Theme.java` *(New)*
- `PSOOP/src/ui/CyberButton.java` *(New)*
- `PSOOP/src/ui/CyberCard.java` *(New)*
- `PSOOP/src/ui/SubjectCODialog.java` *(New)*
- `PSOOP/src/ui/ProfileSettingsDialog.java` *(New)*
- `PSOOP/src/ui/MainWindow.java`
- `PSOOP/src/ui/MenuPanel.java`
- `PSOOP/src/ui/AuthDialog.java`
- `PSOOP/src/ui/LeaderboardDialog.java`
- `PSOOP/src/ui/MatchHistoryDialog.java`
- `PSOOP/src/ui/SettingsDialog.java`
- `PSOOP/src/ui/InstructionsDialog.java`
- `PSOOP/src/game/GameCanvas.java`
- `PSOOP/src/game/GameEngine.java`
- `PSOOP/src/test/CaptureScreenshots.java`

### Browser
- `web/src/styles/main.css`
- `web/index.html`
- `web/src/ui/UIManager.ts`
- `web/src/game/GameEngine.ts`
- `web/tests/browser_qa.ts`

---

## 6. Verification Plan

1. **Compilation**: `build_all.bat` (All 4 modules: Java, C++, ASM, OpenGL).
2. **Automated Unit Tests**: `scripts/test_all.bat` (All 418 academic assertions must pass 100%).
3. **Browser Build & Unit Tests**: `npm test` and `npm run build` in `web`.
4. **Real Browser QA**: `npm run test:browser` on headless Chrome & Edge.
5. **Resolution Verification**: Automated screenshot capture at 1280x720, 1366x768, and 1920x1080 across all screens.
