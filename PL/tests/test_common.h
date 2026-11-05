#ifndef TEST_COMMON_H
#define TEST_COMMON_H

#include <iostream>
#include <string>

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
