// ==============================================================================
// File      : PL/src/searching/hunter_targeting.h
// Subject   : PL-CO4 | Searching and Sorting Techniques
// Topics    : [TOPIC: HunterTargeting] [TOPIC: QuickSort] [TOPIC: MergeSort] [TOPIC: Binary Search]
// Game Role : Powers the PL-CO4 Target-Seeking Hunter enemy's telemetry sorting and lock acquisition.
// ==============================================================================

#ifndef HUNTER_TARGETING_H
#define HUNTER_TARGETING_H

#include <iostream>
#include <string>

// [TOPIC: TargetTelemetry]
// Purpose: Holds a detected target's ID, threat distance key, priority score, and bearing.
// Game Role: Serves as the sortable radar record for TargetSeekingHunter locks.
struct TargetTelemetry {
    int targetId;
    int threatDistance; // Primary key for range targeting
    int priorityScore;  // Secondary tactical score
    double bearingDeg;

    TargetTelemetry() : targetId(0), threatDistance(0), priorityScore(0), bearingDeg(0.0) {}
    TargetTelemetry(int id, int dist, int priority, double bearing = 0.0)
        : targetId(id), threatDistance(dist), priorityScore(priority), bearingDeg(bearing) {}
};

// [TOPIC: HunterSearchMetrics]
// Purpose: Records matched telemetry index, comparison count, and search outcome.
// Game Role: Evaluates Binary Search vs Linear Search cost during target lock queries.
struct HunterSearchMetrics {
    int targetIndex;
    int comparisons;
    bool found;

    HunterSearchMetrics() : targetIndex(-1), comparisons(0), found(false) {}
    HunterSearchMetrics(int idx, int comps, bool f) : targetIndex(idx), comparisons(comps), found(f) {}
};

// [TOPIC: TargetSeekingHunter] [TOPIC: HunterTargeting] [TOPIC: Precondition Validation]
// Purpose: Orders radar targets by distance via QuickSort/MergeSort and locks ranges via Binary Search.
// Game Role: Implements the PL-CO4 sniper enemy's O(N log N) sorting and O(log N) targeting computer.
class TargetSeekingHunter {
public:
    static constexpr int MAX_TARGETS = 64;

private:
    TargetTelemetry targets[MAX_TARGETS];
    int targetCount;
    int hunterId;
    int currentLockTargetId;

    // Internal divide-and-conquer sort helpers
    void internalQuickSort(int low, int high, long long& comparisons, long long& swaps);
    int partition(int low, int high, long long& comparisons, long long& swaps);
    void internalMergeSort(int left, int right, TargetTelemetry temp[], long long& comparisons, long long& moves);
    void merge(int left, int mid, int right, TargetTelemetry temp[], long long& comparisons, long long& moves);

public:
    explicit TargetSeekingHunter(int id = 701);
    ~TargetSeekingHunter() = default;

    bool addTarget(int targetId, int threatDistance, int priorityScore, double bearingDeg = 0.0);
    void clear();

    // [TOPIC: Sorted Precondition Verification]
    // Purpose: Confirms monotonic ascending threatDistance order required by Binary Search.
    // Game Role: Guards sniper targeting against invalid binary lookups on unsorted radar data.
    static bool isSortedAscending(const TargetTelemetry arr[], int size);
    bool isTelemetrySorted() const;

    // [TOPIC: QuickSort] [TOPIC: MergeSort]
    // Purpose: Sorts targets in ascending threatDistance order while recording empirical work counts.
    // Game Role: Prepares radar telemetry for logarithmic firing-solution queries.
    void sortByDistanceQuickSort(long long& outComparisons, long long& outSwaps);
    void sortByDistanceMergeSort(long long& outComparisons, long long& outMoves);

    // [TOPIC: Binary Search] [TOPIC: Linear Search] [TOPIC: Duplicate Range Scan]
    // Purpose: Locates target distances via O(log N) Binary Search, O(N) Linear Search, or duplicate expansion.
    // Game Role: Acquires single or co-distant target locks for the Hunter sniper beam.
    HunterSearchMetrics binarySearchTarget(int queryDistance) const;
    HunterSearchMetrics linearSearchTarget(int queryDistance) const;
    int findAllTargetsAtDistance(int queryDistance, int outIndices[], int maxResults, int& outComparisons) const;

    // State Queries
    int getTargetCount() const { return targetCount; }
    int getHunterId() const { return hunterId; }
    int getCurrentLockId() const { return currentLockTargetId; }
    const TargetTelemetry* getTargetAt(int index) const;
    bool engageTargetAtDistance(int queryDistance);

    void printTelemetry() const;
};

#endif // HUNTER_TARGETING_H
