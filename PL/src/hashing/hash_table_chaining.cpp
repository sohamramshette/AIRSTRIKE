// ==============================================================================
// File      : PL/src/hashing/hash_table_chaining.cpp
// Subject   : PL-CO5 | Hashing Techniques
// Topics    : [TOPIC: HashTableChaining] [TOPIC: Separate Chaining] [TOPIC: DJB2 Hash] [TOPIC: Rehashing]
// Game Role : Implements DJB2 hashing, collision chaining, and dynamic rehashing for inventory.
// ==============================================================================

#include "hash_table_chaining.h"
#include <iomanip>

HashTableChaining::HashTableChaining(size_t initialBuckets)
    : bucketCount(initialBuckets > 0 ? initialBuckets : INITIAL_BUCKETS),
      itemCount(0), collisionCount(0) {
    buckets = new ChainNode*[bucketCount];
    for (size_t i = 0; i < bucketCount; ++i) {
        buckets[i] = nullptr;
    }
}

HashTableChaining::~HashTableChaining() {
    clear();
    delete[] buckets;
}

HashTableChaining::HashTableChaining(const HashTableChaining& other)
    : bucketCount(other.bucketCount), itemCount(0), collisionCount(0) {
    buckets = new ChainNode*[bucketCount];
    for (size_t i = 0; i < bucketCount; ++i) {
        buckets[i] = nullptr;
    }
    for (size_t i = 0; i < other.bucketCount; ++i) {
        ChainNode* curr = other.buckets[i];
        while (curr != nullptr) {
            insert(curr->key, curr->item);
            curr = curr->next;
        }
    }
}

HashTableChaining& HashTableChaining::operator=(const HashTableChaining& other) {
    if (this != &other) {
        clear();
        delete[] buckets;
        bucketCount = other.bucketCount;
        itemCount = 0;
        collisionCount = 0;
        buckets = new ChainNode*[bucketCount];
        for (size_t i = 0; i < bucketCount; ++i) {
            buckets[i] = nullptr;
        }
        for (size_t i = 0; i < other.bucketCount; ++i) {
            ChainNode* curr = other.buckets[i];
            while (curr != nullptr) {
                insert(curr->key, curr->item);
                curr = curr->next;
            }
        }
    }
    return *this;
}

// [TOPIC: DJB2 Hash]
// Purpose: Maps string keys to bucket indices via hash = hash * 33 + c modulo bucketCount.
// Game Role: Distributes arsenal item IDs across buckets to minimize chain collisions.
size_t HashTableChaining::hashFunction(const std::string& key) const {
    unsigned long hash = 5381;
    for (char c : key) {
        // WHY: (hash << 5) + hash equals hash * 33 via fast shift-add for uniform DJB2 mixing.
        hash = ((hash << 5) + hash) + static_cast<unsigned char>(c);
    }
    return hash % bucketCount;
}

// [TOPIC: Rehashing] [TOPIC: Separate Chaining]
// Purpose: Expands the bucket array and migrates existing ChainNodes without reallocating nodes.
// Game Role: Restores O(1) arsenal lookup when bucket chains grow beyond MAX_LOAD_FACTOR.
void HashTableChaining::rehash(size_t newBucketCount) {
    size_t oldBucketCount = bucketCount;
    ChainNode** oldBuckets = buckets;

    bucketCount = newBucketCount;
    buckets = new ChainNode*[bucketCount];
    for (size_t i = 0; i < bucketCount; ++i) {
        buckets[i] = nullptr;
    }

    itemCount = 0;
    collisionCount = 0;

    for (size_t i = 0; i < oldBucketCount; ++i) {
        ChainNode* curr = oldBuckets[i];
        while (curr != nullptr) {
            ChainNode* nextNode = curr->next;
            // WHY: Relink existing heap nodes directly into new buckets to avoid new/delete churn.
            size_t newIdx = hashFunction(curr->key);
            if (buckets[newIdx] != nullptr) {
                collisionCount++;
            }
            curr->next = buckets[newIdx];
            buckets[newIdx] = curr;
            itemCount++;

            curr = nextNode;
        }
    }

    delete[] oldBuckets;
}

// [TOPIC: HashTableChaining] [TOPIC: Load Factor Threshold]
// Purpose: Updates existing keys or prepends a ChainNode at bucket head, triggering rehash if alpha > 0.75.
// Game Role: Adds or upgrades AirStrike arsenal entries while keeping chain lengths bounded.
bool HashTableChaining::insert(const std::string& key, const InventoryItem& item) {
    size_t idx = hashFunction(key);

    // Check if key already exists (update value)
    ChainNode* curr = buckets[idx];
    while (curr != nullptr) {
        if (curr->key == key) {
            curr->item = item;
            return true;
        }
        curr = curr->next;
    }

    // New node insertion at chain head
    if (buckets[idx] != nullptr) {
        collisionCount++; // A collision occurred in this bucket
    }
    buckets[idx] = new ChainNode(key, item, buckets[idx]);
    itemCount++;

    // WHY: Rehash to odd capacity (2*M + 1) above alpha 0.75 to preserve O(1) expected bucket length.
    if (getLoadFactor() > MAX_LOAD_FACTOR) {
        rehash(bucketCount * 2 + 1);
    }
    return true;
}

bool HashTableChaining::search(const std::string& key, InventoryItem& outItem) const {
    size_t idx = hashFunction(key);
    ChainNode* curr = buckets[idx];
    while (curr != nullptr) {
        if (curr->key == key) {
            outItem = curr->item;
            return true;
        }
        curr = curr->next;
    }
    return false;
}

bool HashTableChaining::remove(const std::string& key) {
    size_t idx = hashFunction(key);
    ChainNode* curr = buckets[idx];
    ChainNode* prev = nullptr;

    while (curr != nullptr) {
        if (curr->key == key) {
            // WHY: Handle bucket head (prev == nullptr) separately from interior chain unlinking.
            if (prev == nullptr) {
                buckets[idx] = curr->next;
            } else {
                prev->next = curr->next;
            }
            delete curr;
            itemCount--;
            return true;
        }
        prev = curr;
        curr = curr->next;
    }
    return false;
}

void HashTableChaining::clear() {
    for (size_t i = 0; i < bucketCount; ++i) {
        ChainNode* curr = buckets[i];
        while (curr != nullptr) {
            ChainNode* temp = curr;
            curr = curr->next;
            delete temp;
        }
        buckets[i] = nullptr;
    }
    itemCount = 0;
    collisionCount = 0;
}

void HashTableChaining::printTable() const {
    std::cout << "\n=== Hash Table: Separate Chaining (Items: " << itemCount
              << ", Buckets: " << bucketCount
              << ", Load Factor: " << std::fixed << std::setprecision(2) << getLoadFactor()
              << ", Total Collisions: " << collisionCount << ") ===\n";

    for (size_t i = 0; i < bucketCount; ++i) {
        std::cout << "Bucket [" << std::setw(2) << i << "]: ";
        if (!buckets[i]) {
            std::cout << "[Empty]\n";
        } else {
            ChainNode* curr = buckets[i];
            while (curr != nullptr) {
                std::cout << "-> (\"" << curr->key << "\" | " << curr->item.name
                          << " [Atk +" << curr->item.attackBonus << "]) ";
                curr = curr->next;
            }
            std::cout << "\n";
        }
    }
}

