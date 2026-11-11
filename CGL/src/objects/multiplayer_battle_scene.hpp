#ifndef MULTIPLAYER_BATTLE_SCENE_HPP
#define MULTIPLAYER_BATTLE_SCENE_HPP

#include "common/gl_common.h"
#include <vector>
#include <string>

namespace CGL {

enum class MPViewMode {
    ARENA_OVERVIEW, // Single wide-angle orthographic battlefield view
    SPLIT_SCREEN    // Dual viewports: Player 1 (Left) and Player 2 (Right)
};

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

/**
 * ==============================================================================
 * Master Scene for CGL Multiplayer Demonstrations (CGL-CO1 to CGL-CO6)
 *
 * Demonstrates:
 * - CGL-CO1: Two distinct player craft geometries (Cyan Interceptor & Purple Cruiser)
 * - CGL-CO2: Independent matrix transformations (Translation, Rotation, Scaling)
 * - CGL-CO3: Bresenham laser paths and Midpoint Circle shield bubbles
 * - CGL-CO4: Arena Wide-Angle & Dual Split-Screen Viewport modes
 * - CGL-CO5: Cohen-Sutherland laser beam clipping against combat boundary
 * - CGL-CO6: Gouraud smooth shading, directional lighting, and hull texturing
 * ==============================================================================
 */
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
