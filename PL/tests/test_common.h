// ==============================================================================
// File      : PL/tests/test_common.h
// Subject   : PL-CO1..CO5 | Unit Test Framework
// Topics    : [TOPIC: PL Test Suite] [TOPIC: TestSummary]
// Game Role : Aggregates assertion pass/fail counters across all AIRSTRIKE PL unit test suites.
// ==============================================================================

#ifndef TEST_COMMON_H
#define TEST_COMMON_H

#include <iostream>
#include <string>

// [TOPIC: TestSummary]
// Purpose: Records per-suite assertion outcomes and merges counts into the overall PL report.
// Game Role: Standardizes console PASS/FAIL reporting across PL-CO1 through PL-CO5 tests.
struct TestSummary {
    int totalTests;
    int passedTests;
    int failedTests;

    TestSummary() : totalTests(0), passedTests(0), failedTests(0) {}

    void record(bool success, const std::string& testName, const std::string& detail = "") {
        totalTests++;
        if (success) {
            passedTests++;
            std::cout << "  [PASS] " << testName << "\n";
        } else {
            failedTests++;
            std::cout << "  [FAIL] " << testName;
            if (!detail.empty()) {
                std::cout << " (" << detail << ")";
            }
            std::cout << "\n";
        }
    }

    void merge(const TestSummary& other) {
        totalTests += other.totalTests;
        passedTests += other.passedTests;
        failedTests += other.failedTests;
    }
};

#endif // TEST_COMMON_H

