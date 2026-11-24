// ==============================================================================
// File      : CGL/src/objects/multiplayer_battle_scene.hpp
// Subject   : Scene | Dual-Player CO1-CO6 Combat Visualizer
// Topics    : [TOPIC: Scene Orchestration] [TOPIC: Split-Screen Viewports] [TOPIC: BMP Loader] [TOPIC: HUD Font Renderer]
// Game Role : Declares Screen 8's dual-player AIRSTRIKE battle arena with Ortho Overview and Split-Screen cameras.
// ==============================================================================

#ifndef MULTIPLAYER_BATTLE_SCENE_HPP
#define MULTIPLAYER_BATTLE_SCENE_HPP

#include "common/gl_common.h"
#include <vector>
#include <string>

namespace CGL {

// [TOPIC: Viewport Projection Mode]
// Purpose: Selects between single orthographic arena view and dual perspective split-screen viewports.
// Game Role: Toggled via [V] in MultiplayerBattleScene to demonstrate CO4 multi-viewport rendering.
enum class MPViewMode {
    ARENA_OVERVIEW, // Single wide-angle orthographic battlefield view
    SPLIT_SCREEN    // Dual viewports: Player 1 (Left) and Player 2 (Right)
};

// [TOPIC: Multiplayer Ship Telemetry]
// Purpose: Stores spatial transform, shield state, health, and score for each player craft.
// Game Role: Inspected by CO1-CO6 multiplayer tests and rendered in MultiplayerBattleScene.
struct PlayerShipState {
    float x;
    float y;
    float z;
    float rotation;
    float bank;
    float scale;
    float shieldAlpha;
    int health;
    int score;
    bool shieldActive;
};

// [TOPIC: Scene Orchestration / Split-Screen Viewports / BMP Loader / HUD Font Renderer]
// Purpose: Integrates P1 Interceptor and P2 Heavy Cruiser with Bresenham lasers, Midpoint shields, clipped beams, and BMP textures.
// Game Role: Renders Screen 8 Multiplayer Combat Visualizer and feeds CO1-CO6 multiplayer assertions.
class MultiplayerBattleScene {
public:
    MultiplayerBattleScene();
    ~MultiplayerBattleScene();

    void init();
    void render(int screenWidth, int screenHeight);
    void update(float dt);
    void handleKey(unsigned char key);

    // Viewport mode toggle
    void toggleViewMode();
    MPViewMode getViewMode() const { return viewMode; }
    void setViewMode(MPViewMode mode) { viewMode = mode; }

    // State inspection for automated tests
    const PlayerShipState& getPlayer1() const { return player1; }
    const PlayerShipState& getPlayer2() const { return player2; }

    bool isTextureLoaded() const { return textureLoaded; }
    size_t getTextureBufferSize() const { return textureBytes; }
    bool isLightingEnabled() const { return lightingEnabled; }

private:
    MPViewMode viewMode;
    PlayerShipState player1; // Agile Interceptor (P1)
    PlayerShipState player2; // Heavy Cruiser (P2)

    GLuint textureId;
    bool textureLoaded;
    size_t textureBytes;
    bool lightingEnabled;
    bool wireframe;
    float animTimer;

    // Helper rendering routines
    void renderArenaBoundary();
    void renderPlayer1Ship();
    void renderPlayer2Ship();
    void renderBresenhamBeams();
    void renderMidpointShields();
    void renderClippedCombatBeams();
    void renderHUD(int screenWidth, int screenHeight, int viewportIndex);

    void setupLighting();
    void setupProjection(int w, int h, int cameraPerspective);
};

} // namespace CGL

#endif // MULTIPLAYER_BATTLE_SCENE_HPP
