package com.coralblocks.coralme.util;

/**
 * Interface for objects that can handle memory availability updates.
 */
@FunctionalInterface
interface MemoryCallback {
    void updateIsMemoryAvailable(boolean isAvailable);
}

