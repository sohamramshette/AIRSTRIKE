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

// DJB2 Hash Algorithm: hash = hash * 33 + c
size_t HashTableChaining::hashFunction(const std::string& key) const {
    unsigned long hash = 5381;
    for (char c : key) {
        hash = ((hash << 5) + hash) + static_cast<unsigned char>(c);
    }
    return hash % bucketCount;
}

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
            // Re-insert into new bucket array
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

    // Check load factor and trigger dynamic rehashing if threshold exceeded
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

