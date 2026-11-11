import puppeteer from 'puppeteer-core';
import path from 'node:path';
import { createServer } from 'vite';

const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
const ARTIFACT_DIR = 'C:\\Users\\lenovo\\.gemini\\antigravity\\brain\\e548191e-70dc-46a1-ac05-79371e515d36';
const SCREENSHOT_DIR = path.resolve(process.cwd(), 'docs/screenshots');

async function runVisualQA() {
  const fs = await import('node:fs');
  if (!fs.existsSync(SCREENSHOT_DIR)) {
    fs.mkdirSync(SCREENSHOT_DIR, { recursive: true });
  }

  // Start Vite local dev server
  const server = await createServer({
    root: path.resolve(process.cwd(), 'web'),
    server: { port: 3005 }
  });
  await server.listen();
  console.log('Vite server running on http://localhost:3005');

  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: true,
    args: ['--no-sandbox', '--disable-gpu']
  });

  const page = await browser.newPage();
  await page.setViewport({ width: 1280, height: 800 });
  await page.goto('http://localhost:3005', { waitUntil: 'networkidle0' });

  // 1. Capture Main Menu with Multiplayer Button
  const menuPath = path.join(SCREENSHOT_DIR, 'mp_main_menu.png');
  await page.screenshot({ path: menuPath });
  fs.copyFileSync(menuPath, path.join(ARTIFACT_DIR, 'mp_main_menu.png'));
  console.log('Captured mp_main_menu.png');

  // 2. Open Multiplayer Modal
  await page.click('#btn-open-multiplayer');
  await new Promise(r => setTimeout(r, 400));
  const menuModalPath = path.join(SCREENSHOT_DIR, 'mp_view_menu.png');
  await page.screenshot({ path: menuModalPath });
  fs.copyFileSync(menuModalPath, path.join(ARTIFACT_DIR, 'mp_view_menu.png'));
  console.log('Captured mp_view_menu.png');

  // 3. Switch to Join View
  await page.click('#btn-mp-show-join');
  await new Promise(r => setTimeout(r, 400));
  const joinViewPath = path.join(SCREENSHOT_DIR, 'mp_view_join.png');
  await page.screenshot({ path: joinViewPath });
  fs.copyFileSync(joinViewPath, path.join(ARTIFACT_DIR, 'mp_view_join.png'));
  console.log('Captured mp_view_join.png');

  // 4. Test simulated Lobby state in UI
  await page.evaluate(() => {
    const ui = (window as any).__airstrikeUI;
    if (ui) {
      // Simulate active room state
      const mockRoom = {
        roomId: '00000000-0000-0000-0000-000000000001',
        roomCode: 'X9K2P4',
        hostId: 'host-uuid-1',
        hostUsername: 'STAR-COMMANDER',
        hostReady: true,
        guestId: 'guest-uuid-2',
        guestUsername: 'VIPER-PILOT',
        guestReady: false,
        matchSeed: 42,
        status: 'READY',
        isHost: true,
        isGuest: false,
        createdAt: new Date().toISOString(),
        expiresAt: new Date(Date.now() + 15 * 60 * 1000).toISOString()
      };
      const mockPresences = {
        'host-uuid-1': {
          userId: 'host-uuid-1',
          callsign: 'STAR-COMMANDER',
          presenceStatus: 'online',
          isReady: true,
          joinedAt: Date.now() - 30000
        },
        'guest-uuid-2': {
          userId: 'guest-uuid-2',
          callsign: 'VIPER-PILOT',
          presenceStatus: 'online',
          isReady: false,
          joinedAt: Date.now() - 10000
        }
      };
      ui.renderMultiplayerLobbyState(mockRoom);
      ui.renderMultiplayerPresence(mockPresences);
      ui.switchMultiplayerView('lobby');
    }
  });

  await new Promise(r => setTimeout(r, 400));
  const lobbyViewPath = path.join(SCREENSHOT_DIR, 'mp_view_lobby.png');
  await page.screenshot({ path: lobbyViewPath });
  fs.copyFileSync(lobbyViewPath, path.join(ARTIFACT_DIR, 'mp_view_lobby.png'));
  console.log('Captured mp_view_lobby.png');

  // 5. Simulate Both Players Ready (COUNTDOWN state)
  await page.evaluate(() => {
    const ui = (window as any).__airstrikeUI;
    if (ui) {
      const mockRoomBothReady = {
        roomId: '00000000-0000-0000-0000-000000000001',
        roomCode: 'X9K2P4',
        hostId: 'host-uuid-1',
        hostUsername: 'STAR-COMMANDER',
        hostReady: true,
        guestId: 'guest-uuid-2',
        guestUsername: 'VIPER-PILOT',
        guestReady: true,
        matchSeed: 42,
        status: 'COUNTDOWN',
        isHost: true,
        isGuest: false,
        createdAt: new Date().toISOString(),
        expiresAt: new Date(Date.now() + 15 * 60 * 1000).toISOString()
      };
      const mockPresences = {
        'host-uuid-1': {
          userId: 'host-uuid-1',
          callsign: 'STAR-COMMANDER',
          presenceStatus: 'online',
          isReady: true,
          joinedAt: Date.now() - 30000
        },
        'guest-uuid-2': {
          userId: 'guest-uuid-2',
          callsign: 'VIPER-PILOT',
          presenceStatus: 'online',
          isReady: true,
          joinedAt: Date.now() - 10000
        }
      };
      ui.renderMultiplayerLobbyState(mockRoomBothReady);
      ui.renderMultiplayerPresence(mockPresences);
    }
  });
  await new Promise(r => setTimeout(r, 400));
  const countdownPath = path.join(SCREENSHOT_DIR, 'mp_view_countdown.png');
  await page.screenshot({ path: countdownPath });
  fs.copyFileSync(countdownPath, path.join(ARTIFACT_DIR, 'mp_view_countdown.png'));
  console.log('Captured mp_view_countdown.png');

  // 6. Test Mobile Portrait Fullscreen Rendering (Addressing User's Image)
  await page.setViewport({ width: 412, height: 915, isMobile: true, hasTouch: true });
  // Close multiplayer modal
  await page.click('#multiplayer-modal-close');
  await new Promise(r => setTimeout(r, 300));
  await page.evaluate(() => {
    const updateDimensions = (window as any).__updateDimensions;
    if (updateDimensions) updateDimensions();
  });
  // Launch mission to check full-screen canvas behavior
  await page.click('#btn-start-game');
  await new Promise(r => setTimeout(r, 1200));
  const mobileGamePath = path.join(SCREENSHOT_DIR, 'mobile_portrait_fullscreen_fixed.png');
  await page.screenshot({ path: mobileGamePath });
  fs.copyFileSync(mobileGamePath, path.join(ARTIFACT_DIR, 'mobile_portrait_fullscreen_fixed.png'));
  console.log('Captured mobile_portrait_fullscreen_fixed.png');

  await browser.close();
  await server.close();
  console.log('Visual QA complete! All screenshots saved.');
}

runVisualQA().catch(err => {
  console.error('Visual QA failed:', err);
  process.exit(1);
});
