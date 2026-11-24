// ==============================================================================
// File      : PL/src/multiplayer/multiplayer_leaderboard_search.h
// Subject   : PL-CO4 | Searching and Sorting Techniques
// Topics    : [TOPIC: MultiplayerLeaderboardSearch] [TOPIC: QuickSort] [TOPIC: Binary Search]
// Game Role : Ranks two-player AirStrike match outcomes and queries target scores in O(log N).
// ==============================================================================

#ifndef AIRSTRIKE_MULTIPLAYER_LEADERBOARD_SEARCH_H
#define AIRSTRIKE_MULTIPLAYER_LEADERBOARD_SEARCH_H

#include <string>
#include <vector>

namespace Airstrike::Multiplayer {

// [TOPIC: MatchRecord]
// Purpose: Summarizes a completed two-player match, storing individual scores and max highestScore key.
// Game Role: Unit record sorted and searched in multiplayer match history rankings.
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

// [TOPIC: MultiplayerLeaderboardSearch] [TOPIC: QuickSort] [TOPIC: Binary Search] [TOPIC: Range Search]
// Purpose: Provides in-place QuickSort, iterative Binary Search, and duplicate-score range expansion.
// Game Role: Orders multiplayer match history by top score and fetches rank/tie spans rapidly.
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
