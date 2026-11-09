# AIRSTRIKE — PHASE 10: THE ULTIMATE HINGLISH MEME & BOLLYWOOD ANNOUNCER
**System Specification, Dialogue Taxonomy, Combat Event Engine & Cross-Platform Implementation Report**

---

## 1. Executive Summary & Design Philosophy

Phase 10 transforms **AIRSTRIKE** into a hilarious, entertaining, unpredictable space-combat simulator by embedding a sarcastic, context-aware AI personality:

> **Character Codename**: `AIRSTRIKE AI — THE DESI TOXIC ANNOUNCER`  
> **Linguistic Register**: Natural colloquial Hinglish in Roman script  
> **Comedic Blend**:  
> - **50% Combat Teasing & Savage Roasts**: Poking fun at poor aim, corner squatting, reckless flying, and low shields.  
> - **20% Instagram Reels & Gen-Z Slang**: "Bro is cooked", aura points, skill issues, delulu, canon events, main character energy.  
> - **20% Bollywood Comedy Classics**: Legendary references to *Hera Pheri*, *Sholay*, *Welcome*, *Andaz Apna Apna*, *Golmaal*, *3 Idiots*, *Munna Bhai*, *Don*, and *Pushpa*.  
> - **10% Sarcastic Praise & Hype**: Exaggerated motivational bursts during kill streaks, flawless clears, and boss takedowns.

Crucially, **the announcer interacts with the player DURING active combat**, dynamically reacting to misses, health drops, bullet evasions, boss encounters, and camping behavior in real time rather than staying silent until wave completion.

---

## 2. Canonical Dialogue Taxonomy & Statistics

A single canonical JSON collection was engineered at `config/announcer_dialogues.json` and mirrored to both runtime modules (`PSOOP/src/announcer/announcer_dialogues.json` and `web/src/announcer/announcer_dialogues.json`) ensuring **zero drift** between desktop and browser experiences.

```
Total Curated Dialogues: 292 unique lines
Duplicate IDs: 0 | Duplicate Texts: 0
```

### Category Breakdown & Quota Verification

| Dialogue Pack | Required Quota | Actual Curated | Trigger Coverage | Example Dialogue |
|---|---|---|---|---|
| `DESI_MEME` | ≥ 48 | **52** | Damage, Hull, Health, Camping, Misses, Clears | *"GHEE KHATAM! Ab teri spaceship kaise chalegi?"* |
| `INSTAGRAM_GENZ` | ≥ 58 | **62** | Misses, Accuracy, Boss, Streaks, Death | *"Bro is NOT beating the bad aim allegations."* |
| `BOLLYWOOD` | ≥ 56 | **62** | Boss Spawn, Streaks, Accuracy, Survival, Death | *"Utha le re baba! Is player ko koi utha le!"* |
| `GENERAL_ROAST` | ≥ 40 | **52** | Misses, Hull Damage, Camping, Low Health | *"Were you trying to headbutt their lasers or was that accidental?"* |
| `VICTORY_PRAISE` | ≥ 20 | **32** | Boss Kill, Streaks, Combos, Flawless Clear | *"AIRSTRIKE AI bows down! That boss stood absolutely zero chance!"* |
| `RANDOM_INTERRUPT` | ≥ 20 | **32** | Combat lull, periodic banter, absurd advice | *"Tactical analysis: 10% skill, 20% luck, 70% pure Hinglish willpower."* |
| **Total** | **≥ 200** | **292** | **Full 24 Combat Event Matrix** | **100% Unique Strings** |

---

## 3. Real-Time Combat Event Trigger Engine

The announcer reacts immediately to live telemetry emitted during frame updates:

