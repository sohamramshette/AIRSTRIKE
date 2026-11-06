import { GameEngine } from './game/GameEngine';
import { UIManager } from './ui/UIManager';

document.addEventListener('DOMContentLoaded', () => {
  const canvas = document.getElementById('game-canvas') as HTMLCanvasElement;
  if (!canvas) {
    console.error('Fatal: #game-canvas element not found in DOM.');
    return;
  }

  try {
    const engine = new GameEngine(canvas);
    new UIManager(engine);
    engine.start();

    console.log('%c AIRSTRIKE v1.0.0 (Browser Edition) %c Ready for Deployment ',
      'background: #00E5FF; color: #000; font-weight: bold; padding: 2px 6px; border-radius: 3px;',
      'background: #1E293B; color: #38BDF8; padding: 2px 6px; border-radius: 3px;'
    );
  } catch (err) {
    console.error('Failed to initialize AIRSTRIKE game engine:', err);
  }
});
