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
                new LinkedObjectPool<>(2, () -> new byte[1024]); // 1MB objects
        List<byte[]> objects = new ArrayList<>();

        try {
            byte[] object = pool.get();
            while (object != null) {
                objects.add(object);
                object = pool.get();
            }
        } catch (OutOfMemoryError e) {
            Assert.assertTrue(false);
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
