#include "hunter_targeting.h"
#include <iomanip>
#include <algorithm>

TargetSeekingHunter::TargetSeekingHunter(int id)
    : targetCount(0), hunterId(id), currentLockTargetId(-1) {}

void TargetSeekingHunter::clear() {
    targetCount = 0;
    currentLockTargetId = -1;
}

bool TargetSeekingHunter::addTarget(int targetId, int threatDistance, int priorityScore, double bearingDeg) {
    if (targetCount >= MAX_TARGETS) return false;
    targets[targetCount] = TargetTelemetry(targetId, threatDistance, priorityScore, bearingDeg);
    targetCount++;
    return true;
}

bool TargetSeekingHunter::isSortedAscending(const TargetTelemetry arr[], int size) {
    if (size <= 1) return true;
    for (int i = 0; i < size - 1; ++i) {
        if (arr[i].threatDistance > arr[i + 1].threatDistance) {
            return false;
        }
    }
    return true;
}

bool TargetSeekingHunter::isTelemetrySorted() const {
    return isSortedAscending(targets, targetCount);
}

int TargetSeekingHunter::partition(int low, int high, long long& comparisons, long long& swaps) {
    int pivot = targets[high].threatDistance;
    int i = low - 1;

    for (int j = low; j < high; ++j) {
        comparisons++;
        if (targets[j].threatDistance <= pivot) {
            i++;
            std::swap(targets[i], targets[j]);
            swaps++;
        }
    }
    std::swap(targets[i + 1], targets[high]);
    swaps++;
    return i + 1;
}

void TargetSeekingHunter::internalQuickSort(int low, int high, long long& comparisons, long long& swaps) {
    if (low < high) {
        int pi = partition(low, high, comparisons, swaps);
        internalQuickSort(low, pi - 1, comparisons, swaps);
        internalQuickSort(pi + 1, high, comparisons, swaps);
    }
}

void TargetSeekingHunter::sortByDistanceQuickSort(long long& outComparisons, long long& outSwaps) {
    outComparisons = 0;
    outSwaps = 0;
    if (targetCount <= 1) return;
    internalQuickSort(0, targetCount - 1, outComparisons, outSwaps);
}

void TargetSeekingHunter::merge(int left, int mid, int right, TargetTelemetry temp[],
                                long long& comparisons, long long& moves) {
    int i = left;
    int j = mid + 1;
    int k = left;

    while (i <= mid && j <= right) {
        comparisons++;
        if (targets[i].threatDistance <= targets[j].threatDistance) {
            temp[k++] = targets[i++];
        } else {
            temp[k++] = targets[j++];
        }
        moves++;
    }

    while (i <= mid) {
        temp[k++] = targets[i++];
        moves++;
    }

    while (j <= right) {
        temp[k++] = targets[j++];
        moves++;
    }

    for (int p = left; p <= right; ++p) {
        targets[p] = temp[p];
        moves++;
    }
}

void TargetSeekingHunter::internalMergeSort(int left, int right, TargetTelemetry temp[],
                                           long long& comparisons, long long& moves) {
    if (left < right) {
        int mid = left + (right - left) / 2;
        internalMergeSort(left, mid, temp, comparisons, moves);
        internalMergeSort(mid + 1, right, temp, comparisons, moves);
        merge(left, mid, right, temp, comparisons, moves);
    }
}

void TargetSeekingHunter::sortByDistanceMergeSort(long long& outComparisons, long long& outMoves) {
    outComparisons = 0;
    outMoves = 0;
    if (targetCount <= 1) return;
    TargetTelemetry temp[MAX_TARGETS];
    internalMergeSort(0, targetCount - 1, temp, outComparisons, outMoves);
}

HunterSearchMetrics TargetSeekingHunter::binarySearchTarget(int queryDistance) const {
    // Precondition Verification (PL-CO4 Requirement)
    if (!isTelemetrySorted()) {
        std::cerr << "[WARNING: Precondition Violation] Array not sorted! Binary search results undefined.\n";
    }

    int left = 0;
    int right = targetCount - 1;
    int comps = 0;

    while (left <= right) {
        comps++;
        int mid = left + (right - left) / 2;
        if (targets[mid].threatDistance == queryDistance) {
            return HunterSearchMetrics(mid, comps, true);
        } else if (targets[mid].threatDistance < queryDistance) {
            left = mid + 1;
        } else {
            right = mid - 1;
        }
    }

    return HunterSearchMetrics(-1, comps, false);
}

HunterSearchMetrics TargetSeekingHunter::linearSearchTarget(int queryDistance) const {
    int comps = 0;
    for (int i = 0; i < targetCount; ++i) {
        comps++;
        if (targets[i].threatDistance == queryDistance) {
            return HunterSearchMetrics(i, comps, true);
        }
    }
    return HunterSearchMetrics(-1, comps, false);
}

int TargetSeekingHunter::findAllTargetsAtDistance(int queryDistance, int outIndices[],
                                                  int maxResults, int& outComparisons) const {
    // Locate any matching instance via Binary Search
    HunterSearchMetrics match = binarySearchTarget(queryDistance);
    outComparisons = match.comparisons;
    if (!match.found) return 0;

    int count = 0;
    int mid = match.targetIndex;

    // Scan backwards for duplicate threat distances
    int first = mid;
    while (first >= 0 && targets[first].threatDistance == queryDistance) {
        first--;
    }
    first++; // First index with queryDistance

    // Collect all consecutive matches
    int curr = first;
    while (curr < targetCount && targets[curr].threatDistance == queryDistance && count < maxResults) {
        outIndices[count++] = curr;
        curr++;
    }

    return count;
}

const TargetTelemetry* TargetSeekingHunter::getTargetAt(int index) const {
    if (index >= 0 && index < targetCount) {
        return &targets[index];
    }
    return nullptr;
}

bool TargetSeekingHunter::engageTargetAtDistance(int queryDistance) {
    HunterSearchMetrics res = binarySearchTarget(queryDistance);
    if (res.found) {
        currentLockTargetId = targets[res.targetIndex].targetId;
        return true;
    }
    currentLockTargetId = -1;
    return false;
}

void TargetSeekingHunter::printTelemetry() const {
    std::cout << "\n=== Hunter [" << hunterId << "] Target Telemetry (" << targetCount << " detected) ===\n";
    std::cout << "Sorted Status: " << (isTelemetrySorted() ? "MONOTONICALLY SORTED [OK]" : "UNSORTED") << "\n";
    std::cout << std::left << std::setw(6) << "Index"
              << std::setw(10) << "TargetID"
              << std::setw(12) << "Distance"
              << std::setw(10) << "Priority"
              << "Bearing\n";
    std::cout << "--------------------------------------------------------\n";
    for (int i = 0; i < targetCount; ++i) {
        std::cout << "[" << std::setw(4) << i << "] "
                  << std::setw(10) << targets[i].targetId
                  << std::setw(12) << targets[i].threatDistance
                  << std::setw(10) << targets[i].priorityScore
                  << std::fixed << std::setprecision(1) << targets[i].bearingDeg << " deg"
                  << (targets[i].targetId == currentLockTargetId ? " <-- LOCKED" : "")
                  << "\n";
    }
}
