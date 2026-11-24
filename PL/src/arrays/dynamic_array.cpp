// ==============================================================================
// File      : PL/src/arrays/dynamic_array.cpp
// Subject   : PL-CO1 | Arrays
// Topics    : [TOPIC: Dynamic Array] [TOPIC: Rule of Five] [TOPIC: Geometric Resizing] [TOPIC: new[] delete[]]
// Game Role : Implements heap-allocated DynamicArray with 2x capacity doubling for AIRSTRIKE.
// ==============================================================================

#include "dynamic_array.h"
#include <stdexcept>
#include <utility>

DynamicArray::DynamicArray() : DynamicArray(4) {}

DynamicArray::DynamicArray(size_t initialCapacity)
    : capacity(initialCapacity < 2 ? 2 : initialCapacity), length(0) {
    // WHY: Clamp initial capacity >= 2 so geometric doubling always increases capacity.
    data = new int[capacity];
}

DynamicArray::~DynamicArray() {
    delete[] data;
    data = nullptr;
    length = 0;
    capacity = 0;
}

// [TOPIC: Rule of Five] [TOPIC: Deep Copy Constructor]
// Purpose: Allocates an independent heap buffer and copies existing elements.
// Game Role: Duplicates combat telemetry arrays without aliasing heap memory.
DynamicArray::DynamicArray(const DynamicArray& other)
    : capacity(other.capacity), length(other.length) {
    data = new int[capacity];
    for (size_t i = 0; i < length; ++i) {
        data[i] = other.data[i];
    }
}

// [TOPIC: Rule of Five] [TOPIC: Copy Assignment]
// Purpose: Deep-copies source buffer before freeing old memory for exception and self-assignment safety.
// Game Role: Safely overwrites an existing telemetry array with another snapshot.
DynamicArray& DynamicArray::operator=(const DynamicArray& other) {
    if (this != &other) {
        // WHY: Allocate and copy before delete[] to stay exception-safe if new[] throws.
        int* newData = new int[other.capacity];
        for (size_t i = 0; i < other.length; ++i) {
            newData[i] = other.data[i];
        }
        delete[] data;
        data = newData;
        capacity = other.capacity;
        length = other.length;
    }
    return *this;
}

// [TOPIC: Rule of Five] [TOPIC: Move Constructor]
// Purpose: Transfers ownership of the heap pointer in O(1) and nulls the moved-from source.
// Game Role: Eliminates redundant copies when returning dynamic buffers by value.
DynamicArray::DynamicArray(DynamicArray&& other) noexcept
    : data(other.data), capacity(other.capacity), length(other.length) {
    // WHY: Null source pointer so source destructor does not double-free stolen memory.
    other.data = nullptr;
    other.capacity = 0;
    other.length = 0;
}

// [TOPIC: Rule of Five] [TOPIC: Move Assignment]
// Purpose: Releases current heap buffer and steals source pointer in O(1) noexcept.
// Game Role: Fast ownership transfer of telemetry arrays across game state transitions.
DynamicArray& DynamicArray::operator=(DynamicArray&& other) noexcept {
    if (this != &other) {
        delete[] data;
        data = other.data;
        capacity = other.capacity;
        length = other.length;

        // WHY: Reset moved-from state to empty to preserve valid destruction invariants.
        other.data = nullptr;
        other.capacity = 0;
        other.length = 0;
    }
    return *this;
}

// [TOPIC: Dynamic Array] [TOPIC: Heap Reallocation]
// Purpose: Allocates a larger contiguous array, migrates elements, and frees the old buffer.
// Game Role: Grows combat telemetry capacity dynamically when existing slots fill up.
void DynamicArray::resize(size_t newCapacity) {
    int* newData = new int[newCapacity];
    for (size_t i = 0; i < length; ++i) {
        newData[i] = data[i];
    }
    delete[] data;
    data = newData;
    capacity = newCapacity;
}

// [TOPIC: Amortized O(1) Insertion] [TOPIC: Geometric Resizing]
// Purpose: Appends value at tail, doubling capacity when full to achieve amortized O(1) cost.
// Game Role: Records incoming combat events without per-element heap reallocation.
void DynamicArray::pushBack(int value) {
    if (length >= capacity) {
        // WHY: Doubling capacity yields amortized O(1) pushBack despite occasional O(N) resize.
        resize(capacity * 2);
    }
    data[length++] = value;
}

bool DynamicArray::insert(size_t index, int value) {
    if (index > length) {
        return false;
    }
    if (length >= capacity) {
        // WHY: Double capacity before shifting so newly inserted element fits contiguously.
        resize(capacity * 2);
    }
    for (size_t i = length; i > index; --i) {
        data[i] = data[i - 1];
    }
    data[index] = value;
    length++;
    return true;
}

bool DynamicArray::removeAt(size_t index) {
    if (index >= length) {
        return false;
    }
    for (size_t i = index; i < length - 1; ++i) {
        data[i] = data[i + 1];
    }
    length--;
    return true;
}

bool DynamicArray::deleteValue(int value) {
    int idx = find(value);
    if (idx != -1) {
        return removeAt(static_cast<size_t>(idx));
    }
    return false;
}

int DynamicArray::get(size_t index) const {
    if (index >= length) {
        throw std::out_of_range("DynamicArray index out of bounds");
    }
    return data[index];
}

bool DynamicArray::set(size_t index, int value) {
    if (index >= length) {
        return false;
    }
    data[index] = value;
    return true;
}

int DynamicArray::find(int value) const {
    for (size_t i = 0; i < length; ++i) {
        if (data[i] == value) {
            return static_cast<int>(i);
        }
    }
    return -1;
}

void DynamicArray::clear() {
    length = 0;
}

void DynamicArray::print(const char* label) const {
    std::cout << "[" << label << "] (Size: " << length << ", Cap: " << capacity << ") : [ ";
    for (size_t i = 0; i < length; ++i) {
        std::cout << data[i] << (i + 1 < length ? ", " : " ");
    }
    std::cout << "]\n";
}

int& DynamicArray::operator[](size_t index) {
    return data[index];
}

const int& DynamicArray::operator[](size_t index) const {
    return data[index];
}

