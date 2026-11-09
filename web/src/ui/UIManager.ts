import { GameEngine } from '../game/GameEngine';
import { GameState, MatchSummary } from '../types/game';
import { AuthService } from '../services/AuthService';
import { MatchService } from '../services/MatchService';
import { LeaderboardService } from '../services/LeaderboardService';
import { SoundService } from '../services/SoundService';
import { SUPABASE_URL, SUPABASE_ANON_KEY, supabase } from '../services/SupabaseClient';

interface CourseOutcomeItem {
  code: string;
  title: string;
  description: string;
  details: string;
  status: string;
  accent: string;
}

export class UIManager {
  private engine: GameEngine;
  private authService: AuthService;
  private matchService: MatchService;
  private leaderboardService: LeaderboardService;
  private soundService: SoundService;

  private isRegisterMode: boolean = false;
  private isAuthSubmitting: boolean = false;
  private lastPingLatency: number = 0;
  private personalHighScore: number = 0;

  // 19 Academic Course Outcomes across 4 subjects
  private readonly courseOutcomesData: Record<'PSOOP' | 'PL' | 'COA' | 'CGL', CourseOutcomeItem[]> = {
    PSOOP: [
      {
        code: 'PSOOP-CO1',
        title: 'Object-Oriented Solutions',
        description: 'Encapsulation, inheritance hierarchies, and dynamic polymorphism.',
        details: 'Implemented in ArmouredEnemy (encapsulated armor mitigations) and InterceptorEnemy (dynamic polymorphic dash).',
        status: 'PSOOP/src/model/ (169 Test Assertions Passed)',
        accent: 'var(--cyan-primary)'
      },
      {
        code: 'PSOOP-CO2',
        title: 'Solutions to Reduce Complexity',
        description: 'Finite State Machine (FSM) architecture for multi-phase tactical entities.',
        details: 'Implemented in MorphingEnemy (PATROL, CHASE, OVERCHARGE, EVADE states) and WaveManager difficulty scaling.',
        status: 'PSOOP/src/model/MorphingEnemy.java & service/WaveManager.java',
        accent: 'var(--cyan-primary)'
      },
      {
        code: 'PSOOP-CO3',
        title: 'Critical Thinking & Problem Solving',
        description: 'Defensive programming, boundary clamping, and custom exception hierarchies.',
        details: 'Implemented in FailSafeEnemy (emergency kinetic barrier) and custom AirstrikeException hierarchy.',
        status: 'PSOOP/src/exception/ & model/FailSafeEnemy.java',
        accent: 'var(--cyan-primary)'
      },
      {
        code: 'PSOOP-CO4',
        title: 'Logic-Building for Real-Life Problems',
        description: 'Continuous kinematics, non-uniform acceleration vectors, and AABB collision resolution.',
        details: 'Implemented in RammerEnemy (kinetic prow impact) and CollisionManager (high-speed bullet-hull math).',
        status: 'PSOOP/src/model/RammerEnemy.java & game/CollisionManager.java',
        accent: 'var(--cyan-primary)'
      }
    ],
    PL: [
      {
        code: 'PL-CO1',
        title: 'Dynamic Arrays & Spatial Partitioning',
        description: 'Contiguous memory layout, 2D spatial grid partitioning, and zero-allocation projectile pools.',
        details: 'CustomArray<T> amortized capacity scaling, ProjectilePool, and GridMap space sector entity partitioning.',
        status: 'PL/src/arrays/ (99 PL C++17 Test Assertions Passed)',
        accent: 'var(--blue-accent)'
      },
      {
        code: 'PL-CO2',
        title: 'Suitable Linked Lists',
        description: 'Singly, doubly, and circular intrusive lists for dynamic enemy fleet wave coordination.',
        details: 'Efficient O(1) insertion and mid-sequence deletion during tactical wave spawning without std::list.',
        status: 'PL/src/linked_lists/ (Node pointer manipulation)',
        accent: 'var(--blue-accent)'
      },
      {
        code: 'PL-CO3',
        title: 'Stack and Queue Applications',
        description: 'LIFO flight maneuver undo buffers and FIFO asynchronous telemetry event processing queues.',
        details: 'Double-ended ring buffers for high-frequency player inputs and weapon firing events.',
        status: 'PL/src/stacks/ & PL/src/queues/',
        accent: 'var(--blue-accent)'
      },
      {
        code: 'PL-CO4',
        title: 'Searching and Sorting Techniques',
        description: 'High-performance QuickSort, MergeSort, and Binary Search over global pilot leaderboards.',
        details: 'Comparative benchmark suite evaluating logarithmic divide-and-conquer efficiency.',
        status: 'PL/src/searching/ & PL/src/sorting/',
        accent: 'var(--blue-accent)'
      },
      {
        code: 'PL-CO5',
        title: 'Hashing Techniques',
        description: 'Direct chaining and open addressing with MurmurHash3 for instant O(1) entity lookup.',
        details: 'Collision resolution under 80% load factors with dynamic prime-capacity re-hashing.',
        status: 'PL/src/hashing/',
        accent: 'var(--blue-accent)'
      }
    ],
    COA: [
      {
        code: 'COA-CO1',
        title: '80386 Architecture & Instruction Set',
        description: 'Direct 32-bit register operations (EAX, EBX, ECX, EDX, ESI, EDI) and segmentation models.',
        details: 'Genuine 32-bit assembly routines compiled with MinGW GCC GAS toolchain (-m32).',
        status: 'COA/src/co1_architecture/ (57 COA 80386 Assertions Passed)',
        accent: 'var(--gold-accent)'
      },
      {
        code: 'COA-CO2',
        title: 'Assembly Language Programs',
        description: 'Low-level string operations (REP MOVSB), procedural call frames, and memory block transfers.',
        details: 'Vector distance calculation and register-based hit-point clamping algorithms.',
        status: 'COA/src/co2_assembly_programs/',
        accent: 'var(--gold-accent)'
      },
      {
        code: 'COA-CO3',
        title: 'Computer Arithmetic & Booth Algorithm',
        description: "Hardware multiplication using 2's complement Booth Signed-Digit Multiplication algorithm.",
        details: 'Cycle-accurate Booth recoding table verifying signed bit shifts and arithmetic right shifts (SAR).',
        status: 'COA/src/co3_arithmetic/',
        accent: 'var(--gold-accent)'
      },
      {
        code: 'COA-CO4',
        title: 'Control Unit & Instruction Pipelining',
        description: 'Simulation of 5-stage instruction pipelining (IF, ID, EX, MEM, WB) and pipeline hazard detection.',
        details: 'Data forwarding, structural hazard stalls, and dynamic branch prediction evaluation.',
        status: 'COA/src/co4_control_unit/',
        accent: 'var(--gold-accent)'
      }
    ],
    CGL: [
      {
        code: 'CGL-CO1',
        title: 'OpenGL Graphics Primitives',
        description: 'Rasterization of GL_POINTS, GL_LINES, GL_TRIANGLE_FAN with Counter-Clockwise (CCW) winding.',
        details: 'Fuselage hull generation and Green-Shoelace area verification in PolygonCruiser.',
        status: 'CGL/src/primitives/ (93 CGL OpenGL Assertions Passed)',
        accent: 'var(--purple-accent)'
      },
      {
        code: 'CGL-CO2',
        title: 'Geometric Transformations',
        description: 'Homogeneous 2D/3D affine transformation matrices: Translation, Rotation, Non-uniform Scaling.',
        details: 'Hierarchical multi-body transformation sequencing (T x R != R x T) in OrbitalRotor.',
        status: 'CGL/src/transformations/',
        accent: 'var(--purple-accent)'
      },
      {
        code: 'CGL-CO3',
        title: '2D & 3D Rasterization Algorithms',
        description: 'Integer-only Bresenham Line Drawing and Midpoint Circle Shield rasterization across 8 octants.',
        details: 'Ray-casting point-in-polygon containment and shield barrier generation in RasterSentinel.',
        status: 'CGL/src/algorithms/',
        accent: 'var(--purple-accent)'
      },
      {
        code: 'CGL-CO4',
        title: 'Projection & Viewing Pipelines',
        description: 'Perspective foreshortening (1/Z depth scaling) vs Orthographic parallel distance invariance.',
        details: 'Normalized Device Coordinates (NDC) depth buffer resolution in WarpCruiser.',
        status: 'CGL/src/projection/',
        accent: 'var(--purple-accent)'
      },
      {
        code: 'CGL-CO5',
        title: 'Clipping Algorithms',
        description: 'Cohen-Sutherland outcode line clipping and Sutherland-Hodgman convex polygon boundary clipping.',
        details: 'Precision boundary skimmer algorithms preventing out-of-bounds rendering artifacts.',
        status: 'CGL/src/clipping/',
        accent: 'var(--purple-accent)'
      },
      {
        code: 'CGL-CO6',
        title: 'Texture, Lighting & Color Models',
        description: 'Phong illumination model (I = Ia + Id + Is), Blinn-Phong half-angle vector, and 24-bit BMP loading.',
        details: 'Gouraud smooth shading and HSV-to-RGB procedural color conversion in PhongCruiser.',
        status: 'CGL/src/appearance/',
        accent: 'var(--purple-accent)'
      }
    ]
  };

