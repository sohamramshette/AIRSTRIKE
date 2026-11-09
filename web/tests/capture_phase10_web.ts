import puppeteer from 'puppeteer-core';
import fs from 'node:fs';

const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
const EDGE_PATH = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';
const BASE_URL = 'http://127.0.0.1:3000/';

async function captureWebScreenshots() {
  const execPath = fs.existsSync(EDGE_PATH) ? EDGE_PATH : CHROME_PATH;
  console.log('Launching browser with:', execPath);

  const browser = await puppeteer.launch({
    executablePath: execPath,
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });

  const page = await browser.newPage();
  await page.setViewport({ width: 1024, height: 720 });
  await page.goto(BASE_URL, { waitUntil: 'networkidle0' });

  // 1. Open Profile & Settings Modal
  await page.click('#btn-open-profile');
  await new Promise(r => setTimeout(r, 600));

  await page.screenshot({
    path: 'docs/phase10_screenshots/web_announcer_settings_modal.png',
    fullPage: false
  });
  console.log('[SAVED] docs/phase10_screenshots/web_announcer_settings_modal.png');

  // Close Settings Modal
  await page.click('#profile-settings-modal-close');
  await new Promise(r => setTimeout(r, 400));

  // 2. Launch Game Mission
  await page.click('#btn-start-game');
  await new Promise(r => setTimeout(r, 800));

  // Trigger Desi Announcer Banner in browser via AnnouncerManager
  await page.evaluate(() => {
    if ((window as any).__announcerManager) {
      (window as any).__announcerManager.showBanner(
        "GHEE KHATAM! Ab teri spaceship kaise chalegi?",
        "DESI_MEME",
        true
      );
    }
  });

  // Let animation progress
  await new Promise(r => setTimeout(r, 400));

  await page.screenshot({
    path: 'docs/phase10_screenshots/web_announcer_combat_toast.png',
    fullPage: false
  });
  console.log('[SAVED] docs/phase10_screenshots/web_announcer_combat_toast.png');

  // 3. Trigger Bollywood Boss Banner
  await page.evaluate(() => {
    if ((window as any).__announcerManager) {
      (window as any).__announcerManager.showBanner(
        "Gabbar bhi teri accuracy dekh ke hans raha hai!",
        "BOLLYWOOD",
        true
      );
    }
  });
  await new Promise(r => setTimeout(r, 400));

  await page.screenshot({
    path: 'docs/phase10_screenshots/web_announcer_boss_toast.png',
    fullPage: false
  });
  console.log('[SAVED] docs/phase10_screenshots/web_announcer_boss_toast.png');

  await browser.close();
  console.log('All Web Announcer screenshots captured successfully!');
}

captureWebScreenshots().catch(err => {
  console.error('Screenshot capture failed:', err);
  process.exit(1);
});