| Event Identifier | Trigger Condition | Severity / Priority | Announcer Reaction Style |
|---|---|---|---|
| `MISSED_SHOTS` | ≥ 14 consecutive player bullets fired without an enemy hit | Normal | Bad aim roast, Stormtrooper jokes, Diwali firecracker banter |
| `PLAYER_DAMAGE` | Shield broken or hull health decremented | Normal | Bollywood scream, Reels reaction, sarcastic advice |
| `CRITICAL_HEALTH` | Hull integrity drops below 25% | **HIGH (Override)** | *"GHEE KHATAM!"*, 1 HP dream, desperate calls for emergency backup |
| `CAMPING` | Spaceship remains parked at screen margins ($x < 70$ or $x > \text{width} - 120$) for $> 5.0\text{s}$ | Normal | Rent due notices, Baburao teasing, cowardice allegations |
| `SURROUNDED` | Hostiles flanking left, right, and above player within 240px | Normal | Panic banter, boxing parcel analogies |
| `KILL_STREAK` | Active combo multiplier hits 8x, 16x, 24x | **HIGH** | Hype commentary, Mogambo khush hua, system hang alerts |
| `LOW_ACCURACY` | Hit ratio $< 22\%$ after $\ge 25$ shots discharged | Normal | Crosshair roasts, consultation with eye doctors |
| `HIGH_ACCURACY` | Hit ratio $> 78\%$ after $\ge 25$ shots discharged | Normal | Aimbot accusations, military cybernetics teasing |
| `BOSS_APPEARS` | Flagship Dreadnought / Boss enemy enters playfield | **HIGH (Override)** | Gabbar entrance, final boss drip check, will generation |
| `BOSS_DEFEATED` | Boss health drops to zero and explodes | **HIGH (Override)** | Cinema applause, cosmic scrap metal, legendary pilot status |
| `PLAYER_DEATH` | Spacecraft destroyed and life decremented | **HIGH (Override)** | *"Khatam, tata, bye-bye!"*, respawn jokes, Babumoshai quotes |
| `GAME_OVER` | Zero remaining vessels, final defeat screen | **HIGH (Override)** | L + ratio, funeral violin queue, audit of pilot license |
| `RANDOM_INTERRUPT` | Elapsed combat silence of 10–28 seconds | Normal | Interstellar tea offers, horoscopes, ambient banter |

---

## 4. Selection Architecture & Anti-Repetition Ring Buffer

To prevent repetitive voice spam, both Java and TypeScript implementations leverage identical selection engines:

1. **Category Filtering**: Respects user pack selection (`ALL_PACKS`, `DESI_MEME`, `INSTAGRAM_GENZ`, `BOLLYWOOD`, `GENERAL_ROAST`, `VICTORY_PRAISE`, `RANDOM_INTERRUPT`).
2. **20-Entry FIFO Ring Buffer**: Tracks the last 20 dialogue IDs played. Candidates appearing in this buffer are strictly excluded.
3. **Buffer Depletion Recovery**: If all eligible candidates have been recently used, the oldest 50% of the buffer is purged to recycle variety without jarring repeats.
4. **Frequency & Cooldown Multipliers**:
   - `HIGH`: Min cooldown 4.5s; random interrupt 8–14s.
   - `NORMAL`: Min cooldown 8.5s; random interrupt 14–22s (Default).
   - `LOW`: Min cooldown 18.0s; random interrupt 25–35s.
5. **Priority Event Bypass**: Critical events (`CRITICAL_HEALTH`, `BOSS_APPEARS`, `BOSS_DEFEATED`, `PLAYER_DEATH`, `GAME_OVER`) can bypass normal cooldowns, constrained only by a 1.5s separation window.

---

## 5. Visual HUD Banner Design (Glassmorphic Toast)

Following the Phase 9 design language, the announcer banner appears as a futuristic tactical HUD toast:

- **Positioning**: Top center ($x = \frac{\text{width} - 480}{2}$, $y = 44$, $\text{height} = 56\text{px}$), positioned cleanly between the Vitals Pod (ends at $x=300$) and the Mission Pod (starts at $x=\text{width}-250$).
- **Visual Container**: Dark navy glassmorphic base (`#0A1020` with $92\%$ opacity) with rounded 12px corners.
- **Neon Glow Border**: Electric cyan (`#00F0FF`) with purple gradient accent for Cyber/Meme lines; Warm Gold (`#F59E0B`) for Bollywood/Desi lines.
- **Badge Pill**: Glowing cyan tag reading `⚡ AIRSTRIKE AI • [CATEGORY]`.
- **Typography & Wrapping**: Crisp white bold lettering with automatic two-line wrapping for longer dialogues.
- **Animation**: Smooth 0.35s fade-in, 3.8s hold, and 0.6s ease-out fade.

