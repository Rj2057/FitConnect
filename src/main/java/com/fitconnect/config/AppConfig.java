package com.fitconnect.config;

/**
 * Singleton Pattern — AppConfig
 *
 * Provides a single, globally accessible configuration instance.
 * Uses double-checked locking for thread-safe lazy initialization.
 */ 
//singleton pattern for application-wide configuration
public class AppConfig {

    private static volatile AppConfig instance;

    // Intrinsic configuration values
    private final long refundWindowHours;
    private final int jwtExpiryDays;
    private final String appName;

    private AppConfig() {
        this.refundWindowHours = 1;
        this.jwtExpiryDays = 7;
        this.appName = "FitConnect";
    }

    /**
     * Returns the single instance of AppConfig.
     * Thread-safe via double-checked locking.
     */
    public static AppConfig getInstance() {
        if (instance == null) {
            synchronized (AppConfig.class) {
                if (instance == null) {
                    instance = new AppConfig();
                }
            }
        }
        return instance;
    }

    public long getRefundWindowHours() {
        return refundWindowHours;
    }

    public int getJwtExpiryDays() {
        return jwtExpiryDays;
    }

    public String getAppName() {
        return appName;
    }
}
