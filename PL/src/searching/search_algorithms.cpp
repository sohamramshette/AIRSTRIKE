// ==============================================================================
// File      : PL/src/searching/search_algorithms.cpp
// Subject   : PL-CO4 | Searching and Sorting Techniques
// Topics    : [TOPIC: Linear Search] [TOPIC: Binary Search] [TOPIC: Mid Overflow Prevention]
// Game Role : Implements O(N) linear and O(log N) binary search with empirical benchmarks.
// ==============================================================================

#include "search_algorithms.h"
#include <chrono>
#include <iomanip>

namespace SearchAlgorithms {

// [TOPIC: Linear Search]
// Purpose: Scans an arbitrary array sequentially in O(N) time while counting comparisons.
// Game Role: Searches unsorted integer telemetry without sorting overhead.
SearchMetrics linearSearch(const int arr[], int size, int target) {
    SearchMetrics metrics;
    auto start = std::chrono::high_resolution_clock::now();

    for (int i = 0; i < size; ++i) {
        metrics.comparisons++;
        if (arr[i] == target) {
            metrics.index = i;
            metrics.found = true;
            break;
        }
    }

    auto end = std::chrono::high_resolution_clock::now();
    metrics.elapsedMicroseconds = std::chrono::duration<double, std::micro>(end - start).count();
    return metrics;
}

// [TOPIC: Binary Search] [TOPIC: Mid Overflow Prevention]
// Purpose: Halves a sorted ascending search space iteratively in O(log N) time.
// Game Role: Fast exact score/ID lookup on pre-sorted AirStrike tables.
SearchMetrics binarySearch(const int arr[], int size, int target) {
    SearchMetrics metrics;
    auto start = std::chrono::high_resolution_clock::now();

    int low = 0;
    int high = size - 1;

    while (low <= high) {
        // WHY: Use low + (high - low) / 2 instead of (low + high) / 2 to avoid signed int overflow.
        int mid = low + (high - low) / 2;
        metrics.comparisons++;
        if (arr[mid] == target) {
            metrics.index = mid;
            metrics.found = true;
            break;
        } else if (arr[mid] < target) {
            // WHY: Exclude mid (+1) so the remaining range strictly shrinks and guarantees termination.
            low = mid + 1;
        } else {
            high = mid - 1;
        }
    }

    auto end = std::chrono::high_resolution_clock::now();
    metrics.elapsedMicroseconds = std::chrono::duration<double, std::micro>(end - start).count();
    return metrics;
}

// [TOPIC: Linear Search] [TOPIC: Leaderboard Search]
// Purpose: Finds a PlayerScore entry sequentially across unsorted leaderboard records in O(N).
// Game Role: Queries pilot scores when leaderboard ordering is unavailable.
SearchMetrics linearSearchScores(const PlayerScore scores[], int size, int targetScore) {
    SearchMetrics metrics;
    auto start = std::chrono::high_resolution_clock::now();

    for (int i = 0; i < size; ++i) {
        metrics.comparisons++;
        if (scores[i].score == targetScore) {
            metrics.index = i;
            metrics.found = true;
            break;
        }
    }

    auto end = std::chrono::high_resolution_clock::now();
    metrics.elapsedMicroseconds = std::chrono::duration<double, std::micro>(end - start).count();
    return metrics;
}

// [TOPIC: Binary Search] [TOPIC: Leaderboard Search]
// Purpose: Finds targetScore in an ascending-sorted PlayerScore array in O(log N).
// Game Role: Rapidly locates matching pilot ranks on sorted AirStrike leaderboards.
SearchMetrics binarySearchScores(const PlayerScore scores[], int size, int targetScore) {
    SearchMetrics metrics;
    auto start = std::chrono::high_resolution_clock::now();

    int low = 0;
    int high = size - 1;

    while (low <= high) {
        // WHY: Overflow-safe midpoint prevents signed wrap-around on large datasets.
        int mid = low + (high - low) / 2;
        metrics.comparisons++;
        if (scores[mid].score == targetScore) {
            metrics.index = mid;
            metrics.found = true;
            break;
        } else if (scores[mid].score < targetScore) {
            low = mid + 1;
        } else {
            high = mid - 1;
        }
    }

    auto end = std::chrono::high_resolution_clock::now();
    metrics.elapsedMicroseconds = std::chrono::duration<double, std::micro>(end - start).count();
    return metrics;
}

void printScores(const PlayerScore scores[], int size, const char* label) {
    std::cout << "\n--- " << label << " (" << size << " entries) ---\n";
    std::cout << std::left << std::setw(6) << "Rank"
              << std::setw(18) << "Pilot Codename"
              << std::setw(12) << "Score"
              << "Wave Reached\n";
    std::cout << "--------------------------------------------------------\n";
    for (int i = 0; i < size; ++i) {
        std::cout << "#" << std::setw(5) << (i + 1)
                  << std::setw(18) << scores[i].pilotName
                  << std::setw(12) << scores[i].score
                  << scores[i].waveReached << "\n";
    }
}

} // namespace SearchAlgorithms

