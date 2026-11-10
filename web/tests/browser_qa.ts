import puppeteer, { Browser, Page } from 'puppeteer-core';
import assert from 'node:assert';
import fs from 'node:fs';
import path from 'node:path';

const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
const EDGE_PATH = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';
const BASE_URL = process.env.BASE_URL || 'http://127.0.0.1:3000/';

interface QAResult {
  browser: string;
  passed: boolean;
  checks: string[];
  errors: string[];
}

async function runBrowserQA(browserName: string, executablePath: string): Promise<QAResult> {
  const result: QAResult = {
    browser: browserName,
    passed: true,
    checks: [],
    errors: []
  };

  if (!fs.existsSync(executablePath)) {
    result.errors.push(`Executable not found at ${executablePath}`);
    result.passed = false;
    return result;
  }

  let browser: Browser | null = null;

  try {
    browser = await puppeteer.launch({
      executablePath,
      headless: true,
      args: ['--no-sandbox', '--disable-gpu', '--disable-dev-shm-usage', '--window-size=1280,900']
    });

    const page: Page = await browser.newPage();
    await page.setViewport({ width: 1280, height: 900 });

    // 1. Navigation & Page Load
    await page.goto(BASE_URL, { waitUntil: 'networkidle0' });
    result.checks.push('Page loaded with networkidle0');

    // Check title and menu
    const title = await page.$eval('.game-title', el => el.textContent?.replace(/\s+/g, ' ').trim() || '');
    assert.ok(title.includes('AIRSTRIKE') || title.includes('A I R S T R I K E'), `Title should contain AIRSTRIKE, got: ${title}`);
    result.checks.push(`Main Menu Title verified: ${title}`);

    const subtitle = await page.$eval('.game-subtitle', el => el.textContent?.trim() || '');
    assert.ok(subtitle.length > 0, 'Subtitle should be populated');
    result.checks.push(`Main Menu Subtitle verified: ${subtitle}`);

    const pilotStatus = await page.$eval('#pilot-callsign-display', el => el.textContent?.trim() || '');
    assert.ok(pilotStatus.includes('GUEST') || pilotStatus.includes('CADET'), `Pilot status should indicate guest/cadet, got: ${pilotStatus}`);
    result.checks.push(`Pilot Status verified: ${pilotStatus}`);

    for (const viewport of [{ width: 390, height: 844 }, { width: 320, height: 568 }]) {
      await page.setViewport(viewport);
      const mobileMenu = await page.evaluate(() => {
        const stage = document.querySelector('#viewport-container')!.getBoundingClientRect();
        const buttons = [...document.querySelectorAll('#main-menu-overlay button')];
        const footer = document.querySelector('.academic-footer-bar')!.getBoundingClientRect();
        const hints = document.querySelector('.key-hint')!.getBoundingClientRect();
        return {
          stageHeight: stage.height,
          viewportHeight: window.innerHeight,
          buttonCount: buttons.length,
          buttonsInView: buttons.every(button => {
            const rect = button.getBoundingClientRect();
            return rect.width > 0 && rect.height > 0 && rect.top >= 0 && rect.bottom <= window.innerHeight;
          }),
          footerVisible: footer.width > 0 && footer.height > 0 && footer.bottom <= window.innerHeight,
          hintsVisible: hints.width > 0 && hints.height > 0 && hints.bottom <= window.innerHeight,
          horizontalOverflow: document.documentElement.scrollWidth > window.innerWidth
        };
      });

      assert.ok(Math.abs(mobileMenu.stageHeight - mobileMenu.viewportHeight) < 1, 'Mobile landing page should fill the portrait viewport');
      assert.strictEqual(mobileMenu.buttonCount, 7, 'All landing-page buttons should remain visible');
      assert.ok(mobileMenu.buttonsInView, 'Every landing-page button should fit within the mobile viewport');
      assert.ok(mobileMenu.footerVisible, 'Academic suite and record footer should remain visible');
      assert.ok(mobileMenu.hintsVisible, 'Control hints should remain visible');
      assert.strictEqual(mobileMenu.horizontalOverflow, false, 'Mobile landing page should not overflow horizontally');
      result.checks.push(`Mobile landing page verified at ${viewport.width}x${viewport.height}: all actions, footer, and hints visible`);
    }

    await page.setViewport({ width: 1920, height: 1080 });
    const fullScreenMenu = await page.evaluate(() => {
      const stage = document.querySelector('#viewport-container')!.getBoundingClientRect();
      const card = document.querySelector('.main-menu-card')!.getBoundingClientRect();
      const textElements = [
        ...document.querySelectorAll<HTMLElement>(
          '#main-menu-overlay .game-title, #main-menu-overlay .game-title-text, ' +
          '#main-menu-overlay .game-subtitle, #main-menu-overlay .cyber-btn, ' +
          '.academic-footer-bar, .key-hint'
        )
      ];
      return {
        stageFitsScreen: stage.width <= window.innerWidth && stage.height <= window.innerHeight,
        cardWidth: card.width,
        cardCentered: Math.abs((card.left + card.right) / 2 - (stage.left + stage.right) / 2) < 1,
        cardFitsStage: card.left >= stage.left && card.right <= stage.right &&
          card.top >= stage.top && card.bottom <= stage.bottom,
        textOverflows: textElements
          .filter(element => element.scrollWidth > element.clientWidth + 1)
          .map(element => element.className || element.tagName),
        lineHeightsDefined: textElements.every(element => getComputedStyle(element).lineHeight !== 'normal')
      };
    });
    assert.ok(fullScreenMenu.stageFitsScreen, 'Full-screen game stage should remain within the display');
    assert.ok(fullScreenMenu.cardWidth > 430, 'Main menu should scale up for a full-screen display');
    assert.ok(fullScreenMenu.cardCentered, 'Full-screen menu should remain centered in the game stage');
    assert.ok(fullScreenMenu.cardFitsStage, 'Full-screen menu should fit inside the game stage');
    assert.deepStrictEqual(fullScreenMenu.textOverflows, [], 'Full-screen labels should not overflow their text containers');
    assert.ok(fullScreenMenu.lineHeightsDefined, 'Full-screen labels should use consistent line spacing');
    result.checks.push('Full-screen 1920x1080 layout verified: scaled, centered menu with aligned text and no overflow');

    // Mobile Portrait Launch & Aspect Ratio Integrity
    await page.setViewport({ width: 390, height: 844, isMobile: true, hasTouch: true });
    await page.click('#btn-start-game');
    await page.waitForFunction(() => (window as any).__airstrikeEngine?.state === 'PLAYING', { timeout: 3000 });
    result.checks.push('Mobile direct launch verified: game starts without intrusive modal');

    // Verify Mobile Fullscreen Canvas without Distortion
    const portraitCanvas = await page.evaluate(() => {
      const canvasEl = document.querySelector('#game-canvas') as HTMLCanvasElement;
      const rect = canvasEl.getBoundingClientRect();
      const touchControls = document.querySelector('#touch-controls')!;
      const dpad = document.querySelector('.touch-dpad')!.getBoundingClientRect();
      const fire = document.querySelector('#touch-fire')!.getBoundingClientRect();
      return {
        canvasCssWidth: rect.width,
        canvasCssHeight: rect.height,
        bufferWidth: canvasEl.width,
        bufferHeight: canvasEl.height,
        pixelRatioX: canvasEl.width / rect.width,
        pixelRatioY: canvasEl.height / rect.height,
        fillsScreen: Math.abs(rect.width - window.innerWidth) < 2 && Math.abs(rect.height - window.innerHeight) < 2,
        touchVisible: getComputedStyle(touchControls).display !== 'none',
        dpadRendered: dpad.width > 0 && dpad.height > 0,
        fireRendered: fire.width > 0 && fire.height > 0
      };
    });

    assert.ok(portraitCanvas.fillsScreen, 'Mobile portrait canvas should fill the entire screen (no wasted space)');
    assert.ok(Math.abs(portraitCanvas.pixelRatioX - portraitCanvas.pixelRatioY) < 0.01,
      'Canvas pixel aspect ratio must be 1:1 to guarantee zero stretching or distortion');
    assert.ok(portraitCanvas.touchVisible, 'Touch controls must be visible during mobile gameplay');
    assert.ok(portraitCanvas.dpadRendered, 'D-pad must be rendered on mobile');
    assert.ok(portraitCanvas.fireRendered, 'Virtual FIRE button must be rendered on mobile');
    result.checks.push('Portrait mobile gameplay verified: full-screen immersion, 1:1 pixel fidelity, and active touch deck');

    // Test Touch Fire Interaction
    const fireBtn = await page.$('#touch-fire');
    assert.ok(fireBtn, 'Virtual FIRE button must exist');
    await fireBtn!.evaluate(el => el.dispatchEvent(new TouchEvent('touchstart', { bubbles: true })));
    assert.strictEqual(
      await page.evaluate(() => (window as any).__airstrikeEngine.inputManager.isFiring()),
      true,
      'Pressing virtual FIRE button should trigger weapon discharge'
    );
    await fireBtn!.evaluate(el => el.dispatchEvent(new TouchEvent('touchend', { bubbles: true })));

    // Return to menu
    await page.evaluate(() => (window as any).__airstrikeEngine.returnToMenu());
    await page.waitForFunction(() => (window as any).__airstrikeEngine?.state === 'MENU', { timeout: 2000 });
    result.checks.push('Mobile return to menu verified');

    await page.setViewport({ width: 1280, height: 900 });

    // Phase 9 Modals: Subject & Course Outcomes Modal (Triggered via Settings / Specs)
    await page.evaluate(() => document.getElementById('btn-open-subjects')?.click());
    await page.waitForSelector('#subject-co-modal:not(.hidden)', { timeout: 2000 });
    const coCardCount = await page.$$eval('#co-cards-list .co-card', cards => cards.length);
    assert.ok(coCardCount >= 4, `CO cards should be populated (got ${coCardCount})`);
    result.checks.push(`Course Outcomes modal verified: ${coCardCount} PSOOP cards active`);

    // Switch to CGL tab in modal
    await page.evaluate(() => document.getElementById('tab-co-cgl')?.click());
    await page.waitForFunction(() => {
      const cards = document.querySelectorAll('#co-cards-list .co-card');
      return cards.length === 6;
    }, { timeout: 3000 });
    const cglCardCount = await page.$$eval('#co-cards-list .co-card', cards => cards.length);
    assert.strictEqual(cglCardCount, 6, `CGL tab should display 6 cards, got ${cglCardCount}`);
    result.checks.push(`Course Outcomes CGL tab verified: 6 CGL cards active`);

    await page.evaluate(() => document.getElementById('subject-co-modal-close')?.click());
    await page.waitForSelector('#subject-co-modal.hidden', { timeout: 2000 });
    result.checks.push('Subject & CO modal closed cleanly');

    // Phase 9 Modals: Pilot Dossier & Settings Modal
    await page.click('#btn-open-profile');
    await page.waitForSelector('#profile-settings-modal:not(.hidden)', { timeout: 2000 });
    const dossierCallsign = await page.$eval('#dossier-callsign', el => el.textContent?.trim() || '');
    assert.ok(dossierCallsign.length > 0, 'Dossier callsign should be present');
    result.checks.push(`Pilot Dossier & Settings modal verified: ${dossierCallsign}`);

    await page.evaluate(() => document.getElementById('profile-settings-modal-close')?.click());
    await page.waitForSelector('#profile-settings-modal.hidden', { timeout: 2000 });
    result.checks.push('Pilot Dossier & Settings modal closed cleanly');

    // Phase 9 Modals: Pilot Manual / Instructions Modal
    await page.click('#btn-open-instructions');
    await page.waitForSelector('#instructions-modal:not(.hidden)', { timeout: 2000 });
    result.checks.push('Pilot Flight Manual modal verified');

    await page.evaluate(() => document.getElementById('instructions-modal-close')?.click());
    await page.waitForSelector('#instructions-modal.hidden', { timeout: 2000 });
    result.checks.push('Pilot Flight Manual modal closed cleanly');

    // 2. Modals Verification: Leaderboard
    await page.click('#btn-open-leaderboard');
    await page.waitForSelector('#leaderboard-modal:not(.hidden)', { timeout: 3000 });
    result.checks.push('Leaderboard modal opened');

    // Wait for rows
    await page.waitForFunction(() => {
      const rows = document.querySelectorAll('#leaderboard-table-body tr');
      return rows.length > 0 && !rows[0].textContent?.includes('Fetching');
    }, { timeout: 4000 });

    const rowCount = await page.$$eval('#leaderboard-table-body tr', rows => rows.length);
    assert.ok(rowCount > 0, 'Leaderboard rows should be populated');
    result.checks.push(`Leaderboard populated with ${rowCount} entries`);

    await page.evaluate(() => document.getElementById('leaderboard-modal-close')?.click());
    await page.waitForSelector('#leaderboard-modal.hidden', { timeout: 2000 });
    result.checks.push('Leaderboard modal closed cleanly');

    // 3. Modals Verification: Flight Log History
    await page.click('#btn-open-history');
    await page.waitForSelector('#history-modal:not(.hidden)', { timeout: 2000 });
    result.checks.push('Flight Logs modal opened');
    await page.evaluate(() => document.getElementById('history-modal-close')?.click());
    await page.waitForSelector('#history-modal.hidden', { timeout: 2000 });
    result.checks.push('Flight Logs modal closed cleanly');

    // 4. Modals Verification: Auth Modal (Tabs & Form)
    await page.click('#auth-action-btn');
    await page.waitForSelector('#auth-modal:not(.hidden)', { timeout: 2000 });
    await new Promise(r => setTimeout(r, 300));
    result.checks.push('Auth modal opened');

    // Test tab switching
    await page.click('#tab-register');
    await page.waitForFunction(() => {
      const el = document.getElementById('field-callsign-group');
      return el && el.style.display !== 'none';
    }, { timeout: 2000 });
    result.checks.push('Registration tab toggles callsign field properly');

    await page.click('#tab-login');
    await page.waitForFunction(() => {
      const el = document.getElementById('field-callsign-group');
      return el && el.style.display === 'none';
    }, { timeout: 2000 });
    result.checks.push('Login tab restores single pilot authentication mode');

    await page.evaluate(() => document.getElementById('auth-modal-close')?.click());
    await page.waitForSelector('#auth-modal.hidden', { timeout: 2000 });
    result.checks.push('Auth modal closed cleanly');

    // 5. Mission Launch & Gameplay Mechanics
    await page.click('#btn-start-game');
    await page.waitForSelector('#main-menu-overlay.hidden', { timeout: 2000 });
    result.checks.push('Mission launched: Main Menu dismissed');

    // Verify engine state via QA hook
    const gameState = await page.evaluate(() => (window as any).__airstrikeEngine?.state);
    assert.strictEqual(gameState, 'PLAYING');
    result.checks.push('GameEngine state verified as PLAYING');

    // Check initial player parameters
    const initPos = await page.evaluate(() => {
      const p = (window as any).__airstrikeEngine?.player;
      return { x: p?.x, y: p?.y, health: p?.health, shield: p?.shield, lives: p?.lives };
    });
    assert.strictEqual(initPos.x, 376);
    assert.strictEqual(initPos.y, 540);
    assert.strictEqual(initPos.health, 100);
    assert.strictEqual(initPos.shield, 100);
    assert.strictEqual(initPos.lives, 3);
    result.checks.push(`Player initial vitals verified: Pos(${initPos.x}, ${initPos.y}), Hull=${initPos.health}, Shield=${initPos.shield}, Lives=${initPos.lives}`);

    // 6. Player Movement Verification
    await page.keyboard.down('ArrowLeft');
    await new Promise(r => setTimeout(r, 200));
    await page.keyboard.up('ArrowLeft');

    const posXAfterLeft = await page.evaluate(() => (window as any).__airstrikeEngine?.player?.x);
    assert.ok(posXAfterLeft < 376, `Player should move left (was 376, now ${posXAfterLeft})`);
    result.checks.push(`Player movement Left verified: x moved from 376 to ${posXAfterLeft.toFixed(1)}`);

    await page.keyboard.down('ArrowRight');
    await new Promise(r => setTimeout(r, 350));
    await page.keyboard.up('ArrowRight');

    const posXAfterRight = await page.evaluate(() => (window as any).__airstrikeEngine?.player?.x);
    assert.ok(posXAfterRight > posXAfterLeft, `Player should move right`);
    result.checks.push(`Player movement Right verified: x moved to ${posXAfterRight.toFixed(1)}`);

    // 7. Weapon Firing & Projectiles
    await page.keyboard.down('Space');
    await new Promise(r => setTimeout(r, 250));
    await page.keyboard.up('Space');

    const bulletCount = await page.evaluate(() => (window as any).__airstrikeEngine?.projectiles?.length);
    assert.ok(bulletCount > 0, 'Projectiles should be spawned on firing');
    result.checks.push(`Weapon system verified: ${bulletCount} projectile(s) active on playfield`);

    // 8. Enemy Waves & Spawning
    await page.waitForFunction(() => ((window as any).__airstrikeEngine?.enemies?.length || 0) > 0, { timeout: 4000 });
    const enemyCount = await page.evaluate(() => (window as any).__airstrikeEngine?.enemies?.length);
    assert.ok(enemyCount > 0, 'Hostile enemies should spawn in active wave');
    result.checks.push(`Hostile Fleet Spawning verified: ${enemyCount} hostile(s) engaged`);

    // 9. Tactical Pause & Resume
    await page.keyboard.press('p');
    await page.waitForFunction(() => (window as any).__airstrikeEngine?.state === 'PAUSED', { timeout: 2000 });
    const pausedState = await page.evaluate(() => (window as any).__airstrikeEngine?.state);
    assert.strictEqual(pausedState, 'PAUSED');
    await page.waitForSelector('#pause-overlay:not(.hidden)', { timeout: 2000 });
    result.checks.push('Tactical Pause verified: overlay displayed, engine suspended');

    await page.click('#btn-resume-game');
    const resumedState = await page.evaluate(() => (window as any).__airstrikeEngine?.state);
    assert.strictEqual(resumedState, 'PLAYING');
    await page.waitForSelector('#pause-overlay.hidden', { timeout: 2000 });
    result.checks.push('Tactical Resume verified: engine active, overlay dismissed');

    // 10. Responsive Viewport Tests
    // Test A: Laptop resolution (1366x768)
    await page.setViewport({ width: 1366, height: 768 });
    const canvasBoxLaptop = await page.$eval('#viewport-container', el => {
      const rect = el.getBoundingClientRect();
      return { width: rect.width, height: rect.height };
    });
    assert.ok(canvasBoxLaptop.width > 0 && canvasBoxLaptop.height > 0);
    result.checks.push(`Laptop Viewport (1366x768) verified: Canvas box ${canvasBoxLaptop.width}x${canvasBoxLaptop.height}`);

    // Test B: Tablet resolution (768x1024)
    await page.setViewport({ width: 768, height: 1024 });
    const isTouchControlsPresentTablet = await page.$eval('#touch-controls', el => {
      return window.getComputedStyle(el).display !== 'none';
    });
    result.checks.push(`Tablet Viewport (768x1024) verified: Touch controls display=${isTouchControlsPresentTablet}`);

    // Test C: Mobile resolution (375x667)
    await page.setViewport({ width: 375, height: 667 });
    const isTouchControlsPresentMobile = await page.$eval('#touch-controls', el => {
      return window.getComputedStyle(el).display !== 'none';
    });
    assert.strictEqual(isTouchControlsPresentMobile, true, 'Touch controls must display on mobile');
    result.checks.push('Mobile Viewport (375x667) verified: Virtual D-Pad & Fire zone active');

    // 11. Virtual Touch Controls Interaction
    const touchFireExists = await page.$('#touch-fire');
    assert.ok(touchFireExists !== null, 'Virtual FIRE button present');
    await page.tap('#touch-fire');
    result.checks.push('Virtual touch button interactive on mobile display');

    // 12. Reset Viewport & Test Game Over Sequence
    await page.setViewport({ width: 1280, height: 900 });
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
    const gameoverState = await page.evaluate(() => (window as any).__airstrikeEngine?.state);
    assert.strictEqual(gameoverState, 'GAME_OVER');
    result.checks.push('Game Over Sequence verified: Mission debrief displayed');

    // Test Deploy Again (Restart)
    await page.click('#btn-play-again');
    await page.waitForSelector('#gameover-overlay.hidden', { timeout: 2000 });
    const restartedState = await page.evaluate(() => (window as any).__airstrikeEngine?.state);
    assert.strictEqual(restartedState, 'PLAYING');
    result.checks.push('Deploy Again verified: Fresh mission initialized');

    // Audio Mute toggle test
    const soundToggle = await page.$('#btn-sound-toggle');
    assert.ok(soundToggle !== null);
    await page.click('#btn-sound-toggle');
    const isMuted = await page.evaluate(() => (window as any).__airstrikeEngine?.soundService?.isMuted());
    assert.strictEqual(isMuted, true);
    result.checks.push('Audio Mute toggled successfully');

  } catch (err: unknown) {
    result.passed = false;
    const msg = err instanceof Error ? err.message : String(err);
    result.errors.push(msg);
  } finally {
    if (browser) {
      await browser.close();
    }
  }

  return result;
}

