#ifndef DYNAMIC_ARRAY_H
#define DYNAMIC_ARRAY_H

#include <cstddef>
#include <iostream>

/**
 * Custom dynamic array implementation without using std::vector.
 * Demonstrates manual heap management (new[] / delete[]), amortized geometric expansion,
 * and the C++ Rule of Five.
 */
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

    // Rule of Five: Copy & Move semantics
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
