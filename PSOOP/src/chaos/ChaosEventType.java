package chaos;

import announcer.DialogueCategory;

/**
 * Exhaustive catalog of all 20 Chaos Protocol events.
 * Every event is pedagogically mapped to one of the 19 Course Outcomes (COs)
 * across PSOOP, PL, COA, and CGL.
 */
public enum ChaosEventType {

    // --- 1. PSOOP Mapped Events ---
    REVERSE_CONTROLS(
            "CHAOS_REVERSE_CONTROLS",
            "REVERSE CONTROLS",
            "PSOOP-CO4",
            "Navigational gyro inverted: directional axes reversed (Left <-> Right, Up <-> Down)",
            9.0,
            40.0,
            2,
            "ALERT! Navigational gyro failure! Controls reverse ho gaye!",
            "Bhai, controls ulte ho gaye! Ab reverse gear mein spaceship chala!",
            DialogueCategory.GENERAL_ROAST
    ),
    GRAVITY_WELL(
            "CHAOS_GRAVITY_WELL",
            "GRAVITY WELL",
            "CGL-CO5",
            "Micro-singularity forms near sector center, exerting dynamic gravitational pull and Cohen-Sutherland boundary clipping on ships and projectiles",
            10.0,
            45.0,
            2,
            "WARNING! Micro-singularity detected! Gravity spike incoming!",
            "ALERT! Gravity ne resignation de diya hai! Center of gravity sabko kheench rahi hai!",
            DialogueCategory.DESI_MEME
    ),
    SPEED_DEMON(
            "CHAOS_SPEED_DEMON",
            "SPEED DEMON",
            "PSOOP-CO4",
            "Hyperdrive overclock increases player and enemy movement velocities by 60%",
            10.0,
            35.0,
            1,
            "WARNING! Engine warp coil overclocking in 3... 2... 1...",
            "Nitrous on! Fast & Furious: Space Drift shuru ho chuka hai! Thok mat dena!",
            DialogueCategory.INSTAGRAM_GENZ
    ),
    EMERGENCY_SHIELD(
            "CHAOS_EMERGENCY_SHIELD",
            "EMERGENCY SHIELD",
            "PSOOP-CO3",
            "Defensive invariant protection engages an impenetrable hexagonal barrier absorbing 100% damage",
            8.0,
            55.0,
            1,
            "TACTICAL DEFENSE: Emergency protective barrier initializing!",
            "Kavach kundal active! Ab enemy ka baap bhi damage nahi de sakta!",
            DialogueCategory.BOLLYWOOD
    ),
    STATE_CHAOS(
            "CHAOS_STATE_CHAOS",
            "STATE CHAOS",
            "PSOOP-CO2",
            "Hostile fleet AI state machines undergo rapid, chaotic state transitions between patrol, chase, and overcharge",
            10.0,
            45.0,
            2,
            "SYSTEM ANOMALY: Enemy AI behavior state matrices destabilizing!",
            "AI pagal ho gaya! Koi bhaag raha hai, koi gusse mein fire kar raha hai!",
            DialogueCategory.GENERAL_ROAST
    ),
    TOTAL_CHAOS(
            "CHAOS_TOTAL_CHAOS",
            "TOTAL CHAOS",
            "PSOOP-CO1",
            "Polymorphic anomaly surge: two compatible non-conflicting Chaos Protocols trigger simultaneously",
            12.0,
            75.0,
            4,
            "CRITICAL WARNING: MULTI-SYSTEM COLLAPSE! DUAL PROTOCOL IMMINENT!",
            "TOTAL CHAOS PROTOCOL! Sab system fail ho rahe hain! Survive karke dikha!",
            DialogueCategory.GENERAL_ROAST
    ),

