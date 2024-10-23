package com.coralblocks.coralme.util;

import org.junit.Assert;
import org.junit.Test;

public class LinkedObjectPoolTest {

    @Test
    public void testIncreasingPoolSize() {
        LinkedObjectPool<StringBuilder> pool = new LinkedObjectPool<>(2, StringBuilder::new);
        Assert.assertEquals(2, pool.size());

        StringBuilder sb1 = pool.get();
        StringBuilder sb2 = pool.get();
        Assert.assertNotNull(sb1);
        Assert.assertNotNull(sb2);
        Assert.assertEquals(0, pool.size());

        pool.release(sb1);
        pool.release(sb2);
        Assert.assertEquals(2, pool.size());
    }

    @Test
    public void testRunOutOfInstances() {
        LinkedObjectPool<StringBuilder> pool = new LinkedObjectPool<>(2, StringBuilder::new);
        Assert.assertEquals(2, pool.size());

        StringBuilder sb1 = pool.get();
        StringBuilder sb2 = pool.get();
        Assert.assertNotNull(sb1);
        Assert.assertNotNull(sb2);
        Assert.assertEquals(0, pool.size());

        StringBuilder sb3 = pool.get();
        Assert.assertNotNull("Should be able to create new instance when pool is empty", sb3);

        pool.release(sb1);
        pool.release(sb2);
        pool.release(sb3);
        Assert.assertEquals(3, pool.size());
    }

    @Test
    public void testReleaseAndReuse() {
        LinkedObjectPool<StringBuilder> pool = new LinkedObjectPool<>(1, StringBuilder::new);
        Assert.assertEquals(1, pool.size());

        StringBuilder sb1 = pool.get();
        Assert.assertNotNull(sb1);
        Assert.assertEquals(0, pool.size());

        pool.release(sb1);
        Assert.assertEquals(1, pool.size());

        StringBuilder sb2 = pool.get();
        Assert.assertNotNull(sb2);
        Assert.assertSame("Should reuse released instance", sb1, sb2);
        Assert.assertEquals(0, pool.size());
    }
}
