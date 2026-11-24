// ==============================================================================
// File      : PSOOP/src/game/GameState.java
// Subject   : PSOOP-CO2 | Discrete Game State Enumeration
// Topics    : [TOPIC: Game State Machine] [TOPIC: Enumerations]
// Game Role : Defines the discrete lifecycle states managed by GameEngine.
// ==============================================================================
package game;

/**
 * Enumeration of discrete high-level game states.
 */
public enum GameState {
    MENU,
    PLAYING,
    PAUSED,
    GAME_OVER,
    INSTRUCTIONS,
    SETTINGS
}

