#ifndef SORT_ALGORITHMS_H
#define SORT_ALGORITHMS_H

#include <iostream>
#include <string>
#include "../searching/search_algorithms.h"

/**
 * AirStrike Leaderboard Sorting Algorithms (PL-CO4).
 * Comprehensive implementation of the 5 canonical sorting algorithms:
 * 1. Bubble Sort
 * 2. Selection Sort
 * 3. Insertion Sort
 * 4. Merge Sort (Divide and Conquer)
 * 5. Quick Sort (Partitioning)
 *
 * Each tracks comparisons, swaps/data moves, execution time, and provides
 * academic complexity metrics for viva examinations.
 */
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

namespace SortAlgorithms {
    // Integer Array Sorting
    SortMetrics bubbleSort(int arr[], int size, bool ascending = true);
    SortMetrics selectionSort(int arr[], int size, bool ascending = true);
    SortMetrics insertionSort(int arr[], int size, bool ascending = true);
    SortMetrics mergeSort(int arr[], int size, bool ascending = true);
    SortMetrics quickSort(int arr[], int size, bool ascending = true);

    // AirStrike Leaderboard PlayerScore Sorting
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

