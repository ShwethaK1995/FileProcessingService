package com.accolite.app.test;

import com.accolite.service.FileChunkProcessor;
import com.accolite.service.FileClaimer;
import com.accolite.service.FileParsingScheduler;
import com.accolite.service.FileProcessingService;
import com.accolite.util.FileIngestProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.*;

class FileParsingSchedulerTest {

    @TempDir Path tmp;

    @Test
    void poll_shouldClaimAndSubmitForProcessing() throws Exception {
        Path inputDir = tmp.resolve("input");
        Path processingDir = tmp.resolve("processing");
        Files.createDirectories(inputDir);
        Files.createDirectories(processingDir);

        Path file = inputDir.resolve("a.txt");
        Files.write(file, List.of("123ABL89USRELIANCEIN12341235601"));

        FileIngestProperties props = new FileIngestProperties(
                inputDir.toString(),
                processingDir.toString(),
                tmp.resolve("processed").toString(),
                tmp.resolve("failed").toString(),
                "*.txt",
                1000L,
                10,
                new FileIngestProperties.Completion(FileIngestProperties.Completion.Mode.STABLE, 0, ".done", List.of(".part")),
                31
        );

        ExecutorService exec = Executors.newSingleThreadExecutor();

        FileClaimer claimer = mock(FileClaimer.class);
        FileChunkProcessor.FileCompletenessChecker checker = mock(FileChunkProcessor.FileCompletenessChecker.class);
        FileProcessingService svc = mock(FileProcessingService.class);

        Path claimed = processingDir.resolve("a.txt.processing.X");
        when(checker.isComplete(any(), any())).thenReturn(true);
        when(claimer.claim(any(), any())).thenReturn(Optional.of(claimed));

        FileParsingScheduler scheduler = new FileParsingScheduler(props, exec, claimer, checker, svc);

        scheduler.poll();

        // give async a moment
        exec.shutdown();
        Thread.sleep(100);

        verify(claimer, times(1)).claim(any(), any());
        verify(svc, atLeastOnce()).process(claimed);
    }
}

