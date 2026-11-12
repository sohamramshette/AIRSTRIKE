import puppeteer from 'puppeteer-core';
import path from 'node:path';
import { createServer } from 'vite';

const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
const ARTIFACT_DIR = 'C:\\Users\\lenovo\\.gemini\\antigravity\\brain\\e548191e-70dc-46a1-ac05-79371e515d36';
const SCREENSHOT_DIR = path.resolve(process.cwd(), 'docs/screenshots');

async function runPart7VisualQA() {
  const fs = await import('node:fs');
  if (!fs.existsSync(SCREENSHOT_DIR)) {
    fs.mkdirSync(SCREENSHOT_DIR, { recursive: true });
  }

  // Start Vite local dev server on dedicated port 3009
  const server = await createServer({
    root: path.resolve(process.cwd(), 'web'),
    server: { port: 3009 }
  });
  await server.listen();
  console.log('Vite server running on http://localhost:3009');

  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: true,
    args: ['--no-sandbox', '--disable-gpu']
  });

  const page = await browser.newPage();
  await page.setViewport({ width: 1280, height: 800 });
  await page.goto('http://localhost:3009', { waitUntil: 'networkidle0' });

  // 1. Open 1v1 Multiplayer Provisional Leaderboard Tab
  await page.evaluate(() => {
    const ui = (window as any).__airstrikeUI;
    if (ui) {
      ui.openLeaderboard('multiplayer');

      // Populate sample 1v1 provisional ranking records
      const tbody = document.getElementById('mp-leaderboard-table-body');
      if (tbody) {
        tbody.innerHTML = `
          <tr>
            <td class="rank-gold">🥇 #1</td>
            <td style="font-weight: 600; color: #FFFFFF;">
              STAR-COMMANDER
              <span style="font-size: 9px; color: #FBBF24; border: 1px solid rgba(251,191,36,0.4); border-radius: 3px; padding: 1px 3px; margin-left: 4px;">PROV</span>
            </td>
            <td style="text-align: center; color: var(--purple-accent); font-weight: bold; font-family: var(--font-mono);">1050</td>
            <td style="text-align: center; font-size: 11px; color: var(--text-muted);">2W / 0L / 0D</td>
            <td style="text-align: right; color: #10B981; font-weight: bold;">100.0%</td>
          </tr>
          <tr>
            <td class="rank-silver">🥈 #2</td>
            <td style="font-weight: 600; color: #FFFFFF;">
              VIPER-CADET
              <span style="font-size: 9px; color: #FBBF24; border: 1px solid rgba(251,191,36,0.4); border-radius: 3px; padding: 1px 3px; margin-left: 4px;">PROV</span>
            </td>
            <td style="text-align: center; color: var(--purple-accent); font-weight: bold; font-family: var(--font-mono);">1025</td>
            <td style="text-align: center; font-size: 11px; color: var(--text-muted);">1W / 1L / 0D</td>
            <td style="text-align: right; color: #F59E0B; font-weight: bold;">50.0%</td>
          </tr>
          <tr>
            <td class="rank-bronze">🥉 #3</td>
            <td style="font-weight: 600; color: #FFFFFF;">
              PHOENIX-ACE
              <span style="font-size: 9px; color: #FBBF24; border: 1px solid rgba(251,191,36,0.4); border-radius: 3px; padding: 1px 3px; margin-left: 4px;">PROV</span>
            </td>
            <td style="text-align: center; color: var(--purple-accent); font-weight: bold; font-family: var(--font-mono);">1010</td>
            <td style="text-align: center; font-size: 11px; color: var(--text-muted);">1W / 0L / 1D</td>
            <td style="text-align: right; color: #10B981; font-weight: bold;">66.7%</td>
          </tr>
        `;
      }
    }
  });

  await new Promise(r => setTimeout(r, 600));

  // Capture Multiplayer Leaderboard Modal
  const lbModalPath = path.join(SCREENSHOT_DIR, 'web_mp_leaderboard.png');
  await page.screenshot({ path: lbModalPath });
  fs.copyFileSync(lbModalPath, path.join(ARTIFACT_DIR, 'web_mp_leaderboard.png'));
  console.log('Captured web_mp_leaderboard.png');

  // Close modal
  await page.click('#leaderboard-modal-close');
  await new Promise(r => setTimeout(r, 400));

  // 2. Trigger Multiplayer Match & Provisional Debrief Overlay
  await page.evaluate(() => {
    const engine = (window as any).__airstrikeEngine;
    const combatSync = (window as any).__combatSyncService;
    (window as any).__PILOT_CALLSIGN__ = 'STAR-COMMANDER';

    if (engine) {
      const mockRoom = {
        roomId: 'room-uuid-part7-web',
        roomCode: 'PROV99',
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
        startedAt: new Date(Date.now() - 90000).toISOString(),
        durationSeconds: 90,
        expiresAt: new Date(Date.now() + 15 * 60 * 1000).toISOString()
      };

      engine.startMultiplayerGame(42, mockRoom.startedAt, mockRoom);
      engine.scoreCalc.addBonus(3450);

      if (combatSync) {
        combatSync.handleInboundTelemetry({
          roomId: mockRoom.roomId,
          playerId: 'guest-uuid-002',
          callsign: 'VIPER-CADET',
          score: 2800,
          health: 70,
          shield: 40,
          lives: 2,
          wave: 3,
          combo: 2,
          overheated: false,
          sequence: 85,
          timestamp: Date.now()
        });
      }

      // Finish match to show provisional debrief
      engine.finishMultiplayerMatch();
    }
  });

  await new Promise(r => setTimeout(r, 600));

  // Capture Provisional Debrief Modal
  const debriefPath = path.join(SCREENSHOT_DIR, 'web_mp_provisional_debrief.png');
  await page.screenshot({ path: debriefPath });
  fs.copyFileSync(debriefPath, path.join(ARTIFACT_DIR, 'web_mp_provisional_debrief.png'));
  console.log('Captured web_mp_provisional_debrief.png');

  await browser.close();
  await server.close();
  console.log('Part 7 Visual QA complete! All screenshots saved.');
}

runPart7VisualQA().catch(err => {
  console.error('Web Part 7 Visual QA failed:', err);
  process.exit(1);
});
