// ==============================================================================
// File      : PL/src/hashing/hash_table_chaining.h
// Subject   : PL-CO5 | Hashing Techniques
// Topics    : [TOPIC: HashTableChaining] [TOPIC: Separate Chaining] [TOPIC: DJB2 Hash] [TOPIC: Rehashing]
// Game Role : Indexes AirStrike space arsenal items by string ID with O(1) average lookup.
// ==============================================================================

#ifndef HASH_TABLE_CHAINING_H
#define HASH_TABLE_CHAINING_H

#include <iostream>
#include <string>

// [TOPIC: InventoryItem Record]
// Purpose: Stores an arsenal item's ID, name, rarity tier, attack bonus, and credit price.
// Game Role: Value payload mapped in AirStrike inventory and upgrade hash tables.
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

// [TOPIC: ChainNode] [TOPIC: Separate Chaining]
// Purpose: Singly linked bucket node storing a key-value pair and collision chain pointer.
// Game Role: Links colliding arsenal item keys inside a shared bucket list.
struct ChainNode {
    std::string key;
    InventoryItem item;
    ChainNode* next;

    ChainNode(const std::string& k, const InventoryItem& val, ChainNode* n = nullptr)
        : key(k), item(val), next(n) {}
};

// [TOPIC: HashTableChaining] [TOPIC: Separate Chaining] [TOPIC: Load Factor] [TOPIC: Rehashing]
// Purpose: Closed-addressing hash table using DJB2 hashing, bucket linked lists, and auto-rehashing at alpha > 0.75.
// Game Role: Drives the AirStrike Space Arsenal & Inventory Registry dictionary.
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

    // [TOPIC: Deep Copy Semantics]
    // Purpose: Re-inserts every bucket chain entry into an independent bucket array.
    // Game Role: Safely clones arsenal hash tables without aliasing bucket pointers.
    HashTableChaining(const HashTableChaining& other);
    HashTableChaining& operator=(const HashTableChaining& other);

    // [TOPIC: Hash Dictionary Operations]
    // Purpose: Inserts/updates with rehash check, searches bucket chain, or unlinks by key.
    // Game Role: Registers, queries, and retires player weapon/upgrade entries in O(1) avg time.
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

