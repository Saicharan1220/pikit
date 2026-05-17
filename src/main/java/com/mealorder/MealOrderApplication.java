package com.mealorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main entry point for the Meal Pre-Order & Collect System.
 *
 * DECISION REASONING:
 * - @SpringBootApplication: Auto-configures Spring components (enabled component scanning, auto-configuration)
 * - @EnableCaching: Activates @Cacheable annotation support for performance optimization
 *
 * MAANG INTERVIEW TALKING POINTS:
 * - Shows understanding of Spring auto-configuration
 * - Demonstrates awareness of caching patterns for performance
 */
@SpringBootApplication
@EnableCaching
public class MealOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MealOrderApplication.class, args);
    }
}
