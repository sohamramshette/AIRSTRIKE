#include "dynamic_array.h"
#include <stdexcept>
#include <utility>

DynamicArray::DynamicArray() : DynamicArray(4) {}

DynamicArray::DynamicArray(size_t initialCapacity)
    : capacity(initialCapacity < 2 ? 2 : initialCapacity), length(0) {
    data = new int[capacity];
}

DynamicArray::~DynamicArray() {
    delete[] data;
    data = nullptr;
    length = 0;
    capacity = 0;
}

// Copy Constructor
DynamicArray::DynamicArray(const DynamicArray& other)
    : capacity(other.capacity), length(other.length) {
    data = new int[capacity];
    for (size_t i = 0; i < length; ++i) {
        data[i] = other.data[i];
    }
}

// Copy Assignment
DynamicArray& DynamicArray::operator=(const DynamicArray& other) {
    if (this != &other) {
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

// Move Constructor
DynamicArray::DynamicArray(DynamicArray&& other) noexcept
    : data(other.data), capacity(other.capacity), length(other.length) {
    other.data = nullptr;
    other.capacity = 0;
    other.length = 0;
}

// Move Assignment
DynamicArray& DynamicArray::operator=(DynamicArray&& other) noexcept {
    if (this != &other) {
        delete[] data;
        data = other.data;
        capacity = other.capacity;
        length = other.length;

        other.data = nullptr;
        other.capacity = 0;
        other.length = 0;
    }
    return *this;
}

void DynamicArray::resize(size_t newCapacity) {
    int* newData = new int[newCapacity];
    for (size_t i = 0; i < length; ++i) {
        newData[i] = data[i];
    }
    delete[] data;
    data = newData;
    capacity = newCapacity;
}

void DynamicArray::pushBack(int value) {
    if (length >= capacity) {
        resize(capacity * 2);
    }
    data[length++] = value;
}

bool DynamicArray::insert(size_t index, int value) {
    if (index > length) {
        return false;
    }
    if (length >= capacity) {
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

