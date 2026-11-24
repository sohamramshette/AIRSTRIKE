// ==============================================================================
// File      : PL/src/searching/search_algorithms.h
// Subject   : PL-CO4 | Searching and Sorting Techniques
// Topics    : [TOPIC: Linear Search] [TOPIC: Binary Search] [TOPIC: Search Benchmarking]
// Game Role : Queries AirStrike player scores and pilot ranks with comparison and timing metrics.
// ==============================================================================

#ifndef SEARCH_ALGORITHMS_H
#define SEARCH_ALGORITHMS_H

#include <iostream>
#include <string>

// [TOPIC: PlayerScore Record]
// Purpose: Stores pilot codename, score, and wave reached for sorting and searching modules.
// Game Role: Represents one row on the AirStrike high-score leaderboard.
struct PlayerScore {
    std::string pilotName;
    int score;
    int waveReached;

    PlayerScore() : pilotName(""), score(0), waveReached(0) {}
    PlayerScore(const std::string& name, int s, int w) : pilotName(name), score(s), waveReached(w) {}
};

// [TOPIC: SearchMetrics]
// Purpose: Captures match index, comparison count, microsecond timing, and found flag.
// Game Role: Quantifies O(N) vs O(log N) search efficiency in academic benchmarks.
struct SearchMetrics {
    int index;
    long long comparisons;
    double elapsedMicroseconds;
    bool found;

    SearchMetrics() : index(-1), comparisons(0), elapsedMicroseconds(0.0), found(false) {}
};

// [TOPIC: Linear Search] [TOPIC: Binary Search]
// Purpose: Provides sequential O(N) and sorted-array O(log N) lookup on ints and PlayerScore records.
// Game Role: Looks up leaderboard scores and demonstrates sorted-precondition speedup.
namespace SearchAlgorithms {
    SearchMetrics linearSearch(const int arr[], int size, int target);
    SearchMetrics binarySearch(const int arr[], int size, int target);

    SearchMetrics linearSearchScores(const PlayerScore scores[], int size, int targetScore);
    SearchMetrics binarySearchScores(const PlayerScore scores[], int size, int targetScore);

    // Utility
    void printScores(const PlayerScore scores[], int size, const char* label = "Leaderboard");
}

#endif // SEARCH_ALGORITHMS_H

