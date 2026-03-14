package com.eip.fraud.domain;

public enum RiskLevel {
    LOW,      // 0-30
    MEDIUM,   // 31-60
    HIGH,     // 61-80
    CRITICAL; // 81-100

    public static RiskLevel fromScore(int score) {
        if (score <= 30) return LOW;
        if (score <= 60) return MEDIUM;
        if (score <= 80) return HIGH;
        return CRITICAL;
    }
}
