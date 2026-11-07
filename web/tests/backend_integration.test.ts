import { test, describe } from 'node:test';
import assert from 'node:assert';
import { AuthService } from '../src/services/AuthService';
import { MatchService } from '../src/services/MatchService';
import { LeaderboardService } from '../src/services/LeaderboardService';

describe('Supabase Backend & Authentication Integration Tests', () => {
  test('Leaderboard captures Supabase 401/42501 permission error without fabricating mock records', async () => {
    const auth = AuthService.getInstance();
    await auth.signOut(); // Ensure unauthenticated guest

    const leaderboardService = LeaderboardService.getInstance();
    const result = await leaderboardService.fetchLeaderboard(10);

    assert.strictEqual(result.source, 'cloud');
    // Since Phase 7.2A Option B migration is pending approval, anonymous query returns 401/42501
    assert.strictEqual(result.error, true);
    assert.strictEqual(result.entries.length, 0);
    assert.ok(
      result.message?.includes('Phase 7.2A') || result.message?.includes('permission') || result.message?.includes('grant'),
      `Expected informative RLS notice, got: ${result.message}`
    );
  });

  test('Auth validation rejects invalid email and weak passwords before network call', async () => {
    const auth = AuthService.getInstance();

    const emptyRes = await auth.signUp('', '', '');
    assert.strictEqual(emptyRes.success, false);
    assert.ok(emptyRes.message.includes('required'));

    const weakPassRes = await auth.signUp('pilot@academy.edu', '123', 'VIPER');
    assert.strictEqual(weakPassRes.success, false);
    assert.ok(weakPassRes.message.includes('6 characters'));
  });

  test('Auth login with invalid credentials parses Supabase error response correctly', async () => {
    const auth = AuthService.getInstance();
    const res = await auth.signIn('nonexistent_cadet_9999@test.com', 'wrongpassword123');

    assert.strictEqual(res.success, false);
    assert.ok(res.message.length > 0);
    // Supabase returns "Invalid login credentials" or similar HTTP 400
    assert.ok(
      res.message.toLowerCase().includes('invalid') || res.message.toLowerCase().includes('credentials') || res.message.toLowerCase().includes('confirm'),
      `Expected auth failure message, got: ${res.message}`
    );
  });

  test('Guest matches are strictly isolated and never uploaded to Supabase', async () => {
    const auth = AuthService.getInstance();
    await auth.signOut(); // Ensure guest
    assert.strictEqual(auth.isAuthenticated(), false);

    const matchService = MatchService.getInstance();
    const res = await matchService.saveMatch(999999, 10, 300);

    assert.strictEqual(res.success, true);
    assert.ok(res.message.includes('locally') || res.message.includes('Guest'));

    const guestMatches = matchService.getLocalGuestMatches();
    assert.ok(guestMatches.length > 0);
    assert.strictEqual(guestMatches[0].user_id, 'guest');
    assert.strictEqual(guestMatches[0].score, 999999);
  });

  test('Match submission guards against duplicate concurrent submissions', async () => {
    const matchService = MatchService.getInstance();
    // Re-saving exact same match
    const dupRes = await matchService.saveMatch(999999, 10, 300);
    assert.strictEqual(dupRes.success, false);
    assert.strictEqual(dupRes.message, 'Match score already submitted.');
  });

  test('Callsign resolution falls back to formatted email username when profile record is uncreated', async () => {
    const auth = AuthService.getInstance();
    const callsign = await auth.resolveCallsign('uuid-0000', 'star.viper@galaxy.org');
    assert.strictEqual(callsign, 'STAR.VIPER');
  });

  test('Callsign update rejects unauthenticated pilot and empty input', async () => {
    const auth = AuthService.getInstance();
    await auth.signOut();

    const emptyRes = await auth.updateCallsign('');
    assert.strictEqual(emptyRes.success, false);
    assert.strictEqual(emptyRes.message, 'Pilot not authenticated.');

    const validNameRes = await auth.updateCallsign('APOLLO');
    assert.strictEqual(validNameRes.success, false);
    assert.strictEqual(validNameRes.message, 'Pilot not authenticated.');
  });

  test('Session state clears completely on logout', async () => {
    const auth = AuthService.getInstance();
    await auth.signOut();
    assert.strictEqual(auth.getSession(), null);
    assert.strictEqual(auth.isAuthenticated(), false);
  });
});
