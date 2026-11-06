# AIRSTRIKE: Final Demonstration & Viva Presentation Guide

**Project**: AIRSTRIKE – 2D Space Shooter & Academic Demonstration System  
**Document**: Presentation Walkthrough, Live Demonstration Script & Viva Defense Guide  
**Date**: November 2026  
**Audience**: Project Evaluators, University Examiners, Faculty Reviewers  

---

## 1. Executive Presentation Script (2-Minute Elevator Pitch)

> *"Respected Examiners and Faculty,*  
> *AIRSTRIKE is a comprehensive multi-subject computer science academic engineering project. Rather than treating core subjects as isolated textbook exercises, AIRSTRIKE unites four foundational undergraduate subjects under a high-performance space combat simulation:*  
>  
> *1. **Principles of Object-Oriented Programming (Java SE 26)**: Demonstrates encapsulation, dynamic polymorphism, design patterns, Swing Event Dispatch Thread (EDT) multithreading, and asynchronous cloud score persistence.*  
> *2. **Programming Laboratory (C++17)**: Demonstrates manual dynamic memory management, pointers, and computational complexity without STL container wrappers.*  
> *3. **Computer Organization and Architecture (80386 Assembly)**: Demonstrates native 32-bit registers, addressing modes, bitwise logic, Booth's Multiplication Algorithm, and an educational 5-stage instruction pipelining model.*  
> *4. **Computer Graphics Laboratory (C++17 + OpenGL + GLUT)**: Demonstrates hardware primitives, affine matrix transformations, software rasterization (Bresenham, Midpoint Circle), projections, clipping, and the Phong reflection model with genuine BMP texture loading.*  
>  
> *A crucial architectural foundation is our **Strict Independence Rule**: there is zero coupling, zero JNI, and zero sockets between modules. Each module compiles and runs autonomously with its own native compiler. All 19 Course Outcomes have been verified with 302 automated unit tests passing at 100%."*

---

## 2. Live Demonstration Walkthrough

### Act 1: The Master Unified Launcher & Full Verification
1. Open terminal at project root and launch:
   ```cmd
   airstrike.bat
   ```
2. Demonstrate **Option 6 ("Run All Tests")**:
   - Executes all 4 modules' test suites.
   - Evaluator observes the **Master Academic Test Verification Matrix** showing 302/302 tests passing with 0 regressions.
3. Demonstrate **Option 5 ("Build All Modules")**:
   - Shows compilation across `javac`, `g++`, `gcc -m32`, and OpenGL libraries, culminating in a clean build summary matrix.

---

### Act 2: PSOOP (Java Space Shooter & Supabase Cloud)
1. Select **Option 1 ("PSOOP - Java Space Shooter")**:
   - GUI launches at $800 \times 650$ resolution with procedural starfield animation.
2. Demonstrate **Authentication & Guest Isolation**:
   - Notice the status bar: `PILOT: GUEST / OFFLINE`.
   - Click `"STARFLEET PILOT REGISTRY"` $\rightarrow$ show Login and Registration tabs.
   - Sign in as an authenticated pilot $\rightarrow$ status updates to active green with callsign.
3. Demonstrate **Active Combat (CO1, CO2, CO4)**:
   - Launch mission $\rightarrow$ control spacecraft with `[W/A/S/D]` or Arrow keys.
   - Fire lasers with `[SPACEBAR]`. Demonstrate enemy types: Drone swooping, Fighter sinusoidal weaving, and Boss encounter at wave 3.
   - Pick up power-ups: Health (+), Shields (S), Weapon upgrades (W), Speed (^).
   - Demonstrate streak multiplier scaling from 1x to 4x.
4. Demonstrate **Cloud Score Persistence & Leaderboard**:
   - Suffer fatal damage $\rightarrow$ Game Over screen displays:  
     `"TRANSMITTING FLIGHT LOG TO STARFLEET ARCHIVES..."` followed by `"FLIGHT LOG SECURED IN STARFLEET ARCHIVES"`.
   - Return to menu $\rightarrow$ Click `"GLOBAL LEADERBOARD"` $\rightarrow$ displays top 10 pilots with Gold, Silver, and Bronze badges.
   - Click `"MATCH HISTORY (FLIGHT LOGS)"` $\rightarrow$ displays authenticated pilot's mission records with exact duration and timestamp.

---

### Act 3: PL (C++17 Data Structures)
1. Return to launcher $\rightarrow$ Select **Option 2 ("PL - Data Structures")**.
2. Demonstrate core data structures:
   - **Arrays (PL-CO1)**: Choice `[1]` demonstrates dynamic array geometric capacity doubling (4 to 8) and bullet pooling.
   - **Linked Lists (PL-CO2)**: Choice `[2]` demonstrates Doubly Linked List bidirectional pointer manipulation and Circular Linked List weapon ring cycling.
   - **Stacks & Queues (PL-CO3)**: Choice `[3]` demonstrates LIFO combat undo stack; Choice `[4]` demonstrates circular FIFO wave queue.
   - **Searching & Sorting (PL-CO4)**: Choice `[5]` shows comparison of Linear ($O(N)$) vs Binary Search ($O(\log N)$); Choice `[6]` shows Bubble, Selection, Insertion, Merge, and QuickSort.
   - **Hashing (PL-CO5)**: Choice `[7]` demonstrates Separate Chaining with dynamic rehashing at load factor $\ge 0.75$, and Open Addressing with linear probing and tombstone markers.

