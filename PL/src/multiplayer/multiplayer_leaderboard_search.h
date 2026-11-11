#ifndef AIRSTRIKE_MULTIPLAYER_LEADERBOARD_SEARCH_H
#define AIRSTRIKE_MULTIPLAYER_LEADERBOARD_SEARCH_H

#include <string>
#include <vector>

namespace Airstrike::Multiplayer {

struct MatchRecord {
    std::string matchId;
    std::string player1Id;
    std::string player2Id;
    int player1Score;
    int player2Score;
    int highestScore;
    std::string winnerId;

    MatchRecord()
        : matchId(""), player1Id(""), player2Id(""),
          player1Score(0), player2Score(0), highestScore(0), winnerId("") {}

    MatchRecord(const std::string& mId, const std::string& p1, const std::string& p2,
                int s1, int s2, const std::string& win)
        : matchId(mId), player1Id(p1), player2Id(p2),
          player1Score(s1), player2Score(s2),
          highestScore(s1 > s2 ? s1 : s2), winnerId(win) {}
};

/**
 * ==============================================================================
 * Demonstrator for PL-CO4: Searching and Sorting
 *
 * Implements canonical QuickSort and Binary Search for match records:
 * - QuickSort: O(N log N) recursive in-place sorting by highest score
 * - Binary Search: O(log N) lookup on sorted array
 * - Duplicate handling: returns range [firstIndex, lastIndex] for duplicate scores
 * ==============================================================================
 */
class MultiplayerLeaderboardSearch {
public:
    static void quickSortByScore(std::vector<MatchRecord>& records, int low, int high);
    
    // Binary search returns index of first match record matching targetScore, or -1 if not found
    static int binarySearchByScore(const std::vector<MatchRecord>& sortedRecords, int targetScore);

    // Range search for duplicate scores
    static std::pair<int, int> findScoreRange(const std::vector<MatchRecord>& sortedRecords, int targetScore);

private:
    static int partition(std::vector<MatchRecord>& records, int low, int high);
};

} // namespace Airstrike::Multiplayer

#endif // AIRSTRIKE_MULTIPLAYER_LEADERBOARD_SEARCH_H
