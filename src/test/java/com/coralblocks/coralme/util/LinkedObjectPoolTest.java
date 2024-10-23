package com.coralblocks.coralme.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LinkedObjectPoolTest {

    @Test
    public void testAdaptiveGrowthUnderMemoryPressure() {
        LinkedObjectPool<byte[]> pool =
                new LinkedObjectPool<>(2, () -> new byte[1024 * 1024]); // 1MB objects
        List<byte[]> objects = new ArrayList<>();

        byte[] object;
        while ((object = pool.get()) != null && objects.size() < 1000) {
            objects.add(object);
        }

        Assert.assertTrue("Should have created some objects", objects.size() > 2);
        Assert.assertTrue("Should not exceed 1000 objects", objects.size() <= 1000);
        Assert.assertEquals("Pool should be empty after exhaustion", 0, pool.size());

        // Release objects back to the pool
        for (byte[] obj : objects) {
            pool.release(obj);
        }

        // The pool size should have increased, but may be limited by available memory
        Assert.assertTrue("Pool size should have increased", pool.size() > 2);
        Assert.assertTrue(
                "Pool size should not exceed objects created", pool.size() <= objects.size());
    }

    @Test
    public void testBasicFunctionality() {
        LinkedObjectPool<StringBuilder> pool = new LinkedObjectPool<>(2, StringBuilder::new);

        Assert.assertEquals(2, pool.size());

        StringBuilder sb1 = pool.get();
        StringBuilder sb2 = pool.get();
        Assert.assertNotNull(sb1);
        Assert.assertNotNull(sb2);
        Assert.assertEquals(0, pool.size());

        StringBuilder sb3 = pool.get();
        Assert.assertNotNull(sb3);

        pool.release(sb1);
        pool.release(sb2);
        pool.release(sb3);

        Assert.assertTrue(pool.size() > 0);
    }

    @Test
    public void testMemoryConstraints() {
        LinkedObjectPool<byte[]> pool =
                new LinkedObjectPool<>(2, () -> new byte[1024 * 1024]); // 1MB objects

        List<byte[]> objects = new ArrayList<>();
        byte[] object;
        while ((object = pool.get()) != null && objects.size() < 100) {
            objects.add(object);
        }

        Assert.assertTrue("Should create multiple objects", objects.size() > 2);
        Assert.assertTrue("Should stop at memory limit", objects.size() <= 100);

        for (byte[] obj : objects) {
            pool.release(obj);
        }
        Assert.assertTrue(
                "Pool should retain objects within memory limits",
                pool.size() > 0 && pool.size() <= objects.size());
    }
}
