export class ScoreCalculator {
  private score: number = 0;
  private highScore: number = 0;
  private streak: number = 0;
  private maxStreak: number = 0;
  private comboTimer: number = 0;
  private readonly comboWindow: number = 3.5; // 3.5 seconds

  constructor() {
    this.loadHighScore();
  }

  private loadHighScore(): void {
    if (typeof localStorage !== 'undefined') {
      try {
        const saved = localStorage.getItem('airstrike_high_score');
        if (saved) {
          this.highScore = parseInt(saved, 10) || 0;
        }
      } catch {
        this.highScore = 0;
      }
    } else {
      this.highScore = 0;
    }
  }

  private saveHighScore(): void {
    if (typeof localStorage !== 'undefined') {
      try {
        localStorage.setItem('airstrike_high_score', String(this.highScore));
      } catch {}
    }
  }

  public reset(): void {
    this.score = 0;
    this.streak = 0;
    this.comboTimer = 0;
    this.loadHighScore();
  }

  public update(dt: number): void {
    if (this.comboTimer > 0) {
      this.comboTimer -= dt;
      if (this.comboTimer <= 0) {
        this.streak = 0;
        this.comboTimer = 0;
      }
    }
  }

  public addKill(baseScore: number): { addedPoints: number; multiplier: number; streak: number } {
    this.streak += 1;
    this.comboTimer = this.comboWindow;
    if (this.streak > this.maxStreak) {
      this.maxStreak = this.streak;
    }

    const multiplier = this.getMultiplier();
    const addedPoints = baseScore * multiplier;
    this.score += addedPoints;

    if (this.score > this.highScore) {
      this.highScore = this.score;
      this.saveHighScore();
    }

    return { addedPoints, multiplier, streak: this.streak };
  }

  public addBonus(points: number): void {
    this.score += points;
    if (this.score > this.highScore) {
      this.highScore = this.score;
      this.saveHighScore();
    }
  }

  public onPlayerDamaged(): void {
    // Damage breaks the active kill-streak combo
    this.streak = 0;
    this.comboTimer = 0;
  }

  public getMultiplier(): number {
    if (this.streak >= 15) return 4;
    if (this.streak >= 8) return 3;
    if (this.streak >= 3) return 2;
    return 1;
  }

  public getScore(): number {
    return this.score;
  }

  public getHighScore(): number {
    return this.highScore;
  }

  public getStreak(): number {
    return this.streak;
  }

  public getMaxStreak(): number {
    return this.maxStreak;
  }

  public getComboTimeRemaining(): number {
    return Math.max(0, this.comboTimer);
  }

  public getComboProgress(): number {
    return this.comboWindow > 0 ? Math.max(0, this.comboTimer / this.comboWindow) : 0;
  }
}
