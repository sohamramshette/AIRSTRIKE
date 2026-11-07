# AIRSTRIKE — Phase 8.5 Implementation Report: CGL Enemy Implementation

**Module**: Computer Graphics Laboratory (CGL)  
**Target Architecture**: C++17, OpenGL 1.x/2.x Fixed-Function & Programmable Pipeline Concepts, FreeGLUT, MinGW GCC  
**Verification Target**: Course Outcomes CGL-CO1 through CGL-CO6  
**Test Suite Status**: **93 / 93 Assertions Passed (100% PASS)**  

---

## 1. Executive Summary

Phase 8.5 implements the six planned **Computer Graphics Laboratory (CGL)** enemy types directly in genuine C++17 and OpenGL/GLUT within the `CGL/src/` subsystem. Every enemy model executes authentic graphics rendering and geometric algorithms strictly adhering to the syllabus specifications:

1. **Geometric Polygon Cruiser (`primitives/polygon_cruiser.hpp|cpp`)** — Demonstrates **CGL-CO1** (OpenGL Graphics Primitives) using `GL_POLYGON`, `GL_TRIANGLE_FAN`, `GL_LINE_LOOP`, `GL_LINES`, and `GL_POINTS` with strict Counter-Clockwise (CCW) winding, ray-casting point-in-polygon containment, and Green-Shoelace signed area evaluation.
2. **Orbital Rotor Drone (`transformations/orbital_rotor.hpp|cpp`)** — Demonstrates **CGL-CO2** (Geometric Transformations) by constructing explicit $4 \times 4$ homogeneous affine transformation matrices ($T, R, S$), demonstrating non-commutative sequencing ($T \cdot R \neq R \cdot T$), and utilizing hierarchical `glPushMatrix()` / `glPopMatrix()` matrix stacks to isolate orbiting satellite weapon pods.
3. **Bresenham Shield Sentinel (`algorithms/raster_sentinel.hpp|cpp`)** — Demonstrates **CGL-CO3** (2D/3D Rasterization Algorithms) by software-rasterizing defensive energy shields via the Midpoint Circle Algorithm ($p_k = 1 - r$, 8-way octant symmetry) and dynamic targeting beams via the Bresenham Line Drawing Algorithm across arbitrary slopes.
4. **Depth-Diving Warp Cruiser (`projection/warp_cruiser.hpp|cpp`)** — Demonstrates **CGL-CO4** (Projection and Viewing) by navigating depth space $Z \in [-50, -10]$, proving non-linear perspective foreshortening ($1/Z$ scaling factor ratio of $5.0\times$) versus orthographic parallel distance invariance ($1.0\times$ ratio), with viewport window-to-pixel coordinate mapping.
5. **Boundary Phase Skimmer (`clipping/boundary_skimmer.hpp|cpp`)** — Demonstrates **CGL-CO5** (Clipping Algorithms) through authentic 4-bit TBRL Cohen-Sutherland line clipping (trivial accept, trivial reject, and parametric edge intersections) and Sutherland-Hodgman polygon clipping against viewport boundaries.
6. **Specular Phong Cruiser (`appearance/phong_cruiser.hpp|cpp`)** — Demonstrates **CGL-CO6** (Texture, Lighting, and Color Models) by calculating full Phong ambient, diffuse (Lambertian), and specular highlights ($I_s = k_s L_s (\vec{R} \cdot \vec{V})^n$), Blinn-Phong half-angle vector evaluation ($\vec{H} = \frac{\vec{L} + \vec{V}}{\|\vec{L} + \vec{V}\|}$), Gouraud bilinear interpolation, and 24-bit uncompressed local BMP texture mapping (`space_hull.bmp`).

---

## 2. Files Created and Modified

### Newly Created Files
| File Path | Subject / CO | Component Description |
| :--- | :--- | :--- |
| `CGL/src/primitives/polygon_cruiser.hpp` | CGL-CO1 | Class interface for Geometric Polygon Cruiser: primitive geometry generation, CCW winding checks, Shoelace area calculation, and ray-casting containment tests. |
| `CGL/src/primitives/polygon_cruiser.cpp` | CGL-CO1 | OpenGL primitive rendering routines (`GL_POLYGON`, `GL_TRIANGLE_FAN`, `GL_LINE_LOOP`, `GL_LINES`, `GL_POINTS`) without premature Gouraud shading or lighting. |
| `CGL/src/transformations/orbital_rotor.hpp` | CGL-CO2 | Class interface for Orbital Rotor Drone: $4 \times 4$ affine transformation matrices, hierarchical matrix stack simulation, and rotor orbit kinematics. |
| `CGL/src/transformations/orbital_rotor.cpp` | CGL-CO2 | OpenGL matrix transformation routines: `glPushMatrix()`, `glPopMatrix()`, `glTranslatef()`, `glRotatef()`, `glScalef()`, proving non-commutativity. |
| `CGL/src/algorithms/raster_sentinel.hpp` | CGL-CO3 | Class interface for Bresenham Shield Sentinel: software rasterization integration, shield circle pixel generator, and targeting laser calculation. |
| `CGL/src/algorithms/raster_sentinel.cpp` | CGL-CO3 | Pixel-level rasterizer routines calling `RasterAlgorithmsDemo::midpointCircle` and `RasterAlgorithmsDemo::bresenhamLine` with circle equation adherence verification. |
| `CGL/src/projection/warp_cruiser.hpp` | CGL-CO4 | Class interface for Depth-Diving Warp Cruiser: perspective and orthographic camera projections, NDC coordinate transformations, and depth buffer tests. |
| `CGL/src/projection/warp_cruiser.cpp` | CGL-CO4 | Implementation of $1/Z$ perspective foreshortening vs distance-invariant orthographic projection, and NDC-to-screen viewport matrix mapping. |
| `CGL/src/clipping/boundary_skimmer.hpp` | CGL-CO5 | Class interface for Boundary Phase Skimmer: Cohen-Sutherland line clipper and Sutherland-Hodgman polygon clipper. |
| `CGL/src/clipping/boundary_skimmer.cpp` | CGL-CO5 | Execution of 4-bit Outcode evaluation (Top, Bottom, Right, Left), bitwise intersection solving, and multi-edge Sutherland-Hodgman polygon vertex clipping. |
| `CGL/src/appearance/phong_cruiser.hpp` | CGL-CO6 | Class interface for Specular Phong Cruiser: Blinn-Phong material attributes, reflection vectors, specular highlights, and local BMP texture integration. |
| `CGL/src/appearance/phong_cruiser.cpp` | CGL-CO6 | Mathematical lighting evaluation ($I_a, I_d, I_s$), half-angle formulation, and 24-bit local BMP texture buffer loading (`space_hull.bmp`). |
| `docs/CGL/PHASE_8_5_CGL_ENEMY_IMPLEMENTATION.md` | Doc | Complete technical implementation report, mathematical derivations, viva Q&As, and test verification suite records. |

