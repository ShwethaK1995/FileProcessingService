package com.accolite.app.test;

import com.accolite.service.FileChunkProcessor;
import com.accolite.util.FileIngestProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileCompletenessCheckerTest {

    @TempDir Path tmp;

    @Test
    void stableMode_shouldReturnTrueWhenOldEnoughAndNonEmpty() throws Exception {
        Path f = tmp.resolve("a.txt");
        Files.writeString(f, "data");

        // Make it look "old"
        Files.setLastModifiedTime(f, FileTime.from(Instant.now().minusSeconds(120)));

        FileIngestProperties props = new FileIngestProperties(
                tmp.toString(), tmp.toString(), tmp.toString(), tmp.toString(),
                "*.txt",
                1000L,
                10,
                new FileIngestProperties.Completion(FileIngestProperties.Completion.Mode.STABLE, 60, ".done", List.of(".part", ".tmp")),
                31
        );

        FileChunkProcessor.FileCompletenessChecker checker = new FileChunkProcessor.FileCompletenessChecker();
        assertTrue(checker.isComplete(f, props));
    }

    @Test
    void doneMarkerMode_shouldReturnTrueOnlyWhenMarkerExists() throws Exception {
        Path f = tmp.resolve("b.txt");
        Files.writeString(f, "data");

        FileIngestProperties props = new FileIngestProperties(
                tmp.toString(), tmp.toString(), tmp.toString(), tmp.toString(),
                "*.txt",
                1000L,
                10,
                new FileIngestProperties.Completion(FileIngestProperties.Completion.Mode.DONE_MARKER, 60, ".done", List.of(".part", ".tmp")),
                31
        );

        FileChunkProcessor.FileCompletenessChecker checker = new FileChunkProcessor.FileCompletenessChecker();

        assertFalse(checker.isComplete(f, props));

        Files.createFile(tmp.resolve("b.txt.done"));
        assertTrue(checker.isComplete(f, props));
    }

    @Test
    void tempExtMode_shouldRejectTempExtAndAcceptNormalExtWhenStable() throws Exception {
        Path tempFile = tmp.resolve("c.txt.part");
        Files.writeString(tempFile, "data");
        Files.setLastModifiedTime(tempFile, FileTime.from(Instant.now().minusSeconds(120)));

        Path normal = tmp.resolve("c.txt");
        Files.writeString(normal, "data");
        Files.setLastModifiedTime(normal, FileTime.from(Instant.now().minusSeconds(120)));

        FileIngestProperties props = new FileIngestProperties(
                tmp.toString(), tmp.toString(), tmp.toString(), tmp.toString(),
                "*",
                1000L,
                10,
                new FileIngestProperties.Completion(FileIngestProperties.Completion.Mode.TEMP_EXT, 60, ".done", List.of(".part", ".tmp")),
                31
        );

        FileChunkProcessor.FileCompletenessChecker checker = new FileChunkProcessor.FileCompletenessChecker();
        assertFalse(checker.isComplete(tempFile, props));
        assertTrue(checker.isComplete(normal, props));
    }
}

