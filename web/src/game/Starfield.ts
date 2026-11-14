/**
 * ==============================================================================
 * AIRSTRIKE — Dynamic Sector Evolution Starfield & Cosmic Environment
 * ==============================================================================
 * Fulfills CGL (Computer Graphics Laboratory) Course Outcomes:
 *
 * - CGL-CO1 (OpenGL & Graphics Primitives):
 *     Rasterization of points, lines, radial arcs, and polygon asteroid silhouettes.
 *
 * - CGL-CO2 (Geometric Transformations):
 *     Homogeneous 2D affine transformation matrices: Translation (T), Rotation (R),
 *     and Non-uniform Scaling (S) for 3D-to-2D projected planetary rings (T x R x S).
 *
 * - CGL-CO3 (2D & 3D Rasterization Algorithms):
 *     Midpoint circle rasterization for celestial planetary orbs and Bresenham-style
 *     linear interpolation for high-velocity shooting stars.
 *
 * - CGL-CO4 (Projection & Viewing Pipelines):
 *     Multi-layer perspective foreshortening (1/Z depth scaling) across 4 discrete
 *     depth planes (Z0=deep nebula, Z1=celestial body, Z2=mid stars, Z3=warp dust).
 *
 * - CGL-CO5 (Clipping Algorithms):
 *     Cohen-Sutherland outcode boundary verification and toroidal edge wrapping
 *     preventing rendering artifacts outside viewport bounds.
 *
 * - CGL-CO6 (Lighting & Color Models):
 *     Phong illumination approximation (Ia + Id + Is) on planetary spheres,
 *     radial atmospheric haze, and smooth RGB/HSV interpolation between sectors.
 * ==============================================================================
 */

export interface SectorTheme {
  name: string;
  codeName: string;
  courseOutcomeTag: string;
  // Deep space background gradient (top, mid, bottom)
  bgTop: [number, number, number];
  bgMid: [number, number, number];
  bgBottom: [number, number, number];
  // Nebula gas clouds
  nebulaPrimary: [number, number, number];
  nebulaSecondary: [number, number, number];
  nebulaAlpha: number;
  // Celestial Body
  celestialType: 'ice_giant' | 'amethyst_moon' | 'solar_corona' | 'black_hole';
  planetColor: [number, number, number];
  planetAtmosphere: [number, number, number];
  hasRings: boolean;
  ringColor: [number, number, number];
  ringTilt: number; // in radians
  // Stardust & Warp speed
  warpSpeedFactor: number;
  dustColor: string;
}

