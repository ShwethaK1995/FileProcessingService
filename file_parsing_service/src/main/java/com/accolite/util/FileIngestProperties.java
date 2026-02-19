package com.accolite.util;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "file")
public record FileIngestProperties(
        @NotBlank String inputDir,
        @NotBlank String processingDir,
        @NotBlank String processedDir,
        @NotBlank String failedDir,

        String pattern,
        @Min(1) long pollMs,
        @Min(1) int maxFilesPerRun,
        Completion completion,
        @Min(1) int recordLength
) {

    public record Completion(
            Mode mode,
            @Min(0) long stableSeconds,
            String doneSuffix,
            List<String> tempExts
    ) {
        public enum Mode { STABLE, DONE_MARKER, TEMP_EXT }
    }
}
