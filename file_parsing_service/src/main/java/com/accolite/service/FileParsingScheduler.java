package com.accolite.service;

import com.accolite.util.FileIngestProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Stream;

@Slf4j
@Component
public class FileParsingScheduler {

    private final FileIngestProperties props;
    private final ExecutorService executor;
    private final FileClaimer claimer;
    private final FileChunkProcessor.FileCompletenessChecker checker;
    private final FileProcessingService service;

    public FileParsingScheduler(FileIngestProperties props,
                                ExecutorService executor,
                                FileClaimer claimer,
                                FileChunkProcessor.FileCompletenessChecker checker,
                                FileProcessingService service) {
        this.props = props;
        this.executor = executor;
        this.claimer = claimer;
        this.checker = checker;
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${file.ingest.pollMs:5000}")
    public void poll() {
        try {
            Path inputDir = Paths.get(props.inputDir());
            if (!Files.isDirectory(inputDir)) return;

            PathMatcher matcher = inputDir.getFileSystem()
                    .getPathMatcher("glob:" + (props.pattern() == null ? "*" : props.pattern()));

            ThreadPoolExecutor tpe = (executor instanceof ThreadPoolExecutor) ? (ThreadPoolExecutor) executor : null;

            try (Stream<Path> files = Files.list(inputDir)) {
                files.filter(Files::isRegularFile)
                        .filter(p -> matcher.matches(p.getFileName()))
                        .filter(p -> checker.isComplete(p, props))
                        .limit(props.maxFilesPerRun())
                        .forEach(p -> {
                            // backpressure: stop feeding if queue is full
                            if (tpe != null && tpe.getQueue().remainingCapacity() == 0) {
                                log.warn("Executor queue full; skipping intake this cycle");
                                return;
                            }

                            claimer.claim(p, Paths.get(props.processingDir()))
                                    .ifPresent(claimed ->
                                            executor.submit(() -> service.process(claimed)));
                        });
            }
        } catch (Exception e) {
            log.error("Scheduler error", e);
        }
    }
}
