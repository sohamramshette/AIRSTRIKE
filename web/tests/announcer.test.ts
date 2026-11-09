import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import dialoguesData from '../src/announcer/announcer_dialogues.json';
import { AnnouncerManager } from '../src/announcer/AnnouncerManager';
import { AnnouncerSettingsManager } from '../src/announcer/AnnouncerSettingsManager';
import { DialogueEntry, DialogueCategory } from '../src/announcer/types';

describe('AIRSTRIKE Phase 10: Hinglish Meme & Bollywood Announcer Web Unit Tests', () => {

  it('loads canonical dialogue collection with >= 200 entries', () => {
    const list = dialoguesData.dialogues as DialogueEntry[];
    assert.ok(Array.isArray(list), 'dialogues should be an array');
    assert.ok(list.length >= 200, `Expected >= 200 dialogues, found ${list.length}`);
    assert.strictEqual(list.length, 292, `Expected exact count 292, found ${list.length}`);
  });

  it('satisfies and exceeds category distribution quotas', () => {
    const list = dialoguesData.dialogues as DialogueEntry[];
    const counts: Record<string, number> = {};
    for (const d of list) {
      counts[d.category] = (counts[d.category] || 0) + 1;
    }

    assert.ok((counts['DESI_MEME'] || 0) >= 48, `DESI_MEME count ${counts['DESI_MEME']} < 48`);
    assert.ok((counts['INSTAGRAM_GENZ'] || 0) >= 58, `INSTAGRAM_GENZ count ${counts['INSTAGRAM_GENZ']} < 58`);
    assert.ok((counts['BOLLYWOOD'] || 0) >= 56, `BOLLYWOOD count ${counts['BOLLYWOOD']} < 56`);
    assert.ok((counts['GENERAL_ROAST'] || 0) >= 40, `GENERAL_ROAST count ${counts['GENERAL_ROAST']} < 40`);
    assert.ok((counts['VICTORY_PRAISE'] || 0) >= 25, `VICTORY_PRAISE count ${counts['VICTORY_PRAISE']} < 25`);
    assert.ok((counts['RANDOM_INTERRUPT'] || 0) >= 25, `RANDOM_INTERRUPT count ${counts['RANDOM_INTERRUPT']} < 25`);
  });

  it('guarantees zero duplicate dialogue IDs and zero duplicate texts', () => {
    const list = dialoguesData.dialogues as DialogueEntry[];
    const ids = new Set<string>();
    const texts = new Set<string>();

    for (const d of list) {
      assert.ok(!ids.has(d.id), `Duplicate ID found: ${d.id}`);
      ids.add(d.id);

      const normText = d.text.trim().toLowerCase();
      assert.ok(!texts.has(normText), `Duplicate text found: ${d.text}`);
      texts.add(normText);
    }
  });

  it('matches all mandatory combat triggers across dialogue bank', () => {
    const list = dialoguesData.dialogues as DialogueEntry[];
    const triggers = new Set<string>();
    for (const d of list) {
      for (const t of d.triggers || []) {
        triggers.add(t);
      }
    }

    assert.ok(triggers.has('MISSED_SHOTS'), 'Missing MISSED_SHOTS trigger');
    assert.ok(triggers.has('PLAYER_DAMAGE'), 'Missing PLAYER_DAMAGE trigger');
    assert.ok(triggers.has('CRITICAL_HEALTH'), 'Missing CRITICAL_HEALTH trigger');
    assert.ok(triggers.has('BOSS_APPEARS'), 'Missing BOSS_APPEARS trigger');
    assert.ok(triggers.has('BOSS_DEFEATED'), 'Missing BOSS_DEFEATED trigger');
    assert.ok(triggers.has('KILL_STREAK'), 'Missing KILL_STREAK trigger');
    assert.ok(triggers.has('CAMPING'), 'Missing CAMPING trigger');
    assert.ok(triggers.has('SURROUNDED'), 'Missing SURROUNDED trigger');
    assert.ok(triggers.has('PLAYER_DEATH'), 'Missing PLAYER_DEATH trigger');
    assert.ok(triggers.has('GAME_OVER'), 'Missing GAME_OVER trigger');
  });

  it('manages settings defaults, category filters, and frequency correctly', () => {
    const mgr = AnnouncerSettingsManager.getInstance();
    assert.strictEqual(mgr.getSettings().enabled, true);
    assert.strictEqual(mgr.getSettings().activeCategory, 'ALL_PACKS');
    assert.strictEqual(mgr.getSettings().frequency, 'NORMAL');
    assert.strictEqual(mgr.getSettings().voiceEnabled, false);

    mgr.setActiveCategory('BOLLYWOOD');
    assert.strictEqual(mgr.getSettings().activeCategory, 'BOLLYWOOD');

    mgr.setFrequency('HIGH');
    assert.strictEqual(mgr.getSettings().frequency, 'HIGH');

    // Reset
    mgr.setActiveCategory('ALL_PACKS');
    mgr.setFrequency('NORMAL');
    mgr.setEnabled(true);
  });

  it('executes anti-repetition and priority overrides in AnnouncerManager', () => {
    const manager = AnnouncerManager.getInstance();
    manager.reset();

    const t1 = manager.trigger('CRITICAL_HEALTH', true);
    assert.strictEqual(t1, true, 'First critical health trigger should succeed');

    // Reset cooldown to test ring buffer
    manager.reset();
    const t2 = manager.trigger('CRITICAL_HEALTH', true);
    assert.strictEqual(t2, true, 'Second critical health trigger should succeed without repeating');

    // Test normal cooldown rejection
    manager.reset();
    const tNormal1 = manager.trigger('MISSED_SHOTS', false);
    assert.strictEqual(tNormal1, true, 'Normal trigger starts cooldown');

    const tNormal2 = manager.trigger('MISSED_SHOTS', false);
    assert.strictEqual(tNormal2, false, 'Subsequent normal trigger should be rejected by cooldown');
  });

  it('suppresses triggers when announcer is disabled and resumes when enabled', () => {
    const manager = AnnouncerManager.getInstance();
    const settings = AnnouncerSettingsManager.getInstance();
    manager.reset();

    settings.setEnabled(false);
    const suppressed = manager.trigger('BOSS_APPEARS', true);
    assert.strictEqual(suppressed, false, 'Trigger must be suppressed when announcer is disabled');

    settings.setEnabled(true);
    manager.reset();
    const resumed = manager.trigger('BOSS_APPEARS', true);
    assert.strictEqual(resumed, true, 'Trigger must execute when announcer is enabled');
  });
});
