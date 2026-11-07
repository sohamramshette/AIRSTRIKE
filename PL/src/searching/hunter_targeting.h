#ifndef HUNTER_TARGETING_H
#define HUNTER_TARGETING_H

#include <iostream>
#include <string>

/**
 * Target-Seeking Hunter (PL-CO4).
 *
 * Demonstrates:
 * - Threat Telemetry Sorting via QuickSort (Partitioning) and MergeSort (Divide & Conquer)
 * - Strict Precondition Validation: Verifying array is monotonically sorted before Binary Search
 * - Binary Search on Sorted Telemetry: O(log N) lookup vs O(N) Linear Search
 * - Robust handling of missing targets and duplicate threat distances
 * - Empirical comparison and swap metrics for viva evaluation
 */
struct TargetTelemetry {
    int targetId;
    int threatDistance; // Primary key for range targeting
    int priorityScore;  // Secondary tactical score
    double bearingDeg;

    TargetTelemetry() : targetId(0), threatDistance(0), priorityScore(0), bearingDeg(0.0) {}
    TargetTelemetry(int id, int dist, int priority, double bearing = 0.0)
        : targetId(id), threatDistance(dist), priorityScore(priority), bearingDeg(bearing) {}
};

struct HunterSearchMetrics {
    int targetIndex;
    int comparisons;
    bool found;

    HunterSearchMetrics() : targetIndex(-1), comparisons(0), found(false) {}
    HunterSearchMetrics(int idx, int comps, bool f) : targetIndex(idx), comparisons(comps), found(f) {}
};

class TargetSeekingHunter {
public:
    static constexpr int MAX_TARGETS = 64;

private:
    TargetTelemetry targets[MAX_TARGETS];
    int targetCount;
    int hunterId;
    int currentLockTargetId;

    // Internal sorting routines
    void internalQuickSort(int low, int high, long long& comparisons, long long& swaps);
    int partition(int low, int high, long long& comparisons, long long& swaps);
    void internalMergeSort(int left, int right, TargetTelemetry temp[], long long& comparisons, long long& moves);
    void merge(int left, int mid, int right, TargetTelemetry temp[], long long& comparisons, long long& moves);

public:
    explicit TargetSeekingHunter(int id = 701);
    ~TargetSeekingHunter() = default;

    // Telemetry population
    bool addTarget(int targetId, int threatDistance, int priorityScore, double bearingDeg = 0.0);
    void clear();

    // Precondition Verification
    static bool isSortedAscending(const TargetTelemetry arr[], int size);
    bool isTelemetrySorted() const;

    // Sorting Algorithms (PL-CO4)
    void sortByDistanceQuickSort(long long& outComparisons, long long& outSwaps);
    void sortByDistanceMergeSort(long long& outComparisons, long long& outMoves);

    // Searching Algorithms (PL-CO4)
    HunterSearchMetrics binarySearchTarget(int queryDistance) const;
    HunterSearchMetrics linearSearchTarget(int queryDistance) const;

    // Duplicate Handling: Locates all targets matching exact distance within sorted array
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
