#include "test_searching.h"
#include "../src/searching/search_algorithms.h"

TestSummary runSearchingTests() {
    TestSummary summary;
    std::cout << "\n======================================================\n";
    std::cout << "[TEST SUITE] PL-CO4: Searching Algorithms (6 Tests)\n";
    std::cout << "======================================================\n";

    int arr[] = {100, 250, 400, 550, 700, 850, 1000, 1200};
    int size = sizeof(arr) / sizeof(arr[0]);

    // 1. Linear Search - Target Present
    SearchMetrics mLinHit = SearchAlgorithms::linearSearch(arr, size, 550);
    summary.record(mLinHit.found && mLinHit.index == 3 && mLinHit.comparisons == 4,
                   "LinearSearch: Found target with exact linear comparison count");

    // 2. Linear Search - Target Absent
    SearchMetrics mLinMiss = SearchAlgorithms::linearSearch(arr, size, 9999);
    summary.record(!mLinMiss.found && mLinMiss.index == -1 && mLinMiss.comparisons == size,
                   "LinearSearch: Exhaustive traversal on search miss");

    // 3. Binary Search - Middle Target Present
    SearchMetrics mBinHit = SearchAlgorithms::binarySearch(arr, size, 550);
    summary.record(mBinHit.found && mBinHit.index == 3 && mBinHit.comparisons <= 4,
                   "BinarySearch: O(log N) logarithmic lookup hit");

    // 4. Binary Search - Boundary Elements (First and Last)
    SearchMetrics mBinFirst = SearchAlgorithms::binarySearch(arr, size, 100);
    SearchMetrics mBinLast  = SearchAlgorithms::binarySearch(arr, size, 1200);
    summary.record(mBinFirst.found && mBinFirst.index == 0 && mBinLast.found && mBinLast.index == size - 1,
                   "BinarySearch: Boundary boundary extremes (First and Last)");

    // 5. Binary Search - Target Absent
    SearchMetrics mBinMiss = SearchAlgorithms::binarySearch(arr, size, 300);
    summary.record(!mBinMiss.found && mBinMiss.index == -1,
                   "BinarySearch: Correct miss detection without infinite looping");

    // 6. Leaderboard PlayerScore Searching
    PlayerScore leaderboard[] = {
        PlayerScore("ALPHA_LEADER", 500, 3),
        PlayerScore("BRAVO_WING",   1200, 5),
        PlayerScore("PHANTOM_ACE",  2500, 8),
        PlayerScore("SOLAR_TITAN",  4800, 10)
    };
    int lbSize = 4;
    SearchMetrics lbHit = SearchAlgorithms::binarySearchScores(leaderboard, lbSize, 2500);
    summary.record(lbHit.found && lbHit.index == 2,
                   "BinarySearchScores: AirStrike leaderboard binary search");

    return summary;
}