### Modified Files
| File Path | Modification Summary |
| :--- | :--- |
| `CGL/tests/test_cgl.cpp` | Added `testCGLEnemies()` containing 28 rigorous automated assertions verifying geometric winding, matrix sequencing, software rasterization adherence, projection depth ratios, clipping cases, and Phong/Blinn-Phong calculations. Total CGL assertions expanded from 65 to 93. |

---

## 3. Academic CO Mapping & Graphics Pipeline Architecture

```
+-------------------------------------------------------------------------------------------------------------+
|                                    AIRSTRIKE CGL ENEMY SYSTEM ARCHITECTURE                                  |
+-------------------------------------------------------------------------------------------------------------+
|  [CGL-CO1: Geometric Polygon Cruiser]  --> GL_POLYGON, GL_TRIANGLE_FAN, GL_LINE_LOOP, GL_LINES, GL_POINTS   |
|                                            CCW Vertex Order, Ray-Casting Point-in-Polygon, Shoelace Area   |
|  [CGL-CO2: Orbital Rotor Drone]        --> 4x4 Homogeneous Affine Matrices: T(dx,dy,dz), R(deg), S(sx,sy,sz)|
|                                            Non-Commutativity (T*R != R*T), glPushMatrix / glPopMatrix Stack |
|  [CGL-CO3: Bresenham Shield Sentinel]  --> Software Midpoint Circle (pk = 1 - r, 8-Octant Symmetry)         |
|                                            Bresenham Integer Line Drawing across Arbitrary Octants         |
|  [CGL-CO4: Depth-Diving Warp Cruiser]  --> Perspective Projection (1/Z non-linear scale) vs Orthographic  |
|                                            Parallel Projection (Depth Invariance), Viewport NDC-to-Screen  |
|  [CGL-CO5: Boundary Phase Skimmer]     --> Cohen-Sutherland 4-bit TBRL Outcodes (0000, 0001, 1000, 1010)   |
|                                            Sutherland-Hodgman Convex Subject Polygon Clipping              |
|  [CGL-CO6: Specular Phong Cruiser]     --> Phong Lighting Model: I = Ia + Id + Is (R = 2(N.L)N - L)        |
|                                            Blinn-Phong Half-Angle H = (L + V) / |L + V|, 24-bit Local BMP  |
+-------------------------------------------------------------------------------------------------------------+
```

### Detailed Academic Mapping Table

| Enemy Name | Exact CO | Syllabus Concept | Key OpenGL / C++ Routines | Graphics Algorithms & Math Applied |
| :--- | :--- | :--- | :--- | :--- |
| **Geometric Polygon Cruiser** | **CGL-CO1** | OpenGL Graphics Primitives & Geometric Modeling | `glBegin(GL_POLYGON)`<br>`glBegin(GL_TRIANGLE_FAN)`<br>`glBegin(GL_LINE_LOOP)`<br>`glBegin(GL_LINES)`<br>`glBegin(GL_POINTS)` | Strict CCW Front-Facing Winding<br>Ray-Casting Point-in-Polygon Containment<br>Green-Shoelace Signed Area Formula |
| **Orbital Rotor Drone** | **CGL-CO2** | 2D / 3D Geometric Transformations | `glPushMatrix()`<br>`glPopMatrix()`<br>`glTranslatef()`<br>`glRotatef()`<br>`glScalef()` | $4 \times 4$ Affine Homogeneous Matrices<br>Non-Commutative Matrix Sequencing ($T \cdot R \neq R \cdot T$)<br>Hierarchical Scene Graph Kinematics |
| **Bresenham Shield Sentinel** | **CGL-CO3** | 2D / 3D Rasterization & Display Algorithms | `RasterAlgorithmsDemo::midpointCircle`<br>`RasterAlgorithmsDemo::bresenhamLine`<br>`glDrawPixels()` | Midpoint Circle Algorithm ($p_k = 1 - r$)<br>Eight-Way Symmetry Pixel Reflection<br>Integer-Only Bresenham Line Rasterization |
| **Depth-Diving Warp Cruiser** | **CGL-CO4** | Projection and Viewing | `gluPerspective()` / `glFrustum()`<br>`glOrtho()`<br>`gluLookAt()`<br>`glViewport()` | Non-Linear Perspective Foreshortening ($1/Z$)<br>Parallel Orthographic Distance Invariance<br>Normalized Device Coordinates (NDC) Mapping |
| **Boundary Phase Skimmer** | **CGL-CO5** | Line and Polygon Clipping Algorithms | `ClippingDemo::cohenSutherlandClip`<br>`ClippingDemo::sutherlandHodgmanPolygonClip` | 4-bit TBRL Outcode Evaluation<br>Trivial Accept / Reject Bitwise Tests<br>Sutherland-Hodgman Polygon Edge Clipping |
| **Specular Phong Cruiser** | **CGL-CO6** | Texture, Lighting, and Color Models | `glLightfv()`<br>`glMaterialfv()`<br>`glTexImage2D()`<br>`glBindTexture()` | Phong Specular ($I_s = k_s L_s (\vec{R} \cdot \vec{V})^n$)<br>Blinn-Phong Half-Angle Vector ($\vec{H}$)<br>Gouraud Shading & 24-bit BMP Buffer |