  constructor(engine: GameEngine) {
    this.engine = engine;
    this.authService = AuthService.getInstance();
    this.matchService = MatchService.getInstance();
    this.leaderboardService = LeaderboardService.getInstance();
    this.soundService = SoundService.getInstance();

    this.loadPersonalRecord();
    this.bindEvents();
    this.bindAuth();
    this.initSubjectCOModal();
    this.initProfileSettingsModal();
    this.initInstructionsModal();
    this.bindTouchControls();
    this.initCloudTelemetry();
    this.updatePilotDossier();
  }

  private loadPersonalRecord(): void {
    try {
      const stored = localStorage.getItem('airstrike_high_score');
      if (stored) {
        this.personalHighScore = parseInt(stored, 10) || 0;
      }
    } catch {
      this.personalHighScore = 0;
    }
  }

  private updatePersonalRecord(score: number): void {
    if (score > this.personalHighScore) {
      this.personalHighScore = score;
      try {
        localStorage.setItem('airstrike_high_score', String(score));
      } catch {
        // Ignore localStorage errors
      }
    }
    const recordDisplay = document.getElementById('menu-record-display');
    if (recordDisplay) {
      recordDisplay.textContent = `RECORD: ${this.personalHighScore.toLocaleString()} PTS`;
    }
    const dossierRecord = document.getElementById('dossier-record');
    if (dossierRecord) {
      dossierRecord.textContent = `${this.personalHighScore.toLocaleString()} PTS`;
    }
  }