    // --- 2. PL (C++ Data Structures) Mapped Events ---
    FORMATION_SHUFFLE(
            "CHAOS_FORMATION_SHUFFLE",
            "FORMATION SHUFFLE",
            "PL-CO1",
            "Hostile squadrons reorder into a transposed spatial grid using row-major address calculation",
            8.0,
            40.0,
            2,
            "WARNING: Enemy fleet executing rapid 2D grid matrix transposition!",
            "Musical chairs chal raha hai space mein! Formation shuffle ho gayi!",
            DialogueCategory.DESI_MEME
    ),
    DOUBLE_TROUBLE(
            "CHAOS_DOUBLE_TROUBLE",
            "DOUBLE TROUBLE",
            "PL-CO2",
            "Hostile squadron nodes clone dynamically via doubly linked list splicing, creating mirror squadrons",
            12.0,
            60.0,
            2,
            "RADAR ALERT: Fleet squadron nodes duplicating on dynamic link channels!",
            "Arey bhai bhai bhai! Ek dushman kam tha jo double aa gaye?",
            DialogueCategory.DESI_MEME
    ),
    BOSS_RUSH(
            "CHAOS_BOSS_RUSH",
            "BOSS RUSH",
            "PL-CO3",
            "High-priority heavy capital dreadnought dispatched from the circular wave queue ahead of schedule",
            14.0,
            70.0,
            4,
            "EMERGENCY PRIORITY: Circular queue dispatched high-tier hostile dreadnought!",
            "BOSS RUSH! Asli talent dikhane ka waqt aa gaya, bhidu!",
            DialogueCategory.BOLLYWOOD
    ),
    MAGNETIC_BULLETS(
            "CHAOS_MAGNETIC_BULLETS",
            "MAGNETIC BULLETS",
            "PL-CO4",
            "Player projectiles acquire magnetic guidance via priority target search tracking",
            10.0,
            50.0,
            2,
            "FIRE CONTROL: Target acquisition algorithm locked to nearest threats!",
            "Smart magnetic bullets active! Ab toh miss karke dikha!",
            DialogueCategory.INSTAGRAM_GENZ
    ),
    HASH_FLUX(
            "CHAOS_HASH_FLUX",
            "HASH FLUX",
            "PL-CO5",
            "Hostile fleet shields oscillate hash buckets; open-addressing resonance causes 3x vulnerability",
            11.0,
            50.0,
            3,
            "SPECTRAL SCAN: Enemy shield modulation re-hashing across frequency tables!",
            "Hash key match karo! Sahi frequency pe maro, dushman ek shot mein gayab!",
            DialogueCategory.GENERAL_ROAST
    ),

    // --- 3. COA (80386 Assembly) Mapped Events ---
    QUANTUM_COLLAPSE(
            "CHAOS_QUANTUM_COLLAPSE",
            "QUANTUM COLLAPSE",
            "COA-CO1",
            "80386 Zero Flag status evaluation unleashes a tactical EMP pulse clearing 50% of hostile projectiles",
            8.0,
            60.0,
            3,
            "CORE TELEMETRY: EFLAGS condition code monitor initializing tactical EMP capacitor!",
            "Zero Flag set! EMP blast triggered! Saare hostile laser saaf!",
            DialogueCategory.VICTORY_PRAISE
    ),
    RAPID_FIRE(
            "CHAOS_RAPID_FIRE",
            "RAPID FIRE",
            "COA-CO2",
            "Weapon discharge cadenced to assembly Fibonacci intervals, tripling player rate-of-fire",
            9.0,
            45.0,
            1,
            "POWER SURGE: Capacitor timing synchronized to Fibonacci series pulses!",
            "Unlimited ammo mode activated! Bhai ne trigger daba ke chhod diya!",
            DialogueCategory.INSTAGRAM_GENZ
    ),
    CRITICAL_OVERDRIVE(
            "CHAOS_CRITICAL_OVERDRIVE",
            "CRITICAL OVERDRIVE",
            "COA-CO3",
            "Fire control computer executes Booth two's-complement arithmetic multiplier, scaling shot damage by 2x",
            9.0,
            45.0,
            2,
            "WEAPON ARITHMETIC: Booth signed multiplier loaded into ordnance register!",
            "Booth algorithm ne damage 2x kar diya! Ab enemy ko steamroll karo!",
            DialogueCategory.BOLLYWOOD
    ),
    WEAPON_JAM(
            "CHAOS_WEAPON_JAM",
            "WEAPON JAM",
            "COA-CO4",
            "Weapon feed experiences 5-stage CPU pipeline hazard stalls; firing cooldown temporarily tripled",
            8.0,
            50.0,
            3,
            "HARDWARE FAULT: RAW Data hazard in weapon pipeline! Inserting stall cycles!",
            "Bhai, pipeline stall ho gaya! Hathiyar overheat, thoda sambhal ke!",
            DialogueCategory.GENERAL_ROAST
    ),