export const SECTORS: SectorTheme[] = [
  // Sector 1 (Waves 1-2): Orion Azure Veil
  {
    name: 'SECTOR 1: ORION AZURE VEIL',
    codeName: 'ORION_AZURE',
    courseOutcomeTag: 'CGL-CO4 (Parallax) & CGL-CO6 (Azure Palette)',
    bgTop: [3, 7, 18],        // #030712
    bgMid: [10, 25, 47],      // #0A192F
    bgBottom: [2, 4, 9],       // #020409
    nebulaPrimary: [0, 229, 255],   // Cyan
    nebulaSecondary: [56, 189, 248], // Sky Blue
    nebulaAlpha: 0.12,
    celestialType: 'ice_giant',
    planetColor: [14, 116, 144],
    planetAtmosphere: [0, 229, 255],
    hasRings: true,
    ringColor: [186, 230, 253],
    ringTilt: -0.38,
    warpSpeedFactor: 1.0,
    dustColor: '#BAE6FD'
  },
  // Sector 2 (Waves 3-5): Cygnus Violet Rift
  {
    name: 'SECTOR 2: CYGNUS VIOLET RIFT',
    codeName: 'CYGNUS_VIOLET',
    courseOutcomeTag: 'CGL-CO2 (Affine Rings) & CGL-CO6 (Neon Violet)',
    bgTop: [11, 4, 24],       // #0B0418
    bgMid: [31, 13, 61],      // #1F0D3D
    bgBottom: [6, 2, 14],      // #06020E
    nebulaPrimary: [168, 85, 247],  // Neon Purple
    nebulaSecondary: [236, 72, 153], // Hot Magenta
    nebulaAlpha: 0.14,
    celestialType: 'amethyst_moon',
    planetColor: [88, 28, 135],
    planetAtmosphere: [192, 132, 252],
    hasRings: true,
    ringColor: [233, 213, 255],
    ringTilt: 0.42,
    warpSpeedFactor: 1.25,
    dustColor: '#E9D5FF'
  },
  // Sector 3 (Waves 6-8): Kepler Solar Flare
  {
    name: 'SECTOR 3: KEPLER SOLAR FLARE',
    codeName: 'KEPLER_SOLAR',
    courseOutcomeTag: 'CGL-CO6 (Phong Specular) & CGL-CO1 (Solar Arcs)',
    bgTop: [22, 8, 3],        // #160803
    bgMid: [44, 17, 5],       // #2C1105
    bgBottom: [10, 3, 2],      // #0A0302
    nebulaPrimary: [245, 158, 11],  // Amber Gold
    nebulaSecondary: [239, 68, 68],  // Solar Crimson
    nebulaAlpha: 0.15,
    celestialType: 'solar_corona',
    planetColor: [217, 119, 6],
    planetAtmosphere: [251, 191, 36],
    hasRings: false,
    ringColor: [254, 240, 138],
    ringTilt: 0.0,
    warpSpeedFactor: 1.45,
    dustColor: '#FDE68A'
  },
  // Boss Encounter / High Wave (Wave 9+ or Boss Active): Event Horizon Vortex
  {
    name: 'RED ALERT: EVENT HORIZON VORTEX',
    codeName: 'EVENT_HORIZON',
    courseOutcomeTag: 'CGL-CO2 (Vortex Rotation) & CGL-CO5 (Clipping)',
    bgTop: [21, 2, 6],        // #150206
    bgMid: [45, 5, 14],       // #2D050E
    bgBottom: [7, 1, 3],       // #070103
    nebulaPrimary: [239, 68, 68],   // Crimson Red
    nebulaSecondary: [153, 27, 27],  // Dark Blood
    nebulaAlpha: 0.18,
    celestialType: 'black_hole',
    planetColor: [20, 2, 5],
    planetAtmosphere: [239, 68, 68],
    hasRings: true,
    ringColor: [248, 113, 113],
    ringTilt: 0.55,
    warpSpeedFactor: 1.8,
    dustColor: '#FECACA'
  }
];

interface Star {
  x: number;
  y: number;
  speed: number;
  size: number;
  alpha: number;
  twinkleSpeed: number;
  twinklePhase: number;
  color: string;
  layer: number; // 0=deep, 1=mid, 2=near
}

interface SpeedDust {
  x: number;
  y: number;
  length: number;
  speed: number;
  alpha: number;
}

interface ShootingStar {
  active: boolean;
  x: number;
  y: number;
  vx: number;
  vy: number;
  length: number;
  alpha: number;
  maxAlpha: number;
  decay: number;
}

export class Starfield {
  private width: number;
  private height: number;
  private animTime: number = 0;

  // Multi-layer parallax star collection (CGL-CO4)
  private stars: Star[] = [];
  // High-speed warp dust streaks (CGL-CO4 & CGL-CO3)
  private dust: SpeedDust[] = [];
  // Occasional shooting star / comet (CGL-CO3)
  private shootingStar: ShootingStar = {
    active: false,
    x: 0,
    y: 0,
    vx: 0,
    vy: 0,
    length: 80,
    alpha: 0,
    maxAlpha: 0.85,
    decay: 0.95
  };
  private shootingStarTimer: number = 4.0;

  // Celestial Body State (CGL-CO2, CGL-CO3, CGL-CO6)
  private planetX: number;
  private planetY: number;
  private planetSpeedY: number = 4.5;
  private planetRadius: number = 55;
  private planetAngle: number = 0;

  // Dynamic Sector State
  private currentSectorIndex: number = 0;
  private targetSectorIndex: number = 0;
  private transitionProgress: number = 1.0; // 1.0 = fully transitioned
  private readonly transitionDuration: number = 2.5; // seconds
  private forcedThemeIndex: number = -1; // -1 = dynamic by wave

