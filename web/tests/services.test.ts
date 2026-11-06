import { test, describe } from 'node:test';
import assert from 'node:assert';
import { MatchService } from '../src/services/MatchService';
import { LeaderboardService } from '../src/services/LeaderboardService';

describe('Supabase Web Services Unit Tests', () => {
  test('LeaderboardService returns formatted offline leaderboard entries sorted descending', async () => {
    const service = LeaderboardService.getInstance();
    const result = await service.fetchLeaderboard(10);

    assert.ok(result.entries.length > 0);
    assert.strictEqual(result.entries.length, 10);
    assert.ok(result.entries[0].best_score >= result.entries[1].best_score);
    assert.strictEqual(result.entries[0].username, 'COMMANDER_APEX');
    assert.strictEqual(result.entries[0].best_score, 48500);
  });

  test('MatchService prevents duplicate consecutive submissions', async () => {
    const service = MatchService.getInstance();

    // First save as guest
    const res1 = await service.saveMatch(12500, 3, 142);
    assert.strictEqual(res1.success, true);

    // Immediate duplicate save
    const res2 = await service.saveMatch(12500, 3, 142);
    assert.strictEqual(res2.success, false);
    assert.strictEqual(res2.message, 'Match score already submitted.');
  });

  test('MatchService guest matches persist locally', () => {
    const service = MatchService.getInstance();
    const guestMatches = service.getLocalGuestMatches();
    assert.ok(Array.isArray(guestMatches));
  });
});
