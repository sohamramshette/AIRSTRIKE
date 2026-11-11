#ifndef AIRSTRIKE_MATCH_EVENT_H
#define AIRSTRIKE_MATCH_EVENT_H

#include <cstdint>
#include <string>
#include <vector>

namespace Airstrike::Multiplayer {

/**
 * Common canonical match event data model for native demonstrations.
 * Consumed across PL (C++17), COA (80386 ASM), and CGL (OpenGL).
 */
struct MatchEvent {
    uint32_t eventId;
    std::string eventType;  // "PLAYER_JOINED", "READY", "MATCH_STARTED", "SCORE_UPDATED", "COLLISION", "MATCH_COMPLETED"
    std::string playerId;   // "PILOT-ALPHA" (Player 1) or "PILOT-BRAVO" (Player 2)
    uint64_t timestamp;     // Epoch millisecond timestamp
    int32_t score;          // Active score value
    float positionX;        // X coordinate in [0, 800] viewport
    float positionY;        // Y coordinate in [0, 650] viewport

    MatchEvent()
        : eventId(0), eventType(""), playerId(""), timestamp(0), score(0), positionX(0.0f), positionY(0.0f) {}

    MatchEvent(uint32_t id, const std::string& type, const std::string& player,
               uint64_t time, int32_t sc, float x, float y)
        : eventId(id), eventType(type), playerId(player), timestamp(time), score(sc), positionX(x), positionY(y) {}
};

/**
 * Generates local sample fixtures for multiplayer match demonstrations.
 */
inline std::vector<MatchEvent> getSampleMatchEvents() {
    return {
        MatchEvent(101, "PLAYER_JOINED",   "PILOT-ALPHA", 1731336000100ULL,    0, 250.0f, 520.0f),
        MatchEvent(102, "PLAYER_JOINED",   "PILOT-BRAVO", 1731336000250ULL,    0, 550.0f, 520.0f),
        MatchEvent(103, "READY",           "PILOT-ALPHA", 1731336001000ULL,    0, 250.0f, 520.0f),
        MatchEvent(104, "READY",           "PILOT-BRAVO", 1731336001200ULL,    0, 550.0f, 520.0f),
        MatchEvent(105, "MATCH_STARTED",   "SYSTEM",      1731336004000ULL,    0,   0.0f,   0.0f),
        MatchEvent(106, "SCORE_UPDATED",   "PILOT-ALPHA", 1731336008500ULL, 1200, 270.0f, 480.0f),
        MatchEvent(107, "SCORE_UPDATED",   "PILOT-BRAVO", 1731336009100ULL,  850, 530.0f, 490.0f),
        MatchEvent(108, "COLLISION",       "PILOT-ALPHA", 1731336014200ULL, 2400, 310.0f, 410.0f),
        MatchEvent(109, "SCORE_UPDATED",   "PILOT-BRAVO", 1731336018700ULL, 2900, 500.0f, 380.0f),
        MatchEvent(110, "MATCH_COMPLETED", "PILOT-BRAVO", 1731336030000ULL, 4500, 480.0f, 350.0f)
    };
}

} // namespace Airstrike::Multiplayer

#endif // AIRSTRIKE_MATCH_EVENT_H
