import puppeteer from 'puppeteer-core';
import path from 'node:path';

const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
const ARTIFACT_DIR = 'C:\\Users\\lenovo\\.gemini\\antigravity\\brain\\e548191e-70dc-46a1-ac05-79371e515d36';

async function capture() {
  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: true,
    args: ['--no-sandbox', '--disable-gpu']
  });

  const page = await browser.newPage();

  // 1. Mobile Portrait (390 x 844) - iPhone 14
  await page.setViewport({ width: 390, height: 844, isMobile: true, hasTouch: true });
  await page.goto('http://localhost:3000', { waitUntil: 'networkidle0' });

  // Capture Main Menu
  await page.screenshot({ path: path.join(ARTIFACT_DIR, 'mobile_portrait_menu.png') });

  // Open Course Outcomes Modal
  await page.click('#btn-open-subjects');
  await new Promise(r => setTimeout(r, 400));
  await page.screenshot({ path: path.join(ARTIFACT_DIR, 'mobile_portrait_modal.png') });

  // Close modal
  await page.click('#subject-co-modal-close');
  await new Promise(r => setTimeout(r, 400));

  // Directly Launch Mission
  await page.click('#btn-start-game');
  await new Promise(r => setTimeout(r, 1200));
  await page.screenshot({ path: path.join(ARTIFACT_DIR, 'mobile_portrait_gameplay.png') });

  // Now test Landscape (844 x 390)
  await page.setViewport({ width: 844, height: 390, isMobile: true, hasTouch: true });
  await new Promise(r => setTimeout(r, 800));
  await page.screenshot({ path: path.join(ARTIFACT_DIR, 'mobile_landscape_gameplay.png') });

  // Return to menu
  await page.keyboard.press('KeyM');
  await new Promise(r => setTimeout(r, 600));
  await page.screenshot({ path: path.join(ARTIFACT_DIR, 'mobile_landscape_menu.png') });

  await browser.close();
  console.log('All screenshots captured successfully!');
}

capture().catch(err => {
  console.error(err);
  process.exit(1);
});