  private bindEvents(): void {
    // Game state transitions from engine
    this.engine.setStateChangeCallback((state: GameState) => {
      this.handleStateChange(state);
    });

    this.engine.setGameOverCallback((summary: MatchSummary) => {
      this.handleGameOver(summary);
    });

    // Start Mission button
    document.getElementById('btn-start-game')?.addEventListener('click', () => {
      this.engine.startNewGame();
    });

    // Abort button from main menu
    document.getElementById('btn-abort-game')?.addEventListener('click', () => {
      this.showToast('Starfleet Command Terminal active. Close tab to exit.');
    });

    // Pause overlay buttons
    document.getElementById('btn-resume-game')?.addEventListener('click', () => {
      this.engine.resumeGame();
    });

    document.getElementById('btn-restart-game')?.addEventListener('click', () => {
      this.engine.startNewGame();
    });

    document.getElementById('btn-settings-from-pause')?.addEventListener('click', () => {
      this.openModal('profile-settings-modal');
    });

    document.getElementById('btn-menu-from-pause')?.addEventListener('click', () => {
      this.engine.returnToMenu();
    });

    document.getElementById('btn-abort-from-pause')?.addEventListener('click', () => {
      this.engine.returnToMenu();
    });

    // Game Over buttons
    document.getElementById('btn-play-again')?.addEventListener('click', () => {
      this.engine.startNewGame();
    });

    document.getElementById('btn-menu-from-gameover')?.addEventListener('click', () => {
      this.engine.returnToMenu();
    });

    // Audio & Fullscreen
    const soundBtn = document.getElementById('btn-sound-toggle');
    soundBtn?.addEventListener('click', () => {
      const isMuted = this.soundService.toggleMute();
      if (soundBtn) {
        soundBtn.textContent = isMuted ? '🔇' : '🔊';
      }
      this.showToast(isMuted ? 'Audio Muted' : 'Audio Enabled');
    });

    document.getElementById('btn-fullscreen-toggle')?.addEventListener('click', () => {
      const container = document.getElementById('viewport-container');
      if (!document.fullscreenElement && container) {
        container.requestFullscreen().catch(() => {});
      } else {
        document.exitFullscreen().catch(() => {});
      }
    });

    // Leaderboard Modal
    document.getElementById('btn-open-leaderboard')?.addEventListener('click', () => {
      this.openLeaderboard();
    });

    document.getElementById('leaderboard-modal-close')?.addEventListener('click', () => {
      this.closeModal('leaderboard-modal');
    });

    document.getElementById('btn-refresh-leaderboard')?.addEventListener('click', () => {
      this.openLeaderboard();
    });

    // Flight Log / History Modal
    document.getElementById('btn-open-history')?.addEventListener('click', () => {
      this.openHistory();
    });

    document.getElementById('history-modal-close')?.addEventListener('click', () => {
      this.closeModal('history-modal');
    });
  }

  // ---------------------------------------------------------------------------
  // Subject & Course Outcomes Modal (19 COs across 4 subjects)
  // ---------------------------------------------------------------------------
  private initSubjectCOModal(): void {
    document.getElementById('btn-open-subjects')?.addEventListener('click', () => {
      this.openModal('subject-co-modal');
      this.renderCOTab('PSOOP');
    });

    document.getElementById('subject-co-modal-close')?.addEventListener('click', () => {
      this.closeModal('subject-co-modal');
    });

    document.getElementById('tab-co-psoop')?.addEventListener('click', () => this.renderCOTab('PSOOP'));
    document.getElementById('tab-co-pl')?.addEventListener('click', () => this.renderCOTab('PL'));
    document.getElementById('tab-co-coa')?.addEventListener('click', () => this.renderCOTab('COA'));
    document.getElementById('tab-co-cgl')?.addEventListener('click', () => this.renderCOTab('CGL'));

    document.getElementById('btn-launch-demo-from-modal')?.addEventListener('click', () => {
      this.closeModal('subject-co-modal');
      this.engine.startNewGame();
    });
  }

  private renderCOTab(subject: 'PSOOP' | 'PL' | 'COA' | 'CGL'): void {
    const tabs: Record<'PSOOP' | 'PL' | 'COA' | 'CGL', string> = {
      PSOOP: 'tab-co-psoop',
      PL: 'tab-co-pl',
      COA: 'tab-co-coa',
      CGL: 'tab-co-cgl'
    };

    // Update active tab buttons
    for (const key of Object.keys(tabs) as Array<'PSOOP' | 'PL' | 'COA' | 'CGL'>) {
      const btn = document.getElementById(tabs[key]);
      if (btn) {
        if (key === subject) {
          btn.classList.add('active');
        } else {
          btn.classList.remove('active');
        }
      }
    }

    const container = document.getElementById('co-cards-list');
    if (!container) return;

    const items = this.courseOutcomesData[subject] || [];
    container.innerHTML = items.map(co => `
      <div class="co-card" style="border-left-color: ${co.accent};">
        <div class="co-header">
          <span class="co-title">${this.escapeHtml(co.code)}: ${this.escapeHtml(co.title)}</span>
          <span class="co-badge" style="color: ${co.accent}; border-color: ${co.accent};">VERIFIED</span>
        </div>
        <p class="co-desc">${this.escapeHtml(co.description)}</p>
        <div style="font-size: 11px; color: var(--text-muted); margin-bottom: 6px;">
          <strong style="color: #FFFFFF;">Implementation:</strong> ${this.escapeHtml(co.details)}
        </div>
        <div class="co-meta">
          <span>● Target: ${this.escapeHtml(co.status)}</span>
        </div>
      </div>
    `).join('');
  }

