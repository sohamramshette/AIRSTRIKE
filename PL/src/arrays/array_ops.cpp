// ==============================================================================
// File      : PL/src/arrays/array_ops.cpp
// Subject   : PL-CO1 | Arrays
// Topics    : [TOPIC: 1D Array] [TOPIC: 2D Array] [TOPIC: Element Shifting]
// Game Role : Implements bounds-checked static 1D telemetry and 2D radar grid helpers.
// ==============================================================================

#include "array_ops.h"
#include <iomanip>

namespace ArrayOps {

    void printArray(const int arr[], int size, const char* label) {
        std::cout << "[" << label << "] (Size: " << size << ") : [ ";
        for (int i = 0; i < size; ++i) {
            std::cout << arr[i] << (i + 1 < size ? ", " : " ");
        }
        std::cout << "]\n";
    }

    // [TOPIC: 1D Array] [TOPIC: Insertion Shifting]
    // Purpose: Inserts a value at a validated index by shifting trailing elements right in O(N).
    // Game Role: Adds a prioritized telemetry entry into a fixed-size combat array.
    bool insertAt(int arr[], int& size, int capacity, int index, int value) {
        if (size >= capacity) {
            std::cout << "  [Error] Array capacity overflow (Max: " << capacity << ")\n";
            return false;
        }
        if (index < 0 || index > size) {
            std::cout << "  [Error] Invalid insertion index: " << index << " (Valid: 0 to " << size << ")\n";
            return false;
        }

        // WHY: Shift backward from tail to index so unread elements are not overwritten -> Game: Preserves existing telemetry order.
        for (int i = size; i > index; --i) {
            arr[i] = arr[i - 1];
        }
        arr[index] = value;
        size++;
        return true;
    }

    // [TOPIC: 1D Array] [TOPIC: Deletion Shifting]
    // Purpose: Removes an element at index and shifts trailing elements left to stay contiguous.
    // Game Role: Compacts static telemetry records after an entry expires.
    bool deleteAt(int arr[], int& size, int index) {
        if (size <= 0) {
            std::cout << "  [Error] Array underflow: cannot delete from empty array\n";
            return false;
        }
        if (index < 0 || index >= size) {
            std::cout << "  [Error] Invalid deletion index: " << index << " (Valid: 0 to " << size - 1 << ")\n";
            return false;
        }

        // WHY: Shift forward from index+1 to close the gap in contiguous memory -> Game: Keeps telemetry array compact.
        for (int i = index; i < size - 1; ++i) {
            arr[i] = arr[i + 1];
        }
        size--;
        return true;
    }

    bool deleteValue(int arr[], int& size, int value) {
        int index = searchLinear(arr, size, value);
        if (index == -1) {
            std::cout << "  [Notice] Value " << value << " not found in array\n";
            return false;
        }
        return deleteAt(arr, size, index);
    }

    // [TOPIC: Linear Search] [TOPIC: 1D Array]
    // Purpose: Scans an unsorted array sequentially to find the first matching target index.
    // Game Role: Locates a telemetry ID in small static arrays.
    int searchLinear(const int arr[], int size, int target) {
        for (int i = 0; i < size; ++i) {
            if (arr[i] == target) {
                return i;
            }
        }
        return -1;
    }

    bool updateAt(int arr[], int size, int index, int newValue) {
        if (index < 0 || index >= size) {
            std::cout << "  [Error] Invalid update index: " << index << "\n";
            return false;
        }
        arr[index] = newValue;
        return true;
    }

    void printGrid(const int grid[GRID_ROWS][GRID_COLS], const char* label) {
        std::cout << "--- [" << label << "] (" << GRID_ROWS << "x" << GRID_COLS << " Sectors) ---\n";
        for (int r = 0; r < GRID_ROWS; ++r) {
            std::cout << "  Sector Row " << r << ": [ ";
            for (int c = 0; c < GRID_COLS; ++c) {
                std::cout << std::setw(3) << grid[r][c] << (c + 1 < GRID_COLS ? ", " : " ");
            }
            std::cout << "]\n";
        }
    }

    // [TOPIC: 2D Array] [TOPIC: Bounds Checking]
    // Purpose: Writes a value into a bounds-checked (row, col) cell in the 4x4 matrix.
    // Game Role: Updates radar sector threat counts without out-of-bounds memory corruption.
    bool updateGrid(int grid[GRID_ROWS][GRID_COLS], int row, int col, int value) {
        if (row < 0 || row >= GRID_ROWS || col < 0 || col >= GRID_COLS) {
            std::cout << "  [Error] Invalid grid coordinates: (" << row << ", " << col << ")\n";
            return false;
        }
        grid[row][col] = value;
        return true;
    }

    // [TOPIC: 2D Array] [TOPIC: Row-Major Traversal]
    // Purpose: Searches the 2D radar matrix in row-major order and outputs matching coordinates.
    // Game Role: Finds which radar sector currently holds a tracked entity ID.
    bool searchGrid(const int grid[GRID_ROWS][GRID_COLS], int target, int& outRow, int& outCol) {
        // WHY: Traverse row-by-row to match C++ row-major memory layout for spatial cache locality.
        for (int r = 0; r < GRID_ROWS; ++r) {
            for (int c = 0; c < GRID_COLS; ++c) {
                if (grid[r][c] == target) {
                    outRow = r;
                    outCol = c;
                    return true;
                }
            }
        }
        outRow = -1;
        outCol = -1;
        return false;
    }
}

