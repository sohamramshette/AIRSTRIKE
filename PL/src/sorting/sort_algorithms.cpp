#include "sort_algorithms.h"
#include <chrono>
#include <iomanip>

namespace SortAlgorithms {

// ==========================================
// 1. Bubble Sort (Integers)
// ==========================================
SortMetrics bubbleSort(int arr[], int size, bool ascending) {
    SortMetrics m;
    m.algorithmName = "Bubble Sort";
    m.bestComplexity = "O(N)";
    m.avgComplexity = "O(N^2)";
    m.worstComplexity = "O(N^2)";
    m.spaceComplexity = "O(1)";
    m.stable = true;

    auto start = std::chrono::high_resolution_clock::now();

    for (int i = 0; i < size - 1; ++i) {
        bool swapped = false;
        for (int j = 0; j < size - i - 1; ++j) {
            m.comparisons++;
            bool condition = ascending ? (arr[j] > arr[j + 1]) : (arr[j] < arr[j + 1]);
            if (condition) {
                int temp = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = temp;
                m.swaps++;
                swapped = true;
            }
        }
        if (!swapped) break; // Optimized early exit
    }

    auto end = std::chrono::high_resolution_clock::now();
    m.elapsedMicroseconds = std::chrono::duration<double, std::micro>(end - start).count();
    return m;
}

// ==========================================
// 2. Selection Sort (Integers)
// ==========================================
SortMetrics selectionSort(int arr[], int size, bool ascending) {
    SortMetrics m;
    m.algorithmName = "Selection Sort";
    m.bestComplexity = "O(N^2)";
    m.avgComplexity = "O(N^2)";
    m.worstComplexity = "O(N^2)";
    m.spaceComplexity = "O(1)";
    m.stable = false;

    auto start = std::chrono::high_resolution_clock::now();

    for (int i = 0; i < size - 1; ++i) {
        int targetIdx = i;
        for (int j = i + 1; j < size; ++j) {
            m.comparisons++;
            bool condition = ascending ? (arr[j] < arr[targetIdx]) : (arr[j] > arr[targetIdx]);
            if (condition) {
                targetIdx = j;
            }
        }
        if (targetIdx != i) {
            int temp = arr[i];
            arr[i] = arr[targetIdx];
            arr[targetIdx] = temp;
            m.swaps++;
        }
    }

    auto end = std::chrono::high_resolution_clock::now();
    m.elapsedMicroseconds = std::chrono::duration<double, std::micro>(end - start).count();
    return m;
}

// ==========================================
// 3. Insertion Sort (Integers)
// ==========================================
SortMetrics insertionSort(int arr[], int size, bool ascending) {
    SortMetrics m;
    m.algorithmName = "Insertion Sort";
    m.bestComplexity = "O(N)";
    m.avgComplexity = "O(N^2)";
    m.worstComplexity = "O(N^2)";
    m.spaceComplexity = "O(1)";
    m.stable = true;

    auto start = std::chrono::high_resolution_clock::now();

    for (int i = 1; i < size; ++i) {
        int key = arr[i];
        int j = i - 1;
        while (j >= 0) {
            m.comparisons++;
            bool condition = ascending ? (arr[j] > key) : (arr[j] < key);
            if (condition) {
                arr[j + 1] = arr[j];
                m.swaps++;
                j--;
            } else {
                break;
            }
        }
        arr[j + 1] = key;
    }

    auto end = std::chrono::high_resolution_clock::now();
    m.elapsedMicroseconds = std::chrono::duration<double, std::micro>(end - start).count();
    return m;
}

// ==========================================
// 4. Merge Sort (Integers)
// ==========================================
static void mergeInternal(int arr[], int left, int mid, int right, bool ascending, long long& comparisons, long long& swaps) {
    int n1 = mid - left + 1;
    int n2 = right - mid;

    int* L = new int[n1];
    int* R = new int[n2];

    for (int i = 0; i < n1; ++i) L[i] = arr[left + i];
    for (int j = 0; j < n2; ++j) R[j] = arr[mid + 1 + j];

    int i = 0, j = 0, k = left;
    while (i < n1 && j < n2) {
        comparisons++;
        bool condition = ascending ? (L[i] <= R[j]) : (L[i] >= R[j]);
        if (condition) {
            arr[k] = L[i++];
        } else {
            arr[k] = R[j++];
        }
        swaps++;
        k++;
    }

    while (i < n1) {
        arr[k++] = L[i++];
        swaps++;
    }
    while (j < n2) {
        arr[k++] = R[j++];
        swaps++;
    }

    delete[] L;
    delete[] R;
}

static void mergeSortRecursive(int arr[], int left, int right, bool ascending, long long& comparisons, long long& swaps) {
    if (left < right) {
        int mid = left + (right - left) / 2;
        mergeSortRecursive(arr, left, mid, ascending, comparisons, swaps);
        mergeSortRecursive(arr, mid + 1, right, ascending, comparisons, swaps);
        mergeInternal(arr, left, mid, right, ascending, comparisons, swaps);
    }
}

SortMetrics mergeSort(int arr[], int size, bool ascending) {
    SortMetrics m;
    m.algorithmName = "Merge Sort";
    m.bestComplexity = "O(N log N)";
    m.avgComplexity = "O(N log N)";
    m.worstComplexity = "O(N log N)";
    m.spaceComplexity = "O(N)";
    m.stable = true;

    auto start = std::chrono::high_resolution_clock::now();
    if (size > 1) {
        mergeSortRecursive(arr, 0, size - 1, ascending, m.comparisons, m.swaps);
    }
    auto end = std::chrono::high_resolution_clock::now();
    m.elapsedMicroseconds = std::chrono::duration<double, std::micro>(end - start).count();
    return m;
}

// ==========================================
// 5. Quick Sort (Integers)
// ==========================================
static int partitionInternal(int arr[], int low, int high, bool ascending, long long& comparisons, long long& swaps) {
    int pivot = arr[high];
    int i = low - 1;

    for (int j = low; j < high; ++j) {
        comparisons++;
        bool condition = ascending ? (arr[j] <= pivot) : (arr[j] >= pivot);
        if (condition) {
            i++;
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
            swaps++;
        }
    }
    int temp = arr[i + 1];
    arr[i + 1] = arr[high];
    arr[high] = temp;
    swaps++;
    return i + 1;
}

static void quickSortRecursive(int arr[], int low, int high, bool ascending, long long& comparisons, long long& swaps) {
    if (low < high) {
        int pi = partitionInternal(arr, low, high, ascending, comparisons, swaps);
        quickSortRecursive(arr, low, pi - 1, ascending, comparisons, swaps);
        quickSortRecursive(arr, pi + 1, high, ascending, comparisons, swaps);
    }
}

SortMetrics quickSort(int arr[], int size, bool ascending) {
    SortMetrics m;
    m.algorithmName = "Quick Sort";
    m.bestComplexity = "O(N log N)";
    m.avgComplexity = "O(N log N)";
    m.worstComplexity = "O(N^2)";
    m.spaceComplexity = "O(log N)";
    m.stable = false;

    auto start = std::chrono::high_resolution_clock::now();
    if (size > 1) {
        quickSortRecursive(arr, 0, size - 1, ascending, m.comparisons, m.swaps);
    }
    auto end = std::chrono::high_resolution_clock::now();
    m.elapsedMicroseconds = std::chrono::duration<double, std::micro>(end - start).count();
    return m;
}

// ==========================================
// Leaderboard (PlayerScore) Overloads
// ==========================================
SortMetrics bubbleSortScores(PlayerScore arr[], int size, bool descending) {
    SortMetrics m;
    m.algorithmName = "Bubble Sort (Scores)";
    m.bestComplexity = "O(N)";
    m.avgComplexity = "O(N^2)";
    m.worstComplexity = "O(N^2)";
    m.spaceComplexity = "O(1)";
    m.stable = true;

    auto start = std::chrono::high_resolution_clock::now();
    for (int i = 0; i < size - 1; ++i) {
        bool swapped = false;
        for (int j = 0; j < size - i - 1; ++j) {
            m.comparisons++;
            bool condition = descending ? (arr[j].score < arr[j + 1].score) : (arr[j].score > arr[j + 1].score);
            if (condition) {
                PlayerScore temp = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = temp;
                m.swaps++;
                swapped = true;
            }
        }
        if (!swapped) break;
    }
    auto end = std::chrono::high_resolution_clock::now();
    m.elapsedMicroseconds = std::chrono::duration<double, std::micro>(end - start).count();
    return m;
}

SortMetrics selectionSortScores(PlayerScore arr[], int size, bool descending) {
    SortMetrics m;
    m.algorithmName = "Selection Sort (Scores)";
    m.bestComplexity = "O(N^2)";
    m.avgComplexity = "O(N^2)";
    m.worstComplexity = "O(N^2)";
    m.spaceComplexity = "O(1)";
    m.stable = false;

    auto start = std::chrono::high_resolution_clock::now();
    for (int i = 0; i < size - 1; ++i) {
        int targetIdx = i;
        for (int j = i + 1; j < size; ++j) {
            m.comparisons++;
            bool condition = descending ? (arr[j].score > arr[targetIdx].score) : (arr[j].score < arr[targetIdx].score);
            if (condition) {
                targetIdx = j;
            }
        }
        if (targetIdx != i) {
            PlayerScore temp = arr[i];
            arr[i] = arr[targetIdx];
            arr[targetIdx] = temp;
            m.swaps++;
        }
    }
    auto end = std::chrono::high_resolution_clock::now();
    m.elapsedMicroseconds = std::chrono::duration<double, std::micro>(end - start).count();
    return m;
}

SortMetrics insertionSortScores(PlayerScore arr[], int size, bool descending) {
    SortMetrics m;
    m.algorithmName = "Insertion Sort (Scores)";
    m.bestComplexity = "O(N)";
    m.avgComplexity = "O(N^2)";
    m.worstComplexity = "O(N^2)";
    m.spaceComplexity = "O(1)";
    m.stable = true;

    auto start = std::chrono::high_resolution_clock::now();
    for (int i = 1; i < size; ++i) {
        PlayerScore key = arr[i];
        int j = i - 1;
        while (j >= 0) {
            m.comparisons++;
            bool condition = descending ? (arr[j].score < key.score) : (arr[j].score > key.score);
            if (condition) {
                arr[j + 1] = arr[j];
                m.swaps++;
                j--;
            } else {
                break;
            }
        }
        arr[j + 1] = key;
    }
    auto end = std::chrono::high_resolution_clock::now();
    m.elapsedMicroseconds = std::chrono::duration<double, std::micro>(end - start).count();
    return m;
}

static void mergeScoresInternal(PlayerScore arr[], int left, int mid, int right, bool descending, long long& comparisons, long long& swaps) {
    int n1 = mid - left + 1;
    int n2 = right - mid;

    PlayerScore* L = new PlayerScore[n1];
    PlayerScore* R = new PlayerScore[n2];

    for (int i = 0; i < n1; ++i) L[i] = arr[left + i];
    for (int j = 0; j < n2; ++j) R[j] = arr[mid + 1 + j];

    int i = 0, j = 0, k = left;
    while (i < n1 && j < n2) {
        comparisons++;
        bool condition = descending ? (L[i].score >= R[j].score) : (L[i].score <= R[j].score);
        if (condition) {
            arr[k] = L[i++];
        } else {
            arr[k] = R[j++];
        }
        swaps++;
        k++;
    }

    while (i < n1) {
        arr[k++] = L[i++];
        swaps++;
    }
    while (j < n2) {
        arr[k++] = R[j++];
        swaps++;
    }

    delete[] L;
    delete[] R;
}

static void mergeSortScoresRecursive(PlayerScore arr[], int left, int right, bool descending, long long& comparisons, long long& swaps) {
    if (left < right) {
        int mid = left + (right - left) / 2;
        mergeSortScoresRecursive(arr, left, mid, descending, comparisons, swaps);
        mergeSortScoresRecursive(arr, mid + 1, right, descending, comparisons, swaps);
        mergeScoresInternal(arr, left, mid, right, descending, comparisons, swaps);
    }
}

SortMetrics mergeSortScores(PlayerScore arr[], int size, bool descending) {
    SortMetrics m;
    m.algorithmName = "Merge Sort (Scores)";
    m.bestComplexity = "O(N log N)";
    m.avgComplexity = "O(N log N)";
    m.worstComplexity = "O(N log N)";
    m.spaceComplexity = "O(N)";
    m.stable = true;

    auto start = std::chrono::high_resolution_clock::now();
    if (size > 1) {
        mergeSortScoresRecursive(arr, 0, size - 1, descending, m.comparisons, m.swaps);
    }
    auto end = std::chrono::high_resolution_clock::now();
    m.elapsedMicroseconds = std::chrono::duration<double, std::micro>(end - start).count();
    return m;
}

static int partitionScoresInternal(PlayerScore arr[], int low, int high, bool descending, long long& comparisons, long long& swaps) {
    PlayerScore pivot = arr[high];
    int i = low - 1;

    for (int j = low; j < high; ++j) {
        comparisons++;
        bool condition = descending ? (arr[j].score >= pivot.score) : (arr[j].score <= pivot.score);
        if (condition) {
            i++;
            PlayerScore temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
            swaps++;
        }
    }
    PlayerScore temp = arr[i + 1];
    arr[i + 1] = arr[high];
    arr[high] = temp;
    swaps++;
    return i + 1;
}

static void quickSortScoresRecursive(PlayerScore arr[], int low, int high, bool descending, long long& comparisons, long long& swaps) {
    if (low < high) {
        int pi = partitionScoresInternal(arr, low, high, descending, comparisons, swaps);
        quickSortScoresRecursive(arr, low, pi - 1, descending, comparisons, swaps);
        quickSortScoresRecursive(arr, pi + 1, high, descending, comparisons, swaps);
    }
}

SortMetrics quickSortScores(PlayerScore arr[], int size, bool descending) {
    SortMetrics m;
    m.algorithmName = "Quick Sort (Scores)";
    m.bestComplexity = "O(N log N)";
    m.avgComplexity = "O(N log N)";
    m.worstComplexity = "O(N^2)";
    m.spaceComplexity = "O(log N)";
    m.stable = false;

    auto start = std::chrono::high_resolution_clock::now();
    if (size > 1) {
        quickSortScoresRecursive(arr, 0, size - 1, descending, m.comparisons, m.swaps);
    }
    auto end = std::chrono::high_resolution_clock::now();
    m.elapsedMicroseconds = std::chrono::duration<double, std::micro>(end - start).count();
    return m;
}

void printMetricsTable(const SortMetrics metrics[], int count) {
    std::cout << "\n=========================================================================================\n";
    std::cout << "                  AIR STRIKE SORTING ALGORITHMS BENCHMARK COMPARISON                      \n";
    std::cout << "=========================================================================================\n";
    std::cout << std::left << std::setw(24) << "Algorithm"
              << std::setw(14) << "Comparisons"
              << std::setw(10) << "Swaps"
              << std::setw(14) << "Time (us)"
              << std::setw(12) << "Best"
              << std::setw(12) << "Average"
              << std::setw(10) << "Worst"
              << "Stable?\n";
    std::cout << "-----------------------------------------------------------------------------------------\n";
    for (int i = 0; i < count; ++i) {
        std::cout << std::left << std::setw(24) << metrics[i].algorithmName
                  << std::setw(14) << metrics[i].comparisons
                  << std::setw(10) << metrics[i].swaps
                  << std::fixed << std::setprecision(2) << std::setw(14) << metrics[i].elapsedMicroseconds
                  << std::setw(12) << metrics[i].bestComplexity
                  << std::setw(12) << metrics[i].avgComplexity
                  << std::setw(10) << metrics[i].worstComplexity
                  << (metrics[i].stable ? "Yes" : "No") << "\n";
    }
    std::cout << "=========================================================================================\n";
}

void printComplexitySummary() {
    std::cout << "\n=========================================================================================\n";
    std::cout << "                         THEORETICAL TIME & SPACE COMPLEXITY                             \n";
    std::cout << "=========================================================================================\n";
    std::cout << " 1. Bubble Sort:    Best: O(N) (with flag) | Avg: O(N^2)     | Worst: O(N^2)     | Space: O(1)\n";
    std::cout << " 2. Selection Sort: Best: O(N^2)           | Avg: O(N^2)     | Worst: O(N^2)     | Space: O(1)\n";
    std::cout << " 3. Insertion Sort: Best: O(N)             | Avg: O(N^2)     | Worst: O(N^2)     | Space: O(1)\n";
    std::cout << " 4. Merge Sort:     Best: O(N log N)       | Avg: O(N log N) | Worst: O(N log N) | Space: O(N)\n";
    std::cout << " 5. Quick Sort:     Best: O(N log N)       | Avg: O(N log N) | Worst: O(N^2)     | Space: O(log N)\n";
    std::cout << "=========================================================================================\n";
}

} // namespace SortAlgorithms
