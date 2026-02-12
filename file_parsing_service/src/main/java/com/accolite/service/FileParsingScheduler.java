package com.accolite.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class FileParsingScheduler {
    @Value("${file.input.dir}")
public String INPUT_DIR = "";

    private final FileProcessingService fileProcessingService;

    public FileParsingScheduler(FileProcessingService fileProcessingService) {
        this.fileProcessingService = fileProcessingService;
    }

    /**
     * Runs every day at 6 PM
     */
    @Scheduled(fixedDelay = 10000)
    public void run() {
        log.info("SPRING SCHEDULER IS RUNNING");
        if (INPUT_DIR == null || INPUT_DIR.isBlank()) {
            throw new IllegalStateException("file.input.dir must not be empty");
        }
        File dir = new File(INPUT_DIR);

        if (!dir.exists() || !dir.isDirectory()) {
            log.error("Input directory does not exist: {}", INPUT_DIR);
            return;
        }

        File[] files = dir.listFiles(file ->
                file.isFile() && file.getName().endsWith(".txt"));

        if (files == null || files.length == 0) {
            log.info("No files to process");
            return;
        }

        for (File file : files) {
            try {
                log.info("Processing file: {}", file.getName());

                int recordCount = fileProcessingService.processFile(file);

                log.info("Processed {} records from {}", recordCount, file.getName());

            } catch (Exception e) {
                log.error("Failed processing file: {}", file.getName(), e);
            }
        }
    }
}