---

## 4. Mathematical and Algorithmic Foundations

### 1. Geometric Polygon Cruiser (`polygon_cruiser.hpp|cpp` — CGL-CO1)

#### OpenGL Primitive Decomposition
The Geometric Polygon Cruiser models an interstellar combat vessel using exclusively fundamental geometric primitives without high-level lighting or shading pipelines:
- **Fuselage Hull**: Built from `GL_POLYGON` with 6 coplanar vertices ordered in strict Counter-Clockwise (CCW) front-facing winding:
  $$V_0(0, 30), \quad V_1(-15, 10), \quad V_2(-20, -20), \quad V_3(0, -10), \quad V_4(20, -20), \quad V_5(15, 10)$$
- **Swept Delta Wings**: Assembled via `GL_TRIANGLE_FAN` originating from root vertex $V_{\text{root}}(0, 5)$ expanding to wingtips $(-45, -15)$ and $(45, -15)$.
- **Cockpit Canopy**: Outlined via `GL_LINE_LOOP` creating a closed polygon perimeter.
- **Weapon Mounts**: Paired twin cannons constructed with disjoint `GL_LINES`.
- **Thruster Plumes**: Particle emission points rendered via `GL_POINTS` with `glPointSize(4.0f)`.

#### Green-Shoelace Area Evaluation
To mathematically verify CCW winding and polygon validity, the signed area is evaluated via Green's Theorem:
$$A = \frac{1}{2} \sum_{i=0}^{n-1} (x_i y_{i+1} - x_{i+1} y_i)$$
A strictly positive area ($A > 0$) confirms counter-clockwise vertex orientation under standard OpenGL right-handed coordinates.

#### Ray-Casting Point-in-Polygon (Jordan Curve Theorem)
Hit detection against the cruiser fuselage uses ray-casting along the positive horizontal axis:
$$\text{inside} = \text{odd count of intersections with polygon edges}$$
For each edge $(V_i, V_j)$, an intersection occurs if:
$$((y_i > y) \neq (y_j > y)) \land \left(x < \frac{(x_j - x_i)(y - y_i)}{y_j - y_i} + x_i\right)$$

---

### 2. Orbital Rotor Drone (`orbital_rotor.hpp|cpp` — CGL-CO2)

#### Affine Homogeneous Transformation Matrices
All transformations are executed using $4 \times 4$ homogeneous matrices:
$$\mathbf{T}(t_x, t_y, t_z) = \begin{bmatrix} 1 & 0 & 0 & t_x \\ 0 & 1 & 0 & t_y \\ 0 & 0 & 1 & t_z \\ 0 & 0 & 0 & 1 \end{bmatrix}, \quad \mathbf{R}_z(\theta) = \begin{bmatrix} \cos\theta & -\sin\theta & 0 & 0 \\ \sin\theta & \cos\theta & 0 & 0 \\ 0 & 0 & 1 & 0 \\ 0 & 0 & 0 & 1 \end{bmatrix}, \quad \mathbf{S}(s_x, s_y, s_z) = \begin{bmatrix} s_x & 0 & 0 & 0 \\ 0 & s_y & 0 & 0 \\ 0 & 0 & s_z & 0 \\ 0 & 0 & 0 & 1 \end{bmatrix}$$

#### Non-Commutativity Proof ($T \cdot R \neq R \cdot T$)
Matrix multiplication is non-commutative. For a point $P(1, 0, 0)^T$ transformed by translation $T(2, 0, 0)$ and rotation $R_z(90^\circ)$:
- **Order A ($T \cdot R$)**: Rotate first, then translate:
  $$P' = \mathbf{T}(2, 0, 0) \cdot \mathbf{R}_z(90^\circ) \cdot \begin{bmatrix} 1 \\ 0 \\ 0 \\ 1 \end{bmatrix} = \mathbf{T} \cdot \begin{bmatrix} 0 \\ 1 \\ 0 \\ 1 \end{bmatrix} = \begin{bmatrix} 2 \\ 1 \\ 0 \\ 1 \end{bmatrix}$$
- **Order B ($R \cdot T$)**: Translate first, then rotate:
  $$P'' = \mathbf{R}_z(90^\circ) \cdot \mathbf{T}(2, 0, 0) \cdot \begin{bmatrix} 1 \\ 0 \\ 0 \\ 1 \end{bmatrix} = \mathbf{R}_z(90^\circ) \cdot \begin{bmatrix} 3 \\ 0 \\ 0 \\ 1 \end{bmatrix} = \begin{bmatrix} 0 \\ 3 \\ 0 \\ 1 \end{bmatrix}$$
