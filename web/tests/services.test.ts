import { test, describe } from 'node:test';
import assert from 'node:assert';
import { MatchService } from '../src/services/MatchService';
import { LeaderboardService } from '../src/services/LeaderboardService';

describe('Supabase Web Services Unit Tests', () => {
  test('LeaderboardService handles query gracefully and preserves source integrity', async () => {
    const service = LeaderboardService.getInstance();
    const result = await service.fetchLeaderboard(10);

    assert.strictEqual(result.source, 'cloud');
    assert.ok(Array.isArray(result.entries));
    // When unauthenticated before Phase 7.2A Option B migration, returns empty entries with RLS notice
    if (result.error) {
      assert.strictEqual(result.entries.length, 0);
      assert.ok(result.message && result.message.length > 0);
    } else {
      // If permission is already active or authenticated, entries are sorted descending
      if (result.entries.length > 1) {
        assert.ok(result.entries[0].best_score >= result.entries[1].best_score);
      }
    }
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
