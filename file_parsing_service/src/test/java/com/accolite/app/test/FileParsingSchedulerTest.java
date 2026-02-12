package com.accolite.app.test;


import com.accolite.service.FileParsingScheduler;
import com.accolite.service.FileProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FileParsingSchedulerTest {

    private FileProcessingService fileProcessingService;
    private FileParsingScheduler scheduler;

    @BeforeEach
    void setUp() {
        fileProcessingService = mock(FileProcessingService.class);
        scheduler = new FileParsingScheduler(fileProcessingService);
    }

    // Directory does not exist
    @Test
    void shouldLogErrorWhenDirectoryDoesNotExist() throws Exception {

        scheduler.INPUT_DIR = "./abc";

        scheduler.run();

        verify(fileProcessingService, never()).processFile(any());
    }

    @Test
    void shouldThrowWhenInputDirIsEmpty() {
        scheduler.INPUT_DIR = "";

        assertThrows(IllegalStateException.class, () -> scheduler.run());
    }

    //Directory exists but no files
    @Test
    void shouldDoNothingWhenNoTxtFilesPresent() throws Exception {

        File tempDir = createTempDirectory();

        scheduler.INPUT_DIR = tempDir.getAbsolutePath();

        scheduler.run();

        verify(fileProcessingService, never()).processFile(any());
    }

    // Valid txt file present
    @Test
    void shouldProcessTxtFiles() throws Exception {

        File tempDir = createTempDirectory();
        File txtFile = new File(tempDir, "test.txt");

        try (FileWriter writer = new FileWriter(txtFile)) {
            writer.write("sample content");
        }

        scheduler.INPUT_DIR = tempDir.getAbsolutePath();

        when(fileProcessingService.processFile(any(File.class)))
                .thenReturn(1);

        scheduler.run();

        verify(fileProcessingService, times(1))
                .processFile(any(File.class));
    }

    // Ignore non-txt files
    @Test
    void shouldIgnoreNonTxtFiles() throws Exception {

        File tempDir = createTempDirectory();
        File file = new File(tempDir, "test.csv");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("data");
        }

        scheduler.INPUT_DIR = tempDir.getAbsolutePath();

        scheduler.run();

        verify(fileProcessingService, never()).processFile(any());
    }

    // 5️⃣ Exception during file processing
    @Test
    void shouldContinueWhenProcessingFails() throws Exception {

        File tempDir = createTempDirectory();
        File txtFile = new File(tempDir, "test.txt");

        try (FileWriter writer = new FileWriter(txtFile)) {
            writer.write("data");
        }

        scheduler.INPUT_DIR = tempDir.getAbsolutePath();

        when(fileProcessingService.processFile(any(File.class)))
                .thenThrow(new RuntimeException("Processing failed"));

        scheduler.run();

        verify(fileProcessingService, times(1))
                .processFile(any(File.class));
    }

    // Helper method to create temp directory
    private File createTempDirectory() throws IOException {
        File tempDir = File.createTempFile("temp", "");
        tempDir.delete();
        tempDir.mkdir();
        tempDir.deleteOnExit();
        return tempDir;
    }
}