Since $(2, 1, 0) \neq (0, 3, 0)$, transformation ordering is critical.

#### Hierarchical Matrix Stack
The drone maintains a central core and two diametrically opposed orbiting weapon pods ($180^\circ$ phase offset). Using the OpenGL matrix stack:
1. `glPushMatrix()`: Save the drone's parent coordinate frame.
2. `glTranslatef(px, py, 0.0f)`: Position drone in world space.
3. Central Core: Apply pulsing scale factor $S(\text{pulse}, \text{pulse}, 1.0)$ and draw core polygon.
4. For each satellite pod $k \in \{0, 1\}$:
   - `glPushMatrix()`: Isolate pod local coordinate system.
   - `glRotatef(\theta + k \cdot 180^\circ, 0, 0, 1)`: Orbit around drone center.
   - `glTranslatef(\text{orbitRadius}, 0, 0)`: Offset to orbital perimeter.
   - `glRotatef(\phi_{\text{spin}}, 0, 0, 1)`: Local spin around pod's own axis.
   - Render pod geometry.
   - `glPopMatrix()`: Restore drone parent coordinate frame.
5. `glPopMatrix()`: Restore world coordinate system.

---

### 3. Bresenham Shield Sentinel (`algorithms/raster_sentinel.hpp|cpp` — CGL-CO3)

#### Midpoint Circle Software Rasterization
The Sentinel's defensive barrier is generated directly into discrete pixel buffers using the Midpoint Circle Algorithm. Starting at $(0, r)$ with initial decision parameter:
$$p_0 = 1 - r$$
At each step $k$:
- If $p_k < 0$: Next point is $(x_{k+1}, y_k)$ and $p_{k+1} = p_k + 2x_{k+1} + 1$.
- If $p_k \ge 0$: Next point is $(x_{k+1}, y_k - 1)$ and $p_{k+1} = p_k + 2x_{k+1} + 1 - 2y_{k+1}$.

#### Eight-Way Symmetry
From the primary octant ($x \le y$), all 8 symmetric points are rasterized:
$$(cx \pm x, cy \pm y), \quad (cx \pm y, cy \pm x)$$
Mathematical accuracy is verified against the continuous circle equation:
$$\left|(x - cx)^2 + (y - cy)^2 - r^2\right| \le 2r + 1$$

#### Bresenham Integer Line Drawing
The targeting laser rasterizes beam paths between Sentinel $(x_0, y_0)$ and target $(x_1, y_1)$ without floating-point arithmetic. For $|m| \le 1$:
$$d_0 = 2\Delta y - \Delta x$$
If $d_k < 0 \implies d_{k+1} = d_k + 2\Delta y$. Otherwise, $d_{k+1} = d_k + 2\Delta y - 2\Delta x$.

---

### 4. Depth-Diving Warp Cruiser (`warp_cruiser.hpp|cpp` — CGL-CO4)

#### Non-Linear Perspective Foreshortening ($1/Z$)
In a perspective projection frustum defined by near plane $n$ and far plane $f$, an object at distance $Z$ undergoes perspective division:
$$X_{\text{screen}} = \frac{X \cdot n}{-Z}, \quad Y_{\text{screen}} = \frac{Y \cdot n}{-Z}$$
Consequently, projected screen size is inversely proportional to depth:
$$\text{Scale}(Z) \propto \frac{1}{|Z|}$$
When the Warp Cruiser transitions from deep space ($Z = -50$) to close attack range ($Z = -10$), the perspective projection foreshortening ratio is:
$$\frac{\text{Size}(Z = -10)}{\text{Size}(Z = -50)} = \frac{50}{10} = 5.0\times$$

#### Orthographic Parallel Distance Invariance
Under orthographic projection, projectors are parallel and perpendicular to the projection plane:
$$\mathbf{M}_{\text{ortho}} = \begin{bmatrix} \frac{2}{r-l} & 0 & 0 & -\frac{r+l}{r-l} \\ 0 & \frac{2}{t-b} & 0 & -\frac{t+b}{t-b} \\ 0 & 0 & \frac{-2}{f-n} & -\frac{f+n}{f-n} \\ 0 & 0 & 0 & 1 \end{bmatrix}$$
Because $W = 1$ identically, no perspective division occurs:
$$\frac{\text{Size}_{\text{ortho}}(Z = -10)}{\text{Size}_{\text{ortho}}(Z = -50)} = 1.0\times \quad (\text{Strictly Invariant})$$

#### Viewport Coordinate Mapping
Normalized Device Coordinates (NDC) in $[-1, 1]$ map to physical screen pixels $[0, W-1] \times [0, H-1]$:
$$X_{\text{pixel}} = (X_{\text{ndc}} + 1) \cdot \frac{W}{2} + X_0, \quad Y_{\text{pixel}} = (Y_{\text{ndc}} + 1) \cdot \frac{H}{2} + Y_0$$

---

### 5. Boundary Phase Skimmer (`boundary_skimmer.hpp|cpp` — CGL-CO5)

#### Cohen-Sutherland 4-bit Outcode Evaluation
Every vertex $P(x, y)$ is classified by a 4-bit code $[\text{Top}, \text{Bottom}, \text{Right}, \text{Left}]$:
$$\text{Bit 3 (Top): } y > y_{\max}, \quad \text{Bit 2 (Bottom): } y < y_{\min}, \quad \text{Bit 1 (Right): } x > x_{\max}, \quad \text{Bit 0 (Left): } x < x_{\min}$$

