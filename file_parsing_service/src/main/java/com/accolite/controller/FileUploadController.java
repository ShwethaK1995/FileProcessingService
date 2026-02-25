package com.accolite.controller;

import com.accolite.util.FileIngestProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileIngestProperties props;

    public FileUploadController(FileIngestProperties props) {
        this.props = props;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(
                Map.of(
                        "status", "UP",
                        "service", "parser-service",
                        "time", Instant.now().toString()
                )

        );

    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload file for parsing")
    public ResponseEntity<String> upload(
            @Parameter(
                    description = "File to upload",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart("file") MultipartFile file
    ) {
        try {
            Path inputDir = Paths.get(props.inputDir());
            Files.createDirectories(inputDir);

            String original = file.getOriginalFilename();
            if (original == null || original.isBlank()) {
                return ResponseEntity.badRequest().body("Missing filename");
            }
            original = Paths.get(original).getFileName().toString();

            Path tmp = inputDir.resolve(original + ".part");
            Path fin = inputDir.resolve(original);

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            }

            Files.move(tmp, fin, StandardCopyOption.REPLACE_EXISTING);
            String traceId = "UPL-" + System.currentTimeMillis();
            log.info("[{}] Uploaded file saved to: {}", traceId, fin.toAbsolutePath());

            return ResponseEntity.ok("Uploaded: " + fin.getFileName());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

}
