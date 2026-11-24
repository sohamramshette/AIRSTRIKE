// ==============================================================================
// File      : PL/src/sorting/sort_algorithms.h
// Subject   : PL-CO4 | Searching and Sorting Techniques
// Topics    : [TOPIC: Bubble Sort] [TOPIC: Selection Sort] [TOPIC: Insertion Sort] [TOPIC: Merge Sort] [TOPIC: Quick Sort]
// Game Role : Orders AirStrike pilot leaderboards and benchmarks quadratic vs divide-and-conquer sorts.
// ==============================================================================

#ifndef SORT_ALGORITHMS_H
#define SORT_ALGORITHMS_H

#include <iostream>
#include <string>
#include "../searching/search_algorithms.h"

// [TOPIC: SortMetrics] [TOPIC: Algorithm Complexity]
// Purpose: Captures empirical comparisons, swaps, runtime, asymptotic bounds, and stability metadata.
// Game Role: Powers comparative performance reporting across all 5 AirStrike sorting algorithms.
struct SortMetrics {
    std::string algorithmName;
    long long comparisons;
    long long swaps;
    double elapsedMicroseconds;
    std::string bestComplexity;
    std::string avgComplexity;
    std::string worstComplexity;
    std::string spaceComplexity;
    bool stable;

    SortMetrics()
        : algorithmName(""), comparisons(0), swaps(0), elapsedMicroseconds(0.0),
          bestComplexity(""), avgComplexity(""), worstComplexity(""), spaceComplexity(""), stable(false) {}
};

// [TOPIC: Sorting Algorithms] [TOPIC: Stability] [TOPIC: Divide and Conquer]
// Purpose: Exposes Bubble, Selection, Insertion, Merge, and Quick sort for integers and PlayerScore tables.
// Game Role: Ranks AirStrike high-score leaderboards and generates empirical benchmark tables.
namespace SortAlgorithms {
    // [TOPIC: Integer Array Sorting]
    // Purpose: Sorts raw integer arrays using all 5 canonical sorting strategies.
    // Game Role: Benchmarks sorting complexity on numeric telemetry samples.
    SortMetrics bubbleSort(int arr[], int size, bool ascending = true);
    SortMetrics selectionSort(int arr[], int size, bool ascending = true);
    SortMetrics insertionSort(int arr[], int size, bool ascending = true);
    SortMetrics mergeSort(int arr[], int size, bool ascending = true);
    SortMetrics quickSort(int arr[], int size, bool ascending = true);

    // [TOPIC: Leaderboard Score Sorting]
    // Purpose: Ranks PlayerScore structs (typically descending) while tracking comparisons and swaps.
    // Game Role: Produces ordered AirStrike high-score leaderboards for Binary Search lookups.
    SortMetrics bubbleSortScores(PlayerScore arr[], int size, bool descending = true);
    SortMetrics selectionSortScores(PlayerScore arr[], int size, bool descending = true);
    SortMetrics insertionSortScores(PlayerScore arr[], int size, bool descending = true);
    SortMetrics mergeSortScores(PlayerScore arr[], int size, bool descending = true);
    SortMetrics quickSortScores(PlayerScore arr[], int size, bool descending = true);

    // Comparative Analysis Display
    void printMetricsTable(const SortMetrics metrics[], int count);
    void printComplexitySummary();
}

#endif // SORT_ALGORITHMS_H

