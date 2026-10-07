# Java 21 Virtual Threads (JEP 444) Migration Guide

## Overview
Virtual threads are lightweight threads managed by the Java runtime rather than the OS. They drastically reduce the cost of high-concurrency applications, allowing millions of concurrent tasks.

## Refactoring Guidelines
1. Replace `Executors.newFixedThreadPool(n)` or `Executors.newCachedThreadPool()` with `Executors.newVirtualThreadPerTaskExecutor()`.
2. Do NOT pool virtual threads. Create a new virtual thread per task/request.
3. Replace heavy `synchronized` blocks with `java.util.concurrent.locks.ReentrantLock` to prevent thread pinning on carrier threads.
4. Replace blocking I/O calls (`java.io`) with NIO or virtual thread-friendly blocking I/O APIs (`java.nio.file.Files`).

## Code Example
```java
// Legacy Java 8/11:
ExecutorService executor = Executors.newFixedThreadPool(100);

// Modern Java 21+:
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> performIoTask());
}
```
