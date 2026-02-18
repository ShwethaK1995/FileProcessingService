package com.accolite.service;

import com.accolite.entity.DeadLetterMessage;
import com.accolite.util.FileIngestProperties;
import com.accolite.entity.ParsedRecord;
import com.accolite.kafka.producer.KafkaProducerService;
import com.accolite.util.FileParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
public class FileProcessingService {

    private final FileIngestProperties props;
    private final KafkaProducerService kafka;

    public FileProcessingService(FileIngestProperties props, KafkaProducerService kafka) {
        this.props = props;
        this.kafka = kafka;
    }

    public void process(Path claimedFile) {
        long lineNo = 0;

        try (BufferedReader br = Files.newBufferedReader(claimedFile)) {
            String line;
            while ((line = br.readLine()) != null) {
                String key = claimedFile.getFileName().toString() + ":" + lineNo;
                lineNo++;
                try {
                    ParsedRecord record = FileParser.parseLine(line);

                    kafka.sendRecord(key, record);
                } catch (Exception ex) {
                    kafka.sendDLT(key,new DeadLetterMessage());
                }
            }
            move(claimedFile, props.processedDir());
        } catch (Exception e) {
            log.error("File failed {}", claimedFile, e);
            move(claimedFile, props.failedDir());
        }
    }

    private void move(Path file, String dir) {
        try {
            Files.createDirectories(Paths.get(dir));
            Files.move(file, Paths.get(dir).resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ignored) {}
    }
}