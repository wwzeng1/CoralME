package com.coralblocks.coralme.util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * A utility class that monitors memory usage and updates the LinkedObjectPool with the current
 * available memory.
 */
class MemoryMonitor {
    private final LinkedObjectPool<?> pool;
    private final MemoryMXBean memoryMXBean;
    private volatile boolean running;
    private Thread monitorThread;

    /**
     * Creates a new MemoryMonitor for the given LinkedObjectPool.
     *
     * @param pool the LinkedObjectPool to update with memory information
     */
    MemoryMonitor(LinkedObjectPool<?> pool) {
        this.pool = pool;
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
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
            pool.updateAvailableMemory(availableMemory);
            try {
                Thread.sleep(1000); // Update every second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
