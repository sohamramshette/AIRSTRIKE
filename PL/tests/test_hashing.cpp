#include "test_hashing.h"
#include "../src/hashing/hash_table_chaining.h"
#include "../src/hashing/hash_table_probing.h"

TestSummary runHashingTests() {
    TestSummary summary;
    std::cout << "\n======================================================\n";
    std::cout << "[TEST SUITE] PL-CO5: Hashing Techniques (8 Tests)\n";
    std::cout << "======================================================\n";

    // 1. Separate Chaining: Insert and Search
    HashTableChaining chainTable(5);
    chainTable.insert("WEAPON_PLASMA", InventoryItem("W01", "Plasma Blaster", "COMMON", 25, 100));
    chainTable.insert("SHIELD_NANO",   InventoryItem("S01", "Nano Plating",   "RARE",   10, 250));

    InventoryItem foundItem;
    bool find1 = chainTable.search("WEAPON_PLASMA", foundItem);
    summary.record(find1 && foundItem.attackBonus == 25,
                   "HashTableChaining: Insert and Retrieve key-value pair");

    // 2. Separate Chaining: Collision Resolution (Multiple keys in same bucket)
    // Inserting multiple keys to guarantee collisions
    chainTable.insert("ITEM_ALPHA", InventoryItem("I01", "Alpha Core", "COMMON", 5, 50));
    chainTable.insert("ITEM_BETA",  InventoryItem("I02", "Beta Core",  "RARE",   15, 150));
    chainTable.insert("ITEM_GAMMA", InventoryItem("I03", "Gamma Core", "EPIC",   30, 300));
    bool find2 = chainTable.search("ITEM_GAMMA", foundItem);
    summary.record(find2 && chainTable.getCollisions() > 0,
                   "HashTableChaining: Collisions successfully resolved via linked chain");

    // 3. Separate Chaining: Deletion
    bool remOk = chainTable.remove("SHIELD_NANO");
    bool searchAfterDel = chainTable.search("SHIELD_NANO", foundItem);
    summary.record(remOk && !searchAfterDel,
                   "HashTableChaining: Key deletion updates bucket chain properly");

    // 4. Separate Chaining: Dynamic Rehashing (> 0.75 load factor)
    size_t initialBuckets = chainTable.getBucketCount();
    for (int i = 0; i < 10; ++i) {
        chainTable.insert("KEY_" + std::to_string(i), InventoryItem("K" + std::to_string(i), "Item", "COMMON", 1, 10));
    }
    summary.record(chainTable.getBucketCount() > initialBuckets && chainTable.getLoadFactor() <= 0.75,
                   "HashTableChaining: Automatic rehashing expands bucket capacity");

    // 5. Linear Probing: Insert and Search
    HashTableProbing probeTable(7);
    probeTable.insert("THRUSTER_ION",  InventoryItem("T01", "Ion Afterburner", "RARE", 20, 300));
    probeTable.insert("ARMOR_TITAN",   InventoryItem("A01", "Titan Hull",      "EPIC", 40, 600));
    bool probeFind = probeTable.search("THRUSTER_ION", foundItem);
    summary.record(probeFind && foundItem.attackBonus == 20,
                   "HashTableProbing: Insert and Retrieve key-value pair");

    // 6. Linear Probing: Open Addressing Collision Handling
    probeTable.insert("MISSILE_SWARM", InventoryItem("M01", "Swarm Missiles",  "LEGENDARY", 100, 1500));
    int probes = 0;
    probeTable.search("MISSILE_SWARM", foundItem, probes);
    summary.record(probes >= 1, "HashTableProbing: Linear probe sequence resolves collisions");

    // 7. Linear Probing: Tombstone Deletion and Search Continuation
    probeTable.remove("THRUSTER_ION");
    bool foundAfterTombstone = probeTable.search("MISSILE_SWARM", foundItem);
    summary.record(foundAfterTombstone,
                   "HashTableProbing: Tombstone marker permits continued probe search");

    // 8. Linear Probing: Dynamic Rehashing (> 0.70 load factor)
    size_t probeInitialCap = probeTable.getCapacity();
    for (int i = 0; i < 8; ++i) {
        probeTable.insert("PROBE_KEY_" + std::to_string(i), InventoryItem("P" + std::to_string(i), "Mod", "COMMON", 2, 20));
    }
    summary.record(probeTable.getCapacity() > probeInitialCap,
                   "HashTableProbing: Dynamic rehashing expands table array");

    return summary;
}

