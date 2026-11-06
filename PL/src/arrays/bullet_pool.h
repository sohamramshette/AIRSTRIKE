#ifndef BULLET_POOL_H
#define BULLET_POOL_H

#include <iostream>

/**
 * AirStrike Fixed-Size Bullet Pool (PL-CO1).
 * High-performance object pooling pattern using static array allocation.
 * Avoids dynamic allocation overhead during combat frames.
 */
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

class BulletPool {
public:
    static constexpr int MAX_BULLETS = 32;

private:
    Bullet pool[MAX_BULLETS];
    int nextId;

public:
    BulletPool();

    // Spawns a bullet from pre-allocated pool; returns slot index or -1 if pool exhausted
    int spawn(double x, double y, double vx, double vy, int damage);

    // Deactivates bullet by slot index
    bool deactivate(int index);

    // Deactivates bullet by unique bullet ID
    bool deactivateById(int id);

    // Advances active bullet positions and culls out-of-bounds bullets
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

