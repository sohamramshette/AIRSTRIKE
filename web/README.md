# AIRSTRIKE — Web Browser Edition

**Tactical Space Combat Simulator — College Multi-Subject Educational Gaming Project**

The web edition of **AIRSTRIKE** is a high-performance, responsive HTML5 Canvas 2D space shooter engineered with TypeScript, Vite, and the Supabase JavaScript Client. It allows approximately 500 college students to access the game directly from modern web browsers on laptops, desktop computers, and mobile/tablet devices without installing Java.

Both the standalone Windows desktop client (`release/AIRSTRIKE_Windows_x64.zip`) and this browser edition connect to the **same Supabase PostgreSQL backend**, sharing accounts, match history, and the global leaderboard.

---

## 1. Quick Start

### Prerequisites
- Node.js 18+ (tested on Node.js v24)
- npm 9+

### Installation & Execution
```bash
# Navigate to web client directory
cd web

# Install dependencies
npm install

# Launch Vite development server
npm run dev

# Run automated test suite (30 unit tests)
npm test

# Build optimized production bundle
npm run build

# Preview production build locally
npm run preview
```

The production assets will be built into `web/dist/`, ready for zero-config deployment to any static host (Netlify, Vercel, GitHub Pages, or an internal college web server).

---

## 2. Controls

### Desktop Keyboard & Mouse
| Action | Key / Input |
| :--- | :--- |
| **Move Spacecraft** | `W, A, S, D` or `Arrow Keys` (supports diagonal normalization) |
| **Primary Weapons** | `Spacebar` or `Left Mouse Click` (hold for continuous fire) |
| **Tactical Pause** | `P` or `Escape` |
| **Quick Restart** | `R` (on Game Over screen) |
| **Audio Mute/Unmute** | `M` or on-screen speaker toggle |
| **Fullscreen** | On-screen maximize button |

### Mobile & Tablet Touch Controls
On touch screens, responsive on-screen virtual touch controls automatically activate:
- **Left Hand**: Virtual 4-way D-Pad (Up, Down, Left, Right) or intuitive touch drag.
- **Right Hand**: Large glowing virtual `FIRE` button.

---

## 3. Architecture & Features

- **Rendering Engine**: Pure HTML5 Canvas 2D running at 60 FPS via `requestAnimationFrame` with delta time clamping ($\Delta t \le 0.033\text{ s}$).
- **Vector Spacecraft**: 8-point geometric vector hull polygons matching the desktop Java Swing design, with animated thruster flames and glowing cockpit canopies.
- **Enemies**:
  - **Drone Scouts**: High-speed sine-wave ramming drones ($100\text{ pts}$).
  - **Combat Fighters**: Swept-wing interceptors with lateral cosine banking and dual downward lasers ($250\text{ pts}$).
  - **Leviathan Flagships**: Multi-phase dreadnought bosses every 3rd sector with 5-way spread fan, dual heavy plasma, and targeted player barrages ($2,500\text{ pts}$).
- **Scoring & Combo Multipliers**: $3.5\text{ s}$ kill-streak window scaling from $1\times$ up to $4\times$ multiplier. Taking damage breaks the combo.
- **Power-Ups**: $22\%$ drop rate on enemy destruction offering Hull Repairs ($+40\text{ HP}$), Shield Overcharge ($+50\text{ Shield}$), Weapon Upgrades (Single $\rightarrow$ Dual $\rightarrow$ Triple Spread), and Thruster Boosts ($+25\text{ px/s}$).
- **Synthesized Audio**: Web Audio API oscillator synthesizer delivering arcade laser pews, shield deflections, explosion rumbles, power-up arpeggios, and boss klaxons with zero external audio assets.
- **Cloud Backend**: Supabase authentication (`/auth/v1/`), profile management (`/rest/v1/profiles`), match debrief logging (`/rest/v1/matches`), and global leaderboard ranking (`/rest/v1/leaderboard`).

---

## 4. Environment Configuration

Copy `.env.example` to `.env` or `.env.local` to override credentials if connecting to an alternate Supabase project:
```env
VITE_SUPABASE_URL=https://qbhotsqwpxbmtpqbgpjy.supabase.co
VITE_SUPABASE_PUBLISHABLE_KEY=sb_publishable_45YsUzEdi6crovilqrubwg_Z6QgVVsM
```
*(Only the publishable anon key is ever used; service-role and secret keys are never exposed on the web client).*

