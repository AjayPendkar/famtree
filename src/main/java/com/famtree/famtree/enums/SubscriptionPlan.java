package com.famtree.famtree.enums;

public enum SubscriptionPlan {
    FREE(2),
    BASIC(10),
    PREMIUM(-1); // -1 represents unlimited

    private final int maxRequests;

    SubscriptionPlan(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public boolean hasUnlimitedRequests() {
        return maxRequests == -1;
    }
} 