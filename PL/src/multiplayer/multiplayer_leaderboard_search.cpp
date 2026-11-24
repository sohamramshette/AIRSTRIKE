// ==============================================================================
// File      : PL/src/multiplayer/multiplayer_leaderboard_search.cpp
// Subject   : PL-CO4 | Searching and Sorting Techniques
// Topics    : [TOPIC: MultiplayerLeaderboardSearch] [TOPIC: QuickSort] [TOPIC: Binary Search] [TOPIC: Mid Overflow Prevention]
// Game Role : Implements match-record QuickSort, overflow-safe Binary Search, and tie-range lookup.
// ==============================================================================

#include "multiplayer_leaderboard_search.h"
#include <utility>

namespace Airstrike::Multiplayer {

// [TOPIC: QuickSort] [TOPIC: Lomuto Partition]
// Purpose: Partitions MatchRecords around records[high].highestScore in-place.
// Game Role: Orders multiplayer match brackets recursively in O(N log N) average time.
int MultiplayerLeaderboardSearch::partition(std::vector<MatchRecord>& records, int low, int high) {
    // WHY: Lomuto pivot at high swaps records <= pivot into the left partition in-place.
    int pivot = records[high].highestScore;
    int i = low - 1;

    for (int j = low; j < high; ++j) {
        if (records[j].highestScore <= pivot) {
            ++i;
            std::swap(records[i], records[j]);
        }
    }
    std::swap(records[i + 1], records[high]);
    return i + 1;
}

void MultiplayerLeaderboardSearch::quickSortByScore(std::vector<MatchRecord>& records, int low, int high) {
    if (low < high) {
        int pi = partition(records, low, high);
        quickSortByScore(records, low, pi - 1);
        quickSortByScore(records, pi + 1, high);
    }
}

// [TOPIC: Binary Search] [TOPIC: Mid Overflow Prevention]
// Purpose: Searches ascending MatchRecord vector by highestScore in O(log N).
// Game Role: Finds any match achieving targetScore without scanning the entire history.
int MultiplayerLeaderboardSearch::binarySearchByScore(const std::vector<MatchRecord>& sortedRecords, int targetScore) {
    int low = 0;
    int high = static_cast<int>(sortedRecords.size()) - 1;

    while (low <= high) {
        // WHY: Compute low + (high - low) / 2 to avoid signed integer overflow on large bounds.
        int mid = low + (high - low) / 2;
        if (sortedRecords[mid].highestScore == targetScore) {
            return mid;
        }
        if (sortedRecords[mid].highestScore < targetScore) {
            low = mid + 1;
        } else {
            high = mid - 1;
        }
    }
    return -1;
}

// [TOPIC: Binary Search] [TOPIC: Duplicate Range Search]
// Purpose: Expands left and right from a binary-search hit to return [first, last] for tied scores.
// Game Role: Retrieves all multiplayer matches sharing an identical top score.
std::pair<int, int> MultiplayerLeaderboardSearch::findScoreRange(const std::vector<MatchRecord>& sortedRecords, int targetScore) {
    int idx = binarySearchByScore(sortedRecords, targetScore);
    if (idx == -1) {
        return {-1, -1};
    }

    // WHY: Binary search may hit any element in a run of duplicates, so walk both directions to find boundaries.
    int first = idx;
    while (first > 0 && sortedRecords[first - 1].highestScore == targetScore) {
        --first;
    }

    int last = idx;
    int n = static_cast<int>(sortedRecords.size());
    while (last + 1 < n && sortedRecords[last + 1].highestScore == targetScore) {
        ++last;
    }

    return {first, last};
}

} // namespace Airstrike::Multiplayer
