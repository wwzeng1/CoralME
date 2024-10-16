package com.coralblocks.coralme.util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * A utility class that monitors memory usage and updates a callback with the current
 * available memory status.
 */
class MemoryMonitor {
    private final MemoryCallback callback;
    private final MemoryMXBean memoryMXBean;
    private volatile boolean running;
    private Thread monitorThread;
    private final long minMemory;
    private final long frequency;


    /**
     * Creates a new MemoryMonitor for the given callback.
     *
     * @param callback the MemoryCallback to update with memory information
     * @param percentage the minimum percentage of memory that should be available
     */
    MemoryMonitor(MemoryCallback callback, double percentage, long frequency) {
        this.callback = callback;
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();

        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        this.minMemory = (long)((heapMemoryUsage.getMax() - heapMemoryUsage.getUsed()) * percentage);
        this.frequency = frequency;

        this.running = true;
    }

    /** Starts the memory monitoring thread. */
    void start() {
        monitorThread = new Thread(this::monitorMemory);
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    /** Stops the memory monitoring thread. */
    void stopMonitoring() {
        running = false;
        monitorThread.interrupt();
    }

    private void monitorMemory() {
        while (running && !Thread.currentThread().isInterrupted()) {

            MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
            long availableMemory = heapMemoryUsage.getMax() - heapMemoryUsage.getUsed();
            callback.isMemoryAvailable(availableMemory > minMemory);

            try {
                Thread.sleep(1000); // Update every second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

