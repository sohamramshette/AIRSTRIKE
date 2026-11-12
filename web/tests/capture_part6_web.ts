import puppeteer from 'puppeteer-core';
import path from 'node:path';
import { createServer } from 'vite';

const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
const ARTIFACT_DIR = 'C:\\Users\\lenovo\\.gemini\\antigravity\\brain\\e548191e-70dc-46a1-ac05-79371e515d36';
const SCREENSHOT_DIR = path.resolve(process.cwd(), 'docs/screenshots');

async function runPart6VisualQA() {
  const fs = await import('node:fs');
  if (!fs.existsSync(SCREENSHOT_DIR)) {
    fs.mkdirSync(SCREENSHOT_DIR, { recursive: true });
  }

  // Start Vite local dev server on dedicated port 3008
  const server = await createServer({
    root: path.resolve(process.cwd(), 'web'),
    server: { port: 3008 }
  });
  await server.listen();
  console.log('Vite server running on http://localhost:3008');

  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: true,
    args: ['--no-sandbox', '--disable-gpu']
  });

  const page = await browser.newPage();
  await page.setViewport({ width: 1280, height: 800 });
  await page.goto('http://localhost:3008', { waitUntil: 'networkidle0' });

  // 1. Trigger Multiplayer Combat State with Simulated Peer Telemetry
  await page.evaluate(() => {
    const engine = (window as any).__airstrikeEngine;
    const ui = (window as any).__airstrikeUI;
    (window as any).__PILOT_CALLSIGN__ = 'STAR-COMMANDER';

    if (engine && ui) {
      // Setup mock room
      const mockRoom = {
        roomId: 'room-uuid-part6-web',
        roomCode: 'SYNC88',
        hostId: 'host-uuid-001',
        hostUsername: 'STAR-COMMANDER',
        hostReady: true,
        guestId: 'guest-uuid-002',
        guestUsername: 'VIPER-CADET',
        guestReady: true,
        matchSeed: 42,
        status: 'ACTIVE',
        isHost: true,
        isGuest: false,
        createdAt: new Date().toISOString(),
        startedAt: new Date(Date.now() - 15000).toISOString(), // 15s elapsed
        durationSeconds: 90,
        expiresAt: new Date(Date.now() + 15 * 60 * 1000).toISOString()
      };

      // Start multiplayer game in engine
      engine.startMultiplayerGame(42, mockRoom.startedAt, mockRoom);

      // Simulate player score and opponent telemetry
      engine.scoreCalc.addBonus(2450);

      // Access MultiplayerCombatSyncService singleton
      const combatSync = (window as any).__combatSyncService;
      if (combatSync) {
        combatSync.handleInboundTelemetry({
          roomId: mockRoom.roomId,
          playerId: 'guest-uuid-002',
          callsign: 'VIPER-CADET',
          score: 1950,
          health: 80,
          shield: 60,
          lives: 2,
          wave: 2,
          combo: 3,
          overheated: false,
          sequence: 25,
          timestamp: Date.now()
        });
      }
    }
  });

  await new Promise(r => setTimeout(r, 600));

  // Capture Active Combat HUD
  const combatHudPath = path.join(SCREENSHOT_DIR, 'web_mp_combat_hud.png');
  await page.screenshot({ path: combatHudPath });
  fs.copyFileSync(combatHudPath, path.join(ARTIFACT_DIR, 'web_mp_combat_hud.png'));
  console.log('Captured web_mp_combat_hud.png');

  // 2. Trigger Multiplayer Match Debrief Modal
  await page.evaluate(() => {
    const engine = (window as any).__airstrikeEngine;
    if (engine) {
      // Conclude multiplayer match, transitioning to 1v1 debrief overlay
      engine.finishMultiplayerMatch();
    }
  });

  await new Promise(r => setTimeout(r, 600));

  // Capture Match Results Modal
  const resultsPath = path.join(SCREENSHOT_DIR, 'web_mp_match_results.png');
  await page.screenshot({ path: resultsPath });
  fs.copyFileSync(resultsPath, path.join(ARTIFACT_DIR, 'web_mp_match_results.png'));
  console.log('Captured web_mp_match_results.png');

  await browser.close();
  await server.close();
  console.log('Web Part 6 Visual QA complete! All screenshots saved.');
}

runPart6VisualQA().catch(err => {
  console.error('Web Visual QA failed:', err);
  process.exit(1);
});
