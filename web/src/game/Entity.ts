import { BoundingBox } from '../types/game';

export abstract class Entity {
  public x: number;
  public y: number;
  public vx: number = 0;
  public vy: number = 0;
  public width: number;
  public height: number;
  public active: boolean = true;

  constructor(x: number, y: number, width: number, height: number) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  public getBounds(): BoundingBox {
    return {
      x: this.x,
      y: this.y,
      width: this.width,
      height: this.height
    };
  }

  public abstract update(dt: number): void;
  public abstract render(ctx: CanvasRenderingContext2D): void;
}
