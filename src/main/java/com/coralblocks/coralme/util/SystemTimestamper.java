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

/**
 * <p>This timestamper is here just for illustrative purposes.</p>
 * 
 * <p>There are of course much better ways to get the epoch with nanosecond precision. And without producing any garbage for the GC.</p>
 */
public final class SystemTimestamper implements Timestamper {

	private final long startTimeMillis = System.currentTimeMillis() * 1_000_000L;
	private final long startTimeNano = System.nanoTime();


	@Override
	public long nanoEpoch() {
		return startTimeMillis + (System.nanoTime() - startTimeNano);
	}
}