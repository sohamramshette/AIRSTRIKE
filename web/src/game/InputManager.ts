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
  private gyroscopeEnabled: boolean = false;
  private gyroBaseline: { x: number; y: number; angle: number } | null = null;
  private readonly handleDeviceOrientation = (event: DeviceOrientationEvent): void => {
    if (event.beta === null || event.gamma === null) return;

    const angle = ((screen.orientation?.angle ?? 0) + 360) % 360;
    let tiltX = event.gamma;
    let tiltY = event.beta;

    if (angle === 90) {
      tiltX = event.beta;
      tiltY = -event.gamma;
    } else if (angle === 180) {
      tiltX = -event.gamma;
      tiltY = -event.beta;
    } else if (angle === 270) {
      tiltX = -event.beta;
      tiltY = event.gamma;
    }

    if (!this.gyroBaseline || this.gyroBaseline.angle !== angle) {
      this.gyroBaseline = { x: tiltX, y: tiltY, angle };
      this.virtualMoveX = 0;
      this.virtualMoveY = 0;
      return;
    }

    this.virtualMoveX = this.normalizeTilt(tiltX - this.gyroBaseline.x);
    this.virtualMoveY = this.normalizeTilt(tiltY - this.gyroBaseline.y);
  };

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
      // 1. If the user is currently typing inside an input field, textarea, select, or editable element,
      // completely bypass all game shortcuts so characters like 'P', 'p', Space, 'R', 'M' type normally.
      const target = e.target as HTMLElement | null;
      if (
        target &&
        (target.tagName === 'INPUT' ||
          target.tagName === 'TEXTAREA' ||
          target.tagName === 'SELECT' ||
          target.isContentEditable)
      ) {
        return;
      }

      // 2. If any modal dialog overlay is currently open, do not intercept keys for gameplay
      const openModal = document.querySelector('.modal-overlay:not(.hidden)');
      if (openModal) {
        return;
      }

      this.keys[e.code] = true;
      this.keys[e.key] = true;

      if (e.code === 'Space' || e.key === ' ') {
        this.firePressed = true;
        e.preventDefault();
      } else if (e.code === 'KeyP' || e.key === 'p' || e.key === 'P' || e.code === 'Escape' || e.key === 'Escape') {
        this.pauseRequested = true;
        e.preventDefault();
      } else if (e.code === 'KeyR' || e.key === 'r' || e.key === 'R') {
        this.restartRequested = true;
      } else if (e.code === 'KeyM' || e.key === 'm' || e.key === 'M') {
        this.muteRequested = true;
      }
    });

    window.addEventListener('keyup', (e: KeyboardEvent) => {
      const target = e.target as HTMLElement | null;
      if (
        target &&
        (target.tagName === 'INPUT' ||
          target.tagName === 'TEXTAREA' ||
          target.tagName === 'SELECT' ||
          target.isContentEditable)
      ) {
        return;
      }

      const openModal = document.querySelector('.modal-overlay:not(.hidden)');
      if (openModal) {
        return;
      }

      this.keys[e.code] = false;
      this.keys[e.key] = false;
      if (e.code === 'Space' || e.key === ' ') {
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

    canvas.addEventListener('pointerdown', (e: PointerEvent) => {
      if (e.pointerType === 'touch') {
        this.firePressed = true;
      }
    });

    window.addEventListener('pointerup', (e: PointerEvent) => {
      if (e.pointerType === 'touch') {
        this.firePressed = false;
      }
    });

    window.addEventListener('pointercancel', (e: PointerEvent) => {
      if (e.pointerType === 'touch') {
        this.firePressed = false;
      }
    });

    // Touch events for mobile/tablet gameplay
    canvas.addEventListener('touchstart', (e: TouchEvent) => {
      e.preventDefault();
      const touch = e.touches[0];
      if (!touch) return;
      const rect = canvas.getBoundingClientRect();
      this.touchX = touch.clientX - rect.left;
      this.touchY = touch.clientY - rect.top;
      this.isTouching = true;
      this.firePressed = true;
    }, { passive: false });

    canvas.addEventListener('touchmove', (e: TouchEvent) => {
      e.preventDefault();
      if (!this.isTouching || this.gyroscopeEnabled) return;
      const touch = e.touches[0];
      if (!touch) return;
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
      if (e.touches.length === 0) {
        this.isTouching = false;
        if (!this.gyroscopeEnabled) {
          this.virtualMoveX = 0;
          this.virtualMoveY = 0;
        }
        this.firePressed = false;
      }
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

  public setGyroscopeEnabled(enabled: boolean): void {
    if (this.gyroscopeEnabled === enabled) return;

    this.gyroscopeEnabled = enabled;
    this.gyroBaseline = null;
    this.virtualMoveX = 0;
    this.virtualMoveY = 0;

    if (typeof window === 'undefined') return;
    if (enabled) {
      window.addEventListener('deviceorientation', this.handleDeviceOrientation);
    } else {
      window.removeEventListener('deviceorientation', this.handleDeviceOrientation);
    }
  }

  private normalizeTilt(angle: number): number {
    const deadZone = 2.5;
    const fullTilt = 22;
    if (Math.abs(angle) <= deadZone) return 0;
    return Math.max(-1, Math.min(1, (angle - Math.sign(angle) * deadZone) / (fullTilt - deadZone)));
  }

  public getMovementVector(): { x: number; y: number } {
    let x = 0;
    let y = 0;

    if (this.keys['KeyA'] || this.keys['ArrowLeft'] || this.keys['a'] || this.keys['A']) x -= 1;
    if (this.keys['KeyD'] || this.keys['ArrowRight'] || this.keys['d'] || this.keys['D']) x += 1;
    if (this.keys['KeyW'] || this.keys['ArrowUp'] || this.keys['w'] || this.keys['W']) y -= 1;
    if (this.keys['KeyS'] || this.keys['ArrowDown'] || this.keys['s'] || this.keys['S']) y += 1;

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
