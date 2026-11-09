import puppeteer, { Browser, Page } from 'puppeteer-core';
import fs from 'node:fs';
import path from 'node:path';

const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
const BASE_URL = 'http://127.0.0.1:4173/';
const OUTPUT_DIR = path.resolve('..', 'docs', 'phase9_screenshots');

async function captureScreen(page: Page, filename: string): Promise<void> {
  const targetPath = path.join(OUTPUT_DIR, filename);
  await page.screenshot({ path: targetPath, fullPage: false });
  console.log(`  [SAVED] ${filename}`);
}

async function main(): Promise<void> {
  console.log('[PHASE 9] Generating Browser Screenshot Evidence...');

  if (!fs.existsSync(OUTPUT_DIR)) {
    fs.mkdirSync(OUTPUT_DIR, { recursive: true });
  }

  const browser: Browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: true,
    args: ['--no-sandbox', '--disable-gpu', '--disable-dev-shm-usage', '--window-size=1280,900']
  });

  try {
    const page: Page = await browser.newPage();
    await page.setViewport({ width: 1280, height: 720 });
    await page.goto(BASE_URL, { waitUntil: 'networkidle0' });

    // 1. Main Menu at standard resolution
    console.log('[SCREENSHOT 1/10] Main Menu...');
    await captureScreen(page, 'web_main_menu.png');

    // Multi-resolution main menu captures
    await page.setViewport({ width: 1280, height: 720 });
    await captureScreen(page, 'web_main_menu_1280x720.png');

    await page.setViewport({ width: 1366, height: 768 });
    await captureScreen(page, 'web_main_menu_1366x768.png');

    await page.setViewport({ width: 1920, height: 1080 });
    await captureScreen(page, 'web_main_menu_1920x1080.png');

    // Reset to 1280x720 for modals & gameplay
    await page.setViewport({ width: 1280, height: 720 });

    // 2. Subject & Course Outcomes Modal
    console.log('[SCREENSHOT 2/10] Subject & Course Outcomes Modal...');
    await page.click('#btn-open-subjects');
    await page.waitForSelector('#subject-co-modal:not(.hidden)', { timeout: 2000 });
    await new Promise(r => setTimeout(r, 200));
    await captureScreen(page, 'web_subject_co.png');
    await page.evaluate(() => document.getElementById('subject-co-modal-close')?.click());
    await page.waitForSelector('#subject-co-modal.hidden', { timeout: 2000 });

    // 3. Pilot Dossier & Settings Modal
    console.log('[SCREENSHOT 3/10] Pilot Dossier & Settings Modal...');
    await page.click('#btn-open-profile');
    await page.waitForSelector('#profile-settings-modal:not(.hidden)', { timeout: 2000 });
    await new Promise(r => setTimeout(r, 200));
    await captureScreen(page, 'web_profile_settings.png');
    await page.evaluate(() => document.getElementById('profile-settings-modal-close')?.click());
    await page.waitForSelector('#profile-settings-modal.hidden', { timeout: 2000 });

    // 4. Pilot Authentication Modal
    console.log('[SCREENSHOT 4/10] Pilot Authentication Modal...');
    await page.click('#auth-action-btn');
    await page.waitForSelector('#auth-modal:not(.hidden)', { timeout: 2000 });
    await new Promise(r => setTimeout(r, 200));
    await captureScreen(page, 'web_auth.png');
    await page.evaluate(() => document.getElementById('auth-modal-close')?.click());
    await page.waitForSelector('#auth-modal.hidden', { timeout: 2000 });

    // 5. Global Leaderboard Modal
    console.log('[SCREENSHOT 5/10] Global Leaderboard Modal...');
    await page.click('#btn-open-leaderboard');
    await page.waitForSelector('#leaderboard-modal:not(.hidden)', { timeout: 2000 });
    await new Promise(r => setTimeout(r, 400));
    await captureScreen(page, 'web_leaderboard.png');
    await page.evaluate(() => document.getElementById('leaderboard-modal-close')?.click());
    await page.waitForSelector('#leaderboard-modal.hidden', { timeout: 2000 });

    // 6. Match History Modal
    console.log('[SCREENSHOT 6/10] Match History Modal...');
    await page.click('#btn-open-history');
    await page.waitForSelector('#history-modal:not(.hidden)', { timeout: 2000 });
    await new Promise(r => setTimeout(r, 300));
    await captureScreen(page, 'web_match_history.png');
    await page.evaluate(() => document.getElementById('history-modal-close')?.click());
    await page.waitForSelector('#history-modal.hidden', { timeout: 2000 });

    // 7. Pilot Flight Manual / Instructions Modal
    console.log('[SCREENSHOT 7/10] Pilot Flight Manual Modal...');
    await page.click('#btn-open-instructions');
    await page.waitForSelector('#instructions-modal:not(.hidden)', { timeout: 2000 });
    await new Promise(r => setTimeout(r, 200));
    await captureScreen(page, 'web_instructions.png');
    await page.evaluate(() => document.getElementById('instructions-modal-close')?.click());
    await page.waitForSelector('#instructions-modal.hidden', { timeout: 2000 });

    // 8. Launch Mission & Active Gameplay HUD
    console.log('[SCREENSHOT 8/10] Active Gameplay HUD...');
    await page.click('#btn-start-game');
    await page.waitForSelector('#main-menu-overlay.hidden', { timeout: 2000 });

    // Fire weapon and wait for enemies
    await page.keyboard.down('Space');
    await new Promise(r => setTimeout(r, 300));
    await page.keyboard.up('Space');
    await page.waitForFunction(() => ((window as any).__airstrikeEngine?.enemies?.length || 0) > 0, { timeout: 3000 });
    await new Promise(r => setTimeout(r, 400));
    await captureScreen(page, 'web_hud.png');

    // 9. Tactical Pause Screen
    console.log('[SCREENSHOT 9/10] Tactical Pause Screen...');
    await page.keyboard.press('p');
    await page.waitForSelector('#pause-overlay:not(.hidden)', { timeout: 2000 });
    await new Promise(r => setTimeout(r, 200));
    await captureScreen(page, 'web_pause.png');

    // Resume
    await page.click('#btn-resume-game');
    await page.waitForSelector('#pause-overlay.hidden', { timeout: 2000 });

    // 10. Game Over Screen
    console.log('[SCREENSHOT 10/10] Game Over Screen...');
    await page.evaluate(() => {
      const eng = (window as any).__airstrikeEngine;
      if (eng?.player) {
        eng.player.shield = 0;
        eng.player.health = 0;
        eng.player.lives = 0;
        eng.player.active = false;
        eng.player.takeDamage(100);
      }
    });
    await page.waitForSelector('#gameover-overlay:not(.hidden)', { timeout: 3000 });
    await new Promise(r => setTimeout(r, 300));
    await captureScreen(page, 'web_game_over.png');

    console.log('[SUCCESS] All 10 Web evidence screenshots captured successfully!');
  } finally {
    await browser.close();
  }
}

main().catch(err => {
  console.error('[ERROR] Failed capturing browser screenshots:', err);
  process.exit(1);
});
