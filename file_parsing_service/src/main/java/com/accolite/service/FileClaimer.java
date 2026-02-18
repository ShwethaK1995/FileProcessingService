package com.accolite.service;

import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Component
public class FileClaimer {

    public Optional<Path> claim(Path input, Path processingDir) {
        try {
            Files.createDirectories(processingDir);
            Path target = processingDir.resolve(input.getFileName() + ".processing." + UUID.randomUUID());
            return Optional.of(Files.move(input, target, StandardCopyOption.ATOMIC_MOVE));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