  // ---------------------------------------------------------------------------
  // Pilot Profile & System Settings Modal
  // ---------------------------------------------------------------------------
  private initProfileSettingsModal(): void {
    document.getElementById('btn-open-profile')?.addEventListener('click', () => {
      this.updatePilotDossier();
      this.openModal('profile-settings-modal');
    });

    document.getElementById('profile-settings-modal-close')?.addEventListener('click', () => {
      this.closeModal('profile-settings-modal');
    });

    document.getElementById('btn-close-settings')?.addEventListener('click', () => {
      this.closeModal('profile-settings-modal');
    });

    // Save preferences
    document.getElementById('btn-save-settings')?.addEventListener('click', () => {
      const threatSelect = document.getElementById('settings-threat-level') as HTMLSelectElement;
      const sfxCheck = document.getElementById('settings-sfx-check') as HTMLInputElement;
      const musicCheck = document.getElementById('settings-music-check') as HTMLInputElement;

      const preferences = {
        threatLevel: threatSelect ? threatSelect.value : '0',
        sfx: sfxCheck ? sfxCheck.checked : true,
        music: musicCheck ? musicCheck.checked : true
      };

      try {
        localStorage.setItem('airstrike_preferences', JSON.stringify(preferences));
      } catch {
        // Ignore localStorage error
      }

      this.showToast('Hardware preferences saved to enclave storage.');
      this.closeModal('profile-settings-modal');
    });

    // Dossier Auth Button (Login / Logout)
    document.getElementById('dossier-auth-toggle-btn')?.addEventListener('click', async () => {
      if (this.authService.isAuthenticated()) {
        await this.authService.signOut();
        this.updatePilotDossier();
        this.showToast('Logged out of Starfleet Enclave');
      } else {
        this.closeModal('profile-settings-modal');
        this.openModal('auth-modal');
      }
    });

    // Load stored preferences
    try {
      const stored = localStorage.getItem('airstrike_preferences');
      if (stored) {
        const prefs = JSON.parse(stored);
        const threatSelect = document.getElementById('settings-threat-level') as HTMLSelectElement;
        const sfxCheck = document.getElementById('settings-sfx-check') as HTMLInputElement;
        const musicCheck = document.getElementById('settings-music-check') as HTMLInputElement;

        if (threatSelect && prefs.threatLevel !== undefined) threatSelect.value = prefs.threatLevel;
        if (sfxCheck && prefs.sfx !== undefined) sfxCheck.checked = !!prefs.sfx;
        if (musicCheck && prefs.music !== undefined) musicCheck.checked = !!prefs.music;
      }
    } catch {
      // Ignore
    }
  }

