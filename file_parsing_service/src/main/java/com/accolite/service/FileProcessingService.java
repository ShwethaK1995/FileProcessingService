package com.accolite.service;

import com.accolite.entity.ParsedRecord;
import com.accolite.kafka.producer.KafkaProducerService;
import com.accolite.util.FileParser;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileProcessingService {

    private final KafkaProducerService kafkaProducerService;

    public FileProcessingService(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    public int processFile(File file) throws Exception {
        List<ParsedRecord> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new java.io.FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    ParsedRecord record = FileParser.parseLine(line);
                    System.out.println("The json message for kafka is:" +record);
                    kafkaProducerService.sendRecord(record);
                    records.add(record);
                } catch (Exception e) {
                    System.err.println("Failed to parse line: " + line + " Error: " + e.getMessage());
                    throw new IllegalArgumentException("Invalid line encountered, aborting file: " + line, e);
                }
            }
        }

        return records.size();

    }
}
