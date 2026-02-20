package com.accolite.app.test;

import com.accolite.util.ConcurrencyProperties;
import com.accolite.util.ExecutorConfig;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;

class ExecuterConfigTest {

    @Test
    void fileExecutor_shouldCreateBoundedThreadPool() {
        ExecutorConfig cfg = new ExecutorConfig();
        ExecutorService es = cfg.fileIntakeExecutor(new ConcurrencyProperties(2, 5));

        assertTrue(es instanceof ThreadPoolExecutor);
        ThreadPoolExecutor tpe = (ThreadPoolExecutor) es;

        assertEquals(2, tpe.getCorePoolSize());
        assertEquals(2, tpe.getMaximumPoolSize());
        assertEquals(5, tpe.getQueue().remainingCapacity() + tpe.getQueue().size());

        es.shutdown();
    }
}

