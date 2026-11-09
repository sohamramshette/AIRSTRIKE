package ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 * Academic Curriculum & Course Outcome (CO) Hub for AIRSTRIKE.
 *
 * Exhaustively presents all 4 subjects and 19 Course Outcomes:
 * 1. PSOOP — Java 26 (PSOOP-CO1 to PSOOP-CO4)
 * 2. PL    — C++17 Data Structures & Algorithms (PL-CO1 to PL-CO5)
 * 3. COA   — 32-bit Intel 80386 Assembly Language (COA-CO1 to COA-CO4)
 * 4. CGL   — C++17 / OpenGL / GLUT Computer Graphics (CGL-CO1 to CGL-CO6)
 */
public class SubjectCODialog extends JDialog {

    private final MainWindow mainWindow;
    private final CardLayout cardLayout;
    private final JPanel contentCards;

    private JButton tabPsoopBtn;
    private JButton tabPlBtn;
    private JButton tabCoaBtn;
    private JButton tabCglBtn;

    public SubjectCODialog(JFrame parent, MainWindow mainWindow) {
        super(parent, "STARFLEET ACADEMY – 19 COURSE OUTCOMES MATRIX", true);
        this.mainWindow = mainWindow;

        setSize(740, 600);
        setMinimumSize(new Dimension(680, 520));
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Theme.BG_DEEP);
        setLayout(new BorderLayout());

        // Header Panel
        add(buildHeaderPanel(), BorderLayout.NORTH);

        // Subject Tabs Container
        cardLayout = new CardLayout();
        contentCards = new JPanel(cardLayout);
        contentCards.setBackground(Theme.BG_DEEP);

        contentCards.add(buildPsoopPanel(), "PSOOP");
        contentCards.add(buildPlPanel(), "PL");
        contentCards.add(buildCoaPanel(), "COA");
        contentCards.add(buildCglPanel(), "CGL");
        add(contentCards, BorderLayout.CENTER);

        // Footer Action Panel
        add(buildFooterPanel(), BorderLayout.SOUTH);

