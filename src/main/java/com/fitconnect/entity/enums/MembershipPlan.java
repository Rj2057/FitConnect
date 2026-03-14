package com.fitconnect.entity.enums;

public enum MembershipPlan {
    BASIC(1.0, "Basic access to the gym floor"),
    PRO(1.25, "Gym access with premium classes"),
    ELITE(1.5, "All access including priority support");

    private final double multiplier;
    private final String description;

    MembershipPlan(double multiplier, String description) {
        this.multiplier = multiplier;
        this.description = description;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public String getDescription() {
        return description;
    }
}