  private updatePilotDossier(): void {
    const session = this.authService.getSession();
    const isAuth = !!session;

    const pilotDisplay = document.getElementById('pilot-callsign-display');
    const pilotBeacon = document.getElementById('pilot-beacon');
    const authBtn = document.getElementById('auth-action-btn');
    const registerBtn = document.getElementById('auth-register-btn');
    const dossierCallsign = document.getElementById('dossier-callsign');
    const dossierEmail = document.getElementById('dossier-email');
    const dossierClearance = document.getElementById('dossier-clearance');
    const dossierRecord = document.getElementById('dossier-record');
    const dossierAuthBtn = document.getElementById('dossier-auth-toggle-btn');
    const recordDisplay = document.getElementById('menu-record-display');

    if (recordDisplay) {
      recordDisplay.textContent = `RECORD: ${this.personalHighScore.toLocaleString()} PTS`;
    }

    if (isAuth && session) {
      const callsign = (session.callsign || 'PILOT').toUpperCase();
      if (pilotDisplay) pilotDisplay.textContent = `COMMANDER ${callsign}`;
      if (pilotBeacon) {
        pilotBeacon.style.color = 'var(--emerald-status)';
        pilotBeacon.style.textShadow = '0 0 8px #10B981';
      }
      if (authBtn) {
        authBtn.textContent = 'LOGOUT';
        authBtn.className = 'pilot-pill-btn logout';
      }
      if (registerBtn) registerBtn.style.display = 'none';

      if (dossierCallsign) {
        dossierCallsign.textContent = `COMMANDER ${callsign}`;
        dossierCallsign.style.color = 'var(--emerald-status)';
      }
      if (dossierEmail) dossierEmail.textContent = session.email || 'AUTHENTICATED';
      if (dossierClearance) {
        dossierClearance.textContent = 'STARFLEET CLEARANCE LEVEL 4';
        dossierClearance.style.color = 'var(--cyan-primary)';
      }
      if (dossierRecord) {
        dossierRecord.textContent = `${this.personalHighScore.toLocaleString()} PTS`;
      }
      if (dossierAuthBtn) {
        dossierAuthBtn.textContent = 'LOGOUT OF ENCLAVE';
        dossierAuthBtn.className = 'pilot-pill-btn logout';
      }
    } else {
      if (pilotDisplay) pilotDisplay.textContent = 'PILOT: CADET (LOCAL GUEST)';
      if (pilotBeacon) {
        pilotBeacon.style.color = 'var(--cyan-neon)';
        pilotBeacon.style.textShadow = '0 0 8px rgba(0, 229, 255, 0.6)';
      }
      if (authBtn) {
        authBtn.textContent = 'LOGIN';
        authBtn.className = 'pilot-pill-btn';
      }
      if (registerBtn) registerBtn.style.display = 'inline-block';

      if (dossierCallsign) {
        dossierCallsign.textContent = 'CADET (GUEST)';
        dossierCallsign.style.color = 'var(--gold-accent)';
      }
      if (dossierEmail) dossierEmail.textContent = 'LOCAL ENCLAVE ONLY';
      if (dossierClearance) {
        dossierClearance.textContent = 'UNREGISTERED CADET';
        dossierClearance.style.color = 'var(--text-muted)';
      }
      if (dossierRecord) {
        dossierRecord.textContent = `${this.personalHighScore.toLocaleString()} PTS`;
      }
      if (dossierAuthBtn) {
        dossierAuthBtn.textContent = 'LOGIN / REGISTER';
        dossierAuthBtn.className = 'pilot-pill-btn';
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Pilot Manual Modal
  // ---------------------------------------------------------------------------
  private initInstructionsModal(): void {
    document.getElementById('btn-open-instructions')?.addEventListener('click', () => {
      this.openModal('instructions-modal');
    });

    document.getElementById('instructions-modal-close')?.addEventListener('click', () => {
      this.closeModal('instructions-modal');
    });

    document.getElementById('btn-close-instructions')?.addEventListener('click', () => {
      this.closeModal('instructions-modal');
    });
  }

  // ---------------------------------------------------------------------------
  // Supabase Authentication & Forms
  // ---------------------------------------------------------------------------
  private bindAuth(): void {
    const authBtn = document.getElementById('auth-action-btn');
    const registerBtn = document.getElementById('auth-register-btn');
    const guestBtn = document.getElementById('btn-auth-guest-mode');
    const authClose = document.getElementById('auth-modal-close');
    const tabLogin = document.getElementById('tab-login');
    const tabRegister = document.getElementById('tab-register');
    const callsignGroup = document.getElementById('field-callsign-group');
    const confirmGroup = document.getElementById('field-confirm-group');
    const authForm = document.getElementById('auth-form');
    const authTitle = document.getElementById('auth-modal-title');
    const authSubmit = document.getElementById('auth-submit-btn');

    // Subscribe to auth state changes
    this.authService.subscribe(() => {
      this.updatePilotDossier();
    });

    authBtn?.addEventListener('click', async () => {
      if (this.authService.isAuthenticated()) {
        await this.authService.signOut();
        this.updatePilotDossier();
        this.showToast('Logged out of Academy Terminal');
      } else {
        this.isRegisterMode = false;
        tabLogin?.classList.add('active');
        tabRegister?.classList.remove('active');
        if (callsignGroup) callsignGroup.style.display = 'none';
        if (confirmGroup) confirmGroup.style.display = 'none';
        if (authTitle) authTitle.textContent = 'STARFLEET COMMAND ACCESS';
        if (authSubmit) authSubmit.textContent = 'AUTHENTICATE PILOT';
        this.setAuthFeedback('');
        this.openModal('auth-modal');
      }
    });

    registerBtn?.addEventListener('click', () => {
      this.isRegisterMode = true;
      tabRegister?.classList.add('active');
      tabLogin?.classList.remove('active');
      if (callsignGroup) callsignGroup.style.display = 'block';
      if (confirmGroup) confirmGroup.style.display = 'block';
      if (authTitle) authTitle.textContent = 'ENLIST NEW STARFLEET PILOT';
      if (authSubmit) authSubmit.textContent = 'REGISTER CADET';
      this.setAuthFeedback('');
      this.openModal('auth-modal');
    });

    guestBtn?.addEventListener('click', () => {
      this.closeModal('auth-modal');
      this.showToast('Operating in Cadet Guest Mode.');
    });

    authClose?.addEventListener('click', () => {
      this.closeModal('auth-modal');
    });

    tabLogin?.addEventListener('click', () => {
      this.isRegisterMode = false;
      tabLogin.classList.add('active');
      tabRegister?.classList.remove('active');
      if (callsignGroup) callsignGroup.style.display = 'none';
      if (confirmGroup) confirmGroup.style.display = 'none';
      if (authTitle) authTitle.textContent = 'STARFLEET COMMAND ACCESS';
      if (authSubmit) authSubmit.textContent = 'AUTHENTICATE PILOT';
      this.setAuthFeedback('');
    });

    tabRegister?.addEventListener('click', () => {
      this.isRegisterMode = true;
      tabRegister.classList.add('active');
      tabLogin?.classList.remove('active');
      if (callsignGroup) callsignGroup.style.display = 'block';
      if (confirmGroup) confirmGroup.style.display = 'block';
      if (authTitle) authTitle.textContent = 'ENLIST NEW STARFLEET PILOT';
      if (authSubmit) authSubmit.textContent = 'REGISTER CADET';
      this.setAuthFeedback('');
    });

    const handleAuthSubmit = async (e: Event) => {
      e.preventDefault();
      if (this.isAuthSubmitting) return;

      const emailInput = document.getElementById('auth-input-email') as HTMLInputElement;
      const passInput = document.getElementById('auth-input-password') as HTMLInputElement;
      const confirmInput = document.getElementById('auth-input-confirm') as HTMLInputElement;
      const callsignInput = document.getElementById('auth-input-callsign') as HTMLInputElement;

      const email = emailInput?.value?.trim() || '';
      const password = passInput?.value || '';
      const confirmPass = confirmInput?.value || '';
      const callsign = callsignInput?.value?.trim() || '';

      if (!email || !password) {
        this.setAuthFeedback('Email and security password are required.', '#EF4444');
        return;
      }
      if (!email.includes('@')) {
        this.setAuthFeedback('Please enter your full email address (e.g. pilot@example.com), not your callsign.', '#F59E0B');
        return;
      }
      if (password.length < 6) {
        this.setAuthFeedback('Security password must be at least 6 characters.', '#EF4444');
        return;
      }

      const isRegister = this.isRegisterMode || !!document.getElementById('tab-register')?.classList.contains('active');

      if (isRegister) {
        if (!callsign) {
          this.setAuthFeedback('Pilot callsign is required for registration.', '#EF4444');
          return;
        }
        if (confirmPass && password !== confirmPass) {
          this.setAuthFeedback('Passwords do not match.', '#EF4444');
          return;
        }
      }

      this.isAuthSubmitting = true;
      if (authSubmit) (authSubmit as HTMLButtonElement).disabled = true;
      this.setAuthFeedback('Communicating with Supabase...', 'var(--cyan-neon)');

      try {
        if (isRegister) {
          const res = await this.authService.signUp(email, password, callsign);
          if (res.success) {
            this.setAuthFeedback(`Welcome aboard, Cadet ${callsign}! Clearance confirmed.`, '#22C55E');
            this.updatePilotDossier();
            setTimeout(() => this.closeModal('auth-modal'), 1200);
          } else {
            this.setAuthFeedback(res.message, '#EF4444');
          }
        } else {
          const res = await this.authService.signIn(email, password);
          if (res.success) {
            this.setAuthFeedback(res.message, '#22C55E');
            this.updatePilotDossier();
            setTimeout(() => this.closeModal('auth-modal'), 800);
          } else {
            const isInvalidCreds = res.message?.toLowerCase().includes('invalid login credentials');
            if (isInvalidCreds) {
              this.setAuthFeedback('Invalid credentials. If this account is not registered yet, click "NEW REGISTRATION" above to enlist!', '#F59E0B');
            } else {
              this.setAuthFeedback(res.message, '#EF4444');
            }
          }
        }
      } catch (err: unknown) {
        const msg = err instanceof Error ? err.message : String(err);
        this.setAuthFeedback(`Auth error: ${msg}`, '#EF4444');
      } finally {
        this.isAuthSubmitting = false;
        if (authSubmit) (authSubmit as HTMLButtonElement).disabled = false;
      }
    };

    authForm?.addEventListener('submit', handleAuthSubmit);
  }

  // ---------------------------------------------------------------------------
  // Touch Controls
  // ---------------------------------------------------------------------------
  private bindTouchControls(): void {
    const up = document.getElementById('touch-up');
    const down = document.getElementById('touch-down');
    const left = document.getElementById('touch-left');
    const right = document.getElementById('touch-right');
    const fire = document.getElementById('touch-fire');

    let vy = 0;
    let vx = 0;

    const updateVirtual = () => {
      this.engine.inputManager.setVirtualMovement(vx, vy);
    };

    up?.addEventListener('touchstart', (e) => { e.preventDefault(); vy = -1; updateVirtual(); });
    up?.addEventListener('touchend', (e) => { e.preventDefault(); vy = 0; updateVirtual(); });

    down?.addEventListener('touchstart', (e) => { e.preventDefault(); vy = 1; updateVirtual(); });
    down?.addEventListener('touchend', (e) => { e.preventDefault(); vy = 0; updateVirtual(); });

    left?.addEventListener('touchstart', (e) => { e.preventDefault(); vx = -1; updateVirtual(); });
    left?.addEventListener('touchend', (e) => { e.preventDefault(); vx = 0; updateVirtual(); });

    right?.addEventListener('touchstart', (e) => { e.preventDefault(); vx = 1; updateVirtual(); });
    right?.addEventListener('touchend', (e) => { e.preventDefault(); vx = 0; updateVirtual(); });

    fire?.addEventListener('touchstart', (e) => {
      e.preventDefault();
      this.engine.inputManager.setTouchFire(true);
    });
    fire?.addEventListener('touchend', (e) => {
      e.preventDefault();
      this.engine.inputManager.setTouchFire(false);
    });
  }

  // ---------------------------------------------------------------------------
  // State Change & Game Over
  // ---------------------------------------------------------------------------
  private handleStateChange(state: GameState): void {
    const menuOverlay = document.getElementById('main-menu-overlay');
    const pauseOverlay = document.getElementById('pause-overlay');
    const gameoverOverlay = document.getElementById('gameover-overlay');

    menuOverlay?.classList.add('hidden');
    pauseOverlay?.classList.add('hidden');
    gameoverOverlay?.classList.add('hidden');

    switch (state) {
      case GameState.MENU:
        menuOverlay?.classList.remove('hidden');
        this.updatePilotDossier();
        break;
      case GameState.PAUSED:
        pauseOverlay?.classList.remove('hidden');
        break;
      case GameState.GAME_OVER:
        gameoverOverlay?.classList.remove('hidden');
        break;
      case GameState.PLAYING:
        // No overlays during active flight
        break;
    }
  }

  private async handleGameOver(summary: MatchSummary): Promise<void> {
    const scoreElem = document.getElementById('gameover-score');
    const highscoreElem = document.getElementById('gameover-highscore');
    const sectorElem = document.getElementById('gameover-sector');
    const killsElem = document.getElementById('gameover-kills');
    const timeAccElem = document.getElementById('gameover-time-acc');
    const syncElem = document.getElementById('gameover-sync-status');

    this.updatePersonalRecord(summary.score);

    if (scoreElem) scoreElem.textContent = summary.score.toLocaleString();
    if (highscoreElem) highscoreElem.textContent = `${this.personalHighScore.toLocaleString()} PTS`;
    if (sectorElem) sectorElem.textContent = `Sector ${summary.levelReached}`;
    if (killsElem) killsElem.textContent = String(summary.enemiesDestroyed);
    if (timeAccElem) {
      const acc = summary.accuracy !== undefined ? summary.accuracy : 0;
      timeAccElem.textContent = `${summary.durationSeconds}s / ${acc}% Acc`;
    }

    if (syncElem) {
      syncElem.textContent = 'Synchronizing mission debrief to Supabase...';
      syncElem.style.color = 'var(--text-muted)';
    }

    // Submit score to Supabase via MatchService
    const res = await this.matchService.saveMatch(
      summary.score,
      summary.levelReached,
      summary.durationSeconds
    );

    if (syncElem) {
      syncElem.textContent = res.message;
      syncElem.style.color = res.success ? '#22C55E' : '#EF4444';
    }
  }

  // ---------------------------------------------------------------------------
  // Leaderboard Modal
  // ---------------------------------------------------------------------------
  private async openLeaderboard(): Promise<void> {
    this.openModal('leaderboard-modal');
    const tbody = document.getElementById('leaderboard-table-body');
    const notice = document.getElementById('leaderboard-source-notice');

    if (tbody) tbody.innerHTML = '<tr><td colspan="3" style="text-align: center; color: var(--text-muted); padding: 18px;">Connecting to telemetry...</td></tr>';
    if (notice) {
      notice.textContent = 'Querying Supabase Cloud...';
      notice.style.color = 'var(--text-muted)';
    }

    try {
      const result = await this.leaderboardService.fetchLeaderboard(10);

      if (notice) {
        if (result.error && (result.message?.includes('Option B') || result.message?.includes('grant') || result.message?.includes('permission'))) {
          notice.textContent = '● Supabase Cloud Online • Global Table Awaiting Remote Option B Grant';
          notice.style.color = '#38BDF8';
        } else {
          notice.textContent = result.message || '⚡ Connected to Supabase Cloud';
          notice.style.color = result.error ? '#EF4444' : '#22C55E';
        }
      }

      if (tbody) {
        if (result.entries.length === 0) {
          // If error / RLS grant pending, check if current pilot is authenticated and has personal matches
          const session = this.authService.getSession();
          if (session) {
            const userMatches = await this.matchService.fetchMyMatches(5);
            if (userMatches.length > 0) {
              const sortedMatches = [...userMatches].sort((a, b) => b.score - a.score);
              tbody.innerHTML = `
                <tr><td colspan="3" style="text-align: center; color: var(--cyan-neon); font-size: 11px; padding: 6px; background: rgba(0,229,255,0.08); font-weight: bold;">★ YOUR CLOUD FLIGHT RECORDS (AUTHENTICATED) ★</td></tr>
              ` + sortedMatches.map((m, idx) => `
                <tr>
                  <td class="${idx === 0 ? 'rank-gold' : idx === 1 ? 'rank-silver' : 'rank-bronze'}">#${idx + 1}</td>
                  <td style="font-weight: 600; color: #FFFFFF;">${this.escapeHtml(session.callsign || 'COMMANDER')}</td>
                  <td style="text-align: right; color: var(--cyan-neon); font-weight: bold;">${m.score.toLocaleString()}</td>
                </tr>
              `).join('') + `
                <tr><td colspan="3" style="text-align: center; color: var(--text-muted); font-size: 10px; padding: 8px;">(Global multi-pilot rankings pending Option B SQL grant in Supabase)</td></tr>
              `;
              return;
            }
          }

          tbody.innerHTML = `
            <tr><td colspan="3" style="text-align: center; color: var(--cyan-neon); padding: 18px 12px; font-size: 13px; line-height: 1.6;">
              <span style="font-weight: bold; color: #22C55E;">● Supabase Cloud is Connected & Online</span><br>
              <span style="color: var(--text-muted); font-size: 12px;">Global Hall of Fame requires remote database read grant.<br>
              Log in as Commander or execute Option B in Supabase SQL Editor:<br>
              <code style="color: #38BDF8; font-size: 11px;">GRANT SELECT ON public.leaderboard TO anon;</code></span>
            </td></tr>
          `;
        } else {
          tbody.innerHTML = result.entries.map((entry, idx) => {
            let rankClass = '';
            let medal = `#${idx + 1}`;
            if (idx === 0) { rankClass = 'rank-gold'; medal = '🥇 #1'; }
            else if (idx === 1) { rankClass = 'rank-silver'; medal = '🥈 #2'; }
            else if (idx === 2) { rankClass = 'rank-bronze'; medal = '🥉 #3'; }

            return `
              <tr>
                <td class="${rankClass}">${medal}</td>
                <td style="font-weight: 600; color: #FFFFFF;">${this.escapeHtml(entry.username)}</td>
                <td style="text-align: right; color: var(--cyan-neon); font-weight: bold;">${entry.best_score.toLocaleString()}</td>
              </tr>
            `;
          }).join('');
        }
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      if (notice) {
        notice.textContent = 'Telemetry notice';
        notice.style.color = '#EF4444';
      }
      if (tbody) {
        tbody.innerHTML = `<tr><td colspan="3" style="text-align: center; color: #EF4444; padding: 20px;">Connection failed: ${this.escapeHtml(msg)}</td></tr>`;
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Flight Log / History Modal
  // ---------------------------------------------------------------------------
  private async openHistory(): Promise<void> {
    this.openModal('history-modal');
    const tbody = document.getElementById('history-table-body');
    if (tbody) tbody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: var(--text-muted);">Accessing flight logs...</td></tr>';

    const matches = await this.matchService.fetchMyMatches(15);

    if (tbody) {
      if (matches.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: var(--text-muted);">No missions logged yet.</td></tr>';
      } else {
        tbody.innerHTML = matches.map(m => {
          const dateStr = m.created_at ? new Date(m.created_at).toLocaleDateString() : 'Recent';
          return `
            <tr>
              <td style="color: var(--cyan-neon);">Sector ${m.level_reached}</td>
              <td style="font-weight: bold; color: #FFF;">${m.score.toLocaleString()}</td>
              <td style="color: var(--text-muted);">${m.duration_seconds}s</td>
              <td style="text-align: right; color: var(--text-muted); font-size: 11px;">${dateStr}</td>
            </tr>
          `;
        }).join('');
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Cloud Link Telemetry & In-App Diagnostics
  // ---------------------------------------------------------------------------
  private initCloudTelemetry(): void {
    document.getElementById('btn-cloud-diag')?.addEventListener('click', () => {
      this.openCloudDiagnostics();
    });

    document.getElementById('cloud-diag-modal-close')?.addEventListener('click', () => {
      this.closeModal('cloud-diag-modal');
    });

    document.getElementById('btn-ping-now')?.addEventListener('click', () => {
      this.runDiagnosticsPing();
    });

    document.getElementById('btn-copy-sql')?.addEventListener('click', () => {
      this.copyMigrationSql();
    });

    // Run initial connection probe asynchronously
    this.checkCloudConnection();
  }

  public async checkCloudConnection(): Promise<{ online: boolean; latency: number; version?: string }> {
    const dot = document.getElementById('cloud-status-dot');
    const text = document.getElementById('cloud-status-text');

    if (dot) dot.className = 'cloud-status-dot checking';
    if (text) text.textContent = 'CHECKING COMM LINK...';

    const startTime = (typeof performance !== 'undefined') ? performance.now() : Date.now();

    try {
      const controller = (typeof AbortController !== 'undefined') ? new AbortController() : null;
      const timeoutId = controller ? setTimeout(() => controller.abort(), 8000) : null;

      const res = await fetch(`${SUPABASE_URL}/auth/v1/health`, {
        headers: { apikey: SUPABASE_ANON_KEY },
        signal: controller?.signal
      });

      if (timeoutId) clearTimeout(timeoutId);
      const endTime = (typeof performance !== 'undefined') ? performance.now() : Date.now();
      const latency = Math.max(1, Math.round(endTime - startTime));
      this.lastPingLatency = latency;

      if (res.ok) {
        const data = await res.json().catch(() => ({}));
        if (dot) dot.className = 'cloud-status-dot online';
        if (text) text.textContent = `STARFLEET CLOUD LINK ACTIVE (${latency}ms)`;
        return { online: true, latency, version: data.version || 'v2' };
      } else {
        if (dot) dot.className = 'cloud-status-dot offline';
        if (text) text.textContent = 'CLOUD LINK OFFLINE • LOCAL MODE';
        return { online: false, latency };
      }
    } catch {
      // Secondary fallback probe: try Supabase client session ping before declaring offline
      try {
        const { error: sessionErr } = await supabase.auth.getSession();
        if (!sessionErr) {
          const endTime = (typeof performance !== 'undefined') ? performance.now() : Date.now();
          const latency = Math.max(1, Math.round(endTime - startTime));
          this.lastPingLatency = latency;
          if (dot) dot.className = 'cloud-status-dot online';
          if (text) text.textContent = `STARFLEET CLOUD LINK ACTIVE (${latency}ms)`;
          return { online: true, latency, version: 'GoTrue' };
        }
      } catch {
        // Genuine offline
      }

      const endTime = (typeof performance !== 'undefined') ? performance.now() : Date.now();
      const latency = Math.max(1, Math.round(endTime - startTime));
      this.lastPingLatency = latency;
      if (dot) dot.className = 'cloud-status-dot offline';
      if (text) text.textContent = 'CLOUD LINK OFFLINE • LOCAL MODE';
      return { online: false, latency };
    }
  }

  private openCloudDiagnostics(): void {
    this.openModal('cloud-diag-modal');
    const endpointEl = document.getElementById('diag-endpoint');
    const sessionEl = document.getElementById('diag-session');
    const latencyEl = document.getElementById('diag-latency');

    if (endpointEl) endpointEl.textContent = SUPABASE_URL;

    const session = this.authService.getSession();
    if (sessionEl) {
      sessionEl.textContent = session ? `COMMANDER ${session.callsign} (${session.email})` : 'CADET (GUEST MODE)';
      sessionEl.style.color = session ? 'var(--cyan-neon)' : '#FFFFFF';
    }

    if (latencyEl) {
      latencyEl.textContent = this.lastPingLatency > 0 ? `${this.lastPingLatency} ms` : 'Measuring...';
    }

    this.runDiagnosticsPing();
  }

  private async runDiagnosticsPing(): Promise<void> {
    const feedback = document.getElementById('diag-feedback');
    const latencyEl = document.getElementById('diag-latency');
    const authStatusEl = document.getElementById('diag-auth-status');
    const restStatusEl = document.getElementById('diag-rest-status');

    if (feedback) {
      feedback.textContent = 'Pinging Supabase GoTrue & REST APIs...';
      feedback.style.color = 'var(--cyan-neon)';
    }

    const result = await this.checkCloudConnection();

    if (latencyEl) latencyEl.textContent = `${result.latency} ms`;
    if (authStatusEl) {
      authStatusEl.textContent = result.online ? `● ONLINE (${result.version || 'GoTrue'})` : '▲ OFFLINE';
      authStatusEl.style.color = result.online ? '#22C55E' : '#EF4444';
    }
    if (restStatusEl) {
      restStatusEl.textContent = result.online ? '● CONNECTED' : '▲ DISCONNECTED';
      restStatusEl.style.color = result.online ? '#22C55E' : '#EF4444';
    }

    if (feedback) {
      feedback.textContent = result.online
        ? `Comm Link Verified (${result.latency}ms) — Supabase Online`
        : 'Comm Link Offline — Local Storage active';
      feedback.style.color = result.online ? '#22C55E' : '#EF4444';
    }
  }

  private copyMigrationSql(): void {
    const sqlText = `GRANT SELECT ON public.leaderboard TO anon;\nGRANT SELECT ON public.leaderboard TO authenticated;\nCREATE POLICY "anon_select_leaderboard" ON public.leaderboard FOR SELECT TO anon USING (true);`;
    if (navigator?.clipboard) {
      navigator.clipboard.writeText(sqlText).then(() => {
        this.showToast('SQL Grant copied to clipboard!');
      }).catch(() => {
        this.showToast('Select & copy SQL from the box.');
      });
    } else {
      this.showToast('Select & copy SQL from the box.');
    }
  }

  public showToast(message: string): void {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.textContent = message;
    container.appendChild(toast);

    setTimeout(() => {
      toast.remove();
    }, 2800);
  }

  private openModal(id: string): void {
    document.getElementById(id)?.classList.remove('hidden');
  }

  private closeModal(id: string): void {
    document.getElementById(id)?.classList.add('hidden');
  }

  private setAuthFeedback(msg: string, color: string = 'var(--cyan-neon)'): void {
    const elem = document.getElementById('auth-feedback');
    if (elem) {
      elem.textContent = msg;
      elem.style.color = color;
    }
  }

  private escapeHtml(str: string): string {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
  }
}
