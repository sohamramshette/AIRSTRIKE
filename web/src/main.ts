import { GameEngine } from './game/GameEngine';
import { UIManager } from './ui/UIManager';
import { AnnouncerManager } from './announcer/AnnouncerManager';
import { MultiplayerCombatSyncService } from './services/multiplayer/MultiplayerCombatSyncService';

document.addEventListener('DOMContentLoaded', () => {
  const canvas = document.getElementById('game-canvas') as HTMLCanvasElement;
  if (!canvas) {
    console.error('Fatal: #game-canvas element not found in DOM.');
    return;
  }

  try {
    const engine = new GameEngine(canvas);
    const ui = new UIManager(engine);

    const updateDimensions = () => {
      const container = document.getElementById('viewport-container');
      if (!container) return;
      const isMobilePortrait = window.innerWidth <= 768 && window.innerHeight > window.innerWidth;
      const isNarrow = window.innerWidth <= 540;

      if (isMobilePortrait || isNarrow) {
        const rect = container.getBoundingClientRect();
        const w = Math.round(rect.width);
        const h = Math.round(rect.height);
        if (w > 0 && h > 0 && (engine.width !== w || engine.height !== h)) {
          engine.resize(w, h);
        }
      } else {
        if (engine.width !== 800 || engine.height !== 650) {
          engine.resize(800, 650);
        }
      }
    };

    window.addEventListener('resize', updateDimensions);
    window.addEventListener('orientationchange', () => setTimeout(updateDimensions, 100));
    updateDimensions();

    engine.start();

    // Attach to window for automated QA verification & telemetry
    (window as any).__airstrikeEngine = engine;
    (window as any).__airstrikeUI = ui;
    (window as any).__announcerManager = AnnouncerManager.getInstance();
    (window as any).__combatSyncService = MultiplayerCombatSyncService.getInstance();
    (window as any).__updateDimensions = updateDimensions;

    console.log('%c AIRSTRIKE v1.0.0 (Browser Edition) %c Ready for Deployment ',
      'background: #00E5FF; color: #000; font-weight: bold; padding: 2px 6px; border-radius: 3px;',
      'background: #1E293B; color: #38BDF8; padding: 2px 6px; border-radius: 3px;'
    );
  } catch (err) {
    console.error('Failed to initialize AIRSTRIKE game engine:', err);
  }
});