---

## 6. Audio & Speech Synthesis (TTS)

- **Browser Application**: Integrates the native Web Speech API (`window.speechSynthesis`). Automatically scans installed system voices and selects Indian English (`en-IN`) or Hindi (`hi-IN`), modulated at rate 1.05 and pitch 1.0. Defaults to `OFF` per specification to prevent unsolicited speech.
- **Java Application**: Background asynchronous worker ready for system SAPI/TTS speech synthesis or silent visual toast operation.
- **Settings Toggle**: Directly configurable from the Pilot Dossier / Settings modal in both clients.

---

## 7. Pilot Profile & System Settings Integration

Both platforms feature full configuration controls in the Settings screen:

1. **Active Hinglish Commentary** (Checkbox Toggle)
2. **Dialogue Meme Pack** (Dropdown: All Packs, Desi Memes, Instagram/Gen-Z, Bollywood, Roasts, Victory Praise, Interruptions)
3. **Commentary Frequency** (Dropdown: Normal, High, Low)
4. **Voiceover (TTS)** (Checkbox Toggle)
5. **Test Banter Button** (Interactive preview that immediately renders a sample toast banner and plays voice audio)

---

## 8. Test Execution & Evidence

### A. Java Desktop Automated Tests (`scripts/test_psoop.bat`)
```
========================================================
   AIRSTRIKE PHASE 10: HINGLISH ANNOUNCER TEST SUITE    
========================================================
  [PASS] Dialogue collection loaded with >= 200 entries (Actual: 292)
  [PASS] Dialogue collection matches expected 292 entries
  [PASS] Desi Meme pack has >= 48 entries (Actual: 52)
  [PASS] Instagram & Gen-Z pack has >= 58 entries (Actual: 62)
  [PASS] Bollywood pack has >= 56 entries (Actual: 62)
  [PASS] General Roast pack has >= 40 entries (Actual: 52)
  [PASS] Victory & Praise pack has >= 25 entries (Actual: 32)
  [PASS] Random Interruptions pack has >= 25 entries (Actual: 32)
  [PASS] Zero duplicate dialogue IDs in canonical collection
  [PASS] Zero duplicate dialogue texts in canonical collection
  [PASS] Dialogues match MISSED_SHOTS trigger
  [PASS] Dialogues match PLAYER_DAMAGE trigger
  [PASS] Dialogues match BOSS_APPEARS trigger
  [PASS] Dialogues match BOSS_DEFEATED trigger
  [PASS] Dialogues match CRITICAL_HEALTH trigger
  [PASS] Dialogues match KILL_STREAK trigger
  [PASS] Announcer is enabled by default
  [PASS] Active pack is ALL_PACKS by default
  [PASS] Frequency is NORMAL by default
  [PASS] Voice synthesis is OFF by default
  [PASS] Active pack can be set to BOLLYWOOD
  [PASS] Frequency can be set to HIGH
  [PASS] First critical health trigger succeeded
  [PASS] Second critical health trigger succeeded with anti-repetition
  [PASS] Normal trigger initiates cooldown
  [PASS] Subsequent normal trigger rejected by cooldown timer
  [PASS] Trigger completely suppressed when announcer is disabled
  [PASS] Trigger resumes when announcer is re-enabled
========================================================
TEST RESULTS: 28 / 28 PASSED (100.0%)
========================================================
```

### B. Web Client Automated Tests (`npm --prefix web test`)
```
▶ AIRSTRIKE Phase 10: Hinglish Meme & Bollywood Announcer Web Unit Tests
  ✔ loads canonical dialogue collection with >= 200 entries (0.7ms)
  ✔ satisfies and exceeds category distribution quotas (0.2ms)
  ✔ guarantees zero duplicate dialogue IDs and zero duplicate texts (0.3ms)
  ✔ matches all mandatory combat triggers across dialogue bank (0.2ms)
  ✔ manages settings defaults, category filters, and frequency correctly (0.2ms)
  ✔ executes anti-repetition and priority overrides in AnnouncerManager (0.4ms)
  ✔ suppresses triggers when announcer is disabled and resumes when enabled (0.1ms)
✔ AIRSTRIKE Phase 10: Hinglish Meme & Bollywood Announcer Web Unit Tests (3.0ms)

Total Web Test Pass: 51 / 51 Passed (100.0%) across 8 test suites
```

