// ==============================================================================
// File      : PL/src/arrays/bullet_pool.cpp
// Subject   : PL-CO1 | Arrays
// Topics    : [TOPIC: Object Pool] [TOPIC: BulletPool] [TOPIC: Contiguous Memory]
// Game Role : Implements zero-allocation projectile spawning, movement, and culling for AIRSTRIKE.
// ==============================================================================

#include "bullet_pool.h"
#include <iomanip>

BulletPool::BulletPool() : nextId(1) {
    clear();
}

// [TOPIC: BulletPool] [TOPIC: Slot Reuse]
// Purpose: Scans fixed contiguous array for an inactive slot and initializes a new projectile.
// Game Role: Eliminates heap fragmentation during rapid-fire laser volleys.
int BulletPool::spawn(double x, double y, double vx, double vy, int damage) {
    for (int i = 0; i < MAX_BULLETS; ++i) {
        if (!pool[i].active) {
            // WHY: Reuse existing pre-allocated struct in place to avoid heap new/delete overhead -> Game: Smooth frame pacing.
            pool[i].id = nextId++;
            pool[i].x = x;
            pool[i].y = y;
            pool[i].vx = vx;
            pool[i].vy = vy;
            pool[i].damage = damage;
            pool[i].active = true;
            return i;
        }
    }
    return -1; // WHY: Explicit overflow guard when all MAX_BULLETS slots are live.
}

bool BulletPool::deactivate(int index) {
    if (index >= 0 && index < MAX_BULLETS && pool[index].active) {
        pool[index].active = false;
        return true;
    }
    return false;
}

bool BulletPool::deactivateById(int id) {
    for (int i = 0; i < MAX_BULLETS; ++i) {
        if (pool[i].active && pool[i].id == id) {
            pool[i].active = false;
            return true;
        }
    }
    return false;
}

// [TOPIC: BulletPool] [TOPIC: Contiguous Traversal]
// Purpose: Updates active bullet coordinates linearly in memory and deactivates out-of-bounds slots.
// Game Role: Moves active shots and automatically reclaims escaped projectiles.
void BulletPool::update(double dt, double boundWidth, double boundHeight) {
    for (int i = 0; i < MAX_BULLETS; ++i) {
        if (pool[i].active) {
            pool[i].x += pool[i].vx * dt;
            pool[i].y += pool[i].vy * dt;

            // WHY: Reclaim slots immediately upon exiting arena bounds -> Game: Keeps fixed pool available for fresh shots.
            if (pool[i].x < 0.0 || pool[i].x > boundWidth ||
                pool[i].y < 0.0 || pool[i].y > boundHeight) {
                pool[i].active = false;
            }
        }
    }
}

int BulletPool::getActiveCount() const {
    int count = 0;
    for (int i = 0; i < MAX_BULLETS; ++i) {
        if (pool[i].active) {
            count++;
        }
    }
    return count;
}

const Bullet* BulletPool::getBullet(int index) const {
    if (index >= 0 && index < MAX_BULLETS) {
        return &pool[index];
    }
    return nullptr;
}

Bullet* BulletPool::getBulletMut(int index) {
    if (index >= 0 && index < MAX_BULLETS) {
        return &pool[index];
    }
    return nullptr;
}

void BulletPool::clear() {
    for (int i = 0; i < MAX_BULLETS; ++i) {
        pool[i].id = 0;
        pool[i].x = 0.0;
        pool[i].y = 0.0;
        pool[i].vx = 0.0;
        pool[i].vy = 0.0;
        pool[i].damage = 0;
        pool[i].active = false;
    }
}

void BulletPool::printStatus() const {
    std::cout << "\n--- Bullet Pool Status (" << getActiveCount() << "/" << MAX_BULLETS << " active) ---\n";
    std::cout << std::left << std::setw(6) << "Slot"
              << std::setw(6) << "ID"
              << std::setw(12) << "Position"
              << std::setw(14) << "Velocity"
              << std::setw(8) << "Dmg"
              << std::setw(8) << "Status" << "\n";
    std::cout << "--------------------------------------------------------\n";
    for (int i = 0; i < MAX_BULLETS; ++i) {
        if (pool[i].active) {
            std::cout << "[" << std::setw(4) << i << "] "
                      << std::setw(6) << pool[i].id
                      << "(" << std::fixed << std::setprecision(1) << pool[i].x << "," << pool[i].y << ")   "
                      << "(" << pool[i].vx << "," << pool[i].vy << ")   "
                      << std::setw(8) << pool[i].damage
                      << "ACTIVE\n";
        }
    }
}

