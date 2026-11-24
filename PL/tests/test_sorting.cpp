// ==============================================================================
// File      : PL/tests/test_sorting.cpp
// Subject   : PL-CO4 | Sorting Unit Tests
// Topics    : [TOPIC: Bubble Sort] [TOPIC: Selection Sort] [TOPIC: Insertion Sort] [TOPIC: Merge Sort] [TOPIC: Quick Sort]
// Game Role : Verifies ordering, swap/comparison metrics, and leaderboard ranking across all 5 sorts.
// ==============================================================================

#include "test_sorting.h"
#include "../src/sorting/sort_algorithms.h"

// [TOPIC: Sort Verification Helpers]
// Purpose: Validates monotonic ascending/descending ordering for raw int and PlayerScore arrays.
// Game Role: Confirms post-conditions for all AIRSTRIKE sorting test cases.
static bool isSorted(const int arr[], int size, bool ascending = true) {
    for (int i = 0; i < size - 1; ++i) {
        if (ascending ? (arr[i] > arr[i + 1]) : (arr[i] < arr[i + 1])) {
            return false;
        }
    }
    return true;
}

static bool isScoresSorted(const PlayerScore arr[], int size, bool descending = true) {
    for (int i = 0; i < size - 1; ++i) {
        if (descending ? (arr[i].score < arr[i + 1].score) : (arr[i].score > arr[i + 1].score)) {
            return false;
        }
    }
    return true;
}

// [TOPIC: PL-CO4 Sorting Unit Tests]
// Purpose: Executes 7 automated checks validating Bubble, Selection, Insertion, Merge, and Quick Sort.
// Game Role: Guarantees correct high-score ordering and O(N) best-case Bubble Sort early exit.
TestSummary runSortingTests() {
    TestSummary summary;
    std::cout << "\n======================================================\n";
    std::cout << "[TEST SUITE] PL-CO4: Sorting Algorithms (7 Tests)\n";
    std::cout << "======================================================\n";

    const int initial[] = {64, 34, 25, 12, 22, 11, 90, 88, 45, 50};
    const int n = sizeof(initial) / sizeof(initial[0]);

    // 1. Bubble Sort
    int a1[n];
    for (int i = 0; i < n; ++i) a1[i] = initial[i];
    SortMetrics mBubble = SortAlgorithms::bubbleSort(a1, n, true);
    summary.record(isSorted(a1, n, true) && mBubble.comparisons > 0 && mBubble.swaps > 0,
                   "BubbleSort: Successfully sorts array in ascending order");

    // 2. Selection Sort
    int a2[n];
    for (int i = 0; i < n; ++i) a2[i] = initial[i];
    SortMetrics mSelect = SortAlgorithms::selectionSort(a2, n, true);
    summary.record(isSorted(a2, n, true) && mSelect.swaps <= n,
                   "SelectionSort: Minimal O(N) swaps verification");

    // 3. Insertion Sort
    int a3[n];
    for (int i = 0; i < n; ++i) a3[i] = initial[i];
    SortMetrics mInsert = SortAlgorithms::insertionSort(a3, n, true);
    summary.record(isSorted(a3, n, true) && mInsert.comparisons > 0,
                   "InsertionSort: Successfully sorts array in-place");

    // 4. Merge Sort (Divide & Conquer)
    int a4[n];
    for (int i = 0; i < n; ++i) a4[i] = initial[i];
    SortMetrics mMerge = SortAlgorithms::mergeSort(a4, n, true);
    summary.record(isSorted(a4, n, true) && mMerge.comparisons > 0,
                   "MergeSort: O(N log N) divide-and-conquer sorting");

    // 5. Quick Sort (Partitioning)
    int a5[n];
    for (int i = 0; i < n; ++i) a5[i] = initial[i];
    SortMetrics mQuick = SortAlgorithms::quickSort(a5, n, true);
    summary.record(isSorted(a5, n, true) && mQuick.comparisons > 0,
                   "QuickSort: In-place recursive partitioning sorting");

    // 6. AirStrike Leaderboard High-Score Ordering (Descending)
    PlayerScore leaderboard[] = {
        PlayerScore("NOOB_PILOT",   300, 2),
        PlayerScore("SOLAR_TITAN", 4800, 10),
        PlayerScore("ALPHA_LEADER", 850, 4),
        PlayerScore("PHANTOM_ACE", 3200, 8),
        PlayerScore("VIPER_WING",  1500, 5)
    };
    int lbSize = 5;
    SortAlgorithms::quickSortScores(leaderboard, lbSize, true); // Descending
    summary.record(isScoresSorted(leaderboard, lbSize, true) &&
                   leaderboard[0].score == 4800 && leaderboard[lbSize - 1].score == 300,
                   "QuickSortScores: AirStrike High Scores descending ordering");

    // 7. Bubble Sort Best-Case Optimization (Already Sorted Array)
    int sortedArr[] = {1, 2, 3, 4, 5, 6, 7, 8};
    int sSize = sizeof(sortedArr) / sizeof(sortedArr[0]);
    SortMetrics mEarlyExit = SortAlgorithms::bubbleSort(sortedArr, sSize, true);
    summary.record(mEarlyExit.swaps == 0 && mEarlyExit.comparisons == sSize - 1,
                   "BubbleSort: Early termination flag achieves O(N) best case");

    return summary;
}

