package com.accolite.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ExecutorConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService fileIntakeExecutor(ConcurrencyProperties props) {
        int workers = props.workers();
        int cap = props.queueCapacity();
        return new ThreadPoolExecutor(
                workers, workers, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(cap),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService chunkExecutor(ConcurrencyProperties props) {
        int workers = Math.max(4, props.workers());                    // larger for parsing
        int cap = Math.max(200, props.queueCapacity());
        return new ThreadPoolExecutor(
                workers, workers, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(cap),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
