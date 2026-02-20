package com.accolite.app.test;

import com.accolite.controller.FileUploadController;
import com.accolite.util.FileIngestProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FileUploadController.class)
@ContextConfiguration(classes = {FileUploadController.class, FileUploadControllerTest.PropsConfig.class})
class FileUploadControllerTest {

    @Autowired MockMvc mvc;

    @TempDir static Path tmp;

    @TestConfiguration
    static class PropsConfig {
        @Bean
        FileIngestProperties fileIngestProperties() {
            return new FileIngestProperties(
                    tmp.toString(),           // inputDir
                    tmp.resolve("processing").toString(),
                    tmp.resolve("processed").toString(),
                    tmp.resolve("failed").toString(),
                    "*.txt",
                    1000L,
                    10,
                    null,
                    31
            );
        }
    }

    @Test
    void upload_missingFilename_shouldReturn400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "", MediaType.TEXT_PLAIN_VALUE, "data".getBytes()
        );

        mvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Missing filename"));
    }

    @Test
    void upload_valid_shouldWriteFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "ok.txt", MediaType.TEXT_PLAIN_VALUE, "hello".getBytes()
        );

        mvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isOk());

        assertThat(Files.exists(tmp.resolve("ok.txt"))).isTrue();
        assertThat(Files.exists(tmp.resolve("ok.txt.part"))).isFalse();
    }
}