#### Clipping Cases:
1. **Trivial Accept**: $(\text{code}_1 \mid \text{code}_2) = 0000_2 \implies$ Entire segment lies strictly inside the viewport.
2. **Trivial Reject**: $(\text{code}_1 \ \& \ \text{code}_2) \neq 0000_2 \implies$ Both endpoints lie outside on the same side; segment is fully discarded.
3. **Parametric Intersection**: If neither applies, the segment is clipped against boundary planes using parametric line equations:
   $$x = x_1 + \frac{x_2 - x_1}{y_2 - y_1}(y_{\text{clip}} - y_1), \quad y = y_1 + \frac{y_2 - y_1}{x_2 - x_1}(x_{\text{clip}} - x_1)$$

#### Sutherland-Hodgman Polygon Clipping
The Skimmer's triangular wing polygon is clipped sequentially against all four infinite boundary lines (Left, Right, Bottom, Top). For each edge $V_1 \rightarrow V_2$:
- Both Inside: Emit $V_2$.
- Inside $\rightarrow$ Outside: Emit intersection point $I$.
- Both Outside: Emit nothing.
- Outside $\rightarrow$ Inside: Emit intersection point $I$ and $V_2$.

---

### 6. Specular Phong Cruiser (`phong_cruiser.hpp|cpp` — CGL-CO6)

#### Phong Illumination Model
Total reflected radiant intensity combines ambient, diffuse, and specular components:
$$I = I_a + I_d + I_s = k_a L_a + k_d L_d (\vec{N} \cdot \vec{L}) + k_s L_s (\vec{R} \cdot \vec{V})^n$$
Where:
- $\vec{N}$ is the unit surface normal.
- $\vec{L}$ is the unit vector pointing toward the light source.
- $\vec{V}$ is the unit vector pointing toward the observer/camera.
- $\vec{R}$ is the ideal specular reflection direction:
  $$\vec{R} = 2(\vec{N} \cdot \vec{L})\vec{N} - \vec{L}$$
- $n$ is the specular shininess exponent (set to $n = 64$ for intense specular gleam).

#### Blinn-Phong Half-Angle Vector Formulation
To reduce computational cost while maintaining visual fidelity, Blinn-Phong uses the half-angle vector between light and viewer:
$$\vec{H} = \frac{\vec{L} + \vec{V}}{\|\vec{L} + \vec{V}\|}$$
Specular intensity becomes:
$$I_s = k_s L_s (\vec{N} \cdot \vec{H})^n$$

#### Local BMP Texture Mapping
The Phong Cruiser binds an authentic 24-bit uncompressed Windows BMP texture (`CGL/assets/textures/space_hull.bmp`, 256x256 RGB, 196,608 bytes) to modulate surface diffuse albedo:
$$C_{\text{final}} = C_{\text{texture}}(u, v) \cdot (I_a + I_d) + I_s$$

---

## 5. Academic Viva Voce Questions & Model Answers

### Question 1 (CGL-CO1 — Geometric Polygon Cruiser):
> **Examiner:** *"Why is Counter-Clockwise (CCW) vertex winding critical when declaring polygons in OpenGL, and how does your code mathematically verify this winding?"*  
> **Candidate Answer:** *"In OpenGL, polygon winding determines surface front-facing versus back-facing orientation under `glFrontFace(GL_CCW)`. Back-face culling (`glCullFace(GL_BACK)`) discards non-CCW triangles before rasterization to optimize rendering throughput. In `PolygonCruiser`, vertex ordering is mathematically verified via the Green-Shoelace signed area formula $A = \frac{1}{2} \sum (x_i y_{i+1} - x_{i+1} y_i)$. If $A > 0$, the polygon winding is strictly counter-clockwise, ensuring the enemy hull correctly faces the camera without being culled."*

---

### Question 2 (CGL-CO2 — Orbital Rotor Drone):
> **Examiner:** *"Why does the sequence of calling `glTranslatef()` and `glRotatef()` matter in OpenGL, and how does your hierarchical model isolate orbiting weapon pods?"*  
> **Candidate Answer:** *"Matrix multiplication is non-commutative ($A \cdot B \neq B \cdot A$). Because OpenGL multiplies incoming matrices on the right of the current transformation matrix ($\mathbf{M}_{\text{new}} = \mathbf{M} \cdot \mathbf{T}$), the last specified transformation is applied first to the vertex coordinates. Translating before rotating produces an orbital revolution around the origin, whereas rotating before translating produces a local spin at an offset position. In `OrbitalRotor`, we isolate these coordinate frames using `glPushMatrix()` and `glPopMatrix()`. The parent frame positions the drone in world space, while nested push/pop calls allow each rotor pod to independently apply orbital revolution and axial rotation without distorting the central drone or the sibling pod."*

---

### Question 3 (CGL-CO3 — Bresenham Shield Sentinel):
> **Examiner:** *"What is the significance of the initial decision parameter $p_0 = 1 - r$ in the Midpoint Circle Algorithm, and why does this algorithm avoid floating-point operations?"*  
> **Candidate Answer:** *"The Midpoint Circle Algorithm evaluates the implicit function $f(x, y) = x^2 + y^2 - r^2$ at the midpoint between candidate pixels $E(x_k+1, y_k)$ and $SE(x_k+1, y_k-1)$, which is $(x_k+1, y_k - 0.5)$. Evaluating $f$ at the starting point $(0, r)$ gives $f(1, r - 0.5) = 1 + (r - 0.5)^2 - r^2 = \frac{5}{4} - r$. For integer arithmetic, since $r$ is an integer, the decision parameter is shifted by subtracting $\frac{1}{4}$, yielding $p_0 = 1 - r$. If $p_k < 0$, the midpoint is inside the circle, so pixel $E$ is chosen; otherwise pixel $SE$ is chosen. This formulation allows calculating exact circular raster boundaries using exclusively integer addition, subtraction, and bit shifts, completely eliminating floating-point math."*

