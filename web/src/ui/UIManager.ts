import { GameEngine } from '../game/GameEngine';
import { GameState, MatchSummary } from '../types/game';
import { AuthService } from '../services/AuthService';
import { MatchService } from '../services/MatchService';
import { LeaderboardService } from '../services/LeaderboardService';
import { SoundService } from '../services/SoundService';
import { SUPABASE_URL, SUPABASE_ANON_KEY } from '../services/SupabaseClient';

export class UIManager {
  private engine: GameEngine;
  private authService: AuthService;
  private matchService: MatchService;
  private leaderboardService: LeaderboardService;
  private soundService: SoundService;

  private isRegisterMode: boolean = false;
  private isAuthSubmitting: boolean = false;
  private lastPingLatency: number = 0;

  constructor(engine: GameEngine) {
    this.engine = engine;
    this.authService = AuthService.getInstance();
    this.matchService = MatchService.getInstance();
    this.leaderboardService = LeaderboardService.getInstance();
    this.soundService = SoundService.getInstance();

    this.bindEvents();
    this.bindAuth();
    this.bindTouchControls();
    this.initCloudTelemetry();
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

    // Pause overlay buttons
    document.getElementById('btn-resume-game')?.addEventListener('click', () => {
      this.engine.resumeGame();
    });

    document.getElementById('btn-restart-game')?.addEventListener('click', () => {
      this.engine.startNewGame();
    });

    document.getElementById('btn-menu-from-pause')?.addEventListener('click', () => {
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

  private bindAuth(): void {
    const authBtn = document.getElementById('auth-action-btn');
    const authClose = document.getElementById('auth-modal-close');
    const tabLogin = document.getElementById('tab-login');
    const tabRegister = document.getElementById('tab-register');
    const callsignGroup = document.getElementById('field-callsign-group');
    const authForm = document.getElementById('auth-form');
    const authTitle = document.getElementById('auth-modal-title');
    const authSubmit = document.getElementById('auth-submit-btn');

    // Auth status listener
    this.authService.subscribe((session) => {
      const pilotDisplay = document.getElementById('pilot-callsign-display');
      if (session) {
        if (pilotDisplay) pilotDisplay.textContent = `COMMANDER ${session.callsign}`;
        if (authBtn) authBtn.textContent = 'LOGOUT';
      } else {
        if (pilotDisplay) pilotDisplay.textContent = 'CADET (GUEST)';
        if (authBtn) authBtn.textContent = 'LOGIN / REGISTER';
      }
    });

    authBtn?.addEventListener('click', async () => {
      if (this.authService.isAuthenticated()) {
        await this.authService.signOut();
        this.showToast('Logged out of Academy Terminal');
      } else {
        this.openModal('auth-modal');
      }
    });

    authClose?.addEventListener('click', () => {
      this.closeModal('auth-modal');
    });

    tabLogin?.addEventListener('click', () => {
      this.isRegisterMode = false;
      tabLogin.classList.add('active');
      tabRegister?.classList.remove('active');
      if (callsignGroup) callsignGroup.style.display = 'none';
      if (authTitle) authTitle.textContent = 'PILOT SIGN IN';
      if (authSubmit) authSubmit.textContent = 'SIGN IN';
      this.setAuthFeedback('');
    });

    tabRegister?.addEventListener('click', () => {
      this.isRegisterMode = true;
      tabRegister.classList.add('active');
      tabLogin?.classList.remove('active');
      if (callsignGroup) callsignGroup.style.display = 'block';
      if (authTitle) authTitle.textContent = 'ENLIST NEW PILOT';
      if (authSubmit) authSubmit.textContent = 'REGISTER CADET';
      this.setAuthFeedback('');
    });

    const handleAuthSubmit = async (e: Event) => {
      e.preventDefault();
      if (this.isAuthSubmitting) return;

      const emailInput = document.getElementById('auth-input-email') as HTMLInputElement;
      const passInput = document.getElementById('auth-input-password') as HTMLInputElement;
      const callsignInput = document.getElementById('auth-input-callsign') as HTMLInputElement;

      const email = emailInput?.value?.trim() || '';
      const password = passInput?.value || '';
      const callsign = callsignInput?.value?.trim() || '';

      if (!email || !password) {
        this.setAuthFeedback('Email and password are required.', '#EF4444');
        return;
      }
      if (password.length < 6) {
        this.setAuthFeedback('Password must be at least 6 characters.', '#EF4444');
        return;
      }

      this.isAuthSubmitting = true;
      if (authSubmit) (authSubmit as HTMLButtonElement).disabled = true;
      this.setAuthFeedback('Communicating with Supabase...', 'var(--cyan-neon)');

      try {
        const isRegister = this.isRegisterMode || !!document.getElementById('tab-register')?.classList.contains('active');

        if (isRegister) {
          const res = await this.authService.signUp(email, password, callsign);
          this.setAuthFeedback(res.message, res.success ? '#22C55E' : '#EF4444');
          if (res.success) {
            setTimeout(() => this.closeModal('auth-modal'), 1200);
          }
        } else {
          const res = await this.authService.signIn(email, password);
          this.setAuthFeedback(res.message, res.success ? '#22C55E' : '#EF4444');
          if (res.success) {
            setTimeout(() => this.closeModal('auth-modal'), 800);
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
    const sectorElem = document.getElementById('gameover-sector');
    const timeElem = document.getElementById('gameover-time');
    const killsElem = document.getElementById('gameover-kills');
    const syncElem = document.getElementById('gameover-sync-status');

    if (scoreElem) scoreElem.textContent = summary.score.toLocaleString();
    if (sectorElem) sectorElem.textContent = String(summary.levelReached);
    if (timeElem) timeElem.textContent = `${summary.durationSeconds}s`;
    if (killsElem) killsElem.textContent = String(summary.enemiesDestroyed);

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
        notice.textContent = result.message || '⚡ Connected to Supabase Cloud';
        notice.style.color = result.error ? '#EF4444' : '#22C55E';
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
      const timeoutId = controller ? setTimeout(() => controller.abort(), 4000) : null;

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
        if (text) text.textContent = `● STARFLEET CLOUD LINK ACTIVE (${latency}ms)`;
        return { online: true, latency, version: data.version || 'v2' };
      } else {
        if (dot) dot.className = 'cloud-status-dot offline';
        if (text) text.textContent = '▲ CLOUD LINK OFFLINE • LOCAL MODE';
        return { online: false, latency };
      }
    } catch {
      const endTime = (typeof performance !== 'undefined') ? performance.now() : Date.now();
      const latency = Math.max(1, Math.round(endTime - startTime));
      this.lastPingLatency = latency;
      if (dot) dot.className = 'cloud-status-dot offline';
      if (text) text.textContent = '▲ CLOUD LINK OFFLINE • LOCAL MODE';
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
