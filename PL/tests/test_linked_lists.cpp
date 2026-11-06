#include "test_linked_lists.h"
#include "../src/linked_lists/singly_linked_list.h"
#include "../src/linked_lists/doubly_linked_list.h"
#include "../src/linked_lists/circular_linked_list.h"

TestSummary runLinkedListTests() {
    TestSummary summary;
    std::cout << "\n======================================================\n";
    std::cout << "[TEST SUITE] PL-CO2: Linked Lists (10 Tests)\n";
    std::cout << "======================================================\n";

    // 1. SLL Insert Operations
    SinglyLinkedList sll;
    sll.insertAtHead(1, 0.5, "Launch Thrusters Engaged");
    sll.insertAtTail(3, 2.5, "Approaching Asteroid Belt");
    sll.insertAt(1, 2, 1.5, "Enemy Squadron Radar Contact");
    summary.record(sll.size() == 3 && sll.getHead()->eventId == 1 && sll.search(2) != nullptr,
                   "SinglyLinkedList: Head, Tail, and Positional Insertion");

    // 2. SLL Delete by ID & Search
    bool delMid = sll.deleteById(2);
    summary.record(delMid && sll.size() == 2 && sll.search(2) == nullptr,
                   "SinglyLinkedList: Middle Node Deletion and Search Miss");

    // 3. SLL Deep Copy Isolation
    SinglyLinkedList sllCopy = sll;
    sllCopy.deleteHead();
    summary.record(sll.size() == 2 && sllCopy.size() == 1,
                   "SinglyLinkedList: Deep Copy Independent Node Lifetime");

    // 4. DLL Bidirectional Navigation
    DoublyLinkedList dll;
    dll.insertTail(10, "DRONE", 50);
    dll.insertTail(20, "FIGHTER", 120);
    dll.insertTail(30, "BOSS", 500);
    summary.record(dll.getHead()->enemyId == 10 && dll.getTail()->enemyId == 30 &&
                   dll.getTail()->prev->enemyId == 20 && dll.getHead()->next->enemyId == 20,
                   "DoublyLinkedList: Bidirectional Pointer Linkage");

    // 5. DLL Head & Tail Deletion
    dll.deleteHead();
    summary.record(dll.size() == 2 && dll.getHead()->enemyId == 20 && dll.getHead()->prev == nullptr,
                   "DoublyLinkedList: Head Deletion updates Head and Prev pointer");

    // 6. DLL Delete by Entity ID
    bool dllDel = dll.deleteById(30);
    summary.record(dllDel && dll.size() == 1 && dll.getTail()->enemyId == 20,
                   "DoublyLinkedList: Tail Deletion by Entity ID");

    // 7. CLL Infinite Weapon Cycling
    CircularLinkedList cll;
    cll.insertTail(1, "Plasma Cannon", 5, 25);
    cll.insertTail(2, "Laser Beam", 10, 50);
    cll.insertTail(3, "Photon Torpedo", 25, 120);

    const WeaponNode* w1 = cll.getActiveWeapon(); // 1
    const WeaponNode* w2 = cll.cycleNext();       // 2
    const WeaponNode* w3 = cll.cycleNext();       // 3
    const WeaponNode* w4 = cll.cycleNext();       // Should wrap to 1!
    summary.record(w1->weaponId == 1 && w2->weaponId == 2 && w3->weaponId == 3 && w4->weaponId == 1,
                   "CircularLinkedList: Seamless Endless Cyclic Traversal");

    // 8. CLL Circular Loop Termination
    const WeaponNode* node = cll.getHead();
    int countVisited = 0;
    do {
        countVisited++;
        node = node->next;
    } while (node != cll.getHead());
    summary.record(countVisited == 3, "CircularLinkedList: Proper Circular Termination Detection");

    // 9. CLL Delete Node and Maintain Loop
    bool cllDel = cll.deleteById(2); // Remove Laser Beam
    summary.record(cllDel && cll.size() == 2 && cll.getHead()->next->next == cll.getHead(),
                   "CircularLinkedList: Middle Deletion preserves continuous ring");

    // 10. CLL Clear and Empty State
    cll.clear();
    summary.record(cll.isEmpty() && cll.size() == 0 && cll.getActiveWeapon() == nullptr,
                   "CircularLinkedList: Clear memory safely resets ring pointers");

    return summary;
}

