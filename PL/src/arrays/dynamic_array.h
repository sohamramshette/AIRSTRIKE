// ==============================================================================
// File      : PL/src/arrays/dynamic_array.h
// Subject   : PL-CO1 | Arrays
// Topics    : [TOPIC: Dynamic Array] [TOPIC: Heap Allocation] [TOPIC: Geometric Resizing] [TOPIC: Rule of Five]
// Game Role : Provides a custom resizable array container (without std::vector) for AIRSTRIKE telemetry.
// ==============================================================================

#ifndef DYNAMIC_ARRAY_H
#define DYNAMIC_ARRAY_H

#include <cstddef>
#include <iostream>

// [TOPIC: Dynamic Array] [TOPIC: Rule of Five] [TOPIC: Amortized Geometric Expansion]
// Purpose: Contiguous heap-managed integer array supporting 2x resizing, copy/move semantics, and O(1) indexing.
// Game Role: Stores variable-length AirStrike combat score and wave records without STL vector overhead.
class DynamicArray {
private:
    int* data;
    size_t capacity;
    size_t length;

    void resize(size_t newCapacity);

public:
    DynamicArray();
    explicit DynamicArray(size_t initialCapacity);
    ~DynamicArray();

    // [TOPIC: Rule of Five] [TOPIC: Copy and Move Semantics]
    // Purpose: Ensures deep heap copying on copy and O(1) pointer transfer on move.
    // Game Role: Safely clones or transfers dynamic telemetry buffers across subsystems.
    DynamicArray(const DynamicArray& other);
    DynamicArray& operator=(const DynamicArray& other);
    DynamicArray(DynamicArray&& other) noexcept;
    DynamicArray& operator=(DynamicArray&& other) noexcept;

    void pushBack(int value);
    bool insert(size_t index, int value);
    bool removeAt(size_t index);
    bool deleteValue(int value);
    int  get(size_t index) const;
    bool set(size_t index, int value);
    int  find(int value) const;
    void clear();

    size_t size() const { return length; }
    size_t getCapacity() const { return capacity; }
    bool isEmpty() const { return length == 0; }

    void print(const char* label = "DynamicArray") const;

    // Subscript operator
    int& operator[](size_t index);
    const int& operator[](size_t index) const;
};

#endif // DYNAMIC_ARRAY_H

