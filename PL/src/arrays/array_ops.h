#ifndef ARRAY_OPS_H
#define ARRAY_OPS_H

#include <iostream>

/**
 * Basic 1D and 2D static array operations for AIRSTRIKE combat telemetry.
 */
namespace ArrayOps {

    // 1D Array Operations
    void printArray(const int arr[], int size, const char* label = "Array");
    bool insertAt(int arr[], int& size, int capacity, int index, int value);
    bool deleteAt(int arr[], int& size, int index);
    bool deleteValue(int arr[], int& size, int value);
    int  searchLinear(const int arr[], int size, int target);
    bool updateAt(int arr[], int size, int index, int newValue);

    // 2D Array (Matrix) Operations (e.g. 4x4 Space Radar Matrix)
    constexpr int GRID_ROWS = 4;
    constexpr int GRID_COLS = 4;

    void printGrid(const int grid[GRID_ROWS][GRID_COLS], const char* label = "Radar Grid");
    bool updateGrid(int grid[GRID_ROWS][GRID_COLS], int row, int col, int value);
    bool searchGrid(const int grid[GRID_ROWS][GRID_COLS], int target, int& outRow, int& outCol);
}

#endif // ARRAY_OPS_H