    // --- 4. CGL (OpenGL Graphics) Mapped Events ---
    GEOMETRIC_BARRAGE(
            "CHAOS_GEOMETRIC_BARRAGE",
            "GEOMETRIC BARRAGE",
            "CGL-CO1",
            "Capital ship discharges expanding 8-sided regular polygon projectile rings in OpenGL vertex fan pattern",
            8.0,
            40.0,
            2,
            "BALLISTIC WARNING: Geometric regular polygon projectile ring expanding!",
            "Geometric explosion! Maths pass kiya hota toh dodge kar leta!",
            DialogueCategory.DESI_MEME
    ),
    MIRROR_ATTACK(
            "CHAOS_MIRROR_ATTACK",
            "MIRROR ATTACK",
            "CGL-CO2",
            "Elite interceptor units reflect the player's horizontal maneuvering via affine transformation reflection",
            10.0,
            45.0,
            3,
            "SENSORS DETECT: Interceptors slaved to player reflection transformation matrix!",
            "Mirror mirror on the wall... dushman teri hi copy maar raha hai!",
            DialogueCategory.BOLLYWOOD
    ),
    BULLET_RAIN(
            "CHAOS_BULLET_RAIN",
            "BULLET RAIN",
            "CGL-CO3",
            "Hostile fleet unleashes an algorithmic curtain of plasma bolts following integer Bresenham slopes",
            8.0,
            50.0,
            3,
            "WEATHER WARNING: Bresenham integer-stepped projectile grid incoming!",
            "Mausam vibhag ki warning: Aaj space mein plasma bullets ki barish hogi!",
            DialogueCategory.DESI_MEME
    ),
    TIME_DISTORTION(
            "CHAOS_TIME_DISTORTION",
            "TIME DISTORTION",
            "CGL-CO4",
            "Relativistic chronal field induces perspective depth dilation, slowing enemy movement and shots by 50%",
            10.0,
            50.0,
            1,
            "OPTICAL ANOMALY: Perspective projection depth warp dilating enemy chronal field!",
            "The Matrix mode on! Bullets slow motion mein aa rahi hain, ab mat miss karna!",
            DialogueCategory.BOLLYWOOD
    ),
    STEALTH_CLOAK(
            "CHAOS_STEALTH_CLOAK",
            "STEALTH CLOAK",
            "CGL-CO6",
            "Hostile hulls engage optical camouflage with 25% Gouraud alpha blending and glowing exhaust cues",
            11.0,
            45.0,
            3,
            "OPTICAL SENSORS: Hostile fleet activating stealth camouflage shrouds!",
            "Dushman Mr. India ban gaya! Bas uski red eyes dikh rahi hain!",
            DialogueCategory.BOLLYWOOD
    );

    private final String id;
    private final String displayName;
    private final String courseOutcome;
    private final String description;
    private final double defaultDuration;
    private final double cooldown;
    private final int minDifficulty;
    private final String warningLine;
    private final String activateLine;
    private final DialogueCategory category;

    ChaosEventType(String id, String displayName, String courseOutcome, String description,
                   double defaultDuration, double cooldown, int minDifficulty,
                   String warningLine, String activateLine, DialogueCategory category) {
        this.id = id;
        this.displayName = displayName;
        this.courseOutcome = courseOutcome;
        this.description = description;
        this.defaultDuration = defaultDuration;
        this.cooldown = cooldown;
        this.minDifficulty = minDifficulty;
        this.warningLine = warningLine;
        this.activateLine = activateLine;
        this.category = category;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getCourseOutcome() { return courseOutcome; }
    public String getDescription() { return description; }
    public double getDefaultDuration() { return defaultDuration; }
    public double getCooldown() { return cooldown; }
    public int getMinDifficulty() { return minDifficulty; }
    public String getWarningLine() { return warningLine; }
    public String getActivateLine() { return activateLine; }
    public DialogueCategory getCategory() { return category; }

    /**
     * Determines whether two chaos events can run concurrently without game-breaking conflicts.
     */
    public boolean isCompatibleWith(ChaosEventType other) {
        if (other == null || other == this) return false;

        // Total Chaos cannot stack with another Total Chaos
        if (this == TOTAL_CHAOS || other == TOTAL_CHAOS) return false;

        // Speed conflicts: Cannot have high speed boost and slow time dilation simultaneously
        if ((this == SPEED_DEMON && other == TIME_DISTORTION) ||
            (this == TIME_DISTORTION && other == SPEED_DEMON)) {
            return false;
        }

        // Weapon conflicts: Cannot have rapid fire and weapon jam simultaneously
        if ((this == RAPID_FIRE && other == WEAPON_JAM) ||
            (this == WEAPON_JAM && other == RAPID_FIRE)) {
            return false;
        }

        // Control conflicts: Do not stack controls inversion with time dilation or gravity well
        if (this == REVERSE_CONTROLS && (other == GRAVITY_WELL || other == TIME_DISTORTION)) {
            return false;
        }
        if (other == REVERSE_CONTROLS && (this == GRAVITY_WELL || this == TIME_DISTORTION)) {
            return false;
        }

        return true;
    }
}