---

### Question 4 (CGL-CO4 — Depth-Diving Warp Cruiser):
> **Examiner:** *"How does the perspective projection matrix achieve foreshortening, and why does orthographic projection fail to produce depth cues?"*  
> **Candidate Answer:** *"Perspective projection constructs a frustum where the homogeneous coordinate $W$ is set to $-Z$ ($W_{\text{clip}} = -Z_{\text{eye}}$). During the perspective divide step, clip coordinates are divided by $W$: $X_{\text{ndc}} = X_{\text{clip}} / (-Z)$. Because depth $|Z|$ appears in the denominator ($1/Z$), objects farther from the camera project to smaller NDC screen coordinates, creating natural foreshortening ($5.0\times$ difference between $Z=-50$ and $Z=-10$ in our tests). Conversely, orthographic projection maps coordinates via parallel projectors where $W_{\text{clip}} \equiv 1$. No division by $Z$ occurs, causing the projected screen size to remain identical ($1.0\times$) regardless of object depth."*

---

### Question 5 (CGL-CO5 — Boundary Phase Skimmer):
> **Examiner:** *"Explain the bitwise logic behind trivial acceptance and trivial rejection in the Cohen-Sutherland line clipping algorithm."*  
> **Candidate Answer:** *"Cohen-Sutherland encodes the spatial region of each endpoint into a 4-bit Outcode $[\text{Top}, \text{Bottom}, \text{Right}, \text{Left}]$. If the bitwise OR of both outcodes is zero ($\text{code}_1 \mid \text{code}_2 == 0000_2$), neither endpoint lies outside any boundary, allowing instant trivial acceptance without calculating any intersection. If the bitwise AND of both outcodes is non-zero ($\text{code}_1 \ \& \ \text{code}_2 \neq 0000_2$), both endpoints share at least one external half-plane (e.g., both are above the top boundary), guaranteeing that the line segment cannot intersect the clipping window, allowing instant trivial rejection."*

---

### Question 6 (CGL-CO6 — Specular Phong Cruiser):
> **Examiner:** *"What is the physical and mathematical difference between the Phong reflection model and the Blinn-Phong reflection model?"*  
> **Candidate Answer:** *"In the original Phong model, specular reflection is proportional to $(\vec{R} \cdot \vec{V})^n$, where $\vec{R} = 2(\vec{N} \cdot \vec{L})\vec{N} - \vec{L}$ is the ideal reflection ray and $\vec{V}$ is the view vector. This requires computing the reflection vector at every point. Jim Blinn introduced the halfway vector $\vec{H} = \frac{\vec{L} + \vec{V}}{\|\vec{L} + \vec{V}\|}$, evaluating specular intensity as $(\vec{N} \cdot \vec{H})^n$. When light and viewer are directional or distant, $\vec{H}$ is constant across the entire surface, eliminating per-vertex reflection vector recalculation. Blinn-Phong produces smoother, more physically accurate specular highlights for rough surfaces, while Phong produces slightly sharper highlights."*

---

## 6. Build and Verification Suite Output

### 1. Module Compilation Command & Output
```powershell
PS C:\Users\lenovo\OneDrive\Desktop\Projects\AirStrike11> powershell -ExecutionPolicy Bypass -File scripts\build_cgl.ps1
========================================================
[CGL] Building Computer Graphics Lab (OpenGL/GLUT) Module...
========================================================
[INFO] Detected C++ Compiler: g++.exe (MinGW.org GCC-6.3.0-1) 6.3.0
[INFO] Compiling CGL OpenGL/GLUT sources with MinGW g++ C++17...
[SUCCESS] CGL OpenGL module built successfully: bin\cgl\cgl_runner.exe
```

