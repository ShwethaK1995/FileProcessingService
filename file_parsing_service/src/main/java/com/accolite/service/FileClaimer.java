package com.accolite.service;

import com.accolite.util.FileIngestProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class FileClaimer {

    private final int recordLength;

    public FileClaimer(FileIngestProperties props) {
        this.recordLength = props.recordLength();
    }

    public Optional<Path> claim(Path input, Path processingDir) {
        try {
            String name = input.getFileName().toString();

            // skip temp/in-progress uploads
            if (name.endsWith(".part") || name.contains(".processing.")) {
                return Optional.empty();
            }

            // enforce extension if needed
            if (!name.endsWith(".txt")) {
                return Optional.empty();
            }

            long size = Files.size(input);
            if (size <= 0) return Optional.empty();

            // If you are fixed-width: ensure file aligns to record size
            if (size % recordLength != 0) {
                log.warn("Skipping file {}: size {} not multiple of recordLength {}", input, size, recordLength);
                return Optional.empty();
            }

            Files.createDirectories(processingDir);
            Path target = processingDir.resolve(name + ".processing." + UUID.randomUUID());

            try {
                return Optional.of(Files.move(input, target, StandardCopyOption.ATOMIC_MOVE));
            } catch (Exception atomicFail) {
                // fallback if ATOMIC_MOVE not supported
                log.warn("ATOMIC_MOVE failed for {}. Falling back to non-atomic move.", input, atomicFail);
                return Optional.of(Files.move(input, target, StandardCopyOption.REPLACE_EXISTING));
            }
        } catch (Exception e) {
            log.error("Failed to claim file {}", input, e);
            return Optional.empty();
        }
    }
}

