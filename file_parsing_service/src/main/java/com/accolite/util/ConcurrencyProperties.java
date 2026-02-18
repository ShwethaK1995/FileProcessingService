package com.accolite.util;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "concurrency")
public record ConcurrencyProperties(
        @Min(1) int workers,
        @Min(1) int queueCapacity
) {}
