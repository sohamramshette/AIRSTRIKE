export class InputManager {
  private keys: Record<string, boolean> = {};
  public firePressed: boolean = false;
  public pauseRequested: boolean = false;
  public restartRequested: boolean = false;
  public muteRequested: boolean = false;

  // Touch controls state
  private touchX: number = 0;
  private touchY: number = 0;
  private isTouching: boolean = false;
  private virtualMoveX: number = 0;
  private virtualMoveY: number = 0;
  private touchFireActive: boolean = false;

  private canvas: HTMLCanvasElement | null = null;

  constructor() {
    this.bindKeyboard();
  }

  public attachCanvas(canvas: HTMLCanvasElement): void {
    this.canvas = canvas;
    this.bindTouchAndMouse(canvas);
  }

  public getCanvas(): HTMLCanvasElement | null {
    return this.canvas;
  }

  private bindKeyboard(): void {
    if (typeof window === 'undefined') return;

    window.addEventListener('keydown', (e: KeyboardEvent) => {
      this.keys[e.code] = true;

      if (e.code === 'Space') {
        this.firePressed = true;
        e.preventDefault();
      } else if (e.code === 'KeyP' || e.code === 'Escape') {
        this.pauseRequested = true;
        e.preventDefault();
      } else if (e.code === 'KeyR') {
        this.restartRequested = true;
      } else if (e.code === 'KeyM') {
        this.muteRequested = true;
      }
    });

    window.addEventListener('keyup', (e: KeyboardEvent) => {
      this.keys[e.code] = false;
      if (e.code === 'Space') {
        this.firePressed = false;
      }
    });
  }

  private bindTouchAndMouse(canvas: HTMLCanvasElement): void {
    // Mouse firing support
    canvas.addEventListener('mousedown', (e: MouseEvent) => {
      if (e.button === 0) {
        this.firePressed = true;
      }
    });

    window.addEventListener('mouseup', () => {
      this.firePressed = false;
    });

    // Touch events for mobile/tablet gameplay
    canvas.addEventListener('touchstart', (e: TouchEvent) => {
      e.preventDefault();
      const touch = e.touches[0];
      const rect = canvas.getBoundingClientRect();
      this.touchX = touch.clientX - rect.left;
      this.touchY = touch.clientY - rect.top;
      this.isTouching = true;
      this.firePressed = true;
    }, { passive: false });

    canvas.addEventListener('touchmove', (e: TouchEvent) => {
      e.preventDefault();
      if (!this.isTouching) return;
      const touch = e.touches[0];
      const rect = canvas.getBoundingClientRect();
      const currentX = touch.clientX - rect.left;
      const currentY = touch.clientY - rect.top;

      const scaleX = 800 / rect.width;
      const scaleY = 650 / rect.height;

      const deltaX = (currentX - this.touchX) * scaleX;
      const deltaY = (currentY - this.touchY) * scaleY;

      // Sensitivity factor
      this.virtualMoveX = Math.max(-1, Math.min(1, deltaX / 18));
      this.virtualMoveY = Math.max(-1, Math.min(1, deltaY / 18));
    }, { passive: false });

    const endTouch = (e: TouchEvent) => {
      e.preventDefault();
      this.isTouching = false;
      this.virtualMoveX = 0;
      this.virtualMoveY = 0;
      this.firePressed = false;
    };

    canvas.addEventListener('touchend', endTouch, { passive: false });
    canvas.addEventListener('touchcancel', endTouch, { passive: false });
  }

  public setVirtualMovement(vx: number, vy: number): void {
    this.virtualMoveX = vx;
    this.virtualMoveY = vy;
  }

  public setTouchFire(active: boolean): void {
    this.touchFireActive = active;
  }

  public getMovementVector(): { x: number; y: number } {
    let x = 0;
    let y = 0;

    if (this.keys['KeyA'] || this.keys['ArrowLeft']) x -= 1;
    if (this.keys['KeyD'] || this.keys['ArrowRight']) x += 1;
    if (this.keys['KeyW'] || this.keys['ArrowUp']) y -= 1;
    if (this.keys['KeyS'] || this.keys['ArrowDown']) y += 1;

    // Merge virtual touch control input
    if (Math.abs(this.virtualMoveX) > 0.05) x = this.virtualMoveX;
    if (Math.abs(this.virtualMoveY) > 0.05) y = this.virtualMoveY;

    return { x, y };
  }

  public isFiring(): boolean {
    return this.firePressed || this.touchFireActive || !!this.keys['Space'];
  }

  public consumePause(): boolean {
    const val = this.pauseRequested;
    this.pauseRequested = false;
    return val;
  }

  public consumeRestart(): boolean {
    const val = this.restartRequested;
    this.restartRequested = false;
    return val;
  }

  public consumeMute(): boolean {
    const val = this.muteRequested;
    this.muteRequested = false;
    return val;
  }
}
