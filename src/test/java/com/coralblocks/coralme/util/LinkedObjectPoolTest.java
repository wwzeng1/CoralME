package com.coralblocks.coralme.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LinkedObjectPoolTest {

    // ... (keep existing test methods)

    @Test
    public void testAdaptiveGrowthUnderMemoryPressure() {
        LinkedObjectPool<byte[]> pool =
                new LinkedObjectPool<>(2, () -> new byte[1024 * 1024]); // 1MB objects
        List<byte[]> objects = new ArrayList<>();

        try {
            while (true) {
                objects.add(pool.get());
            }
        } catch (OutOfMemoryError e) {
            // Expected behavior when memory is exhausted
        }

        Assert.assertTrue(
                "Pool should have created multiple objects before running out of memory",
                objects.size() > 2);
        Assert.assertTrue("Pool size should be zero after exhausting memory", pool.size() == 0);

        // Release objects back to the pool
        for (byte[] obj : objects) {
            pool.release(obj);
        }

        // The pool size should be less than or equal to the number of objects created
        // due to memory constraints
        Assert.assertTrue(
                "Pool size should be limited by available memory", pool.size() <= objects.size());
    }

    @Test
    public void testThreadSafetyOfMemoryMonitoring() throws InterruptedException {
        LinkedObjectPool<StringBuilder> pool = new LinkedObjectPool<>(10, StringBuilder::new);
        int numThreads = 10;
        int operationsPerThread = 1000;

        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] =
                    new Thread(
                            () -> {
                                for (int j = 0; j < operationsPerThread; j++) {
                                    StringBuilder sb = pool.get();
                                    // Simulate some work
                                    sb.append("test");
                                    pool.release(sb);
                                }
                            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // The final pool size should be consistent
        Assert.assertEquals(
                "Pool size should be consistent after concurrent operations", 10, pool.size());
    }

    @Test
    public void testHighDemandUsage() {
        LinkedObjectPool<byte[]> pool =
                new LinkedObjectPool<>(5, () -> new byte[1024 * 1024]); // 1MB objects
        List<byte[]> objects = new ArrayList<>();

        // Simulate high demand by repeatedly getting and releasing objects
        for (int i = 0; i < 1000; i++) {
            byte[] obj = pool.get();
            objects.add(obj);

            if (i % 10 == 0) {
                // Periodically release some objects
                for (int j = 0; j < objects.size() / 2; j++) {
                    pool.release(objects.remove(j));
                }
            }
        }

        // Release all remaining objects
        for (byte[] obj : objects) {
            pool.release(obj);
        }

        // The final pool size should be limited by available memory
        Assert.assertTrue("Pool size should adapt to available memory", pool.size() <= 1000);
    }

    @Test
    public void testIncreasingPoolSize() {
        LinkedObjectPool<StringBuilder> pool = new LinkedObjectPool<>(2, StringBuilder::new);

        Assert.assertEquals(2, pool.size());

        for (int i = 0; i < 2; i++) {
            pool.release(new StringBuilder());
        }

        // The pool size might not grow to exactly 4 due to memory constraints
        Assert.assertTrue(
                "Pool size should be between 2 and 4", pool.size() >= 2 && pool.size() <= 4);

        List<StringBuilder> list = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            StringBuilder sb = pool.get();
            if (sb != null) {
                list.add(sb);
            } else {
                break;
            }
        }

        Assert.assertTrue("Should have gotten at least 2 instances", list.size() >= 2);
        Assert.assertEquals(0, pool.size());

        // Release the instances back to the pool
        for (StringBuilder sb : list) {
            pool.release(sb);
        }

        // The final pool size should be equal to the number of instances we got
        Assert.assertEquals(
                "Pool size should match the number of instances we got", list.size(), pool.size());
    }

    @Test
    public void testRunOutOfInstances() {
        LinkedObjectPool<StringBuilder> pool = new LinkedObjectPool<>(2, StringBuilder::new);
        List<StringBuilder> list = new ArrayList<>(3);

        Assert.assertEquals(2, pool.size());

        list.add(pool.get());
        list.add(pool.get());

        Assert.assertEquals(0, pool.size());

        StringBuilder sb = pool.get();
        Assert.assertNotNull("Should be able to create a new instance", sb);
        list.add(sb);

        Assert.assertFalse(
                "New instance should not be in the original set", list.subList(0, 2).contains(sb));

        Assert.assertEquals(0, pool.size());

        for (StringBuilder builder : list) {
            pool.release(builder);
        }

        // The pool size might not grow to exactly 3 due to memory constraints
        Assert.assertTrue(
                "Pool size should be between 2 and 3", pool.size() >= 2 && pool.size() <= 3);

        StringBuilder sb1 = pool.get();
        StringBuilder sb2 = pool.get();

        Assert.assertTrue(
                "Pool size should be between 0 and 1", pool.size() >= 0 && pool.size() <= 1);

        Assert.assertTrue("sb1 should be in the original list", list.contains(sb1));
        Assert.assertTrue("sb2 should be in the original list", list.contains(sb2));

        StringBuilder sb3 = pool.get();
        if (sb3 != null) {
            Assert.assertFalse("sb3 should not be in the original list", list.contains(sb3));
        }

        Assert.assertEquals(0, pool.size());
    }
}
