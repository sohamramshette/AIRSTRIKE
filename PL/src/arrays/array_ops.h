// ==============================================================================
// File      : PL/src/arrays/array_ops.h
// Subject   : PL-CO1 | Arrays
// Topics    : [TOPIC: 1D Array] [TOPIC: 2D Array] [TOPIC: Bounds Checking]
// Game Role : Declares static 1D telemetry and 2D radar matrix operations for AIRSTRIKE.
// ==============================================================================

#ifndef ARRAY_OPS_H
#define ARRAY_OPS_H

#include <iostream>

// [TOPIC: Static Array Operations] [TOPIC: 1D Array] [TOPIC: 2D Array]
// Purpose: Provides bounds-checked insert, delete, search, and update on fixed 1D/2D arrays.
// Game Role: Maintains static radar sector matrices and fixed-capacity combat telemetry lists.
namespace ArrayOps {

    // [TOPIC: 1D Array] [TOPIC: Contiguous Memory]
    // Purpose: Prints, inserts, deletes, searches, and updates elements in a 1D array.
    // Game Role: Tracks fixed-size wave metrics and telemetry counters.
    void printArray(const int arr[], int size, const char* label = "Array");
    bool insertAt(int arr[], int& size, int capacity, int index, int value);
    bool deleteAt(int arr[], int& size, int index);
    bool deleteValue(int arr[], int& size, int value);
    int  searchLinear(const int arr[], int size, int target);
    bool updateAt(int arr[], int size, int index, int newValue);

    // [TOPIC: 2D Array] [TOPIC: Row-Major Matrix]
    // Purpose: Defines a 4x4 static radar matrix with coordinate update and search helpers.
    // Game Role: Models coarse 4x4 AirStrike space radar sectors.
    constexpr int GRID_ROWS = 4;
    constexpr int GRID_COLS = 4;

    void printGrid(const int grid[GRID_ROWS][GRID_COLS], const char* label = "Radar Grid");
    bool updateGrid(int grid[GRID_ROWS][GRID_COLS], int row, int col, int value);
    bool searchGrid(const int grid[GRID_ROWS][GRID_COLS], int target, int& outRow, int& outCol);
}

#endif // ARRAY_OPS_H

