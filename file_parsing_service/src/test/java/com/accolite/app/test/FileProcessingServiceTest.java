package com.accolite.app.test;

import com.accolite.entity.DeadLetterMessage;
import com.accolite.entity.ParsedRecord;
import com.accolite.kafka.producer.KafkaProducerService;
import com.accolite.service.FileProcessingService;
import com.accolite.util.ConcurrencyProperties;
import com.accolite.util.FileIngestProperties;
import com.accolite.util.FileParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileProcessingServiceTest {

    @TempDir Path tmp;
    KafkaProducerService kafka = mock(KafkaProducerService.class);
    FileParser parser = mock(FileParser.class);
    ExecutorService chunkExecutor = mock(ExecutorService.class);
    ConcurrencyProperties concurrencyProps = mock(ConcurrencyProperties.class);

    @Test
    void process_shouldSendRecordsAndMoveToProcessed_andSendDLTOnBadLine() throws Exception {
        Path processingDir = tmp.resolve("processing");
        Path processedDir = tmp.resolve("processed");
        Path failedDir = tmp.resolve("failed");
        Files.createDirectories(processingDir);

        Path file = processingDir.resolve("input.txt");

        // line1 valid (31 chars)
        String good = "123ABL89" + "US" + "RELIANCEIN" + "1234" + "123560" + "1";
        // line2 invalid
        String bad = "short";

        Files.write(file, List.of(good, bad));

        FileIngestProperties props = new FileIngestProperties(
                tmp.resolve("input").toString(),
                processingDir.toString(),
                processedDir.toString(),
                failedDir.toString(),
                "*.txt",
                1000L,
                10,
                null,
                31
        );

        FileProcessingService svc = new FileProcessingService(props,chunkExecutor,concurrencyProps,parser,kafka);

        svc.process(file);

        // record sent once
        verify(kafka, times(1)).sendRecord(anyString(), any(ParsedRecord.class),anyString(),anyLong());
        // dlt sent once (bad line)
        verify(kafka, times(1)).sendDLT(anyString(), any(DeadLetterMessage.class));

        // moved to processed
        assertTrue(Files.exists(processedDir.resolve("input.txt")));
        assertFalse(Files.exists(file));
        assertFalse(Files.exists(failedDir.resolve("input.txt")));
    }

    @Test
    void process_whenReadFails_shouldMoveToFailed() throws Exception {
        Path processingDir = tmp.resolve("processing");
        Path processedDir = tmp.resolve("processed");
        Path failedDir = tmp.resolve("failed");
        Files.createDirectories(processingDir);

        Path file = processingDir.resolve("input.txt");
        Files.writeString(file, "data");

        // Make it unreadable (may not work on all OS). Safer: pass a directory instead of file.
        Path dirInstead = processingDir.resolve("dirAsFile");
        Files.createDirectories(dirInstead);

        FileIngestProperties props = new FileIngestProperties(
                tmp.resolve("input").toString(),
                processingDir.toString(),
                processedDir.toString(),
                failedDir.toString(),
                "*.txt",
                1000L,
                10,
                null,
                31
        );

        KafkaProducerService kafka = mock(KafkaProducerService.class);
        FileProcessingService svc = new FileProcessingService(props,chunkExecutor,concurrencyProps,parser,kafka);

        svc.process(dirInstead);

        assertTrue(Files.exists(failedDir.resolve("dirAsFile")));
        verifyNoInteractions(kafka);
    }
}

