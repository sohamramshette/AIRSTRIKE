interface Star {
  x: number;
  y: number;
  speed: number;
  size: number;
  alpha: number;
  twinkleSpeed: number;
  twinklePhase: number;
  color: string;
}

export class Starfield {
  private stars: Star[] = [];
  private width: number;
  private height: number;
  private animTime: number = 0;

  constructor(width: number = 800, height: number = 650, starCount: number = 85) {
    this.width = width;
    this.height = height;
    this.initStars(starCount);
  }

  private initStars(count: number): void {
    const starColors = ['#FFFFFF', '#B4FFFF', '#BAE6FD', '#E0F2FE', '#FDE047'];

    for (let i = 0; i < count; i++) {
      // 3 Parallax layers: 0 (deep, slow), 1 (mid), 2 (close, fast)
      const layer = Math.random() < 0.5 ? 0 : Math.random() < 0.75 ? 1 : 2;
      let speed = 25;
      let size = 1.0;
      let alpha = 0.4;

      if (layer === 1) {
        speed = 60;
        size = 1.8;
        alpha = 0.75;
      } else if (layer === 2) {
        speed = 120;
        size = 2.4;
        alpha = 0.95;
      }

      this.stars.push({
        x: Math.random() * this.width,
        y: Math.random() * this.height,
        speed,
        size,
        alpha,
        twinkleSpeed: 1.5 + Math.random() * 3.5,
        twinklePhase: Math.random() * Math.PI * 2,
        color: starColors[Math.floor(Math.random() * starColors.length)]
      });
    }
  }

  public update(dt: number): void {
    this.animTime += dt;
    for (const star of this.stars) {
      star.y += star.speed * dt;
      if (star.y > this.height) {
        star.y = 0;
        star.x = Math.random() * this.width;
      }
    }
  }

  public render(ctx: CanvasRenderingContext2D): void {
    ctx.save();

    // Deep cosmic gradient
    const grad = ctx.createLinearGradient(0, 0, 0, this.height);
    grad.addColorStop(0, '#040711');
    grad.addColorStop(0.5, '#070C1B');
    grad.addColorStop(1, '#020409');
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, this.width, this.height);

    // Subtle drifting nebula gas cloud
    const nebulaGrad = ctx.createRadialGradient(
      this.width * 0.5 + Math.sin(this.animTime * 0.05) * 60,
      this.height * 0.35 + Math.cos(this.animTime * 0.04) * 40,
      50,
      this.width * 0.5,
      this.height * 0.35,
      350
    );
    nebulaGrad.addColorStop(0, 'rgba(56, 189, 248, 0.04)');
    nebulaGrad.addColorStop(0.5, 'rgba(124, 58, 237, 0.03)');
    nebulaGrad.addColorStop(1, 'rgba(0, 0, 0, 0)');
    ctx.fillStyle = nebulaGrad;
    ctx.fillRect(0, 0, this.width, this.height);

    // Stars
    for (const star of this.stars) {
      const currentAlpha = star.alpha * (0.7 + 0.3 * Math.sin(this.animTime * star.twinkleSpeed + star.twinklePhase));
      ctx.globalAlpha = Math.max(0.1, Math.min(1, currentAlpha));
      ctx.fillStyle = star.color;
      ctx.shadowBlur = star.size > 2 ? 6 : 0;
      ctx.shadowColor = star.color;

      ctx.beginPath();
      ctx.arc(star.x, star.y, star.size, 0, Math.PI * 2);
      ctx.fill();
    }

    ctx.restore();
  }
}

