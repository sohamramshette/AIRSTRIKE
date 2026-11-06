#ifndef HASH_TABLE_CHAINING_H
#define HASH_TABLE_CHAINING_H

#include <iostream>
#include <string>

/**
 * Hash Table with Separate Chaining (PL-CO5).
 * Application: AirStrike Space Arsenal & Inventory Registry.
 *
 * Collision Resolution: Each bucket holds a singly linked list of collided key-value pairs.
 * Features:
 * - DJB2 polynomial hash function
 * - Load factor tracking (items / buckets)
 * - Automatic dynamic rehashing when load factor > 0.75
 * - Explicit collision counter for academic viva verification
 */
struct InventoryItem {
    std::string itemId;
    std::string name;
    std::string rarity;    // "COMMON", "RARE", "EPIC", "LEGENDARY"
    int attackBonus;
    int creditValue;

    InventoryItem() : itemId(""), name(""), rarity(""), attackBonus(0), creditValue(0) {}
    InventoryItem(const std::string& id, const std::string& n, const std::string& r, int atk, int val)
        : itemId(id), name(n), rarity(r), attackBonus(atk), creditValue(val) {}
};

struct ChainNode {
    std::string key;
    InventoryItem item;
    ChainNode* next;

    ChainNode(const std::string& k, const InventoryItem& val, ChainNode* n = nullptr)
        : key(k), item(val), next(n) {}
};

class HashTableChaining {
public:
    static constexpr size_t INITIAL_BUCKETS = 7;
    static constexpr double MAX_LOAD_FACTOR = 0.75;

private:
    ChainNode** buckets;
    size_t bucketCount;
    size_t itemCount;
    int collisionCount;

    size_t hashFunction(const std::string& key) const;
    void rehash(size_t newBucketCount);

public:
    explicit HashTableChaining(size_t initialBuckets = INITIAL_BUCKETS);
    ~HashTableChaining();

    // Prevent inadvertent shallow copy
    HashTableChaining(const HashTableChaining& other);
    HashTableChaining& operator=(const HashTableChaining& other);

    // Operations
    bool insert(const std::string& key, const InventoryItem& item);
    bool search(const std::string& key, InventoryItem& outItem) const;
    bool remove(const std::string& key);

    // Metrics
    double getLoadFactor() const { return static_cast<double>(itemCount) / bucketCount; }
    size_t size() const { return itemCount; }
    size_t getBucketCount() const { return bucketCount; }
    int getCollisions() const { return collisionCount; }

    void clear();
    void printTable() const;
};

#endif // HASH_TABLE_CHAINING_H

