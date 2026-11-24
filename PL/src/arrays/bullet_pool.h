// ==============================================================================
// File      : PL/src/arrays/bullet_pool.h
// Subject   : PL-CO1 | Arrays
// Topics    : [TOPIC: Object Pool] [TOPIC: Static Array] [TOPIC: Contiguous Memory] [TOPIC: Projectile Pooling]
// Game Role : Pre-allocates fixed projectile slots to prevent runtime heap allocation during combat.
// ==============================================================================

#ifndef BULLET_POOL_H
#define BULLET_POOL_H

#include <iostream>

// [TOPIC: Bullet Entity] [TOPIC: Contiguous Struct Layout]
// Purpose: Stores projectile kinematics, damage, and active-slot flag in a cache-friendly struct.
// Game Role: Represents a single player or enemy laser shot inside BulletPool.
struct Bullet {
    int id;
    double x;
    double y;
    double vx;
    double vy;
    int damage;
    bool active;

    Bullet() : id(0), x(0.0), y(0.0), vx(0.0), vy(0.0), damage(0), active(false) {}
};

// [TOPIC: BulletPool] [TOPIC: Static Array] [TOPIC: Object Pool]
// Purpose: Manages a fixed MAX_BULLETS array of reusable Bullet slots with overflow protection.
// Game Role: Recycles laser projectiles across frames without new/delete overhead.
class BulletPool {
public:
    static constexpr int MAX_BULLETS = 32;

private:
    Bullet pool[MAX_BULLETS];
    int nextId;

public:
    BulletPool();

    // [TOPIC: Pool Allocation]
    // Purpose: Claims the first inactive slot and assigns projectile state, or returns -1 if full.
    // Game Role: Fires a new weapon projectile during active gameplay.
    int spawn(double x, double y, double vx, double vy, int damage);

    bool deactivate(int index);
    bool deactivateById(int id);

    // [TOPIC: Pool Simulation & Culling]
    // Purpose: Integrates active projectile positions by dt and frees slots that exit arena bounds.
    // Game Role: Advances laser fire each frame and reclaims off-screen bullet slots.
    void update(double dt, double boundWidth = 800.0, double boundHeight = 600.0);

    // Returns count of currently active bullets
    int getActiveCount() const;

    // Capacity of the static pool
    int getCapacity() const { return MAX_BULLETS; }

    // Read access to bullet at slot index
    const Bullet* getBullet(int index) const;
    Bullet* getBulletMut(int index);

    // Reset pool
    void clear();

    // Console diagnostic display
    void printStatus() const;
};

#endif // BULLET_POOL_H

