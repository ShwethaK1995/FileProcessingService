package com.accolite.app.test;

import com.accolite.service.FileClaimer;
import com.accolite.util.FileIngestProperties;
import io.swagger.v3.oas.annotations.extensions.Extensions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.configuration.IMockitoConfiguration;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileClaimerTest {

    @TempDir Path tmp;

    FileIngestProperties props = new FileIngestProperties(
            "in", "processing", "processed", "failed",
            "*.txt", 1000L, 10,
            null,
            31
    );

    @Test
    void claim_shouldMoveFileIntoProcessingDirAndReturnNewPath() throws Exception {
        Path inputDir = tmp.resolve("input");
        Path processingDir = tmp.resolve("processing");
        Files.createDirectories(inputDir);

        Path f = inputDir.resolve("x.txt");

        // write exactly 31 bytes (recordLength)
        String record31 = "123ABL89USRELIANCEIN12341235601"; // 31 chars
        assertEquals(31, record31.length());
        Files.writeString(f, record31, java.nio.charset.StandardCharsets.US_ASCII);

        // If your FileClaimer requires props/recordLength, construct it accordingly:
        FileIngestProperties props = new FileIngestProperties(
                inputDir.toString(),
                processingDir.toString(),
                tmp.resolve("processed").toString(),
                tmp.resolve("failed").toString(),
                "*.txt",
                1000L,
                10,
                new FileIngestProperties.Completion(
                        FileIngestProperties.Completion.Mode.STABLE,
                        0,
                        ".done",
                        java.util.List.of(".part", ".tmp")
                ),
                31
        );

        FileClaimer claimer = new FileClaimer(props);

        Optional<Path> claimed = claimer.claim(f, processingDir);

        assertTrue(claimed.isPresent());
        assertFalse(Files.exists(f), "original should be moved");
        assertTrue(Files.exists(claimed.get()), "claimed file should exist");
        assertTrue(claimed.get().getFileName().toString().startsWith("x.txt.processing."));
    }


    @Test
    void claim_missingFile_shouldReturnEmpty() {
        FileClaimer claimer = new FileClaimer(props);
        Optional<Path> claimed = claimer.claim(tmp.resolve("does-not-exist.txt"), tmp.resolve("processing"));
        assertTrue(claimed.isEmpty());
    }
}