async function main() {
  console.log('==============================================================================');
  console.log('       AIRSTRIKE PHASE 7.2C.1 — REAL BROWSER QA VERIFICATION SUITE');
  console.log('==============================================================================\n');

  console.log('[1/2] Executing Browser QA on Google Chrome...');
  const chromeResult = await runBrowserQA('Google Chrome', CHROME_PATH);

  console.log(`\n--- Chrome Results ---`);
  chromeResult.checks.forEach(c => console.log(`  [PASS] ${c}`));
  if (chromeResult.errors.length > 0) {
    chromeResult.errors.forEach(e => console.log(`  [FAIL] ${e}`));
  }
  console.log(`Chrome Status: ${chromeResult.passed ? 'PASSED (100%)' : 'FAILED'}\n`);

  console.log('[2/2] Executing Browser QA on Microsoft Edge...');
  const edgeResult = await runBrowserQA('Microsoft Edge', EDGE_PATH);

  console.log(`\n--- Edge Results ---`);
  edgeResult.checks.forEach(c => console.log(`  [PASS] ${c}`));
  if (edgeResult.errors.length > 0) {
    edgeResult.errors.forEach(e => console.log(`  [FAIL] ${e}`));
  }
  console.log(`Edge Status: ${edgeResult.passed ? 'PASSED (100%)' : 'FAILED'}\n`);

  if (!chromeResult.passed || !edgeResult.passed) {
    process.exit(1);
  }
  console.log('==============================================================================');
  console.log('       ALL BROWSER QA CHECKS PASSED ON BOTH CHROME & EDGE!');
  console.log('==============================================================================');
}

main();
