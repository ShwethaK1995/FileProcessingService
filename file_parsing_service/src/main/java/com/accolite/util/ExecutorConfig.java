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
    public ExecutorService fileExecutor(ConcurrencyProperties props) {
        int workers = props.workers() > 0 ? props.workers() : 4;
        int cap = props.queueCapacity() > 0 ? props.queueCapacity() : 100;
        return new ThreadPoolExecutor(
                workers,
                workers ,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(cap),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }
}