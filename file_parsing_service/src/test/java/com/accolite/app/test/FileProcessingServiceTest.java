package com.accolite.app.test;

import com.accolite.entity.ParsedRecord;
import com.accolite.kafka.producer.KafkaProducerService;
import com.accolite.service.FileProcessingService;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FileProcessingServiceTest {

    private KafkaProducerService kafkaProducerService =
            mock(KafkaProducerService.class);

    private FileProcessingService service =
            new FileProcessingService(kafkaProducerService);

    @Test
    void shouldProcessFileAndSendToKafka() throws Exception {

        File tempFile = createTempFileWithContent("12345678USDESC123456ISIN0001231");

        int count = service.processFile(tempFile);

        verify(kafkaProducerService, times(1))
                .sendRecord(new ParsedRecord("12345678", "US", "DESC123456", "ISIN", 1.23, "Y"));

        assertEquals(1, count);
    }

    @Test
    void shouldReturnZeroWhenFileIsEmpty() throws Exception {
        File tempFile = File.createTempFile("empty", ".txt");

        int count = service.processFile(tempFile);

        assertEquals(0, count);
    }

    @Test
    void shouldHandleInvalidLineGracefully() throws Exception {
        File tempFile = createTempFileWithContent("short");

        assertThrows(IllegalArgumentException.class, () -> service.processFile(tempFile));
        verify(kafkaProducerService, never()).sendRecord(any());
    }

    private File createTempFileWithContent(String content) throws IOException {
        File tempFile = File.createTempFile("file-processing", ".txt");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
        tempFile.deleteOnExit();
        return tempFile;
    }

}