---

### Act 4: COA (80386 Assembly Language)
1. Return to launcher $\rightarrow$ Select **Option 3 ("COA - 80386 Assembly")**.
2. Demonstrate low-level architecture:
   - **CO1 (Architecture)**: Choice `[1]` shows general-purpose subregister partitioning (`EAX` $\rightarrow$ `AX` $\rightarrow$ `AH/AL`), EFLAGS transitions (`CF`, `ZF`, `SF`, `OF`), and Scaled-Index-Base addressing (`[EBX + ESI*4 + 8]`).
   - **CO2 (Programs)**: Choice `[2]` demonstrates 10 assembly programs including recursive factorial (stack activation frames), Fibonacci series, and bitwise logic masks.
   - **CO3 (Computer Arithmetic)**: Choice `[3]` demonstrates Booth's Signed Multiplication step-by-step register trace table ($A, Q, Q_{-1}$) and Restoring Division.
   - **CO4 (Control Unit & Pipeline)**: Choice `[4]` displays opcode control signal matrix and 5-stage pipeline hazard detection (RAW data stalls and branch flush penalty).

---

### Act 5: CGL (OpenGL & GLUT Visual Graphics)
1. Return to launcher $\rightarrow$ Select **Option 4 ("CGL - OpenGL")**.
2. Walkthrough interactive graphics demonstrations:
   - Press `[1]` (**CO1 Primitives**): Shows 8 primitives composing spacecraft vector assets.
   - Press `[2]` (**CO2 Transformations**): Use `[W/A/S/D]` to translate, `[Q/E]` to rotate, `[P]` for arbitrary pivot rotation, `[O]` to prove non-commutative matrix order ($T \cdot R \ne R \cdot T$).
   - Press `[3]` (**CO3 Algorithms**): Press `[M]` to cycle through Bresenham line drawing across all 8 octants, midpoint circle, ellipse, scanline polygon fill, and 3D wireframe mesh.
   - Press `[4]` (**CO4 Projection**): Press `[P]` to toggle between 2D orthographic radar view and 3D perspective foreshortening view; arrow keys orbit camera via `gluLookAt`.
   - Press `[5]` (**CO5 Clipping**): Press `[M]` to cycle Cohen-Sutherland outcode line clipping, Liang-Barsky parametric clipping, and Sutherland-Hodgman polygon clipping.
   - Press `[6]` (**CO6 Appearance**): Shows genuine local 24-bit BMP texture loading (`space_hull.bmp`), texture wrapping/filtering, and Phong reflection (ambient, diffuse, specular).
   - Press `[0]`: Integrated real-time 3D space combat environment uniting all graphics outcomes.

---

## 3. High-Frequency Viva Questions & Model Answers

### Category A: PSOOP (Java Object-Oriented Programming)

**Q1: How does AIRSTRIKE utilize polymorphism in its entity architecture?**  
*Answer*: All combat objects derive from abstract base class `Entity`, which specifies abstract methods `update(double dt)` and `render(Graphics2D g)`. In the game engine, a polymorphic collection `List<Entity>` processes heterogeneous entities (drones, fighters, boss ships, power-ups) through a single uniform loop. At runtime, the JVM dynamic method dispatch executes each derived class's unique flight and render logic without explicit type casting.

**Q2: Why was `SwingUtilities.invokeLater()` essential for game loop rendering?**  
*Answer*: Java Swing components are strictly single-threaded and not thread-safe. They must only be accessed and modified on the Event Dispatch Thread (EDT). The game physics and state logic execute on a dedicated high-precision background timer thread to maintain an exact 60 FPS update rate. Calling `SwingUtilities.invokeLater()` queues canvas repaint requests onto the EDT, preventing race conditions, graphical tearing, and UI thread deadlock.

**Q3: How does the game ensure database network delays don't freeze the 60 FPS game loop?**  
*Answer*: All network communication with Supabase PostgREST endpoints is executed asynchronously off the main thread using Java's built-in `CompletableFuture` and asynchronous `HttpClient.sendAsync()`. When a match finishes, score transmission occurs in a background worker thread. When the response arrives, results post back to the game engine or UI without interrupting continuous frame updates.

---

### Category B: Programming Laboratory (PL – Standard C++17)

**Q4: Why did you implement custom data structures rather than using the standard C++ STL?**  
*Answer*: The academic objective of the Programming Laboratory Course Outcomes (PL-CO1 to PL-CO5) is to understand manual dynamic memory allocation, pointer manipulation, and algorithmic time complexity at the fundamental computer science level. Using `std::vector` or `std::unordered_map` obscures concepts like Rule of Five memory management, amortized array resizing, linked pointer updates, circular buffer indices, and open-addressing collision resolution.