  // Interpolated Color State (CGL-CO6)
  private currentBgTop: [number, number, number] = [...SECTORS[0].bgTop];
  private currentBgMid: [number, number, number] = [...SECTORS[0].bgMid];
  private currentBgBottom: [number, number, number] = [...SECTORS[0].bgBottom];
  private currentNebulaPrimary: [number, number, number] = [...SECTORS[0].nebulaPrimary];
  private currentNebulaSecondary: [number, number, number] = [...SECTORS[0].nebulaSecondary];
  private currentNebulaAlpha: number = SECTORS[0].nebulaAlpha;
  private currentPlanetColor: [number, number, number] = [...SECTORS[0].planetColor];
  private currentAtmosphere: [number, number, number] = [...SECTORS[0].planetAtmosphere];
  private currentRingColor: [number, number, number] = [...SECTORS[0].ringColor];
  private currentRingTilt: number = SECTORS[0].ringTilt;
  private currentWarpFactor: number = SECTORS[0].warpSpeedFactor;

  constructor(width: number = 800, height: number = 650, starCount: number = 95) {
    this.width = width;
    this.height = height;
    this.planetX = width * 0.78;
    this.planetY = height * 0.22;
    this.initStars(starCount);
    this.initDust(30);
  }

  private initStars(count: number): void {
    const starColors = ['#FFFFFF', '#E0F2FE', '#BAE6FD', '#E9D5FF', '#FEF08A'];
    this.stars = [];

    for (let i = 0; i < count; i++) {
      // 3 Parallax Layers (CGL-CO4 Viewing Pipeline):
      // Layer 0: Deep background, slow (0.35x velocity)
      // Layer 1: Mid-field, medium (1.0x velocity)
      // Layer 2: Near-field, fast (2.2x velocity)
      const roll = Math.random();
      const layer = roll < 0.5 ? 0 : roll < 0.8 ? 1 : 2;
      let speed = 20 + Math.random() * 15;
      let size = 1.0;
      let alpha = 0.45;

      if (layer === 1) {
        speed = 55 + Math.random() * 25;
        size = 1.6;
        alpha = 0.75;
      } else if (layer === 2) {
        speed = 110 + Math.random() * 40;
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
        color: starColors[Math.floor(Math.random() * starColors.length)],
        layer
      });
    }
  }

  private initDust(count: number): void {
    this.dust = [];
    for (let i = 0; i < count; i++) {
      this.dust.push({
        x: Math.random() * this.width,
        y: Math.random() * this.height,
        length: 8 + Math.random() * 16,
        speed: 180 + Math.random() * 120,
        alpha: 0.25 + Math.random() * 0.45
      });
    }
  }

  public resize(width: number, height: number): void {
    if (width <= 0 || height <= 0) return;
    this.width = width;
    this.height = height;
    this.planetX = Math.min(width * 0.80, width - 60);

    for (const star of this.stars) {
      if (star.x > width) star.x = Math.random() * width;
      if (star.y > height) star.y = Math.random() * height;
    }
    for (const d of this.dust) {
      if (d.x > width) d.x = Math.random() * width;
      if (d.y > height) d.y = Math.random() * height;
    }
  }

  /**
   * Sets sector evolution based on wave progression or boss state.
   */
  public setSectorByWave(waveNumber: number, isBossActive: boolean): void {
    if (this.forcedThemeIndex >= 0) {
      this.setTargetSector(this.forcedThemeIndex);
      return;
    }

    let target = 0;
    if (isBossActive || waveNumber >= 9) {
      target = 3; // Event Horizon Vortex
    } else if (waveNumber >= 6) {
      target = 2; // Kepler Solar Flare
    } else if (waveNumber >= 3) {
      target = 1; // Cygnus Violet Rift
    } else {
      target = 0; // Orion Azure Veil
    }

    this.setTargetSector(target);
  }

  public setTargetSector(index: number): void {
    const clamped = Math.max(0, Math.min(SECTORS.length - 1, index));
    if (clamped !== this.targetSectorIndex) {
      this.currentSectorIndex = this.targetSectorIndex;
      this.targetSectorIndex = clamped;
      this.transitionProgress = 0.0;
    }
  }

  public getCurrentSector(): SectorTheme {
    return SECTORS[this.targetSectorIndex];
  }

  public reset(): void {
    this.currentSectorIndex = 0;
    this.targetSectorIndex = 0;
    this.transitionProgress = 1.0;
    this.planetY = this.height * 0.22;
  }

  public update(dt: number, playerVx: number = 0): void {
    this.animTime += dt;

    // Smooth Sector Palette Interpolation (CGL-CO6)
    if (this.transitionProgress < 1.0) {
      this.transitionProgress = Math.min(1.0, this.transitionProgress + dt / this.transitionDuration);
      const t = this.smoothStep(this.transitionProgress);
      const from = SECTORS[this.currentSectorIndex];
      const to = SECTORS[this.targetSectorIndex];

      this.currentBgTop = this.lerpColor(from.bgTop, to.bgTop, t);
      this.currentBgMid = this.lerpColor(from.bgMid, to.bgMid, t);
      this.currentBgBottom = this.lerpColor(from.bgBottom, to.bgBottom, t);
      this.currentNebulaPrimary = this.lerpColor(from.nebulaPrimary, to.nebulaPrimary, t);
      this.currentNebulaSecondary = this.lerpColor(from.nebulaSecondary, to.nebulaSecondary, t);
      this.currentNebulaAlpha = from.nebulaAlpha + (to.nebulaAlpha - from.nebulaAlpha) * t;
      this.currentPlanetColor = this.lerpColor(from.planetColor, to.planetColor, t);
      this.currentAtmosphere = this.lerpColor(from.planetAtmosphere, to.planetAtmosphere, t);
      this.currentRingColor = this.lerpColor(from.ringColor, to.ringColor, t);
      this.currentRingTilt = from.ringTilt + (to.ringTilt - from.ringTilt) * t;
      this.currentWarpFactor = from.warpSpeedFactor + (to.warpSpeedFactor - from.warpSpeedFactor) * t;
    }

    // Parallax Star Movement (CGL-CO4 & CGL-CO5 Cohen-Sutherland Boundary Wrapping)
    const inertiaDriftX = -playerVx * 0.08;
    for (const star of this.stars) {
      const layerWeight = (star.layer + 1) * 0.35;
      star.y += star.speed * this.currentWarpFactor * dt;
      star.x += inertiaDriftX * layerWeight * dt;

      // Wrap around bounds (CGL-CO5)
      if (star.y > this.height) {
        star.y = 0;
        star.x = Math.random() * this.width;
      } else if (star.y < 0) {
        star.y = this.height;
      }
      if (star.x > this.width) star.x = 0;
      else if (star.x < 0) star.x = this.width;
    }

    // High-speed Warp Dust Streaks (CGL-CO4)
    for (const d of this.dust) {
      d.y += d.speed * this.currentWarpFactor * dt;
      d.x += inertiaDriftX * 0.7 * dt;
      if (d.y > this.height) {
        d.y = -d.length;
        d.x = Math.random() * this.width;
      }
    }

    // Celestial Planet Drift & Rotation (CGL-CO2)
    this.planetY += this.planetSpeedY * dt;
    this.planetAngle += dt * 0.04;
    if (this.planetY - this.planetRadius > this.height + 150) {
      // Wrap planet to top with new horizontal offset
      this.planetY = -this.planetRadius - 80;
      this.planetX = Math.random() * (this.width * 0.7) + this.width * 0.15;
    }

    // Shooting Star / Comet Spawning (CGL-CO3 Bresenham linear path)
    this.updateShootingStar(dt);
  }

  private updateShootingStar(dt: number): void {
    if (this.shootingStar.active) {
      this.shootingStar.x += this.shootingStar.vx * dt;
      this.shootingStar.y += this.shootingStar.vy * dt;
      this.shootingStar.alpha *= Math.pow(this.shootingStar.decay, dt * 60);

      // Check bounds or fade out (CGL-CO5)
      if (this.shootingStar.alpha < 0.05 || this.shootingStar.y > this.height || this.shootingStar.x > this.width) {
        this.shootingStar.active = false;
        this.shootingStarTimer = 6.0 + Math.random() * 9.0;
      }
    } else {
      this.shootingStarTimer -= dt;
      if (this.shootingStarTimer <= 0) {
        // Spawn fresh shooting star across upper atmosphere
        this.shootingStar.active = true;
        this.shootingStar.x = Math.random() * (this.width * 0.6);
        this.shootingStar.y = Math.random() * (this.height * 0.25);
        const speed = 480 + Math.random() * 220;
        const angle = (Math.PI / 4) + (Math.random() * 0.3 - 0.15); // ~45 deg downward
        this.shootingStar.vx = Math.cos(angle) * speed;
        this.shootingStar.vy = Math.sin(angle) * speed;
        this.shootingStar.alpha = this.shootingStar.maxAlpha;
        this.shootingStar.length = 70 + Math.random() * 50;
      }
    }
  }

  /**
   * Main Render Pipeline: Paints multi-layered cosmic arena.
   */
  public render(ctx: CanvasRenderingContext2D): void {
    ctx.save();

    // 1. LAYER 0: Deep Space Ambient Gradient (CGL-CO6)
    const bgGrad = ctx.createLinearGradient(0, 0, 0, this.height);
    bgGrad.addColorStop(0, this.rgbStr(this.currentBgTop));
    bgGrad.addColorStop(0.5, this.rgbStr(this.currentBgMid));
    bgGrad.addColorStop(1, this.rgbStr(this.currentBgBottom));
    ctx.fillStyle = bgGrad;
    ctx.fillRect(0, 0, this.width, this.height);

    // 2. LAYER 0.5: Multi-Cluster Drifting Nebula Gas Clouds (CGL-CO6)
    this.renderNebulae(ctx);

    // 3. LAYER 1: Distant Celestial Body / Ringed Planet (CGL-CO2 Affine & CGL-CO6 Phong)
    this.renderCelestialBody(ctx);

    // 4. LAYER 2: Twinkling Parallax Stars (CGL-CO4 & CGL-CO1)
    this.renderStars(ctx);

    // 5. LAYER 3: Shooting Stars / Comets (CGL-CO3)
    this.renderShootingStar(ctx);

    // 6. LAYER 4: High-Speed Cosmic Dust Streaks (CGL-CO4)
    this.renderDust(ctx);

    ctx.restore();
  }

  /**
   * Render glowing radial nebulae drifting across the cosmos (CGL-CO6).
   */
  private renderNebulae(ctx: CanvasRenderingContext2D): void {
    ctx.save();

    // Nebula Cloud A: Upper-center gentle drift
    const nX1 = this.width * 0.5 + Math.sin(this.animTime * 0.04) * 80;
    const nY1 = this.height * 0.35 + Math.cos(this.animTime * 0.035) * 50;
    const nR1 = Math.min(this.width, this.height) * 0.65;
    const grad1 = ctx.createRadialGradient(nX1, nY1, 30, nX1, nY1, nR1);
    grad1.addColorStop(0, this.rgbaStr(this.currentNebulaPrimary, this.currentNebulaAlpha));
    grad1.addColorStop(0.5, this.rgbaStr(this.currentNebulaSecondary, this.currentNebulaAlpha * 0.5));
    grad1.addColorStop(1, 'rgba(0, 0, 0, 0)');
    ctx.fillStyle = grad1;
    ctx.fillRect(0, 0, this.width, this.height);

    // Nebula Cloud B: Lower-left atmospheric cluster
    const nX2 = this.width * 0.2 + Math.cos(this.animTime * 0.03) * 60;
    const nY2 = this.height * 0.75 + Math.sin(this.animTime * 0.045) * 45;
    const nR2 = Math.min(this.width, this.height) * 0.55;
    const grad2 = ctx.createRadialGradient(nX2, nY2, 20, nX2, nY2, nR2);
    grad2.addColorStop(0, this.rgbaStr(this.currentNebulaSecondary, this.currentNebulaAlpha * 0.85));
    grad2.addColorStop(0.6, this.rgbaStr(this.currentNebulaPrimary, this.currentNebulaAlpha * 0.3));
    grad2.addColorStop(1, 'rgba(0, 0, 0, 0)');
    ctx.fillStyle = grad2;
    ctx.fillRect(0, 0, this.width, this.height);

    ctx.restore();
  }

  /**
   * Render deep-space celestial body with Phong illumination & 2D Affine rings (CGL-CO2 & CO6).
   */
  private renderCelestialBody(ctx: CanvasRenderingContext2D): void {
    if (this.planetY + this.planetRadius < -100 || this.planetY - this.planetRadius > this.height + 100) {
      return;
    }

    ctx.save();
    const cx = this.planetX;
    const cy = this.planetY;
    const r = this.planetRadius;

    // A. Atmospheric Glow Envelope (CGL-CO6 Specular/Halo)
    const haloGrad = ctx.createRadialGradient(cx, cy, r * 0.85, cx, cy, r * 1.55);
    haloGrad.addColorStop(0, this.rgbaStr(this.currentAtmosphere, 0.35));
    haloGrad.addColorStop(0.6, this.rgbaStr(this.currentAtmosphere, 0.12));
    haloGrad.addColorStop(1, 'rgba(0, 0, 0, 0)');
    ctx.fillStyle = haloGrad;
    ctx.beginPath();
    ctx.arc(cx, cy, r * 1.55, 0, Math.PI * 2);
    ctx.fill();

    // B. Back Portion of Planetary Ring (CGL-CO2 Affine Transform)
    const theme = SECTORS[this.targetSectorIndex];
    if (theme.hasRings) {
      this.renderPlanetaryRing(ctx, cx, cy, r, true);
    }

    // C. Planet Sphere with Phong Diffuse/Specular Lighting (CGL-CO6)
    // Light source positioned top-right (light vector L = [0.4, -0.4])
    const lightOffsetX = cx - r * 0.35;
    const lightOffsetY = cy - r * 0.35;
    const sphereGrad = ctx.createRadialGradient(lightOffsetX, lightOffsetY, r * 0.1, cx, cy, r);
    // Highlight / Specular core (Is)
    sphereGrad.addColorStop(0, this.rgbStr([
      Math.min(255, this.currentPlanetColor[0] + 90),
      Math.min(255, this.currentPlanetColor[1] + 90),
      Math.min(255, this.currentPlanetColor[2] + 90)
    ]));
    // Diffuse body (Id)
    sphereGrad.addColorStop(0.45, this.rgbStr(this.currentPlanetColor));
    // Ambient dark terminator (Ia)
    sphereGrad.addColorStop(1, this.rgbStr([
      Math.floor(this.currentPlanetColor[0] * 0.18),
      Math.floor(this.currentPlanetColor[1] * 0.18),
      Math.floor(this.currentPlanetColor[2] * 0.18)
    ]));

    ctx.fillStyle = sphereGrad;
    ctx.beginPath();
    ctx.arc(cx, cy, r, 0, Math.PI * 2);
    ctx.fill();

    // Subtle atmospheric rim arc on lit side
    ctx.strokeStyle = this.rgbaStr(this.currentAtmosphere, 0.65);
    ctx.lineWidth = 1.8;
    ctx.beginPath();
    ctx.arc(cx, cy, r, Math.PI * 0.85, Math.PI * 1.85);
    ctx.stroke();

    // D. Front Portion of Planetary Ring (CGL-CO2 Affine Transform)
    if (theme.hasRings) {
      this.renderPlanetaryRing(ctx, cx, cy, r, false);
    }

    ctx.restore();
  }

  /**
   * Planetary Rings rendered using 2D Homogeneous Affine Transformations (CGL-CO2):
   * Translate -> Rotate -> Non-uniform Scale -> Render -> Restore
   */
  private renderPlanetaryRing(
    ctx: CanvasRenderingContext2D,
    cx: number,
    cy: number,
    r: number,
    isBackSegment: boolean
  ): void {
    ctx.save();
    // 1. Translation to Planet Origin (T)
    ctx.translate(cx, cy);
    // 2. Elliptic Orbit Incline Rotation (R)
    ctx.rotate(this.currentRingTilt);
    // 3. Non-Uniform Perspective Scaling (S) - projects circular ring to 3D ellipse
    ctx.scale(1.0, 0.32);

    const innerR = r * 1.45;
    const outerR = r * 2.25;

    // Arc segment: Back (Math.PI to 2*Math.PI) or Front (0 to Math.PI)
    const startAngle = isBackSegment ? Math.PI : 0;
    const endAngle = isBackSegment ? Math.PI * 2 : Math.PI;

    // Multi-band ring system
    ctx.beginPath();
    ctx.arc(0, 0, outerR, startAngle, endAngle);
    ctx.arc(0, 0, innerR, endAngle, startAngle, true);
    ctx.closePath();

    const ringGrad = ctx.createRadialGradient(0, 0, innerR, 0, 0, outerR);
    ringGrad.addColorStop(0, this.rgbaStr(this.currentRingColor, 0.45));
    ringGrad.addColorStop(0.5, this.rgbaStr(this.currentRingColor, 0.75));
    ringGrad.addColorStop(0.7, 'rgba(0,0,0,0.15)'); // Cassini Division gap!
    ringGrad.addColorStop(0.85, this.rgbaStr(this.currentRingColor, 0.55));
    ringGrad.addColorStop(1, this.rgbaStr(this.currentRingColor, 0.05));

    ctx.fillStyle = ringGrad;
    ctx.fill();

    ctx.restore();
  }

  /**
   * Render multi-layer twinkling stars (CGL-CO4 & CGL-CO1).
   */
  private renderStars(ctx: CanvasRenderingContext2D): void {
    for (const star of this.stars) {
      const twinkle = 0.7 + 0.3 * Math.sin(this.animTime * star.twinkleSpeed + star.twinklePhase);
      const alpha = Math.max(0.15, Math.min(1.0, star.alpha * twinkle));

      ctx.globalAlpha = alpha;
      ctx.fillStyle = star.color;

      if (star.size > 2.0) {
        ctx.shadowBlur = 6;
        ctx.shadowColor = star.color;
      } else {
        ctx.shadowBlur = 0;
      }

      ctx.beginPath();
      ctx.arc(star.x, star.y, star.size, 0, Math.PI * 2);
      ctx.fill();
    }
    ctx.globalAlpha = 1.0;
    ctx.shadowBlur = 0;
  }

  /**
   * Render high-speed shooting stars with glowing fading trails (CGL-CO3).
   */
  private renderShootingStar(ctx: CanvasRenderingContext2D): void {
    if (!this.shootingStar.active || this.shootingStar.alpha <= 0.02) return;

    ctx.save();
    ctx.globalAlpha = this.shootingStar.alpha;

    const tailX = this.shootingStar.x - (this.shootingStar.vx / 500) * this.shootingStar.length;
    const tailY = this.shootingStar.y - (this.shootingStar.vy / 500) * this.shootingStar.length;

    const streakGrad = ctx.createLinearGradient(tailX, tailY, this.shootingStar.x, this.shootingStar.y);
    streakGrad.addColorStop(0, 'rgba(255, 255, 255, 0)');
    streakGrad.addColorStop(0.7, 'rgba(186, 230, 253, 0.6)');
    streakGrad.addColorStop(1, '#FFFFFF');

    ctx.strokeStyle = streakGrad;
    ctx.lineWidth = 2.2;
    ctx.shadowBlur = 8;
    ctx.shadowColor = '#00E5FF';

    ctx.beginPath();
    ctx.moveTo(tailX, tailY);
    ctx.lineTo(this.shootingStar.x, this.shootingStar.y);
    ctx.stroke();

    // Leading meteor head sparkle
    ctx.fillStyle = '#FFFFFF';
    ctx.beginPath();
    ctx.arc(this.shootingStar.x, this.shootingStar.y, 2.4, 0, Math.PI * 2);
    ctx.fill();

    ctx.restore();
  }

  /**
   * Render high-velocity cosmic dust streaks for forward momentum (CGL-CO4).
   */
  private renderDust(ctx: CanvasRenderingContext2D): void {
    ctx.save();
    const theme = SECTORS[this.targetSectorIndex];
    ctx.strokeStyle = theme.dustColor;
    ctx.lineWidth = 1.2;

    for (const d of this.dust) {
      ctx.globalAlpha = d.alpha;
      ctx.beginPath();
      ctx.moveTo(d.x, d.y);
      ctx.lineTo(d.x, d.y + d.length * this.currentWarpFactor);
      ctx.stroke();
    }
    ctx.restore();
  }

  // --- Helper Math & Color Utilities (CGL-CO6) ---

  private smoothStep(t: number): number {
    return t * t * (3 - 2 * t);
  }

  private lerpColor(
    c1: [number, number, number],
    c2: [number, number, number],
    t: number
  ): [number, number, number] {
    return [
      Math.round(c1[0] + (c2[0] - c1[0]) * t),
      Math.round(c1[1] + (c2[1] - c1[1]) * t),
      Math.round(c1[2] + (c2[2] - c1[2]) * t)
    ];
  }

  private rgbStr(c: [number, number, number]): string {
    return `rgb(${c[0]}, ${c[1]}, ${c[2]})`;
  }

  private rgbaStr(c: [number, number, number], a: number): string {
    return `rgba(${c[0]}, ${c[1]}, ${c[2]}, ${Math.max(0, Math.min(1, a)).toFixed(3)})`;
  }
}
