package com.legacylifter.config;

import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executors;

/**
 * Configures virtual threads for the entire application.
 *
 * <p>Virtual threads (Project Loom, JEP 444) allow massive concurrency without the
 * overhead of platform threads. Every blocking I/O operation (DB queries, HTTP calls,
 * file reads) will automatically yield the carrier thread.</p>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Replaces the default task executor with a virtual-thread-per-task executor.
     * All @Async methods and CompletableFuture operations use virtual threads.
     */
    @Bean
    public AsyncTaskExecutor applicationTaskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }

    /**
     * Configures Tomcat to handle each incoming HTTP request on a virtual thread
     * instead of a platform thread from a fixed pool.
     */
    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerCustomizer() {
        return protocolHandler -> protocolHandler.setExecutor(
                Executors.newVirtualThreadPerTaskExecutor()
        );
    }
}
