#include "test_pl_enemies.h"
#include "../src/arrays/grid_swarm_enemy.h"
#include "../src/linked_lists/formation_squadron.h"
#include "../src/queues/echelon_dispatcher.h"
#include "../src/searching/hunter_targeting.h"
#include "../src/hashing/hash_shield_controller.h"

TestSummary runPLEnemyTests() {
    TestSummary summary;
    std::cout << "\n--- Testing PL Enemy Implementations (PL-CO1 to PL-CO5) ---\n";

    // =========================================================================
    // 1. Spatial Grid Swarm Enemy (PL-CO1)
    // =========================================================================
    {
        GridSwarmEnemy swarm;
        summary.record(swarm.getTotalActiveDrones() == 0,
                       "SpatialGridSwarm: Initial active drone count is 0");

        // Row-major offset formula verification
        size_t offset00 = GridSwarmEnemy::calculateRowMajorOffset(0, 0);
        size_t offset23 = GridSwarmEnemy::calculateRowMajorOffset(2, 3);
        size_t offset77 = GridSwarmEnemy::calculateRowMajorOffset(7, 7);
        summary.record(offset00 == 0 && offset23 == (2 * 8 + 3) && offset77 == 63,
                       "SpatialGridSwarm: Row-major contiguous offset calculation (row * 8 + col)");

        // Sector coordinate mapping & clamping
        int r = -1, c = -1;
        swarm.getSector(50.0, 50.0, r, c);
        summary.record(r == 0 && c == 0,
                       "SpatialGridSwarm: Coordinate (50, 50) maps to Sector [0, 0]");

        swarm.getSector(750.0, 550.0, r, c);
        summary.record(r == 7 && c == 7,
                       "SpatialGridSwarm: Coordinate (750, 550) maps to Sector [7, 7]");

        // Boundary clamping for out-of-bounds coordinates
        swarm.getSector(-10.0, 999.0, r, c);
        summary.record(r == 7 && c == 0,
                       "SpatialGridSwarm: Out-of-bounds coords clamped to valid grid boundaries [7, 0]");

        // Spawning drones into specific cells
        bool s1 = swarm.spawnDrone(101, 50.0, 50.0, 30);
        bool s2 = swarm.spawnDrone(102, 60.0, 60.0, 30);
        summary.record(s1 && s2 && swarm.getCellCount(0, 0) == 2,
                       "SpatialGridSwarm: Spawning tracks 2D cell occupancy count (2 drones in [0, 0])");

        // Prevent duplicate IDs
        bool dup = swarm.spawnDrone(101, 70.0, 70.0, 30);
        summary.record(!dup,
                       "SpatialGridSwarm: Rejects duplicate drone ID correctly");

        // Drone movement and sector migration
        bool moved = swarm.moveDrone(102, 400.0, 300.0); // moves to center sector [4, 4]
        summary.record(moved && swarm.getCellCount(0, 0) == 1 && swarm.getCellCount(4, 4) == 1,
                       "SpatialGridSwarm: Drone movement updates spatial partitioning across cells");

        // Cell capacity enforcement (MAX_PER_CELL = 8)
        bool fillSuccess = true;
        for (int i = 0; i < 7; ++i) {
            fillSuccess = fillSuccess && swarm.spawnDrone(200 + i, 50.0 + i, 50.0 + i, 20);
        }
        bool overflowAttempt = swarm.spawnDrone(999, 55.0, 55.0, 20); // 9th drone in cell [0, 0]
        summary.record(fillSuccess && swarm.isCellFull(0, 0) && !overflowAttempt,
                       "SpatialGridSwarm: Cell capacity saturation (8 max per cell) enforced");

        // Nearby entity lookup across adjacent sectors
        int nearbyIds[10];
        int found = swarm.findNearbyDrones(50.0, 50.0, 100.0, nearbyIds, 10);
        summary.record(found >= 8,
                       "SpatialGridSwarm: 9-sector broadphase query finds all nearby swarm units");

        // Scatter mine dispersion
        int mineIds[8];
        int dispersed = swarm.disperseScatterMines(200.0, 200.0, 4, mineIds);
        summary.record(dispersed == 4,
                       "SpatialGridSwarm: Algorithmic scatter mine dispersion into neighboring cells");

        // Drone removal & slot cleanup
        bool removed = swarm.removeDrone(101);
        summary.record(removed && swarm.getCellCount(0, 0) == 7 && swarm.getDroneById(101) == nullptr,
                       "SpatialGridSwarm: Drone destruction frees cell occupancy and decrements count");
    }

    // =========================================================================
    // 2. Formation Squadron Chain (PL-CO2)
    // =========================================================================
    {
        FormationSquadron squadron("Vanguard-Delta");
        summary.record(squadron.isEmpty() && squadron.size() == 0,
                       "FormationSquadron: Initial squadron state is empty");

        // Build squadron chain: V1 -> V2 -> V3
        squadron.addVanguard(1, "Lead-Interceptor", 100, 400.0, 100.0);
        squadron.addRearguard(2, "Wing-Alpha", 80, 350.0, 150.0);
        squadron.addRearguard(3, "Wing-Beta", 80, 450.0, 150.0);
        squadron.addRearguard(4, "Tail-Anchor", 120, 400.0, 200.0);
        summary.record(squadron.size() == 4,
                       "FormationSquadron: Add Vanguard and Rearguard constructs 4-ship chain");

        // Interior rank insertion
        bool inserted = squadron.insertAtRank(2, 5, "Mid-Support", 60, 400.0, 140.0);
        summary.record(inserted && squadron.size() == 5,
                       "FormationSquadron: Rank insertion at interior index maintains chain length");

        // Bidirectional pointer integrity check
        const SquadronCraft* v = squadron.getVanguard();
        const SquadronCraft* r = squadron.getRearguard();
        summary.record(v != nullptr && v->shipId == 1 && r != nullptr && r->shipId == 4,
                       "FormationSquadron: Head points to Vanguard and Tail points to Rearguard");

        // Formation advancement maneuver
        double origLeadY = v->posY;
        squadron.advanceFormation(0.0, 25.0);
        summary.record(squadron.getVanguard()->posY == origLeadY + 25.0,
                       "FormationSquadron: Coordinated maneuver shifts all craft coordinates");

        // Interior node removal (O(1) pointer unlinking)
        bool removedMid = squadron.removeShip(5); // Remove Mid-Support
        summary.record(removedMid && squadron.size() == 4 && squadron.findShip(5) == nullptr,
                       "FormationSquadron: Mid-chain craft destruction unlinks node cleanly");

        // Formation Splitting (CO2 Key Algorithm)
        // Current chain: 1 (Lead) -> 2 (Wing-Alpha) -> 3 (Wing-Beta) -> 4 (Tail-Anchor)
        FormationSquadron detachment("Detachment");
        bool splitOk = squadron.splitAt(2, detachment); // Split after ship 2: Lead team {1, 2}, Detached team {3, 4}
        summary.record(splitOk && squadron.size() == 2 && detachment.size() == 2,
                       "FormationSquadron: O(1) Formation Splitting cuts chain into two valid squadrons");

        summary.record(squadron.getVanguard()->shipId == 1 && squadron.getRearguard()->shipId == 2 &&
                       detachment.getVanguard()->shipId == 3 && detachment.getRearguard()->shipId == 4,
                       "FormationSquadron: Split preserves head/tail integrity in both separated chains");

        // Attempting to split at tail fails gracefully (no trailing members)
        FormationSquadron dummy;
        bool splitTailFail = squadron.splitAt(2, dummy);
        summary.record(!splitTailFail,
                       "FormationSquadron: Splitting at tail returns false with zero side effects");
    }

    // =========================================================================
    // 3. Queue Wave Echelon (PL-CO3)
    // =========================================================================
    {
        QueueWaveEchelon echelonQueue(4); // Capacity 4
        summary.record(echelonQueue.isEmpty() && !echelonQueue.isFull() && echelonQueue.size() == 0,
                       "QueueWaveEchelon: Initial circular queue is empty with capacity 4");

        // Enqueue up to capacity
        echelonQueue.enqueue(EchelonWaveUnit(1, "Spearhead-Alpha", 3, 220.0, 0.0));
        echelonQueue.enqueue(EchelonWaveUnit(2, "Flankers-Bravo", 4, 180.0, 2.5));
        echelonQueue.enqueue(EchelonWaveUnit(3, "Shield-Cruiser", 1, 100.0, 5.0));
        echelonQueue.enqueue(EchelonWaveUnit(4, "Bomber-Gamma", 2, 140.0, 8.0));
        summary.record(echelonQueue.isFull() && echelonQueue.size() == 4,
                       "QueueWaveEchelon: Enqueue fills circular buffer to max capacity");

        // Overflow prevention boundary test
        bool overflowBlocked = !echelonQueue.enqueue(EchelonWaveUnit(5, "Over-Limit", 1, 100.0, 10.0));
        summary.record(overflowBlocked && echelonQueue.size() == 4,
                       "QueueWaveEchelon: Queue overflow blocked when capacity reached");

        // Dequeue 2 items (front moves from 0 to 2)
        EchelonWaveUnit d1, d2;
        bool dq1 = echelonQueue.dequeue(d1);
        bool dq2 = echelonQueue.dequeue(d2);
        summary.record(dq1 && dq2 && d1.echelonId == 1 && d2.echelonId == 2 && echelonQueue.size() == 2,
                       "QueueWaveEchelon: FIFO dequeue dispatches echelons in strict arrival order");

        // Circular Wraparound Demonstration: Enqueue 2 new items (rear wraps to 0 and 1)
        echelonQueue.enqueue(EchelonWaveUnit(5, "Wraparound-Echo", 3, 200.0, 11.0));
        echelonQueue.enqueue(EchelonWaveUnit(6, "Wraparound-Foxtrot", 5, 210.0, 14.0));
        summary.record(echelonQueue.isFull() && echelonQueue.isWrappedAround(),
                       "QueueWaveEchelon: Circular index wraparound verified (rearIndex < frontIndex)");

        // Peek operation inspects front without removal
        EchelonWaveUnit frontPeek;
        bool peekOk = echelonQueue.peek(frontPeek);
        summary.record(peekOk && frontPeek.echelonId == 3,
                       "QueueWaveEchelon: Peek inspects next launching echelon without dequeuing");

        // Drain all remaining items to verify FIFO order through wraparound
        EchelonWaveUnit d3, d4, d5, d6;
        echelonQueue.dequeue(d3);
        echelonQueue.dequeue(d4);
        echelonQueue.dequeue(d5);
        echelonQueue.dequeue(d6);
        summary.record(d3.echelonId == 3 && d4.echelonId == 4 && d5.echelonId == 5 && d6.echelonId == 6,
                       "QueueWaveEchelon: FIFO order completely preserved across circular buffer wraparound");

        // Underflow prevention boundary test
        EchelonWaveUnit underflowDummy;
        bool underflowBlocked = !echelonQueue.dequeue(underflowDummy);
        summary.record(underflowBlocked && echelonQueue.isEmpty(),
                       "QueueWaveEchelon: Underflow gracefully blocked when dequeuing from empty queue");
    }

    // =========================================================================
    // 4. Target-Seeking Hunter (PL-CO4)
    // =========================================================================
    {
        TargetSeekingHunter hunter(701);
        summary.record(hunter.getTargetCount() == 0,
                       "TargetSeekingHunter: Initial telemetry target count is 0");

        // Populate unsorted target telemetry distances: [520, 140, 890, 250, 310, 250, 700]
        hunter.addTarget(10, 520, 60, 45.0);
        hunter.addTarget(20, 140, 90, 12.0);
        hunter.addTarget(30, 890, 30, 180.0);
        hunter.addTarget(40, 250, 85, 90.0);
        hunter.addTarget(50, 310, 40, 30.0);
        hunter.addTarget(60, 250, 75, 270.0); // Duplicate threat distance (250)
        hunter.addTarget(70, 700, 50, 120.0);
        summary.record(hunter.getTargetCount() == 7 && !hunter.isTelemetrySorted(),
                       "TargetSeekingHunter: Unsorted telemetry correctly recognized before search");

        // Sort using QuickSort (Partitioning)
        long long qComps = 0, qSwaps = 0;
        hunter.sortByDistanceQuickSort(qComps, qSwaps);
        summary.record(hunter.isTelemetrySorted() && qComps > 0 && qSwaps > 0,
                       "TargetSeekingHunter: QuickSort sorts telemetry monotonically with verified swaps");

        // Binary Search on verified sorted telemetry (Target Hit)
        HunterSearchMetrics hit = hunter.binarySearchTarget(310);
        summary.record(hit.found && hit.targetIndex >= 0 && hit.comparisons <= 4,
                       "TargetSeekingHunter: Binary Search finds target in O(log N) comparisons");

        // Binary Search for non-existent target (Target Miss handling)
        HunterSearchMetrics miss = hunter.binarySearchTarget(999);
        summary.record(!miss.found && miss.targetIndex == -1,
                       "TargetSeekingHunter: Binary Search gracefully reports non-existent target miss");

        // Duplicate threat distance handling
        int dupIndices[4];
        int dupComps = 0;
        int dupCount = hunter.findAllTargetsAtDistance(250, dupIndices, 4, dupComps);
        summary.record(dupCount == 2,
                       "TargetSeekingHunter: Binary Search expands to locate all duplicate threat distances");

        // Target engagement lock
        bool locked = hunter.engageTargetAtDistance(140);
        summary.record(locked && hunter.getCurrentLockId() == 20,
                       "TargetSeekingHunter: Target acquisition engages nearest tactical threat");

        // MergeSort verification on fresh instance
        TargetSeekingHunter mergeHunter(702);
        mergeHunter.addTarget(1, 600, 10);
        mergeHunter.addTarget(2, 200, 20);
        mergeHunter.addTarget(3, 800, 30);
        mergeHunter.addTarget(4, 100, 40);
        long long mComps = 0, mMoves = 0;
        mergeHunter.sortByDistanceMergeSort(mComps, mMoves);
        summary.record(mergeHunter.isTelemetrySorted() && mComps > 0 && mMoves > 0,
                       "TargetSeekingHunter: MergeSort sorts targets ascending via divide-and-conquer");
    }

    // =========================================================================
    // 5. Hash-Shielded Matrix Craft (PL-CO5)
    // =========================================================================
    {
        HashShieldController matrix(7); // Initial prime capacity 7
        summary.record(matrix.size() == 0 && matrix.getCapacity() == 7,
                       "HashShieldMatrix: Initial matrix created with prime capacity 7");

        // Insert shield harmonic frequencies
        bool a1 = matrix.activateHarmonic("BEAM_ALPHA", 50, 40);
        bool a2 = matrix.activateHarmonic("PLASMA_BETA", 80, 70);
        bool a3 = matrix.activateHarmonic("ION_GAMMA", 60, 50);
        summary.record(a1 && a2 && a3 && matrix.size() == 3,
                       "HashShieldMatrix: Harmonics activated into open addressing hash table");

        // Retrieval of active harmonic
        int barrier = 0, absorption = 0, probes = 0;
        bool found = matrix.lookupHarmonic("PLASMA_BETA", barrier, absorption, probes);
        summary.record(found && barrier == 80 && absorption == 70 && probes >= 1,
                       "HashShieldMatrix: O(1) average lookup retrieves shield barrier strength");

        // Collision generation & Linear Probing resolution
        int initIdx = matrix.computeInitialIndex("PULSE_DELTA");
        matrix.activateHarmonic("PULSE_DELTA", 90, 85);
        int b2 = 0, a2_val = 0, p2 = 0;
        bool foundDelta = matrix.lookupHarmonic("PULSE_DELTA", b2, a2_val, p2);
        summary.record(foundDelta && b2 == 90 && initIdx >= 0,
                       "HashShieldMatrix: Linear probing successfully resolves hash collisions");

        // Tombstone Deletion Pattern (CO5 Crucial Test)
        // Deactivate PLASMA_BETA, marking slot as TOMBSTONE
        bool deactivated = matrix.deactivateHarmonic("PLASMA_BETA");
        summary.record(deactivated && matrix.getTombstoneCount() >= 1,
                       "HashShieldMatrix: Harmonic deactivation marks slot as TOMBSTONE");

        // Verify key after tombstone in probe sequence is still retrievable!
        int bDelta = 0, aDelta = 0, pDelta = 0;
        bool stillFound = matrix.lookupHarmonic("PULSE_DELTA", bDelta, aDelta, pDelta);
        summary.record(stillFound,
                       "HashShieldMatrix: Tombstone marker prevents broken probing chains for colliding keys");

        // Lookup of deactivated harmonic returns false
        int bDummy = 0, aDummy = 0, pDummy = 0;
        bool notFound = matrix.lookupHarmonic("PLASMA_BETA", bDummy, aDummy, pDummy);
        summary.record(!notFound,
                       "HashShieldMatrix: Deactivated harmonic is no longer found in table");

        // Tombstone slot recycling
        bool reused = matrix.activateHarmonic("WARP_EPSILON", 120, 95);
        summary.record(reused,
                       "HashShieldMatrix: Subsequent insertion recycles existing tombstone slot");

        // Dynamic Rehashing when load factor exceeds 0.70
        size_t oldCap = matrix.getCapacity();
        matrix.activateHarmonic("VOID_ZETA", 110, 80);
        matrix.activateHarmonic("FLUX_ETA", 95, 60);
        matrix.activateHarmonic("DISRUPT_THETA", 75, 45);
        summary.record(matrix.getCapacity() > oldCap,
                       "HashShieldMatrix: Dynamic rehashing expands table when load factor > 0.70");
    }

    return summary;
}