**Q5: What is the Rule of Five, and where was it applied in AIRSTRIKE?**  
*Answer*: In modern C++, if a class manages raw heap resources, it must explicitly define five special member functions: Destructor, Copy Constructor, Copy Assignment Operator, Move Constructor, and Move Assignment Operator. We applied this in `DynamicArray`, `DoublyLinkedList`, and `HashTable`. This prevents double-free errors, dangling pointers, and shallow copy bugs when entities are duplicated or resized.

**Q6: How do you handle element deletion in a hash table using linear probing?**  
*Answer*: In open addressing with linear probing, simple nullification of an empty slot breaks search chains for subsequent colliding keys. We resolve this by inserting a special `TOMBSTONE` (or `DELETED`) sentinel marker. Search operations treat the tombstone as an occupied slot and continue probing, while insert operations treat it as an available slot for new data.

---

### Category C: Computer Organization & Architecture (COA – 80386 Assembly)

**Q7: Why does Booth's Algorithm work for signed binary multiplication?**  
*Answer*: Standard binary multiplication treats operands as unsigned magnitudes. Booth's algorithm operates directly on two's complement signed integers by inspecting adjacent bit pairs $(Q_0, Q_{-1})$. It replaces strings of consecutive binary $1$s with a single subtraction at the start and a single addition at the end (e.g., multiplying by $7$, or $0111_2$, is computed as $+8 - 1$). This significantly reduces addition operations and natively handles negative numbers without sign-magnitude conversion.

**Q8: Explain the difference between Restoring and Non-Restoring division.**  
*Answer*: In Restoring Division, after each left shift, the divisor is subtracted from the accumulator. If the result is negative ($A < 0$), the subtraction was invalid, the quotient bit is set to $0$, and the divisor is added back (restored). In Non-Restoring Division, if $A$ is negative, restoration is skipped, the quotient bit is set to $0$, and in the next step, addition ($+M$) is performed instead of subtraction, saving clock cycles.

**Q9: What is the significance of the 80386 flat memory model compared to segmentation?**  
*Answer*: In 16-bit real mode, the 8086 used 64 KB segmented memory (`Segment * 16 + Offset`). The Intel 80386 introduced 32-bit protected mode with 4 GB linear addressing. In modern 32-bit OS environments, a "flat memory model" is configured where segment registers (`CS`, `DS`, `SS`) point to descriptors spanning the full 4 GB range with base address $0$. Consequently, programmers work directly with linear 32-bit effective addresses without manual segment manipulation.

---

### Category D: Computer Graphics Laboratory (CGL – OpenGL)

**Q10: Why is matrix transformation order non-commutative ($T \cdot R \ne R \cdot T$)?**  
*Answer*: Affine transformations are represented as $4 \times 4$ homogeneous matrices. Matrix multiplication is non-commutative ($A \cdot B \ne B \cdot A$). Applying translation followed by rotation moves the object and then rotates it about the world origin, sweeping it along an arc. Applying rotation followed by translation rotates the object in place and then moves it to the target position, producing entirely different spatial coordinates.

**Q11: Why is Bresenham's Line Algorithm superior to the Digital Differential Analyzer (DDA)?**  
*Answer*: DDA calculates floating-point slope $m = \Delta y / \Delta x$ and increments coordinates using floating-point addition and `round()`, which requires expensive floating-point hardware units. Bresenham's algorithm multiplies the line equation by $2\Delta x$ to derive an integer decision variable $P_k = 2\Delta y - \Delta x$. Subsequent decisions require only integer additions and bitwise shifts ($2\Delta y$, $2\Delta y - 2\Delta x$), running significantly faster on hardware.

**Q12: Explain the three components of the Phong Reflection Model.**  
*Answer*:  
1. **Ambient Reflection ($I_a = k_a L_a$)**: Constant baseline illumination simulating scattered, indirect light across all surfaces equally.  
2. **Diffuse Reflection ($I_d = k_d L_d (\mathbf{N} \cdot \mathbf{L})$)**: Lambertian reflection based on the angle between the surface normal $\mathbf{N}$ and light direction $\mathbf{L}$, scattering light uniformly in all directions.  
3. **Specular Reflection ($I_s = k_s L_s (\mathbf{R} \cdot \mathbf{V})^n$)**: Bright highlights visible when the viewer vector $\mathbf{V}$ aligns with the reflected light ray $\mathbf{R}$, modulated by shininess exponent $n$.

---

### Category E: Cloud Database & Security (Supabase)

**Q13: Why is it safe to include the Supabase Publishable Key in the client application?**  
*Answer*: The Supabase Publishable (`anon`) key is designed specifically for client-side applications. It conveys no administrative privileges. Database access is strictly governed by **Row-Level Security (RLS)** policies defined inside PostgreSQL. Even with the publishable key, a user can only perform actions explicitly permitted by SQL policies (e.g., inserting records where `auth.uid() = user_id`). The administrative `service_role` key is strictly prohibited and actively rejected by our configuration loader.
