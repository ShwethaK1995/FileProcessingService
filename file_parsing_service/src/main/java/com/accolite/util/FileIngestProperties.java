package com.accolite.util;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "file.ingest")
public record FileIngestProperties(
        String inputDir,
        String processingDir,
        String processedDir,
        String failedDir,
        String pattern,
        long pollMs,
        int maxFilesPerRun,
        Completion completion
) {
    public record Completion(
            Mode mode,
            long stableSeconds,
            String doneSuffix,
            List<String> tempExts
    ) {
        public enum Mode { STABLE, DONE_MARKER, TEMP_EXT }
    }
}

