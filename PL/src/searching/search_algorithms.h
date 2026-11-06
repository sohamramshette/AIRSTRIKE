#ifndef SEARCH_ALGORITHMS_H
#define SEARCH_ALGORITHMS_H

#include <iostream>
#include <string>

/**
 * AirStrike Leaderboard Searching Algorithms (PL-CO4).
 * Compares Linear Search vs Binary Search with empirical comparison tracking
 * and execution time benchmarking.
 */
struct PlayerScore {
    std::string pilotName;
    int score;
    int waveReached;

    PlayerScore() : pilotName(""), score(0), waveReached(0) {}
    PlayerScore(const std::string& name, int s, int w) : pilotName(name), score(s), waveReached(w) {}
};

struct SearchMetrics {
    int index;
    long long comparisons;
    double elapsedMicroseconds;
    bool found;

    SearchMetrics() : index(-1), comparisons(0), elapsedMicroseconds(0.0), found(false) {}
};

namespace SearchAlgorithms {
    // Integer array searching
    SearchMetrics linearSearch(const int arr[], int size, int target);
    SearchMetrics binarySearch(const int arr[], int size, int target);

    // Leaderboard score searching
    SearchMetrics linearSearchScores(const PlayerScore scores[], int size, int targetScore);
    SearchMetrics binarySearchScores(const PlayerScore scores[], int size, int targetScore);

    // Utility
    void printScores(const PlayerScore scores[], int size, const char* label = "Leaderboard");
}

#endif // SEARCH_ALGORITHMS_H

