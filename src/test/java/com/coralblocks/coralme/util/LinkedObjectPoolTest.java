package com.coralblocks.coralme.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LinkedObjectPoolTest {

    // ... (keep existing test methods)

    @Test
    public void testAdaptiveGrowthUnderMemoryPressure() {
        // ... (keep existing method implementation)
    }

    @Test
    public void testThreadSafetyOfMemoryMonitoring() throws InterruptedException {
        // ... (keep existing method implementation)
    }

    @Test
    public void testHighDemandUsage() {
        // ... (keep existing method implementation)
    }

    @Test
    public void testLIFOForGoodCaching() {
        LinkedObjectPool<StringBuilder> pool = new LinkedObjectPool<>(2, StringBuilder::new);

        Assert.assertEquals(2, pool.size());

        StringBuilder sb1 = pool.get();
        Assert.assertNotNull(sb1);
        pool.release(sb1);

        StringBuilder sb2 = pool.get();
        Assert.assertNotNull(sb2);
        Assert.assertTrue(sb1 == sb2); // Should be the same instance due to LIFO behavior

        // Get another object to test LIFO order
        StringBuilder sb3 = pool.get();
        Assert.assertNotNull(sb3);
        pool.release(sb3);

        StringBuilder sb4 = pool.get();
        Assert.assertNotNull(sb4);
        Assert.assertTrue(sb3 == sb4); // Should be the same instance due to LIFO behavior
    }

    @Test
    public void testIncreasingPoolSize() {
        LinkedObjectPool<StringBuilder> pool = new LinkedObjectPool<>(2, StringBuilder::new);

        Assert.assertEquals(2, pool.size());

        // Get all objects from the pool
        StringBuilder sb1 = pool.get();
        StringBuilder sb2 = pool.get();
        Assert.assertNotNull(sb1);
        Assert.assertNotNull(sb2);
        Assert.assertEquals(0, pool.size());

        // Release objects back to the pool
        pool.release(sb1);
        pool.release(sb2);
        Assert.assertEquals(2, pool.size());

        // Add new objects to increase pool size
        pool.release(new StringBuilder());
        pool.release(new StringBuilder());
        Assert.assertTrue(pool.size() >= 2 && pool.size() <= 4);

        // Get objects from the pool
        List<StringBuilder> objects = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            StringBuilder sb = pool.get();
            if (sb != null) {
                objects.add(sb);
            } else {
                break; // Stop if we get null due to memory constraints
            }
        }

        Assert.assertTrue(objects.size() >= 2 && objects.size() <= 4);
        Assert.assertEquals(0, pool.size());

        // Release objects back to the pool
        for (StringBuilder sb : objects) {
            pool.release(sb);
        }

        Assert.assertTrue(pool.size() >= 2 && pool.size() <= 4);
    }
}
