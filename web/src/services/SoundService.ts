export class SoundService {
  private static instance: SoundService;
  private ctx: AudioContext | null = null;
  private muted: boolean = false;
  private musicMuted: boolean = false;
  private bgm: HTMLAudioElement | null = null;
  private musicStarted: boolean = false;

  private constructor() {
    if (typeof localStorage !== 'undefined') {
      try {
        const saved = localStorage.getItem('airstrike_sound_muted');
        if (saved !== null) {
          this.muted = saved === 'true';
        }
        const savedMusic = localStorage.getItem('airstrike_music_muted');
        if (savedMusic !== null) {
          this.musicMuted = savedMusic === 'true';
        }
      } catch {}
    }
    this.initBGM();
  }

  public static getInstance(): SoundService {
    if (!SoundService.instance) {
      SoundService.instance = new SoundService();
    }
    return SoundService.instance;
  }

  private initBGM(): void {
    if (typeof Audio === 'undefined') return;
    try {
      this.bgm = new Audio('/audio/bgm.mp3');
      this.bgm.loop = true;
      this.bgm.volume = 0.45;
      this.bgm.preload = 'auto';

      // One-time interaction trigger to comply with modern browser autoplay policies
      if (typeof window !== 'undefined') {
        const unlockAudio = () => {
          if (!this.musicStarted && !this.musicMuted && !this.muted) {
            this.startMusic();
          }
          window.removeEventListener('click', unlockAudio);
          window.removeEventListener('keydown', unlockAudio);
          window.removeEventListener('touchstart', unlockAudio);
          window.removeEventListener('pointerdown', unlockAudio);
        };
        window.addEventListener('click', unlockAudio, { passive: true });
        window.addEventListener('keydown', unlockAudio, { passive: true });
        window.addEventListener('touchstart', unlockAudio, { passive: true });
        window.addEventListener('pointerdown', unlockAudio, { passive: true });
      }
    } catch {}
  }

  public startMusic(): void {
    if (this.muted || this.musicMuted || !this.bgm) return;
    try {
      const p = this.bgm.play();
      if (p !== undefined) {
        p.then(() => {
          this.musicStarted = true;
        }).catch(() => {
          // Handled upon subsequent user gesture
        });
      }
    } catch {}
  }

  public pauseMusic(): void {
    if (this.bgm && !this.bgm.paused) {
      try {
        this.bgm.pause();
      } catch {}
    }
  }

  public resumeMusic(): void {
    if (this.muted || this.musicMuted) return;
    if (this.bgm && this.bgm.paused) {
      try {
        this.bgm.play().catch(() => {});
      } catch {}
    }
  }

  public toggleMusic(): boolean {
    this.musicMuted = !this.musicMuted;
    try {
      if (typeof localStorage !== 'undefined') {
        localStorage.setItem('airstrike_music_muted', String(this.musicMuted));
      }
    } catch {}

    if (this.musicMuted) {
      this.pauseMusic();
    } else {
      this.startMusic();
    }
    return this.musicMuted;
  }

  public setMusicMuted(muted: boolean): void {
    this.musicMuted = muted;
    try {
      if (typeof localStorage !== 'undefined') {
        localStorage.setItem('airstrike_music_muted', String(this.musicMuted));
      }
    } catch {}

    if (this.musicMuted) {
      this.pauseMusic();
    } else {
      this.startMusic();
    }
  }

  public isMusicMuted(): boolean {
    return this.musicMuted;
  }

  private initCtx(): AudioContext | null {
    if (!this.ctx && typeof window !== 'undefined') {
      const AudioCtx = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
      if (AudioCtx) {
        this.ctx = new AudioCtx();
      }
    }
    if (this.ctx && this.ctx.state === 'suspended') {
      this.ctx.resume().catch(() => {});
    }
    return this.ctx;
  }

  public toggleMute(): boolean {
    this.muted = !this.muted;
    try {
      if (typeof localStorage !== 'undefined') {
        localStorage.setItem('airstrike_sound_muted', String(this.muted));
      }
    } catch {}

    if (this.muted) {
      this.pauseMusic();
    } else if (!this.musicMuted) {
      this.startMusic();
    }
    return this.muted;
  }

  public isMuted(): boolean {
    return this.muted;
  }

  public playLaser(): void {
    if (this.muted) return;
    const ctx = this.initCtx();
    if (!ctx) return;

    try {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();

      osc.type = 'sawtooth';
      osc.frequency.setValueAtTime(880, ctx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(180, ctx.currentTime + 0.08);

      gain.gain.setValueAtTime(0.12, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.08);

      osc.connect(gain);
      gain.connect(ctx.destination);

      osc.start();
      osc.stop(ctx.currentTime + 0.09);
    } catch {}
  }

  public playEnemyLaser(): void {
    if (this.muted) return;
    const ctx = this.initCtx();
    if (!ctx) return;

    try {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();

      osc.type = 'triangle';
      osc.frequency.setValueAtTime(420, ctx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(140, ctx.currentTime + 0.12);

      gain.gain.setValueAtTime(0.15, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.12);

      osc.connect(gain);
      gain.connect(ctx.destination);

      osc.start();
      osc.stop(ctx.currentTime + 0.13);
    } catch {}
  }

  public playBossLaser(): void {
    if (this.muted) return;
    const ctx = this.initCtx();
    if (!ctx) return;

    try {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();

      osc.type = 'square';
      osc.frequency.setValueAtTime(260, ctx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(60, ctx.currentTime + 0.2);

      gain.gain.setValueAtTime(0.2, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.2);

      osc.connect(gain);
      gain.connect(ctx.destination);

      osc.start();
      osc.stop(ctx.currentTime + 0.21);
    } catch {}
  }

  public playExplosion(): void {
    if (this.muted) return;
    const ctx = this.initCtx();
    if (!ctx) return;

    try {
      const bufferSize = ctx.sampleRate * 0.25;
      const buffer = ctx.createBuffer(1, bufferSize, ctx.sampleRate);
      const data = buffer.getChannelData(0);
      for (let i = 0; i < bufferSize; i++) {
        data[i] = Math.random() * 2 - 1;
      }

      const noise = ctx.createBufferSource();
      noise.buffer = buffer;

      const filter = ctx.createBiquadFilter();
      filter.type = 'lowpass';
      filter.frequency.setValueAtTime(700, ctx.currentTime);
      filter.frequency.exponentialRampToValueAtTime(80, ctx.currentTime + 0.25);

      const gain = ctx.createGain();
      gain.gain.setValueAtTime(0.35, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.25);

      noise.connect(filter);
      filter.connect(gain);
      gain.connect(ctx.destination);

      noise.start();
      noise.stop(ctx.currentTime + 0.26);
    } catch {}
  }

  public playBossExplosion(): void {
    if (this.muted) return;
    const ctx = this.initCtx();
    if (!ctx) return;

    try {
      const bufferSize = ctx.sampleRate * 0.8;
      const buffer = ctx.createBuffer(1, bufferSize, ctx.sampleRate);
      const data = buffer.getChannelData(0);
      for (let i = 0; i < bufferSize; i++) {
        data[i] = Math.random() * 2 - 1;
      }

      const noise = ctx.createBufferSource();
      noise.buffer = buffer;

      const filter = ctx.createBiquadFilter();
      filter.type = 'lowpass';
      filter.frequency.setValueAtTime(400, ctx.currentTime);
      filter.frequency.exponentialRampToValueAtTime(40, ctx.currentTime + 0.8);

      const gain = ctx.createGain();
      gain.gain.setValueAtTime(0.5, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.8);

      noise.connect(filter);
      filter.connect(gain);
      gain.connect(ctx.destination);

      noise.start();
      noise.stop(ctx.currentTime + 0.82);
    } catch {}
  }

  public playShieldHit(): void {
    if (this.muted) return;
    const ctx = this.initCtx();
    if (!ctx) return;

    try {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();

      osc.type = 'sine';
      osc.frequency.setValueAtTime(900, ctx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(450, ctx.currentTime + 0.1);

      gain.gain.setValueAtTime(0.2, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.1);

      osc.connect(gain);
      gain.connect(ctx.destination);

      osc.start();
      osc.stop(ctx.currentTime + 0.11);
    } catch {}
  }

  public playPowerUp(): void {
    if (this.muted) return;
    const ctx = this.initCtx();
    if (!ctx) return;

    try {
      const notes = [523.25, 659.25, 783.99, 1046.50]; // C5, E5, G5, C6
      notes.forEach((freq, idx) => {
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();

        osc.type = 'sine';
        osc.frequency.setValueAtTime(freq, ctx.currentTime + idx * 0.05);

        gain.gain.setValueAtTime(0.15, ctx.currentTime + idx * 0.05);
        gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + idx * 0.05 + 0.08);

        osc.connect(gain);
        gain.connect(ctx.destination);

        osc.start(ctx.currentTime + idx * 0.05);
        osc.stop(ctx.currentTime + idx * 0.05 + 0.09);
      });
    } catch {}
  }

  public playBossWarning(): void {
    if (this.muted) return;
    const ctx = this.initCtx();
    if (!ctx) return;

    try {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();

      osc.type = 'sawtooth';
      osc.frequency.setValueAtTime(320, ctx.currentTime);
      osc.frequency.setValueAtTime(480, ctx.currentTime + 0.15);

      gain.gain.setValueAtTime(0.25, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.35);

      osc.connect(gain);
      gain.connect(ctx.destination);

      osc.start();
      osc.stop(ctx.currentTime + 0.36);
    } catch {}
  }
}