### C. Real Browser QA Verification (`npm --prefix web run test:browser`)
- **Google Chrome**: 100% Passed (All 26 checks verified including Left/Right kinematics, weapon discharge, pause overlay, viewport responsiveness, debrief screens).
- **Microsoft Edge**: 100% Passed (All 26 checks verified).

### D. Academic Subject Multi-Language Preservation
- **PL C++17**: MinGW GCC build successful (`bin/pl/`)
- **COA 80386 Assembly**: GAS 32-bit build successful (`bin/coa/coa_runner.exe`)
- **CGL OpenGL/GLUT**: MinGW C++17 build successful (`bin/cgl/cgl_runner.exe`)
- **Supabase Cloud & RLS**: All 53 Auth & 50 Match/Leaderboard tests passing 100%.

---

## 9. Visual Evidence Gallery

The captured screenshots below demonstrate the visual design of the Announcer across desktop and browser environments:

| Screen / Feature | Java Swing Implementation | Web TypeScript / Canvas Implementation |
|---|---|---|
| **Combat Desi Toast** | `docs/phase10_screenshots/java_announcer_combat_toast.png` | `docs/phase10_screenshots/web_announcer_combat_toast.png` |
| **Bollywood Boss Encounter** | `docs/phase10_screenshots/java_announcer_boss_toast.png` | `docs/phase10_screenshots/web_announcer_boss_toast.png` |
| **Announcer System Settings** | `docs/phase10_screenshots/java_announcer_settings_modal.png` | `docs/phase10_screenshots/web_announcer_settings_modal.png` |

---

## 10. Summary of Modified & Created Files

1. **Canonical Dialogue Bank**:
   - `config/announcer_dialogues.json` (292 entries across 6 categories)
   - `PSOOP/src/announcer/announcer_dialogues.json` (Mirrored resource)
   - `web/src/announcer/announcer_dialogues.json` (Mirrored asset)
   - `web/public/config/announcer_dialogues.json` (Public asset)
2. **Java Announcer Architecture**:
   - `PSOOP/src/announcer/AnnouncerEvent.java` (24 combat triggers)
   - `PSOOP/src/announcer/DialogueCategory.java` (6 packs + ALL_PACKS)
   - `PSOOP/src/announcer/CommentaryFrequency.java` (LOW, NORMAL, HIGH)
   - `PSOOP/src/announcer/DialogueEntry.java` (Model)
   - `PSOOP/src/announcer/AnnouncerSettings.java` (Singleton configuration)
   - `PSOOP/src/announcer/AnnouncerManager.java` (Core engine, ring buffer, HUD banner renderer)
   - `PSOOP/src/game/GameEngine.java` (Telemetry integration, life loss, game over, boss presence)
   - `PSOOP/src/ui/ProfileSettingsDialog.java` (Announcer preferences UI)
   - `PSOOP/src/test/AnnouncerTest.java` (Unit test suite)
   - `PSOOP/src/test/CapturePhase10Screenshots.java` (Screenshot evidence generator)
   - `scripts/test_psoop.bat` (Updated with [TEST 4/4] Announcer test suite)
3. **Web Announcer Architecture**:
   - `web/src/announcer/types.ts` (TypeScript interfaces & enums)
   - `web/src/announcer/AnnouncerSettingsManager.ts` (LocalStorage persistence)
   - `web/src/announcer/AnnouncerManager.ts` (Core engine, Web Speech API TTS, Canvas HUD banner renderer)
   - `web/src/game/GameEngine.ts` (Telemetry hooks, boss presence, HUD render)
   - `web/src/ui/UIManager.ts` (Settings modal wiring and preview trigger)
   - `web/src/main.ts` (Exposed QA window hooks)
   - `web/index.html` (Settings controls in modal)
   - `web/tests/announcer.test.ts` (Unit test suite)
   - `web/tests/capture_phase10_web.ts` (Puppeteer screenshot generator)
4. **Documentation**:
   - `docs/PHASE_10_HINGLISH_ANNOUNCER.md` (This document)

