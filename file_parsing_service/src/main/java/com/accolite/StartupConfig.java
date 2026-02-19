package com.accolite;

import com.accolite.service.FileParsingScheduler;
import com.accolite.service.FileProcessingService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StartupConfig {

    @Bean
    public ApplicationRunner runOnceAtStartup(FileParsingScheduler fileParsingScheduler) {
        return args -> {
            fileParsingScheduler.poll();   // or fileProcessingService.processAll()
        };
    }
}
