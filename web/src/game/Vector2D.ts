export class Vector2D {
  public x: number;
  public y: number;

  constructor(x: number = 0, y: number = 0) {
    this.x = x;
    this.y = y;
  }

  public set(x: number, y: number): this {
    this.x = x;
    this.y = y;
    return this;
  }

  public add(v: Vector2D): this {
    this.x += v.x;
    this.y += v.y;
    return this;
  }

  public scale(s: number): this {
    this.x *= s;
    this.y *= s;
    return this;
  }

  public magnitude(): number {
    return Math.sqrt(this.x * this.x + this.y * this.y);
  }

  public normalize(): this {
    const mag = this.magnitude();
    if (mag > 0.00001) {
      this.x /= mag;
      this.y /= mag;
    }
    return this;
  }

  public static distance(v1: Vector2D, v2: Vector2D): number {
    const dx = v1.x - v2.x;
    const dy = v1.y - v2.y;
    return Math.sqrt(dx * dx + dy * dy);
  }
}
