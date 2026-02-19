package com.accolite.service;

import com.accolite.entity.DeadLetterMessage;
import com.accolite.entity.ParsedRecord;
import com.accolite.kafka.producer.KafkaProducerService;
import com.accolite.util.FileIngestProperties;
import com.accolite.util.FileParser;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.Callable;

public class FileChunkProcessor implements Callable<Integer> {

    private final File file;
    private final long startRecord;
    private final long endRecord;
    private final int recordLength;
    private final FileParser fileParser;
    private final KafkaProducerService kafkaProducerService;

    public FileChunkProcessor(File file,
                              long startRecord,
                              long endRecord,
                              int recordLength,
                              FileParser fileParser,
                              KafkaProducerService kafkaProducerService) {
        this.file = file;
        this.startRecord = startRecord;
        this.endRecord = endRecord;
        this.recordLength = recordLength;
        this.fileParser = fileParser;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Override
    public Integer call() {

        int processedCount = 0;
        int failedCount = 0;

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {

            long startOffset = startRecord * recordLength;
            raf.seek(startOffset);

            byte[] buffer = new byte[recordLength];

            for (long i = startRecord; i < endRecord; i++) {

                int bytesRead = raf.read(buffer);

                if (bytesRead != recordLength) {
                    throw new IllegalStateException("Unexpected record length at record: " + i);
                }

                String record = new String(buffer, StandardCharsets.UTF_8);
                long lineNumber = i + 1;
                String key = file.getName() + ":" + lineNumber;

                try {

                    ParsedRecord parsedRecord = fileParser.parseLine(record);

                    kafkaProducerService.sendRecord(key, parsedRecord);

                    processedCount++;

                } catch (Exception recordException) {
                        failedCount++;

                        DeadLetterMessage dlt = new DeadLetterMessage();
                        dlt.setRawRecord(record);
                        dlt.setErrorMessage(recordException.getMessage());
                        dlt.setFileName(file.getName());
                        dlt.setRecordIndex(lineNumber);
                        dlt.setTimestamp(System.currentTimeMillis());

                        kafkaProducerService.sendDLT(key, dlt);
                    }
                }
            } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        System.out.println("Chunk " + startRecord + " - " + endRecord +
                    " processed: " + processedCount +
                    " failed: " + failedCount);

            return processedCount;


    }

    @Component
    public static class FileCompletenessChecker {

        public boolean isComplete(Path file, FileIngestProperties props) {
            FileIngestProperties.Completion completion = props.completion();
            if (completion == null || completion.mode() == null) {
                return isStable(file, 60);
            }

            return switch (completion.mode()) {
                case STABLE -> isStable(file, completion.stableSeconds());
                case DONE_MARKER -> hasDoneMarker(file, completion.doneSuffix());
                case TEMP_EXT -> isNotTemp(file, completion.tempExts()) && isStable(file, completion.stableSeconds());
            };
        }

        private boolean isStable(Path file, long stableSeconds) {
            try {
                BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
                long ageMs = System.currentTimeMillis() - attrs.lastModifiedTime().toMillis();
                return ageMs >= stableSeconds * 1000L && attrs.size() > 0;
            } catch (IOException e) {
                return false;
            }
        }

        private boolean hasDoneMarker(Path file, String doneSuffix) {
            String suffix = (doneSuffix == null || doneSuffix.isBlank()) ? ".done" : doneSuffix;
            Path marker = file.resolveSibling(file.getFileName().toString() + suffix);
            return Files.exists(marker);
        }

        private boolean isNotTemp(Path file, java.util.List<String> tempExts) {
            String name = file.getFileName().toString();
            if (tempExts == null || tempExts.isEmpty()) {
                // default temp extensions
                return !(name.endsWith(".tmp") || name.endsWith(".part"));
            }
            for (String ext : tempExts) {
                if (ext != null && !ext.isBlank() && name.endsWith(ext.trim())) return false;
            }
            return true;
        }
    }
}
