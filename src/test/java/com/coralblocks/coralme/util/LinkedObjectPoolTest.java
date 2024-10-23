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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;


public class LinkedObjectPoolTest {

	@Test
	public void testSameInstance() {

		LinkedObjectPool<StringBuilder> pool = new LinkedObjectPool<>(8, StringBuilder::new);

		Assert.assertEquals(8, pool.size());

		StringBuilder sb1 = pool.get();

		Assert.assertEquals(7, pool.size());

		pool.release(sb1);

		Assert.assertEquals(8, pool.size());

		StringBuilder sb2 = pool.get();

		Assert.assertEquals(7, pool.size());

		Assert.assertTrue(sb1 == sb2); // has to be same instance

		StringBuilder sb3 = pool.get();
		StringBuilder sb4 = pool.get();

		Assert.assertEquals(5, pool.size());

		pool.release(sb4);
		pool.release(sb3);

		Assert.assertEquals(7, pool.size());

		StringBuilder sb5 = pool.get();
		StringBuilder sb6 = pool.get();

		Assert.assertEquals(5, pool.size());

		// pool is LIFO (stack)
		Assert.assertTrue(sb5 == sb3);
		Assert.assertTrue(sb6 == sb4);
	}

	@Test
	public void testRunOutOfInstances() {

		LinkedObjectPool<StringBuilder> pool = new LinkedObjectPool<>(2, StringBuilder::new);

		Set<StringBuilder> set = new HashSet<StringBuilder>(2);

		Assert.assertEquals(2, pool.size());

		set.add(pool.get());
		set.add(pool.get());

		Assert.assertEquals(0, pool.size());

		StringBuilder sb = pool.get();
		Assert.assertNotEquals(null, sb);

		Assert.assertEquals(false, set.contains(sb));

		Assert.assertEquals(0, pool.size());

		pool.release(sb);

		Iterator<StringBuilder> iter = set.iterator();
		while(iter.hasNext()) pool.release(iter.next());

		Assert.assertEquals(3, pool.size()); // pool has grown from initial 2 to 3

		StringBuilder sb1 = pool.get();
		StringBuilder sb2 = pool.get();

		Assert.assertEquals(1, pool.size());

		Assert.assertEquals(true, set.contains(sb1));
		Assert.assertEquals(true, set.contains(sb2));
		Assert.assertEquals(false, set.contains(pool.get()));

		Assert.assertEquals(0, pool.size());
	}

	@Test
	public void testIncreasingPoolSize() {

		LinkedObjectPool<StringBuilder> pool = new LinkedObjectPool<>(2, StringBuilder::new);

		Assert.assertEquals(2, pool.size());

		for(int i = 0; i < 2; i++) pool.release(new StringBuilder());

		Assert.assertEquals(4, pool.size());

		for(int i = 0; i < 4; i++) pool.get();

		Assert.assertEquals(0, pool.size());
	}

	@Test
	public void testLIFOForGoodCaching() {

		LinkedObjectPool<StringBuilder> pool = new LinkedObjectPool<>(2, StringBuilder::new);

		Assert.assertEquals(2, pool.size());

		StringBuilder sb1 = pool.get();
		pool.release(sb1);

		StringBuilder sb2 = pool.get();
		Assert.assertTrue(sb1 == sb2);
	}

	@Test
	public void testConcurrentGetAndRelease() throws InterruptedException {
		final int POOL_SIZE = 10;
		final int THREAD_COUNT = 100;
		final int OPERATIONS_PER_THREAD = 1000;

		LinkedObjectPool<StringBuilder> pool = new LinkedObjectPool<>(POOL_SIZE, StringBuilder::new);
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
		CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
		AtomicInteger totalOperations = new AtomicInteger(0);

		for (int i = 0; i < THREAD_COUNT; i++) {
			executorService.submit(() -> {
				try {
					for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
						StringBuilder sb = pool.get();
						// Simulate some work
						Thread.sleep(1);
						pool.release(sb);
						totalOperations.incrementAndGet();
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		executorService.shutdown();

		Assert.assertEquals(THREAD_COUNT * OPERATIONS_PER_THREAD, totalOperations.get());
		Assert.assertTrue(pool.size() >= POOL_SIZE);
	}

	@Test
	public void testPoolGrowthBeyondInitialCapacity() {
		final int INITIAL_SIZE = 5;
		final int GROWTH_FACTOR = 3;

		LinkedObjectPool<StringBuilder> pool = new LinkedObjectPool<>(INITIAL_SIZE, StringBuilder::new);

		// Get all initial objects
		for (int i = 0; i < INITIAL_SIZE; i++) {
			pool.get();
		}

		Assert.assertEquals(0, pool.size());

		// Get more objects to force pool growth
		for (int i = 0; i < GROWTH_FACTOR * INITIAL_SIZE; i++) {
			StringBuilder sb = pool.get();
			pool.release(sb);
		}

		Assert.assertTrue(pool.size() > INITIAL_SIZE);
	}

	@Test
	public void testEdgeCases() {
		// Test empty pool
		LinkedObjectPool<StringBuilder> emptyPool = new LinkedObjectPool<>(0, StringBuilder::new);
		Assert.assertEquals(0, emptyPool.size());
		StringBuilder sb = emptyPool.get();
		Assert.assertNotNull(sb);
		emptyPool.release(sb);
		Assert.assertEquals(1, emptyPool.size());

		// Test very large pool
		final int LARGE_SIZE = 1_000_000;
		LinkedObjectPool<StringBuilder> largePool = new LinkedObjectPool<>(LARGE_SIZE, StringBuilder::new);
		Assert.assertEquals(LARGE_SIZE, largePool.size());

		// Get and release a large number of objects
		for (int i = 0; i < LARGE_SIZE; i++) {
			StringBuilder largeSb = largePool.get();
			Assert.assertNotNull(largeSb);
			largePool.release(largeSb);
		}

		Assert.assertEquals(LARGE_SIZE, largePool.size());
	}

	@Test
	public void testCustomBuilder() {
		final int POOL_SIZE = 5;
		final String PREFIX = "CustomObject_";
		AtomicInteger counter = new AtomicInteger(0);

		LinkedObjectPool<String> customPool = new LinkedObjectPool<>(POOL_SIZE, () -> PREFIX + counter.getAndIncrement());

		Assert.assertEquals(POOL_SIZE, customPool.size());

		Set<String> uniqueObjects = new HashSet<>();
		for (int i = 0; i < POOL_SIZE; i++) {
			String obj = customPool.get();
			Assert.assertTrue(obj.startsWith(PREFIX));
			uniqueObjects.add(obj);
		}

		Assert.assertEquals(POOL_SIZE, uniqueObjects.size());
		Assert.assertEquals(0, customPool.size());

		// Release objects back to the pool
		for (String obj : uniqueObjects) {
			customPool.release(obj);
		}

		Assert.assertEquals(POOL_SIZE, customPool.size());
	}
}