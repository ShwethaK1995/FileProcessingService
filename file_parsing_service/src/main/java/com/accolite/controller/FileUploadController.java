package com.accolite.controller;

import com.accolite.util.FileIngestProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileIngestProperties props;

    public FileUploadController(FileIngestProperties props) {
        this.props = props;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        Path inputDir = Paths.get(props.inputDir());
        Files.createDirectories(inputDir);

        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            return ResponseEntity.badRequest().body("Missing filename");
        }

        Path tmp = inputDir.resolve(original + ".part");
        Path fin = inputDir.resolve(original);

        // Write temp, then commit
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
        }
        Files.move(tmp, fin, StandardCopyOption.REPLACE_EXISTING);

        return ResponseEntity.ok("Uploaded: " + fin.getFileName());
    }
}
