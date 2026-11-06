export interface ParticleOptions {
  x: number;
  y: number;
  vx: number;
  vy: number;
  color: string;
  size: number;
  life: number; // in seconds
  decay?: number;
  isText?: boolean;
  text?: string;
}

export class Particle {
  public x: number;
  public y: number;
  public vx: number;
  public vy: number;
  public color: string;
  public size: number;
  public maxLife: number;
  public life: number;
  public active: boolean = true;
  public isText: boolean;
  public text?: string;

  constructor(opt: ParticleOptions) {
    this.x = opt.x;
    this.y = opt.y;
    this.vx = opt.vx;
    this.vy = opt.vy;
    this.color = opt.color;
    this.size = opt.size;
    this.maxLife = opt.life;
    this.life = opt.life;
    this.isText = !!opt.isText;
    this.text = opt.text;
  }

  public update(dt: number): void {
    this.x += this.vx * dt;
    this.y += this.vy * dt;
    this.life -= dt;

    if (this.life <= 0) {
      this.active = false;
    }
  }

  public render(ctx: CanvasRenderingContext2D): void {
    if (!this.active) return;
    const alpha = Math.max(0, Math.min(1, this.life / this.maxLife));

    ctx.save();
    ctx.globalAlpha = alpha;

    if (this.isText && this.text) {
      ctx.font = 'bold 14px "Segoe UI", Roboto, monospace';
      ctx.fillStyle = this.color;
      ctx.shadowBlur = 6;
      ctx.shadowColor = this.color;
      ctx.fillText(this.text, this.x, this.y);
    } else {
      ctx.fillStyle = this.color;
      ctx.shadowBlur = 8;
      ctx.shadowColor = this.color;
      ctx.beginPath();
      ctx.arc(this.x, this.y, Math.max(1, this.size * alpha), 0, Math.PI * 2);
      ctx.fill();
    }

    ctx.restore();
  }
}

export class ParticleSystem {
  private particles: Particle[] = [];

  public update(dt: number): void {
    for (let i = this.particles.length - 1; i >= 0; i--) {
      const p = this.particles[i];
      p.update(dt);
      if (!p.active) {
        this.particles.splice(i, 1);
      }
    }
  }

  public render(ctx: CanvasRenderingContext2D): void {
    for (const p of this.particles) {
      p.render(ctx);
    }
  }

  public clear(): void {
    this.particles = [];
  }

  public spawnSparks(x: number, y: number, color: string = '#00E5FF', count: number = 8): void {
    for (let i = 0; i < count; i++) {
      const angle = Math.random() * Math.PI * 2;
      const speed = 60 + Math.random() * 140;
      this.particles.push(
        new Particle({
          x,
          y,
          vx: Math.cos(angle) * speed,
          vy: Math.sin(angle) * speed,
          color,
          size: 2.5 + Math.random() * 2,
          life: 0.25 + Math.random() * 0.2
        })
      );
    }
  }

  public spawnExplosion(x: number, y: number, isLarge: boolean = false, baseColor: string = '#FF5A28'): void {
    const count = isLarge ? 45 : 20;
    const colors = [baseColor, '#FFAA00', '#FF3232', '#FFF050', '#FFFFFF'];

    for (let i = 0; i < count; i++) {
      const angle = Math.random() * Math.PI * 2;
      const speed = (isLarge ? 120 : 70) + Math.random() * (isLarge ? 280 : 160);
      const color = colors[Math.floor(Math.random() * colors.length)];
      this.particles.push(
        new Particle({
          x,
          y,
          vx: Math.cos(angle) * speed,
          vy: Math.sin(angle) * speed,
          color,
          size: (isLarge ? 4 : 2.5) + Math.random() * 3,
          life: (isLarge ? 0.6 : 0.35) + Math.random() * (isLarge ? 0.5 : 0.25)
        })
      );
    }
  }

  public spawnFloatingText(x: number, y: number, text: string, color: string = '#FFD700'): void {
    this.particles.push(
      new Particle({
        x,
        y,
        vx: (Math.random() - 0.5) * 20,
        vy: -55,
        color,
        size: 14,
        life: 0.9,
        isText: true,
        text
      })
    );
  }
}

