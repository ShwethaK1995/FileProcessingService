package com.accolite.service;

import com.accolite.entity.DeadLetterMessage;
import com.accolite.util.ConcurrencyProperties;
import com.accolite.util.FileIngestProperties;
import com.accolite.entity.ParsedRecord;
import com.accolite.kafka.producer.KafkaProducerService;
import com.accolite.util.FileParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class FileProcessingService {

    private final FileIngestProperties props;
    private final ExecutorService chunkExecutor;
    private final FileParser fileParser;
    private final KafkaProducerService kafka;
    private final ConcurrencyProperties concurrencyProps;

    // choose based on your file format
    private static final int RECORD_LENGTH = 31;          // matches FileParser substring usage
    private static final long CHUNK_RECORDS = 10_000;

    ObjectMapper mapper = new ObjectMapper();

    public FileProcessingService(FileIngestProperties props,
                                 @Qualifier("chunkExecutor") ExecutorService chunkExecutor,
                                 ConcurrencyProperties concurrencyProps,
                                 FileParser fileParser,
                                 KafkaProducerService kafka) {
        this.props = props;
        this.chunkExecutor = chunkExecutor;
        this.concurrencyProps= concurrencyProps;
        this.fileParser = fileParser;
        this.kafka = kafka;
    }

    public void process(Path claimedFile) {
        log.info("FileProcessingService::process:enter file={}", claimedFile.getFileName());

        final int workers = Math.max(concurrencyProps.workers(), 4);
        final int queueCap = Math.max(concurrencyProps.queueCapacity(), 1000);

        record LineTask(long lineNo, String line) {}

        BlockingQueue<LineTask> queue = new ArrayBlockingQueue<>(queueCap);
        AtomicBoolean readerFailed = new AtomicBoolean(false);

        ExecutorService workerPool = Executors.newFixedThreadPool(workers);

        Runnable worker = () -> {
            while (true) {
                LineTask task;
                try {
                    task = queue.take();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }

                if (task.lineNo() == -1) return; // stop signal

                String key = claimedFile.getFileName().toString() + ":" + task.lineNo();
                try {
                    ParsedRecord record = FileParser.parseLine(task.line());
                    log.info("the object sent to kafka is :{}", record);

                    // Send the OBJECT (JsonSerializer will write JSON bytes)
                    kafka.sendRecord(key, record, String.valueOf(claimedFile.getFileName()), task.lineNo());
                    log.info("the message sent to kafka successfully!!");
                } catch (Exception ex) {
                    DeadLetterMessage dlt = new DeadLetterMessage();
                    dlt.setRawRecord(task.line());
                    dlt.setErrorMessage(ex.getMessage());
                    dlt.setFileName(claimedFile.getFileName().toString());
                    dlt.setRecordIndex(task.lineNo());
                    dlt.setTimestamp(System.currentTimeMillis());
                    kafka.sendDLT(key, dlt);
                }
            }
        };

        for (int i = 0; i < workers; i++) {
            workerPool.submit(worker);
        }

        long lineNo = 0;
        try (BufferedReader br = Files.newBufferedReader(claimedFile)) {
            String line;
            while ((line = br.readLine()) != null) {
                queue.put(new LineTask(lineNo++, line)); // blocks when queue full => backpressure
            }
        } catch (Exception e) {
            readerFailed.set(true);
            log.error("Reader failed file={}", claimedFile, e);
        } finally {
            // stop workers
            for (int i = 0; i < workers; i++) {
                try {
                    queue.put(new LineTask(-1, ""));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
            workerPool.shutdown();
            try {
                workerPool.awaitTermination(10, TimeUnit.MINUTES);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        if (readerFailed.get()) {
            move(claimedFile, props.failedDir());
        } else {
            move(claimedFile, props.processedDir());
        }

        log.info("FileProcessingService::process:exit file={}", claimedFile.getFileName());
    }



    private void move(Path file, String dir) {
        try {
            Files.createDirectories(Paths.get(dir));
            Files.move(file, Paths.get(dir).resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            log.error("Failed to move file={} to dir={}", file, dir, ex);
        }
    }

    @PostConstruct
    void logConcurrency() {
        log.info("Concurrency config: workers={}, queueCapacity={}",
                concurrencyProps.workers(), concurrencyProps.queueCapacity());
    }
}
