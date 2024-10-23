/*
 * Copyright 2023 (c) CoralBlocks - http://www.coralblocks.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.coralblocks.coralme.util;

import java.util.function.Supplier;

/**
 * An object pool backed up by an internal linked list with adaptive memory management. Instances
 * will be created on demand if the pool runs out of instances, and the pool's growth is limited
 * based on available memory. If memory is not available to create new instances, the get() method
 * will return null.
 *
 * <p><b>NOTE:</b> This data structure is designed to be used by <b>single-threaded systems</b> for
 * object pooling, but includes a separate thread for memory monitoring.
 *
 * @param <E> the type of objects this object pool will hold
 */
public final class LinkedObjectPool<E> implements ObjectPool<E>, MemoryCallback {

    private final LinkedObjectList<E> queue;
    private final Supplier<? extends E> supplier;
    private volatile boolean isMemoryAvailable = true;
    private final MemoryMonitor memoryMonitor;
    private static final double MEMORY_THRESHOLD = 0.05; // 5% of max memory
    private final int maxSize;

    /**
     * Creates a LinkedObjectPool with adaptive memory management.
     *
     * @param initialSize the initial size of the pool (how many instances it will initially have)
     * @param s the supplier that will be used to create the instances
     * @param maxSize the maximum size of the pool
     */
    public LinkedObjectPool(int initialSize, Supplier<? extends E> s, int maxSize) {
        supplier = s;
        queue = new LinkedObjectList<>(initialSize);
        this.maxSize = maxSize;

        for (int i = 0; i < initialSize; i++) {
            queue.addLast(supplier.get());
        }

        memoryMonitor = new MemoryMonitor(this, 0.05, 1000); // 5 percent
        memoryMonitor.start();
    }

    public LinkedObjectPool(int initialSize, Supplier<? extends E> s) {
        this(initialSize, s, Integer.MAX_VALUE);
    }

    /**
     * The number of instances currently inside this pool. Note that if all the instances are
     * checked-out from this pool, the size returned will be zero.
     *
     * @return the number of instances currently sitting inside this pool (and not checked-out by
     *     anyone)
     */
    public final int size() {
        return queue.size();
    }

    /**
     * Retrieves an instance from the pool or creates a new one if the pool is empty. This method
     * considers memory constraints when creating new instances.
     *
     * @return an instance from the pool, or null if memory is not available to create a new
     *     instance
     */
    @Override
    public final E get() {
        if (!queue.isEmpty()) {
            return queue.removeLast();
        }

        if (isMemoryAvailable) {
            return supplier.get();
        }

        return null; // Return null when memory is not available to create a new instance
    }

    /**
     * Returns an instance back to the pool. This method considers memory constraints when adding
     * the instance back to the pool.
     *
     * @param e the instance to return back to the pool (i.e. release to the pool)
     */
    @Override
    public final void release(E e) {
        if (queue.size() < maxSize) {
            queue.addLast(e);
        }
        // If the pool is at maximum capacity, the object is discarded and left for garbage collection
    }

    /**
     * Updates the available memory. This method is called by the MemoryMonitor.
     *
     * @param isMemoryAvailable the current available memory
     */
    public void updateIsMemoryAvailable(boolean isMemoryAvailable) {
         this.isMemoryAvailable = isMemoryAvailable;
         if (!isMemoryAvailable) {
             // Aggressively reduce pool size when memory is low
             while (queue.size() > maxSize / 2) {
                 queue.removeLast();
             }
         }
    }

    /** Stops the memory monitor thread. Should be called when the pool is no longer needed. */
    public void shutdown() {
        memoryMonitor.stopMonitoring();
    }
}
