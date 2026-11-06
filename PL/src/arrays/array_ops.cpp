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

    bool insertAt(int arr[], int& size, int capacity, int index, int value) {
        if (size >= capacity) {
            std::cout << "  [Error] Array capacity overflow (Max: " << capacity << ")\n";
            return false;
        }
        if (index < 0 || index > size) {
            std::cout << "  [Error] Invalid insertion index: " << index << " (Valid: 0 to " << size << ")\n";
            return false;
        }

        // Shift elements to the right to make room
        for (int i = size; i > index; --i) {
            arr[i] = arr[i - 1];
        }
        arr[index] = value;
        size++;
        return true;
    }

    bool deleteAt(int arr[], int& size, int index) {
        if (size <= 0) {
            std::cout << "  [Error] Array underflow: cannot delete from empty array\n";
            return false;
        }
        if (index < 0 || index >= size) {
            std::cout << "  [Error] Invalid deletion index: " << index << " (Valid: 0 to " << size - 1 << ")\n";
            return false;
        }

        // Shift elements to the left to overwrite
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

    bool updateGrid(int grid[GRID_ROWS][GRID_COLS], int row, int col, int value) {
        if (row < 0 || row >= GRID_ROWS || col < 0 || col >= GRID_COLS) {
            std::cout << "  [Error] Invalid grid coordinates: (" << row << ", " << col << ")\n";
            return false;
        }
        grid[row][col] = value;
        return true;
    }

    bool searchGrid(const int grid[GRID_ROWS][GRID_COLS], int target, int& outRow, int& outCol) {
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

