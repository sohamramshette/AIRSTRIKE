# AIRSTRIKE Phase 5: Computer Graphics Laboratory (CGL)
## C++17, OpenGL & GLUT Graphics Demonstration and Academic Implementation

---

## 1. Executive Summary & Technical Compliance

Phase 5 implements the complete **Computer Graphics Laboratory (CGL)** syllabus for the **AIRSTRIKE** academic space shooter and demonstration system. The implementation demonstrates all six Course Outcomes (**CGL-CO1** through **CGL-CO6**) using modern standard C++17, OpenGL, and GLUT.

### 1.1 Strict Compliance Verification
* **Programming Language**: Standard C++17 (`-std=c++17`).
* **Graphics API**: Standard OpenGL 2.1 / Compatibility Profile with GLUT (MinGW GCC 6.3.0 `libglut32.a`, `libglu32.a`, `libopengl32.a`).
* **Algorithmic Authenticity**: All required 2D/3D graphics algorithms (DDA, Bresenham's Integer Line Drawing across all 8 octants, Midpoint Circle, Midpoint Ellipse, Scanline Polygon Fill, Cohen-Sutherland Line Clipping, Liang-Barsky Parametric Clipping, and Sutherland-Hodgman Polygon Clipping) are implemented from **first mathematical principles in pure C++** and plotted directly via raster pixel functions (`GL_POINTS` / `GL_LINES`). Built-in OpenGL high-level drawing routines are never used as substitutes for the core algorithms.
* **Dual Execution Modes**:
  1. **Headless Test Mode** (`--test` / `-t`): Executes all mathematical algorithm test suites with 42 assertions and exits with code 0 without creating an interactive window event loop.
  2. **Automated Screenshot Mode** (`--screenshot` / `-s`): Renders all 6 Course Outcome demonstration screens and the Integrated Combat Scene, captures framebuffer pixels using `glReadPixels`, and writes genuine uncompressed 24-bit `.bmp` images to `docs/screenshots/CGL/`.
  3. **Interactive Window Mode**: Full-featured 60 FPS real-time interactive simulation with keyboard controls.
* **Non-Interference**: Existing Java (`PSOOP`), C++17 (`PL`), and 80386 Assembly (`COA`) modules remain untouched, fully functional, and pass all master build validations with 100% success.

---

## 2. Course Outcome Syllabus Mapping

| Outcome ID | Description | Primary Source Files | Verification Method | Status |
| :--- | :--- | :--- | :--- | :--- |
| **CGL-CO1** | OpenGL Graphics Primitives | `CGL/src/primitives/space_primitives.hpp`, `.cpp` | 8-Cell Primitive Showcase (`1`), 4 Unit Tests | **Completed (Phase 5)** |
| **CGL-CO2** | Geometric Transformations | `CGL/src/transformations/transform_demo.hpp`, `.cpp` | Interactive Matrix Controller (`2`), 5 Unit Tests | **Completed (Phase 5)** |
| **CGL-CO3** | 2D and 3D Algorithms | `CGL/src/algorithms/raster_algorithms.hpp`, `.cpp` | Software Rasterizer & 3D Mesh (`3`), 13 Unit Tests | **Completed (Phase 5)** |
| **CGL-CO4** | Projection and Viewing | `CGL/src/projection/viewing_demo.hpp`, `.cpp` | Split-Screen Radar & Cockpit View (`4`), 2 Unit Tests | **Completed (Phase 5)** |
| **CGL-CO5** | Clipping Algorithms | `CGL/src/clipping/clipping_algorithms.hpp`, `.cpp` | Boundary Clipping Canvas (`5`), 10 Unit Tests | **Completed (Phase 5)** |
| **CGL-CO6** | Texture, Lighting and Color | `CGL/src/appearance/lighting_texture_demo.hpp`, `.cpp` | Phong Illumination & Procedural UV (`6`), 8 Unit Tests | **Completed (Phase 5)** |

---

## 3. CGL-CO1: OpenGL Graphics Primitives

### 3.1 OpenGL State Machine & Architecture
OpenGL operates as a client-server state machine:
* The **Client** (the host C++ application) issues state changes and vertex streams.
* The **Server** (the graphics hardware/driver pipeline) transforms vertices, rasterizes primitives, and evaluates fragment attributes into the framebuffer.

Double buffering eliminates screen tearing and flickering:
* **Back Buffer**: Off-screen memory buffer where all drawing operations take place (`glClear`, `glBegin`/`glEnd`).
* **Front Buffer**: On-screen memory buffer actively scanned out by the display controller.
* **`glutSwapBuffers()`**: Atomically exchanges the front and back buffer pointers during vertical blanking (V-Sync).

### 3.2 Primitive Showcase Implementation
In `CGL/src/primitives/space_primitives.cpp`, the screen is partitioned into eight structured cells demonstrating all standard primitives:

1. **`GL_POINTS`**: Renders disconnected vertices. Used for cosmic starfields and plasma sparks. Configured with `glPointSize(3.0f)` and per-vertex colors (`glColor4f`).
2. **`GL_LINES`**: Connects pairs of vertices $(V_0, V_1), (V_2, V_3)$. Implements twin laser beams and targeting crosshairs. Features line width control (`glLineWidth(2.5f)`) and vertex gradient interpolation.
3. **`GL_LINE_STRIP`**: Connects vertices in a continuous sequence $V_0 \to V_1 \to V_2 \dots \to V_n$. Demonstrates smooth guided flight trajectories and missile curves.
4. **`GL_LINE_LOOP`**: Connects vertices in sequence and automatically closes the loop by connecting $V_n \to V_0$. Used for asteroid collision bounding wireframes.
5. **`GL_TRIANGLES`**: Fundamental building block of 3D computer graphics. Groups triples of vertices $(V_0, V_1, V_2)$. Implements the space fighter cockpit fuselage and swept delta wings with Counter-Clockwise (CCW) front-face vertex winding.
6. **`GL_TRIANGLE_STRIP`**: Generates a mesh of triangles where each new vertex forms a triangle with the preceding two vertices: $(V_0, V_1, V_2), (V_1, V_2, V_3) \dots$. Efficiently renders the fighter's deflector energy shield ribbon.
7. **`GL_TRIANGLE_FAN`**: Shares a central hub vertex $V_0$ connected to successive perimeter vertices $(V_0, V_1, V_2), (V_0, V_2, V_3) \dots$. Generates radial plasma blast waves and scout saucer hulls.
8. **`GL_QUADS`**: Four-sided planar polygons $(V_0, V_1, V_2, V_3)$ representing space station photovoltaic solar panel arrays.

Interactive toggle (`W` key) switches dynamically between `glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)` (wireframe) and `GL_FILL` (solid).

---

## 4. CGL-CO2: Geometric Transformations

### 4.1 Homogeneous Coordinates
In Euclidean geometry, affine transformations like translation cannot be represented as matrix multiplications in 2D ($2 \times 2$) or 3D ($3 \times 3$) Cartesian coordinates. By adding a scaling coordinate $w = 1$, points are represented in **Homogeneous Coordinates**:
$$\mathbf{P}_{2D} = \begin{bmatrix} x \\ y \\ 1 \end{bmatrix}, \quad \mathbf{P}_{3D} = \begin{bmatrix} x \\ y \\ z \\ 1 \end{bmatrix}$$

This allows translation, rotation, scaling, reflection, and shearing to be unified into linear $4 \times 4$ matrix operations.

### 4.2 Affine Transformation Matrices
In column-major OpenGL notation ($v' = M \cdot v$):

* **Translation Matrix $T(t_x, t_y, t_z)$**:
  $$T = \begin{bmatrix} 1 & 0 & 0 & t_x \\ 0 & 1 & 0 & t_y \\ 0 & 0 & 1 & t_z \\ 0 & 0 & 0 & 1 \end{bmatrix}$$

* **Rotation Matrix around Z-Axis $R_z(\theta)$**:
  $$R_z(\theta) = \begin{bmatrix} \cos\theta & -\sin\theta & 0 & 0 \\ \sin\theta & \cos\theta & 0 & 0 \\ 0 & 0 & 1 & 0 \\ 0 & 0 & 0 & 1 \end{bmatrix}$$

* **Scaling Matrix $S(s_x, s_y, s_z)$**:
  $$S = \begin{bmatrix} s_x & 0 & 0 & 0 \\ 0 & s_y & 0 & 0 \\ 0 & 0 & s_z & 0 \\ 0 & 0 & 0 & 1 \end{bmatrix}$$

* **2D Affine Shearing Matrix $M_{shear}$**:
  $$M_{shear} = \begin{bmatrix} 1 & sh_x & 0 & 0 \\ sh_y & 1 & 0 & 0 \\ 0 & 0 & 1 & 0 \\ 0 & 0 & 0 & 1 \end{bmatrix} \implies \begin{cases} x' = x + sh_x \cdot y \\ y' = y + sh_y \cdot x \end{cases}$$

* **Reflection**: Achieved by scaling with negative unity:
  - Across X-axis: $S(1, -1, 1)$
  - Across Y-axis: $S(-1, 1, 1)$

### 4.3 Composite Transformations & Non-Commutativity
Matrix multiplication is non-commutative:
$$T \cdot R \neq R \cdot T$$
* **$T \cdot R$ (Translate, then Rotate)**: The object translates to $(t_x, t_y)$ and rotates about its own local center.
* **$R \cdot T$ (Rotate, then Translate)**: The object rotates about the world coordinate origin, causing it to orbit along a circular arc.

### 4.4 Arbitrary Pivot Point Transformation
To rotate an object about an arbitrary point $P(x_p, y_p)$ rather than the origin $(0, 0)$:
$$M = T(x_p, y_p) \cdot R(\theta) \cdot T(-x_p, -y_p)$$
1. Translate pivot point $P$ to the origin ($T(-x_p, -y_p)$).
2. Perform rotation about origin ($R(\theta)$).
3. Translate origin back to pivot point $P$ ($T(x_p, y_p)$).

### 4.5 Hierarchical Transformations (Matrix Stack)
OpenGL maintains an internal matrix stack (`GL_MODELVIEW`). Using `glPushMatrix()` and `glPopMatrix()`, child objects inherit their parent's spatial frame:
```cpp
glPushMatrix(); // Save World Space
    glTranslatef(shipX, shipY, 0.0f); // Move parent fighter
    glRotatef(shipAngle, 0, 0, 1);
    drawFighter();

    glPushMatrix(); // Save Parent Ship Space
        glRotatef(droneOrbitAngle, 0, 0, 1); // Orbit around ship
        glTranslatef(0.8f, 0.0f, 0.0f);     // Distance from ship
        glRotatef(droneSelfSpin, 0, 0, 1);   // Spin drone
        drawEscortDrone();
    glPopMatrix(); // Restore Parent Ship Space
glPopMatrix(); // Restore World Space
```

---

## 5. CGL-CO3: 2D and 3D Algorithms

### 5.1 Digital Differential Analyzer (DDA) Line Drawing
The DDA algorithm calculates pixel positions using the differential equation of a straight line:
$$m = \frac{\Delta y}{\Delta x} = \frac{y_2 - y_1}{x_2 - x_1}$$
$$\text{steps} = \max(|\Delta x|, |\Delta y|)$$
$$x_{\text{inc}} = \frac{\Delta x}{\text{steps}}, \quad y_{\text{inc}} = \frac{\Delta y}{\text{steps}}$$

At each iteration $k$:
$$x_{k+1} = x_k + x_{\text{inc}}, \quad y_{k+1} = y_k + y_{\text{inc}}$$
$$\text{Pixel} = (\text{round}(x), \text{round}(y))$$

**Limitations**: Requires floating-point arithmetic and explicit rounding per step, which is computationally expensive on microcontrollers and older CPUs.

### 5.2 Bresenham's Integer Line Drawing Algorithm
Bresenham's algorithm eliminates all floating-point math by using pure integer arithmetic and an integer decision variable $p_k$.

#### Mathematical Derivation for Octant 1 ($0 \le m \le 1$):
Given current pixel $(x_k, y_k)$, the next pixel candidate is either East $E(x_k + 1, y_k)$ or North-East $NE(x_k + 1, y_k + 1)$.
The vertical distances from the true line $y = m(x_k + 1) + c$ are:
$$d_1 = y - y_k = m(x_k + 1) + c - y_k$$
$$d_2 = (y_k + 1) - y = y_k + 1 - m(x_k + 1) - c$$
$$d_1 - d_2 = 2m(x_k + 1) - 2y_k + 2c - 1$$

Multiplying by $\Delta x$ eliminates the division in $m = \Delta y / \Delta x$:
$$p_k = \Delta x(d_1 - d_2) = 2\Delta y \cdot x_k - 2\Delta x \cdot y_k + C$$
At step $k=0$, $x_0, y_0$:
$$p_0 = 2\Delta y - \Delta x$$

**Recurrence Relation**:
$$p_{k+1} - p_k = 2\Delta y - 2\Delta x(y_{k+1} - y_k)$$
* If $p_k < 0$: Choose East $E \implies y_{k+1} = y_k$:
  $$p_{k+1} = p_k + 2\Delta y$$
* If $p_k \ge 0$: Choose North-East $NE \implies y_{k+1} = y_k + 1$:
  $$p_{k+1} = p_k + 2\Delta y - 2\Delta x$$

#### Full 8-Octant Generalization
In `CGL/src/algorithms/raster_algorithms.cpp`, the algorithm is extended to all octants by tracking step directions $s_x = \text{sgn}(x_2 - x_1)$ and $s_y = \text{sgn}(y_2 - y_1)$, branching on $\Delta x \ge \Delta y$ vs $\Delta y > \Delta x$.

### 5.3 Midpoint Circle Drawing Algorithm
Exploits **8-way octant symmetry**: computing points in the first octant ($0 \le x \le y$) yields eight symmetric points:
$$(x_c \pm x, y_c \pm y) \quad \text{and} \quad (x_c \pm y, y_c \pm x)$$

Circle function: $f(x, y) = x^2 + y^2 - r^2$.
Evaluating at the midpoint between candidate pixels $E(x_k + 1, y_k)$ and $SE(x_k + 1, y_k - 1)$:
$$p_k = f\left(x_k + 1, y_k - \frac{1}{2}\right) = (x_k + 1)^2 + \left(y_k - \frac{1}{2}\right)^2 - r^2$$

Initial parameter at $(0, r)$:
$$p_0 = 1 + \left(r - \frac{1}{2}\right)^2 - r^2 = \frac{5}{4} - r \approx 1 - r \quad (\text{for integer } r)$$

**Decision Update**:
* If $p_k < 0$: Midpoint is inside circle $\implies$ choose $E(x_k + 1, y_k)$:
  $$p_{k+1} = p_k + 2x_{k+1} + 1$$
* If $p_k \ge 0$: Midpoint is outside or on circle $\implies$ choose $SE(x_k + 1, y_k - 1)$:
  $$p_{k+1} = p_k + 2x_{k+1} - 2y_{k+1} + 1$$

### 5.4 Midpoint Ellipse Drawing Algorithm
The ellipse equation is $r_y^2 x^2 + r_x^2 y^2 - r_x^2 r_y^2 = 0$.
The perimeter is split into two regions based on the tangent slope:
* **Region 1**: Slope $\frac{dy}{dx} \ge -1 \iff 2r_y^2 x \le 2r_x^2 y$. Unit step in $x$.
  $$p1_0 = r_y^2 - r_x^2 r_y + \frac{1}{4}r_x^2$$
  - If $p1_k < 0$: $p1_{k+1} = p1_k + 2r_y^2 x_{k+1} + r_y^2$
  - If $p1_k \ge 0$: $p1_{k+1} = p1_k + 2r_y^2 x_{k+1} - 2r_x^2 y_{k+1} + r_y^2$
* **Region 2**: Slope $\frac{dy}{dx} < -1 \iff 2r_y^2 x > 2r_x^2 y$. Unit step in $y$.
  $$p2_0 = r_y^2\left(x_0 + \frac{1}{2}\right)^2 + r_x^2(y_0 - 1)^2 - r_x^2 r_y^2$$
  - If $p2_k > 0$: $p2_{k+1} = p2_k - 2r_x^2 y_{k+1} + r_x^2$
  - If $p2_k \le 0$: $p2_{k+1} = p2_k + 2r_y^2 x_{k+1} - 2r_x^2 y_{k+1} + r_x^2$

### 5.5 Scanline Polygon Fill Algorithm
Fills an arbitrary polygon using an **Edge Table (ET)** and an **Active Edge Table (AET)**:
1. Construct the Edge Table bucketed by minimum scanline $y_{\min}$. Horizontal edges ($\Delta y = 0$) are ignored.
2. For each scanline $y$ from $y_{\min}$ to $y_{\max}$:
   - Move edges from ET bucket $y$ into the AET.
   - Remove edges from AET where $y = y_{\max}$.
   - Sort AET entries by current $x$-intersection ($x_{\text{curr}}$).
   - Fill spans of pixels between pairs of intersections: $[x_{2j}, x_{2j+1}]$.
   - Advance $x_{\text{curr}} \mathrel{+}= 1/m = \Delta x / \Delta y$ for all active edges.

---

## 6. CGL-CO4: Projection and Viewing

### 6.1 The 3D Viewing Pipeline
$$\text{World Space} \xrightarrow{\mathbf{V} \ (\text{gluLookAt})} \text{Eye Space} \xrightarrow{\mathbf{P} \ (\text{glOrtho} / \text{gluPerspective})} \text{Clip Space} \xrightarrow{\div w} \text{NDC } [-1, 1]^3 \xrightarrow{\mathbf{M}_{vp} \ (\text{glViewport})} \text{Screen Pixels}$$

1. **Viewing Matrix $\mathbf{V}$**: Positions the camera using `gluLookAt(eye, center, up)`.
2. **Projection Matrix $\mathbf{P}$**: Defines the view volume and maps it to clip coordinates.
3. **Perspective Division**: Divides $[x_c, y_c, z_c, w_c]^T$ by $w_c$, yielding Normalized Device Coordinates (NDC) in $[-1, 1]^3$.
4. **Viewport Transformation $\mathbf{M}_{vp}$**: Maps NDC to pixel window coordinates $(x_w, y_w)$.

### 6.2 Orthographic vs Perspective Projection

| Feature | Orthographic Projection (`glOrtho`) | Perspective Projection (`gluPerspective`) |
| :--- | :--- | :--- |
| **Projectors** | Parallel to each other | Converge at Center of Projection (COP) |
| **Object Scale** | Invariant to distance $Z$ | Foreshortened inversely with distance ($1/z$) |
| **Vanishing Points** | None (parallel lines remain parallel) | 1, 2, or 3 vanishing points |
| **View Frustum** | Rectangular parallelepiped (box) | Truncated pyramid |
| **Use in AirStrike** | Tactical radar minimap, 2D HUD | Main 3D chase camera, asteroid depth |

---

## 7. CGL-CO5: Clipping Algorithms

### 7.1 Cohen-Sutherland Line Clipping Algorithm
Uses a 4-bit region outcode ($\text{TBRL} = \text{Top}, \text{Bottom}, \text{Right}, \text{Left}$) assigned to each endpoint:
* Bit 0: $\text{Left} = 1 \iff x < x_{\min}$
* Bit 1: $\text{Right} = 2 \iff x > x_{\max}$
* Bit 2: $\text{Bottom} = 4 \iff y < y_{\min}$
* Bit 3: $\text{Top} = 8 \iff y > y_{\max}$

```
 1001 | 1000 | 1010
 -----+------+-----  y_max
 0001 | 0000 | 0010
 -----+------+-----  y_min
 0101 | 0100 | 0110
     x_min  x_max
```

1. **Trivial Accept**: $(\text{code}_1 \mid \text{code}_2) == 0$ (Both endpoints inside the window).
2. **Trivial Reject**: $(\text{code}_1 \ \& \ \text{code}_2) \neq 0$ (Both endpoints share an outside region).
3. **Subdivision**: Select an endpoint outside the window, find its intersection with the boundary line, update the coordinates, and recalculate the outcode.

### 7.2 Liang-Barsky Parametric Line Clipping Algorithm
Represents the line parametrically:
$$P(u) = P_1 + u(P_2 - P_1), \quad 0 \le u \le 1$$
$$x(u) = x_1 + u\Delta x, \quad y(u) = y_1 + u\Delta y$$

The condition $x_{\min} \le x(u) \le x_{\max}$ and $y_{\min} \le y(u) \le y_{\max}$ is written as four inequalities:
$$u \cdot p_k \le q_k, \quad k \in \{1, 2, 3, 4\}$$
* $p_1 = -\Delta x, \quad q_1 = x_1 - x_{\min}$ (Left)
* $p_2 = \Delta x, \quad q_2 = x_{\max} - x_1$ (Right)
* $p_3 = -\Delta y, \quad q_3 = y_1 - y_{\min}$ (Bottom)
* $p_4 = \Delta y, \quad q_4 = y_{\max} - y_1$ (Top)

Parameters $u_1$ (entering) and $u_2$ (exiting) are updated:
* If $p_k < 0$: $u_1 = \max(u_1, q_k / p_k)$
* If $p_k > 0$: $u_2 = \min(u_2, q_k / p_k)$
* If $u_1 > u_2$, the line is completely rejected.

**Efficiency Advantage**: Replaces floating-point line intersection calculations with parameter comparisons.

### 7.3 Sutherland-Hodgman Polygon Clipping Algorithm
Clips a polygon against each of the four infinite boundary lines (Left, Right, Bottom, Top) sequentially:
For each edge $(V_{\text{prev}}, V_{\text{curr}})$:
* **In $\to$ In**: Emit $V_{\text{curr}}$.
* **In $\to$ Out**: Emit intersection point $I$.
* **Out $\to$ Out**: Emit nothing.
* **Out $\to$ In**: Emit intersection point $I$, then emit $V_{\text{curr}}$.

---

## 8. CGL-CO6: Texture, Lighting and Color Models

### 8.1 Color Spaces
* **RGB**: Additive color model with primary color components $[0.0, 1.0]^3$.
* **RGBA**: Adds alpha channel $a \in [0.0, 1.0]$ for transparency via alpha blending:
  $$\mathbf{C}_{\text{out}} = \alpha \cdot \mathbf{C}_{\text{src}} + (1 - \alpha) \cdot \mathbf{C}_{\text{dst}}$$
* **HSV**: Cylindrical representation parameterized by Hue ($[0, 360^\circ]$), Saturation ($[0, 1]$), and Value ($[0, 1]$). Converted to RGB via piecewise chroma functions.

### 8.2 Shading Models vs Illumination Models
It is crucial to distinguish between an **illumination model** and a **shading model**:
* **Phong Illumination Model**: The mathematical reflection formula combining Ambient, Diffuse (Lambert), and Specular highlights:
  $$I = I_{\text{ambient}} + I_{\text{diffuse}} + I_{\text{specular}} = k_a L_a + k_d L_d \max(0, \vec{N} \cdot \vec{L}) + k_s L_s \max(0, \vec{R} \cdot \vec{V})^n$$
* **Gouraud Shading (`GL_SMOOTH`)**: Per-vertex lighting. Surface normals are specified at vertices, the Phong illumination model is calculated at each vertex, and the resulting colors are bilinearly interpolated across the polygon fragments. This is the hardware mode utilized by fixed-function OpenGL.
* **Phong Shading (Per-Fragment)**: Surface normals are interpolated across polygon fragments, and the reflection equation is evaluated at every individual pixel. Standard fixed-function OpenGL 2.1 does **not** perform Phong shading (which requires custom GLSL fragment shaders).
* **Flat Shading (`GL_FLAT`)**: A single normal is assigned per polygon face, producing visible polygonal facets.

### 8.3 The Phong Reflection Model Components
Total surface illumination $I$ combines three components:
1. **Ambient Component ($I_a$)**: Background indirect illumination scattering:
   $$I_a = k_a \cdot L_a$$
2. **Diffuse Component ($I_d$)**: Lambert's Cosine Law for rough matte surfaces:
   $$I_d = k_d \cdot L_d \cdot \max(0, \vec{N} \cdot \vec{L})$$
3. **Specular Component ($I_s$)**: Glossy mirror-like reflection highlight:
   $$I_s = k_s \cdot L_s \cdot \max(0, \vec{R} \cdot \vec{V})^n$$
   where $\vec{R} = 2(\vec{N} \cdot \vec{L})\vec{N} - \vec{L}$, and $n$ is the material shininess exponent.

### 8.4 Actual Image Texture Loading & Texture Mapping
In compliance with Phase 5.1, `CGL/src/common/image_loader.cpp` implements a zero-dependency 24-bit uncompressed Windows Bitmap (`.bmp`) loader (`loadBMP`):
* Reads `BITMAPFILEHEADER` and `BITMAPINFOHEADER` from disk.
* Unpacks pixel rows with 4-byte padding alignment and converts BGR order to RGB.
* **Actual Local Texture Assets**:
  - `CGL/assets/textures/space_hull.bmp`: 256×256 24-bit metallic starfighter plating with rivets, circuit traces, and hazard warning chevrons.
  - `CGL/assets/textures/alien_surface.bmp`: 256×256 24-bit cratered asteroid terrain with glowing magma channels.
* **Graceful Fallback**: If the local image file is missing or corrupted, the system reports a console warning and falls back cleanly to the procedural in-memory texture generators without crashing.
* **Texture Parameters**:
  - Wrapping modes: `GL_REPEAT` (tiled) vs `GL_CLAMP` (clamped borders), toggled via key `[K]`.
  - Filtering modes: `GL_LINEAR` (bilinear interpolation) vs `GL_NEAREST` (point sampling), toggled via key `[F]`.
  - Texture source toggle: local file vs procedural in-memory, toggled via key `[U]`.

---

## 9. Comprehensive Viva Voce Questions & Answers

### Q1: What is the OpenGL State Machine and how does it execute commands?
**Answer**: OpenGL maintains an internal state consisting of active colors, transformation matrices, lighting parameters, and primitive modes. Functions like `glColor3f`, `glEnable`, or `glMatrixMode` modify these states, which remain active for all subsequent drawing commands until explicitly modified.

### Q2: Why is double buffering necessary in real-time interactive computer graphics?
**Answer**: Single buffering causes visual tearing and flickering because the display hardware scans the framebuffer while rasterization commands are updating it. Double buffering draws into a hidden back buffer and swaps buffer pointers atomically during vertical blanking (`glutSwapBuffers()`), presenting only fully rendered frames.

### Q3: What is the significance of vertex winding order in OpenGL?
**Answer**: By default, vertices specified in Counter-Clockwise (CCW) order define the front face of a polygon, while Clockwise (CW) vertices define the back face. This enables Back-Face Culling (`glCullFace(GL_BACK)`), which skips rendering non-visible faces and doubles rasterization throughput.

### Q4: Why are homogeneous coordinates $(x, y, z, 1)$ necessary for affine transformations?
**Answer**: In Cartesian coordinates, translation is vector addition ($v + t$), which cannot be combined with matrix multiplication ($R \cdot v$). Homogeneous coordinates add a fourth dimension ($w=1$), allowing translation to be expressed as a linear matrix multiplication ($T \cdot v$) and enabling unified composite transformation pipelines.

### Q5: Why is matrix multiplication non-commutative ($T \cdot R \neq R \cdot T$)?
**Answer**: $T \cdot R$ translates an object and then rotates it around its local origin. In contrast, $R \cdot T$ rotates the translation vector itself, causing the object to orbit around the world coordinate origin at distance $T$.

### Q6: How is rotation about an arbitrary pivot point $(x_p, y_p)$ performed mathematically?
**Answer**: By applying the composite sequence $M = T(x_p, y_p) \cdot R(\theta) \cdot T(-x_p, -y_p)$. The pivot point is translated to the origin, the rotation is evaluated, and the object is translated back to the original pivot location.

### Q7: Why is Bresenham's line algorithm computationally superior to DDA?
**Answer**: DDA requires floating-point additions and runtime rounding per pixel ($\text{round}(y)$). Bresenham's algorithm uses an integer decision variable ($p_k$), eliminating floating-point math, divisions, and rounding, and executing using fast integer additions and bit shifts.

### Q8: What is the initial decision parameter for Bresenham's line algorithm in Octant 1?
**Answer**: $p_0 = 2\Delta y - \Delta x$. If $p_k < 0$, the East pixel is chosen and $p_{k+1} = p_k + 2\Delta y$. If $p_k \ge 0$, the North-East pixel is chosen and $p_{k+1} = p_k + 2\Delta y - 2\Delta x$.

### Q9: Explain 8-way symmetry in the Midpoint Circle algorithm.
**Answer**: A circle centered at $(x_c, y_c)$ is symmetric across the four coordinate axes and the diagonals $y = \pm x$. Calculating points in one octant ($0 \le x \le y$) produces eight symmetric pixels: $(x_c \pm x, y_c \pm y)$ and $(x_c \pm y, y_c \pm x)$, reducing trigonometric/algebraic evaluations by $87.5\%$.

### Q10: How does the Midpoint Ellipse algorithm handle the transition between Region 1 and Region 2?
**Answer**: Region 1 corresponds to a slope $|m| < 1$, where unit steps are taken along $x$. Region 2 corresponds to a slope $|m| \ge 1$, where unit steps are taken along $y$. The transition boundary occurs when the tangent slope equals $-1$, which corresponds to $2r_y^2 x = 2r_x^2 y$.

### Q11: How does the Scanline polygon fill algorithm avoid redrawing pixels?
**Answer**: It constructs an Edge Table (ET) sorted by minimum scanline $y$. An Active Edge Table (AET) tracks only edges intersecting the current scanline. Edge intersections are sorted by $x$, and pixels are filled between pairs using the parity rule ($[x_0, x_1], [x_2, x_3]$), rendering each interior pixel exactly once.

### Q12: What is the 3D Viewing Pipeline?
**Answer**: The transformation sequence that maps 3D world geometry to 2D screen pixels: World Coordinates $\xrightarrow{\mathbf{V}}$ Eye Space $\xrightarrow{\mathbf{P}}$ Clip Coordinates $\xrightarrow{\div w}$ Normalized Device Coordinates (NDC) $\xrightarrow{\mathbf{M}_{vp}}$ Viewport Screen Pixels.

### Q13: What is the fundamental difference between Orthographic and Perspective projection?
**Answer**: In orthographic projection, projectors are parallel and objects maintain constant size regardless of distance $Z$. In perspective projection, projectors converge at the Center of Projection (COP), causing objects to foreshorten inversely with depth ($1/z$).

### Q14: What is Perspective Division and why does it occur after the projection matrix?
**Answer**: Perspective projection represents depth scaling as $w = -z$. Perspective division divides $(x_c, y_c, z_c)$ by $w_c$ to obtain NDC in $[-1, 1]^3$, producing the non-linear foreshortening characteristic of optical lenses.

### Q15: How does the Cohen-Sutherland algorithm perform trivial acceptance and rejection?
**Answer**: Using 4-bit region outcodes ($\text{TBRL}$):
* Trivial Accept: $(\text{code}_1 \mid \text{code}_2) == 0$ (both endpoints are inside).
* Trivial Reject: $(\text{code}_1 \ \& \ \text{code}_2) \neq 0$ (both endpoints share an exterior half-plane).

### Q16: Why is the Liang-Barsky line clipping algorithm faster than Cohen-Sutherland?
**Answer**: Cohen-Sutherland repeatedly recalculates line-boundary intersections using floating-point slope divisions. Liang-Barsky uses a parametric formulation $P(u) = P_1 + u\Delta P$ and solves four simple inequalities ($u \cdot p_k \le q_k$), replacing multiple intersection calculations with simple min/max parameter updates.

### Q17: What is the known limitation of the Sutherland-Hodgman polygon clipping algorithm?
**Answer**: When clipping concave polygons that wrap around clipping boundaries, Sutherland-Hodgman produces extraneous connecting lines across the polygon boundary. The Weiler-Atherton algorithm resolves this by generating separate polygon contours.

### Q18: What are the three components of the Phong Illumination Model?
**Answer**:
1. Ambient ($I_a = k_a L_a$): Constant indirect background illumination.
2. Diffuse ($I_d = k_d L_d (\vec{N} \cdot \vec{L})$): Lambertian reflection scattering equally in all directions.
3. Specular ($I_s = k_s L_s (\vec{R} \cdot \vec{V})^n$): Glossy mirror highlight focused around reflection vector $\vec{R}$.

### Q19: What is the difference between Flat shading and Gouraud shading?
**Answer**: Flat shading computes a single normal per polygon, resulting in visible faceting. Gouraud shading computes normal vectors at vertices, evaluates lighting per vertex, and bilinearly interpolates colors across fragments, producing smooth surface curvature.

### Q20: Explain the difference between `GL_NEAREST` and `GL_LINEAR` texture filtering.
**Answer**: `GL_NEAREST` selects the single nearest texel to the pixel center, producing sharp, pixelated, retro-style textures. `GL_LINEAR` performs bilinear interpolation among the four surrounding texels, producing smooth transitions.

---

## 10. Automated Test Suite & Screenshot Gallery

### 10.1 Automated Test Execution
Run the automated test harness:
```bash
bin/cgl/cgl_runner.exe --test
```
**Result**: **65 out of 65 assertions pass (100% test success rate)** across all six Course Outcomes:
* **CGL-CO1**: 4 Assertions (Point2D, Vec2, Color4 data types and equality).
* **CGL-CO2**: 12 Assertions (4×4 matrices, coordinate transformations for translation, rotation, scaling, reflection across X and Y axes, arbitrary pivot point rotation and invariance, combined transformation coordinates, and coordinate proof of non-commutativity).
* **CGL-CO3**: 21 Assertions (DDA line, Bresenham across all 8 octants, negative slopes, reverse direction, vertical & horizontal segments, Midpoint circle symmetry & equation adherence, Midpoint ellipse intercepts & regions, Scanline polygon fill interior coverage, 3D cube vertex transformations, and Euler rotations Rx & Ry).
* **CGL-CO4**: 7 Assertions (Orthographic projection depth invariance, Perspective projection $1/z$ foreshortening ratio, camera LookAt view matrix, and Viewport dimensions mapping NDC to screen pixels).
* **CGL-CO5**: 10 Assertions (Cohen-Sutherland TBRL outcodes, trivial accept, trivial reject, horizontal edge clipping, Liang-Barsky parametric values $u_1, u_2$ and boundary points, and Sutherland-Hodgman polygon clipping).
* **CGL-CO6**: 11 Assertions (HSV to RGB conversion, actual local BMP image loading, missing texture graceful handling, procedural in-memory fallback buffers, Phong Ambient, Diffuse, and Specular calculations, reflection vector formula, and Gouraud shading per-vertex bilinear interpolation).

### 10.2 Captured Screenshot Gallery
The following screenshots were generated directly by `cgl_runner.exe --screenshot` via `glReadPixels`:

| Screenshot File | Description | Target Course Outcome |
| :--- | :--- | :--- |
| `docs/screenshots/CGL/cgl_co1_primitives.bmp` | 8-Cell OpenGL Primitives Display | **CGL-CO1** |
| `docs/screenshots/CGL/cgl_co2_transformations.bmp` | Matrix Controller & Hierarchical Drones | **CGL-CO2** |
| `docs/screenshots/CGL/cgl_co3_algorithms.bmp` | DDA vs Bresenham, Curves & 3D Meshes | **CGL-CO3** |
| `docs/screenshots/CGL/cgl_co4_projection.bmp` | Split Viewport: Ortho Radar & Cockpit | **CGL-CO4** |
| `docs/screenshots/CGL/cgl_co5_clipping.bmp` | Boundary Clipping Canvas | **CGL-CO5** |
| `docs/screenshots/CGL/cgl_co6_appearance.bmp` | Phong Lighting & Local BMP File Texture | **CGL-CO6** |
| `docs/screenshots/CGL/cgl_integrated_scene.bmp` | Integrated 2D/3D Space Combat Arena | **All Outcomes** |

---

## 11. Build and Run Instructions

### Direct Compilation with MinGW GCC:
```cmd
scripts\build_cgl.bat
```
or via PowerShell:
```powershell
.\scripts\build_cgl.ps1
```

### Launch Interactive Graphics Environment:
```cmd
bin\cgl\cgl_runner.exe
```

### Keyboard Controls Summary:
* `[1]`–`[6]`: Switch between Course Outcome Demos (CO1 to CO6)
* `[0]`: Switch to Integrated 2D/3D AirStrike Combat Arena
* `[W]`: Toggle Wireframe in CO1 / Move Ship Up in Combat Scene / Translate Up in CO2
* `[Arrows]` / `[WASD]`: Maneuver player fighter / translate objects / orbit camera
* `[Q]` / `[E]`: Rotate object in CO2
* `[+]` / `[-]`: Scale object / adjust algorithm radius / camera zoom / shininess
* `[M]`: Cycle modes in CO3 (Lines, Curves, Scanline, 3D) and CO5 (Clipping algorithms)
* `[P]`: Toggle Pivot Point in CO2 / Toggle Projection (Split, Ortho, Perspective) in CO4
* `[L]`: Toggle Dynamic Lighting on/off in CO6
* `[T]`: Toggle Texture Mapping on/off in CO6
* `[U]`: Toggle Texture Source (Local BMP Image File vs Procedural In-Memory) in CO6
* `[K]`: Toggle Texture Wrapping (`GL_REPEAT` vs `GL_CLAMP`) in CO6
* `[F]`: Toggle Reflection in CO2 / Toggle Texture Filtering (`GL_LINEAR` vs `GL_NEAREST`) in CO6
* `[S]`: Toggle Shading (Flat vs Smooth Gouraud) in CO6 / Toggle Shield in Combat Scene
* `[7]` / `[8]` / `[9]`: Toggle Ambient, Diffuse, and Specular light components in CO6
* `[SPACE]`: Fire Lasers in Combat Scene / Pause animation
* `[R]`: Reset current demonstration parameters
* `[ESC]`: Exit application cleanly

---

## 12. Phase 5.1 Technical Audit & Corrections Report

### 12.1 Issues Identified and Addressed

1. **Local Image File Texture Loading**:
   - *Previous state*: Only procedural in-memory textures were demonstrated.
   - *Correction*: Developed `CGL/src/common/image_loader.cpp` and `.hpp` implementing a zero-dependency uncompressed 24-bit BMP loader (`loadBMP`). Placed actual image assets `CGL/assets/textures/space_hull.bmp` and `alien_surface.bmp` (256×256 pixels, 24bpp) on disk. Mapped them to the 3D starfighter and sphere with texture coordinates, texture wrapping (`GL_REPEAT`/`GL_CLAMP`), and filtering (`GL_LINEAR`/`GL_NEAREST`). Handled missing files gracefully with fallback to procedural generation.
2. **Transformation Coordinate Verification**:
   - *Previous state*: Tests only asserted 4×4 matrix elements.
   - *Correction*: Added `transformPoint` function and created rigorous tests verifying expected transformed coordinates for translation, rotation, scaling, reflection across X-axis ($(2, 3) \to (2, -3)$), reflection across Y-axis ($(2, 3) \to (-2, 3)$), arbitrary pivot point rotation ($(2, 1) \to (1, 2)$ around pivot $(1, 1)$), pivot invariance, and coordinate proof of non-commutativity ($(2, 1) \neq (0, 3)$).
3. **Bresenham & Raster Algorithm Test Coverage**:
   - *Previous state*: Bresenham was tested on Octant 1 and Octant 2.
   - *Correction*: Expanded test suite to systematically verify all eight octants (Octants 1–8), negative slopes, reverse line traversal, vertical and horizontal lines, Midpoint circle equation adherence, Midpoint ellipse axis intercepts, 3D cube vertex centroid transformations, and 3D Euler rotations Rx and Ry.
4. **Projection & Viewing Verification**:
   - *Previous state*: Evaluated simple ratios.
   - *Correction*: Added pure mathematical matrix generation functions (`getOrthoMatrix`, `getPerspectiveMatrix`, `getLookAtMatrix`, `projectPoint`, `viewportMap`) to `ViewingDemo`. Asserted depth invariance in orthographic view ($X_{\text{ndc}} = 0.5$ regardless of $Z$), perspective $1/z$ foreshortening ratio ($5.0\times$), camera eye-space transformation, and screen viewport mapping.
5. **Lighting & Shading Technical Accuracy**:
   - *Previous state*: Report ambiguously referenced Phong shading.
   - *Correction*: Clarified that fixed-function OpenGL 2.1 implements **Gouraud Shading** (`GL_SMOOTH`), which evaluates the **Phong Illumination Model** (Ambient + Diffuse + Specular reflection equation) per-vertex and interpolates colors across fragments. Explicitly noted that per-fragment Phong shading requires custom GLSL fragment shaders.
6. **Automated Test Count Expansion**:
   - Tests increased from 42 to **65 assertions**, all passing with a 100% success rate.
7. **Screenshot Updates**:
   - Captured 7 genuine 24-bit BMP screenshots showing actual local BMP texture loading and updated HUD overlays.

### 12.2 Remaining Technical Scope & Academic Limitations
* **Hardware Profile**: Uses standard fixed-function OpenGL 2.1 / Compatibility Profile and GLUT for maximum portability across college computing laboratory environments without requiring external shader loaders (GLEW/GLAD) or modern Vulkan/DirectX drivers.
* **Texture Format**: The native file loader supports uncompressed 24-bit and 32-bit Windows Bitmap (`.bmp`) files. Compressed image formats like JPEG or PNG are intentionally omitted to avoid introducing complex third-party dependencies (e.g. `libpng`, `libjpeg`, `zlib`).

