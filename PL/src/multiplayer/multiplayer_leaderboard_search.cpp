#include "multiplayer_leaderboard_search.h"
#include <utility>

namespace Airstrike::Multiplayer {

int MultiplayerLeaderboardSearch::partition(std::vector<MatchRecord>& records, int low, int high) {
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

int MultiplayerLeaderboardSearch::binarySearchByScore(const std::vector<MatchRecord>& sortedRecords, int targetScore) {
    int low = 0;
    int high = static_cast<int>(sortedRecords.size()) - 1;

    while (low <= high) {
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

std::pair<int, int> MultiplayerLeaderboardSearch::findScoreRange(const std::vector<MatchRecord>& sortedRecords, int targetScore) {
    int idx = binarySearchByScore(sortedRecords, targetScore);
    if (idx == -1) {
        return {-1, -1};
    }

    // Expand backwards
    int first = idx;
    while (first > 0 && sortedRecords[first - 1].highestScore == targetScore) {
        --first;
    }

    // Expand forwards
    int last = idx;
    int n = static_cast<int>(sortedRecords.size());
    while (last + 1 < n && sortedRecords[last + 1].highestScore == targetScore) {
        ++last;
    }

    return {first, last};
}

} // namespace Airstrike::Multiplayer
