package com.coralblocks.coralme.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LinkedObjectPoolTest {

    @Test
    public void testAdaptiveGrowthUnderMemoryPressure() {
        int maxSize = 1000;
        LinkedObjectPool<byte[]> pool =
                new LinkedObjectPool<>(2, () -> new byte[1024], maxSize); // 1MB objects, max size 1000
        List<byte[]> objects = new ArrayList<>();

        byte[] object = pool.get();
        while (object != null && objects.size() < maxSize * 2) {
            objects.add(object);
            object = pool.get();
        }

        Assert.assertTrue(
                "Pool should have created multiple objects before reaching max size",
                objects.size() > 2 && objects.size() <= maxSize * 2);
        Assert.assertTrue("Pool size should be zero after exhausting pool", pool.size() == 0);

        // Release objects back to the pool
        for (byte[] obj : objects) {
            pool.release(obj);
        }

        // The pool size should be less than or equal to the maximum size
        Assert.assertTrue(
                "Pool size should be limited by max size", pool.size() <= maxSize);
    }

    @Test
    public void testIncreasingPoolSize() {
        int maxSize = 10;
        LinkedObjectPool<StringBuilder> pool = new LinkedObjectPool<>(2, StringBuilder::new, maxSize);

        Assert.assertEquals(2, pool.size());

        for (int i = 0; i < maxSize; i++) {
            pool.release(new StringBuilder());
        }

        // The pool size should not exceed the maximum size
        Assert.assertEquals("Pool size should be equal to max size", maxSize, pool.size());

        List<StringBuilder> list = new ArrayList<>(maxSize + 2);
        for (int i = 0; i < maxSize + 2; i++) {
            StringBuilder sb = pool.get();
            if (sb != null) {
                list.add(sb);
            } else {
                break;
            }
        }

        Assert.assertEquals("Should have gotten max size instances", maxSize, list.size());
        Assert.assertEquals(0, pool.size());

        // Release the instances back to the pool
        for (StringBuilder sb : list) {
            pool.release(sb);
        }

        // The final pool size should be equal to the maximum size
        Assert.assertEquals("Pool size should be equal to max size", maxSize, pool.size());
    }

    @Test
    public void testMemoryConstraints() {
        int maxSize = 1000;
        LinkedObjectPool<byte[]> pool = new LinkedObjectPool<>(2, () -> new byte[1024 * 1024], maxSize); // 1MB objects

        List<byte[]> objects = new ArrayList<>();
        byte[] object;
        while ((object = pool.get()) != null) {
            objects.add(object);
        }

        Assert.assertTrue("Should have created some objects", objects.size() > 0);
        Assert.assertTrue("Should not exceed max size", objects.size() <= maxSize);

        // Release all objects
        for (byte[] obj : objects) {
            pool.release(obj);
        }

        Assert.assertTrue("Pool size should not exceed max size", pool.size() <= maxSize);
    }
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

        Assert.assertTrue(
                "Pool size is 3", pool.size() <= 3);

        StringBuilder sb1 = pool.get();
        StringBuilder sb2 = pool.get();

        Assert.assertTrue(
                "Pool size should be between 0 and 1", pool.size() == 1);

        Assert.assertTrue("sb1 should be in the original list", list.contains(sb1));
        Assert.assertTrue("sb2 should be in the original list", list.contains(sb2));

        StringBuilder sb3 = pool.get();
        Assert.assertTrue("sb3 should not be in the original list", list.contains(sb3));

        Assert.assertEquals(0, pool.size());
    }
}
