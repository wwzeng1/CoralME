package com.coralblocks.coralme.util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.function.Consumer;

/**
 * A utility class that monitors memory usage and updates a callback with the current
 * available memory status.
 */
public final class MemoryMonitor {

    private final Consumer<Boolean> callback;
    private final long frequency;

    private final MemoryMXBean memoryMXBean;
    private final long minMemory;

    private Thread monitorThread;
    private volatile boolean running;


    /**
     * Creates a new MemoryMonitor for the given callback.
     *
     * @param callback the Consumer<Boolean> to update with memory information
     * @param percentage the minimum percentage of memory that should be available
     * @param frequency the frequency at which to check memory usage, in milliseconds
     */
    MemoryMonitor(Consumer<Boolean> callback, double percentage, long frequency) {
        this.callback = callback;
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        this.frequency = frequency;

        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        this.minMemory = (long)((heapMemoryUsage.getMax() - heapMemoryUsage.getUsed()) * percentage);
    }

    /** Starts the memory monitoring thread. */
    void start() {
        monitorThread = new Thread(this::monitorMemory);
        monitorThread.setDaemon(true);
        monitorThread.start();
        running = true;

    }

    /** Stops the memory monitoring thread. */
    void stopMonitoring() {
        monitorThread.interrupt();
        running = false;
    }

    private void monitorMemory() {

        while (running && !Thread.currentThread().isInterrupted()) {

            MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
            long availableMemory = heapMemoryUsage.getMax() - heapMemoryUsage.getUsed();
            callback.accept(availableMemory > minMemory);

            try {
                Thread.sleep(frequency);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}