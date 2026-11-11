// ==============================================================================
// AIRSTRIKE Phase 12: Multiplayer Input Validation & Code Sanitization
// FILE: web/src/services/multiplayer/MultiplayerValidation.ts
// ==============================================================================

export class MultiplayerValidation {
  private static readonly ROOM_CODE_REGEX = /^[A-Z0-9]{6}$/;
  private static readonly UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

  /**
   * Sanitizes and normalizes a candidate room code string.
   */
  public static sanitizeRoomCode(rawCode: string | null | undefined): string {
    if (!rawCode) return '';
    return rawCode.trim().toUpperCase().replace(/[^A-Z0-9]/g, '');
  }

  /**
   * Validates whether a normalized string matches the 6-character room code format.
   */
  public static isValidRoomCode(code: string): boolean {
    const sanitized = this.sanitizeRoomCode(code);
    return this.ROOM_CODE_REGEX.test(sanitized);
  }

  /**
   * Validates whether an ID matches standard UUID v4 format.
   */
  public static isValidUUID(id: string): boolean {
    if (!id || typeof id !== 'string') return false;
    return this.UUID_REGEX.test(id.trim());
  }

  /**
   * Validates score bounds during combat telemetry.
   */
  public static isScoreWithinPhysicalBounds(score: number, durationSeconds: number): boolean {
    if (score < 0 || durationSeconds <= 0) return false;
    const maxTheoretical = durationSeconds * 450 + 2000;
    return score <= maxTheoretical;
  }
}