        selectTab("PSOOP");
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(14, 20, 12, 20)
        ));

        JLabel title = new JLabel("ACADEMIC CURRICULUM & COURSE OUTCOMES", SwingConstants.CENTER);
        title.setFont(Theme.HEADER_1);
        title.setForeground(Theme.CYAN_PRIMARY);
        header.add(title, BorderLayout.NORTH);

        JLabel sub = new JLabel("4 Undergraduate Engineering Subjects • 19 Mapped Outcomes • 418 Verified Assertions", SwingConstants.CENTER);
        sub.setFont(Theme.BODY);
        sub.setForeground(Theme.TEXT_MUTED);
        sub.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
        header.add(sub, BorderLayout.CENTER);

        // Subject navigation tabs
        JPanel tabs = new JPanel(new GridLayout(1, 4, 8, 0));
        tabs.setOpaque(false);

        tabPsoopBtn = createTabButton("1. PSOOP (Java)");
        tabPsoopBtn.addActionListener(e -> selectTab("PSOOP"));
        tabs.add(tabPsoopBtn);

        tabPlBtn = createTabButton("2. PL (C++17)");
        tabPlBtn.addActionListener(e -> selectTab("PL"));
        tabs.add(tabPlBtn);

        tabCoaBtn = createTabButton("3. COA (80386 ASM)");
        tabCoaBtn.addActionListener(e -> selectTab("COA"));
        tabs.add(tabCoaBtn);

        tabCglBtn = createTabButton("4. CGL (OpenGL)");
        tabCglBtn.addActionListener(e -> selectTab("CGL"));
        tabs.add(tabCglBtn);

        header.add(tabs, BorderLayout.SOUTH);
        return header;
    }

    private void selectTab(String subject) {
        cardLayout.show(contentCards, subject);

        resetTabStyle(tabPsoopBtn);
        resetTabStyle(tabPlBtn);
        resetTabStyle(tabCoaBtn);
        resetTabStyle(tabCglBtn);

        switch (subject) {
            case "PSOOP" -> setActiveTabStyle(tabPsoopBtn, Theme.CYAN_PRIMARY);
            case "PL"    -> setActiveTabStyle(tabPlBtn, Theme.BLUE_ACCENT);
            case "COA"   -> setActiveTabStyle(tabCoaBtn, Theme.GOLD_ACCENT);
            case "CGL"   -> setActiveTabStyle(tabCglBtn, Theme.PURPLE_ACCENT);
        }
    }

    private JButton createTabButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.BODY_BOLD);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 32));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        return btn;
    }

    private void resetTabStyle(JButton btn) {
        btn.setBackground(new Color(22, 31, 56));
        btn.setForeground(Theme.TEXT_MUTED);
        btn.setBorder(BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1));
    }

    private void setActiveTabStyle(JButton btn, Color accent) {
        btn.setBackground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40));
        btn.setForeground(Theme.TEXT_WHITE);
        btn.setBorder(BorderFactory.createLineBorder(accent, 1));
    }

    // =========================================================================
    // Subject Panels (PSOOP, PL, COA, CGL)
    // =========================================================================

    private JScrollPane buildPsoopPanel() {
        JPanel list = createCoListPanel();

        list.add(createCoCard(
                "PSOOP-CO1: Object-Oriented Solutions",
                "Encapsulation, inheritance hierarchies, and dynamic polymorphism.",
                "Implemented in `ArmouredEnemy` (encapsulated armor mitigations) and `InterceptorEnemy` (dynamic polymorphic high-speed dash).",
                "PSOOP/src/model/ (169 Test Assertions Passed)",
                Theme.CYAN_PRIMARY
        ));

        list.add(createCoCard(
                "PSOOP-CO2: Solutions to Reduce Complexity",
                "Finite State Machine (FSM) architecture for multi-phase tactical entities.",
                "Implemented in `MorphingEnemy` (PATROL, CHASE, OVERCHARGE, EVADE states) and `WaveManager` difficulty progression.",
                "PSOOP/src/model/MorphingEnemy.java & service/WaveManager.java",
                Theme.CYAN_PRIMARY
        ));

        list.add(createCoCard(
                "PSOOP-CO3: Critical Thinking & Problem Solving",
                "Defensive programming, boundary clamping, and custom exception hierarchies.",
                "Implemented in `FailSafeEnemy` (fail-safe emergency kinetic barrier) and custom `AirstrikeException` hierarchy.",
                "PSOOP/src/exception/ & model/FailSafeEnemy.java",
                Theme.CYAN_PRIMARY
        ));

        list.add(createCoCard(
                "PSOOP-CO4: Logic-Building for Real-Life Problems",
                "Continuous kinematics, non-uniform acceleration vectors, and AABB collision resolution.",
                "Implemented in `RammerEnemy` (kinetic prow impact) and `CollisionManager` (high-speed bullet-hull math).",
                "PSOOP/src/model/RammerEnemy.java & game/CollisionManager.java",
                Theme.CYAN_PRIMARY
        ));

        return wrapInScroll(list);
    }

    private JScrollPane buildPlPanel() {
        JPanel list = createCoListPanel();

        list.add(createCoCard(
                "PL-CO1: Dynamic Arrays & Spatial Partitioning",
                "Contiguous memory layout, 2D spatial grid partitioning, and zero-allocation projectile pools.",
                "`CustomArray<T>` amortized capacity scaling, `ProjectilePool`, and `GridMap` space sector entity partitioning.",
                "PL/src/arrays/ (99 PL C++17 Test Assertions Passed)",
                Theme.BLUE_ACCENT
        ));

        list.add(createCoCard(
                "PL-CO2: Suitable Linked Lists",
                "Singly, doubly, and circular intrusive lists for dynamic enemy fleet wave coordination.",
                "Efficient $O(1)$ insertion and mid-sequence deletion during tactical wave spawning.",
                "PL/src/linked_lists/ (Node pointer manipulation without std::list)",
                Theme.BLUE_ACCENT
        ));

        list.add(createCoCard(
                "PL-CO3: Stack and Queue Applications",
                "LIFO flight maneuver undo buffers and FIFO asynchronous telemetry event processing queues.",
                "Double-ended ring buffers for high-frequency player inputs and weapon firing events.",
                "PL/src/stacks/ & PL/src/queues/",
                Theme.BLUE_ACCENT
        ));

        list.add(createCoCard(
                "PL-CO4: Searching and Sorting Techniques",
                "High-performance QuickSort, MergeSort, and Binary Search over global pilot leaderboards.",
                "Comparative benchmark suite evaluating logarithmic divide-and-conquer efficiency.",
                "PL/src/searching/ & PL/src/sorting/",
                Theme.BLUE_ACCENT
        ));

        list.add(createCoCard(
                "PL-CO5: Hashing Techniques",
                "Direct chaining and open addressing with MurmurHash3 for instant $O(1)$ entity lookup.",
                "Collision resolution under 80% load factors with dynamic prime-capacity re-hashing.",
                "PL/src/hashing/",
                Theme.BLUE_ACCENT
        ));

        return wrapInScroll(list);
    }

    private JScrollPane buildCoaPanel() {
        JPanel list = createCoListPanel();

        list.add(createCoCard(
                "COA-CO1: 80386 Architecture & Instruction Set",
                "Direct 32-bit register operations (EAX, EBX, ECX, EDX, ESI, EDI) and segmentation models.",
                "Genuine 32-bit assembly routines compiled with MinGW GCC GAS toolchain (`-m32`).",
                "COA/src/co1_architecture/ (57 COA 80386 Assertions Passed)",
                Theme.GOLD_ACCENT
        ));

        list.add(createCoCard(
                "COA-CO2: Assembly Language Programs",
                "Low-level string operations (`REP MOVSB`), procedural call frames, and memory block transfers.",
                "Vector distance calculation and register-based hit-point clamping algorithms.",
                "COA/src/co2_assembly_programs/",
                Theme.GOLD_ACCENT
        ));

        list.add(createCoCard(
                "COA-CO3: Computer Arithmetic & Booth Algorithm",
                "Hardware multiplication using 2's complement Booth Signed-Digit Multiplication algorithm.",
                "Cycle-accurate Booth recoding table verifying signed bit shifts and arithmetic right shifts (`SAR`).",
                "COA/src/co3_arithmetic/",
                Theme.GOLD_ACCENT
        ));

        list.add(createCoCard(
                "COA-CO4: Control Unit & Instruction Pipelining",
                "Simulation of 5-stage instruction pipelining (IF, ID, EX, MEM, WB) and pipeline hazard detection.",
                "Data forwarding, structural hazard stalls, and dynamic branch prediction evaluation.",
                "COA/src/co4_control_unit/",
                Theme.GOLD_ACCENT
        ));

        return wrapInScroll(list);
    }

    private JScrollPane buildCglPanel() {
        JPanel list = createCoListPanel();

        list.add(createCoCard(
                "CGL-CO1: OpenGL Graphics Primitives",
                "Rasterization of `GL_POINTS`, `GL_LINES`, `GL_TRIANGLE_FAN` with Counter-Clockwise (CCW) winding.",
                "Fuselage hull generation and Green-Shoelace area verification in `PolygonCruiser`.",
                "CGL/src/primitives/ (93 CGL OpenGL Assertions Passed)",
                Theme.PURPLE_ACCENT
        ));

        list.add(createCoCard(
                "CGL-CO2: Geometric Transformations",
                "Homogeneous 2D/3D affine transformation matrices: Translation, Rotation, Non-uniform Scaling.",
                "Hierarchical multi-body transformation sequencing ($T \\times R \\neq R \\times T$) in `OrbitalRotor`.",
                "CGL/src/transformations/",
                Theme.PURPLE_ACCENT
        ));

        list.add(createCoCard(
                "CGL-CO3: 2D & 3D Rasterization Algorithms",
                "Integer-only Bresenham Line Drawing and Midpoint Circle Shield rasterization across 8 octants.",
                "Ray-casting point-in-polygon containment and shield barrier generation in `RasterSentinel`.",
                "CGL/src/algorithms/",
                Theme.PURPLE_ACCENT
        ));

        list.add(createCoCard(
                "CGL-CO4: Projection & Viewing Pipelines",
                "Perspective foreshortening ($1/Z$ depth scaling) vs Orthographic parallel distance invariance.",
                "Normalized Device Coordinates (NDC) depth buffer resolution in `WarpCruiser`.",
                "CGL/src/projection/",
                Theme.PURPLE_ACCENT
        ));

        list.add(createCoCard(
                "CGL-CO5: Clipping Algorithms",
                "Cohen-Sutherland outcode line clipping and Sutherland-Hodgman convex polygon boundary clipping.",
                "Precision boundary skimmer algorithms preventing out-of-bounds rendering artifacts.",
                "CGL/src/clipping/",
                Theme.PURPLE_ACCENT
        ));

        list.add(createCoCard(
                "CGL-CO6: Texture, Lighting & Color Models",
                "Phong illumination model ($I = I_a + I_d + I_s$), Blinn-Phong half-angle vector, and 24-bit BMP loading.",
                "Gouraud smooth shading and HSV-to-RGB procedural color conversion in `PhongCruiser`.",
                "CGL/src/appearance/",
                Theme.PURPLE_ACCENT
        ));

        return wrapInScroll(list);
    }

    private JPanel createCoListPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.BG_DEEP);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        return panel;
    }

    private JPanel createCoCard(String title, String summary, String details, String fileLocation, Color accent) {
        CyberCard card = new CyberCard(new BorderLayout(8, 6), accent, 10, true);
        card.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.HEADER_2);
        titleLbl.setForeground(accent);
        card.add(titleLbl, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        JLabel sumLbl = new JLabel("<html><body style='width: 580px;'>" + summary + "</body></html>");
        sumLbl.setFont(Theme.BODY);
        sumLbl.setForeground(Theme.TEXT_WHITE);
        body.add(sumLbl);

        body.add(Box.createVerticalStrut(4));

        JLabel detLbl = new JLabel("<html><body style='width: 580px;'>" + details + "</body></html>");
        detLbl.setFont(Theme.BODY);
        detLbl.setForeground(Theme.TEXT_MUTED);
        body.add(detLbl);

        body.add(Box.createVerticalStrut(4));

        JLabel locLbl = new JLabel("Target Module: " + fileLocation);
        locLbl.setFont(Theme.MONO_SMALL);
        locLbl.setForeground(Theme.CYAN_PRIMARY);
        body.add(locLbl);

        card.add(body, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JScrollPane wrapInScroll(JPanel content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.getViewport().setBackground(Theme.BG_DEEP);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel buildFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Theme.BG_PANEL);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(10, 20, 12, 20)
        ));

        // Academic status label
        JLabel status = new JLabel("418 / 418 Academic Assertions Verified (100% Pass Rate)");
        status.setFont(Theme.BODY_BOLD);
        status.setForeground(Theme.EMERALD_STATUS);
        footer.add(status, BorderLayout.WEST);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        CyberButton arenaBtn = CyberButton.hero("✦   LAUNCH PSOOP ARENA");
        arenaBtn.setPreferredSize(new Dimension(220, 34));
        arenaBtn.addActionListener(e -> {
            dispose();
            if (mainWindow != null) {
                mainWindow.startEnemyDemo();
            }
        });
        btnPanel.add(arenaBtn);

        CyberButton closeBtn = CyberButton.danger("✕   CLOSE");
        closeBtn.setPreferredSize(new Dimension(100, 34));
        closeBtn.addActionListener(e -> dispose());
        btnPanel.add(closeBtn);

        footer.add(btnPanel, BorderLayout.EAST);
        return footer;
    }
}