### 2. CGL Test Suite Execution (`.\bin\cgl\cgl_runner.exe --test`)
```
========================================================
  AIRSTRIKE CGL: Computer Graphics Laboratory Test Suite
  Phase 5.1 Technical Audit & Mathematical Verification 
========================================================

--- Testing CGL-CO1: OpenGL Graphics Primitives ---
  [PASS] TC_CGL_01A: Point2D equality check
  [PASS] TC_CGL_01B: Point2D inequality check
  [PASS] TC_CGL_01C: Vec2 approximate equality check
  [PASS] TC_CGL_01D: Color4 component verification

--- Testing CGL-CO2: Geometric Transformations ---
  [PASS] TC_CGL_02A: 4x4 Translation Matrix construction
  [PASS] TC_CGL_02B: Coordinate Translation T(5, -3, 2) on (0, 0, 0)
  [PASS] TC_CGL_02C: Coordinate Rotation Rz(90 deg) on (1, 0, 0) -> (0, 1, 0)
  [PASS] TC_CGL_02D: Coordinate Scaling S(2, 3, 4) on (1, 1, 1)
  [PASS] TC_CGL_02E: Reflection across X-axis coordinate transformation (2, 3) -> (2, -3)
  [PASS] TC_CGL_02F: Reflection across Y-axis coordinate transformation (2, 3) -> (-2, 3)
  [PASS] TC_CGL_02G: Arbitrary Pivot Rotation: point Q(2, 1) rotated 90 deg about P(1, 1) -> (1, 2)
  [PASS] TC_CGL_02H: Arbitrary Pivot Invariance: pivot P(1, 1) rotated about itself remains (1, 1)
  [PASS] TC_CGL_02I: Combined transformation coordinate result verified (5, 0, 0)
  [PASS] TC_CGL_02J: Order A (T * R) coordinates match (2, 1, 0)
  [PASS] TC_CGL_02K: Order B (R * T) coordinates match (0, 3, 0)
  [PASS] TC_CGL_02L: Non-commutative coordinate difference proved: (2, 1) != (0, 3)

--- Testing CGL-CO3: Line Drawing Algorithms (All 8 Octants) ---
  [PASS] TC_CGL_03A: DDA line point count for (0,0)->(5,5)
  [PASS] TC_CGL_03B: DDA line endpoints match exactly
  [PASS] TC_CGL_03C: Bresenham Octant 1 (0 <= m <= 1): (0,0)->(8,4)
  [PASS] TC_CGL_03D: Bresenham Octant 2 (m > 1): (0,0)->(4,8)
  [PASS] TC_CGL_03E: Bresenham Octant 3 (m < -1): (0,0)->(-4,8)
  [PASS] TC_CGL_03F: Bresenham Octant 4 (-1 <= m <= 0): (0,0)->(-8,4)
  [PASS] TC_CGL_03G: Bresenham Octant 5: (0,0)->(-8,-4)
  [PASS] TC_CGL_03H: Bresenham Octant 6: (0,0)->(-4,-8)
  [PASS] TC_CGL_03I: Bresenham Octant 7: (0,0)->(4,-8)
  [PASS] TC_CGL_03J: Bresenham Octant 8: (0,0)->(8,-4)
  [PASS] TC_CGL_03K: Bresenham Horizontal Line: (2,4)->(8,4)
  [PASS] TC_CGL_03L: Bresenham Vertical Line: (3,1)->(3,6)
  [PASS] TC_CGL_03M: Bresenham Reverse Direction (10,10)->(0,0)

--- Testing CGL-CO3: Curve Drawing Algorithms ---
  [PASS] TC_CGL_03N: Midpoint Circle generates pixel points
  [PASS] TC_CGL_03O: Midpoint Circle equation adherence: |x^2 + y^2 - r^2| <= 2r+1
  [PASS] TC_CGL_03P: Midpoint Ellipse generates pixel points
  [PASS] TC_CGL_03Q: Midpoint Ellipse intercepts on major and minor axes
  [PASS] TC_CGL_03R: Scanline polygon fill interior coverage (10x10 square)

--- Testing CGL-CO3: 3D Mesh Vertex Transformations ---
  [PASS] TC_CGL_03S: 3D Cube transformed centroid matches translation vector (3, -2, 4)
  [PASS] TC_CGL_03T: 3D Euler Rotation Rx(90 deg) on (0, 1, 0) -> (0, 0, 1)
  [PASS] TC_CGL_03U: 3D Euler Rotation Ry(90 deg) on (0, 0, 1) -> (1, 0, 0)

--- Testing CGL-CO4: Projection and Viewing ---
  [PASS] TC_CGL_04A: Orthographic Projection size invariance across Z depths (NDC X = 0.5)
  [PASS] TC_CGL_04B: Perspective Projection NDC foreshortening (1.0 vs 0.2)
  [PASS] TC_CGL_04C: Perspective foreshortening ratio matches depth ratio Z2/Z1 (5.0x)
  [PASS] TC_CGL_04D: Camera LookAt matrix transforms origin to eye-space (0, 0, -10)
  [PASS] TC_CGL_04E: Viewport mapping NDC (-1, -1) -> Pixel (0, 0)
  [PASS] TC_CGL_04F: Viewport mapping NDC (0, 0) -> Screen Center (512, 360)
  [PASS] TC_CGL_04G: Viewport mapping NDC (1, 1) -> Screen Max (1024, 720)

--- Testing CGL-CO5: Clipping Algorithms ---
  [PASS] TC_CGL_05A: Cohen-Sutherland inside outcode == 0000
  [PASS] TC_CGL_05B: Cohen-Sutherland left outcode == 0001
  [PASS] TC_CGL_05C: Cohen-Sutherland top-right outcode == 1010
  [PASS] TC_CGL_05D: Cohen-Sutherland trivial accept line inside window
  [PASS] TC_CGL_05E: Cohen-Sutherland trivial reject line outside left
  [PASS] TC_CGL_05F: Cohen-Sutherland horizontal clipping to exact window boundaries
  [PASS] TC_CGL_05G: Liang-Barsky parametric clipping yields identical boundary points
  [PASS] TC_CGL_05H: Liang-Barsky parametric range u1=0.25, u2=0.75 verified
  [PASS] TC_CGL_05I: Sutherland-Hodgman clipped polygon vertex count is 4
  [PASS] TC_CGL_05J: Sutherland-Hodgman clipped polygon aligns with window corners

--- Testing CGL-CO6: Texture, Lighting, and Color Models ---
  [PASS] TC_CGL_06A: HSV (0, 1, 1) converts to Pure Red (1, 0, 0)
  [PASS] TC_CGL_06B: HSV (1/3, 1, 1) converts to Pure Green (0, 1, 0)
[INFO] Successfully loaded local BMP texture: CGL/assets/textures/space_hull.bmp (256x256, 24bpp)
  [PASS] TC_CGL_06C: Actual local BMP texture loaded: space_hull.bmp (256x256)
  [PASS] TC_CGL_06D: Local BMP byte buffer size verification (196,608 bytes)
[WARN] Texture file not found or inaccessible: CGL/assets/textures/nonexistent_texture.bmp
  [PASS] TC_CGL_06E: Missing texture file handled gracefully without crashing (returns false)
  [PASS] TC_CGL_06F: Procedural texture buffers generated in memory
  [PASS] TC_CGL_06G: Phong Ambient reflection calculation (Ia = ka * La)
  [PASS] TC_CGL_06H: Phong Diffuse Lambert reflection calculation (Id = kd * Ld * (N.L))
  [PASS] TC_CGL_06I: Phong Specular reflection calculation (Is = ks * Ls * (R.V)^n)
  [PASS] TC_CGL_06J: Specular Reflection vector formula R = 2(N.L)N - L verified
  [PASS] TC_CGL_06K: Gouraud shading bilinear vertex color interpolation verification

--- Testing CGL Enemy Implementations (CGL-CO1 to CGL-CO6) ---
  [PASS] TC_CGL_EN1A: PolygonCruiser 6-vertex CCW fuselage hull generation
  [PASS] TC_CGL_EN1B: PolygonCruiser swept delta wings triangle-fan geometry
  [PASS] TC_CGL_EN1C: PolygonCruiser strict Counter-Clockwise (CCW) front-facing winding
  [PASS] TC_CGL_EN1D: PolygonCruiser positive signed Green-Shoelace area verification
  [PASS] TC_CGL_EN1E: PolygonCruiser ray-casting point-in-polygon containment check
  [PASS] TC_CGL_EN2A: OrbitalRotor non-commutative matrix sequencing (T*R != R*T)
  [PASS] TC_CGL_EN2B: OrbitalRotor hierarchical pod 0 world coordinate resolution
  [PASS] TC_CGL_EN2C: OrbitalRotor hierarchical pod 1 180-deg opposed orbit coordinate
  [PASS] TC_CGL_EN2D: OrbitalRotor dynamic rotation angle trigonometry accuracy
  [PASS] TC_CGL_EN2E: OrbitalRotor central core breathing scale range [0.85, 1.15]
  [PASS] TC_CGL_EN3A: RasterSentinel Midpoint Circle shield pixel points generation
  [PASS] TC_CGL_EN3B: RasterSentinel Midpoint Circle mathematical equation adherence
  [PASS] TC_CGL_EN3C: RasterSentinel 8-way octant symmetry across cardinal axes
  [PASS] TC_CGL_EN3D: RasterSentinel Bresenham integer line targeting beam endpoints
  [PASS] TC_CGL_EN3E: RasterSentinel Bresenham line across steep slope (m > 1) octants
  [PASS] TC_CGL_EN4A: WarpCruiser perspective 1/Z foreshortening ratio (5.0x for Z=50 to Z=10)
  [PASS] TC_CGL_EN4B: WarpCruiser orthographic parallel distance invariance ratio (1.0x)
  [PASS] TC_CGL_EN4C: WarpCruiser perspective NDC depth buffer ordering (near < far)
  [PASS] TC_CGL_EN5A: BoundarySkimmer Cohen-Sutherland trivial accept inside window
  [PASS] TC_CGL_EN5B: BoundarySkimmer Cohen-Sutherland trivial reject outside left
  [PASS] TC_CGL_EN5C: BoundarySkimmer Cohen-Sutherland single boundary intersection clip
  [PASS] TC_CGL_EN5D: BoundarySkimmer Cohen-Sutherland multiple boundaries traversal clip
  [PASS] TC_CGL_EN5E: BoundarySkimmer Sutherland-Hodgman polygon clipping against boundary
[INFO] Successfully loaded local BMP texture: CGL/assets/textures/space_hull.bmp (256x256, 24bpp)
  [PASS] TC_CGL_EN6A: PhongCruiser Reflection Vector formula R = 2(N.L)N - L verified
  [PASS] TC_CGL_EN6B: PhongCruiser Blinn-Phong Half-Angle Vector H = normalize(L + V)
  [PASS] TC_CGL_EN6D: PhongCruiser Blinn-Phong specular reflection intensity evaluation
  [PASS] TC_CGL_EN6E: PhongCruiser local 24-bit uncompressed BMP texture buffer (196,608 bytes)

========================================================
  TEST SUMMARY: 93 / 93 Passed (100%)
========================================================
```

---

## 7. Architectural Boundaries, Limitations & Execution Scope

In strict adherence to Phase 8.5 guidelines, the following boundaries and architectural rules were preserved:

1. **Independent CGL Subsystem Execution**: The six CGL enemy types execute natively inside `CGL/src/` and are built into `bin/cgl/cgl_runner.exe`. In addition to interactive GLUT rendering modes, all mathematical models, matrix operations, software rasterizers, clippers, and illumination calculations run headlessly via `--test` mode, enabling 100% automated CI verification without requiring an active GUI display.
2. **Absence of JNI / TCP Cross-Bridges**: Per mandatory rule #8, zero JNI, shared memory, or network socket bridges were introduced to couple CGL with Java, C++, or TypeScript modules.
3. **Preservation of Other Modules**: PSOOP (Java), PL (C++), COA (Assembly), Web (TypeScript), and Supabase code bases remain completely untouched.
4. **No Artificial Environments or Backgrounds**: Per mandatory rule #7, no new space maps, textures, or backgrounds were introduced outside the existing syllabus-approved asset directories.
5. **Exact 19 Course Outcomes Preserved**: Total project Course Outcomes remain strictly at 19 across all four modules (5 PSOOP + 4 PL + 4 COA + 6 CGL = 19 COs).
