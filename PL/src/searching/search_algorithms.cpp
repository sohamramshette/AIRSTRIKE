#include "search_algorithms.h"
#include <chrono>
#include <iomanip>

namespace SearchAlgorithms {

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

SearchMetrics binarySearch(const int arr[], int size, int target) {
    SearchMetrics metrics;
    auto start = std::chrono::high_resolution_clock::now();

    int low = 0;
    int high = size - 1;

    while (low <= high) {
        int mid = low + (high - low) / 2;
        metrics.comparisons++;
        if (arr[mid] == target) {
            metrics.index = mid;
            metrics.found = true;
            break;
        } else if (arr[mid] < target) {
            low = mid + 1;
        } else {
            high = mid - 1;
        }
    }

    auto end = std::chrono::high_resolution_clock::now();
    metrics.elapsedMicroseconds = std::chrono::duration<double, std::micro>(end - start).count();
    return metrics;
}

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

SearchMetrics binarySearchScores(const PlayerScore scores[], int size, int targetScore) {
    SearchMetrics metrics;
    auto start = std::chrono::high_resolution_clock::now();

    int low = 0;
    int high = size - 1;

    while (low <= high) {
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
