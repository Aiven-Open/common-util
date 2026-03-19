/*
         Copyright 2025 Aiven Oy and project contributors

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing,
        software distributed under the License is distributed on an
        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
        KIND, either express or implied.  See the License for the
        specific language governing permissions and limitations
        under the License.

        SPDX-License-Identifier: Apache-2
 */
package io.aiven.commons.util.timing;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class BackoffTest {

	/**
	 * The amount of extra time that we will allow for timing errors.
	 */
	private static final long TIMING_DELTA_MS = 250;

	@Test
	void backoffTest() throws InterruptedException {
		final Timer timer = new Timer(Duration.ofSeconds(1));
		final Backoff backoff = new Backoff(timer.getBackoffConfig());
		final long estimatedDelay = backoff.estimatedDelay();
		assertThat(estimatedDelay).isLessThan(500);

		// execute delay without timer running.
		final StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		backoff.delay();
		stopWatch.stop();
		assertThat(stopWatch.getDuration().toMillis()).as("Result without timer running").isBetween(
				estimatedDelay - backoff.getMaxJitter() - TIMING_DELTA_MS,
				estimatedDelay + backoff.getMaxJitter() + TIMING_DELTA_MS);

		timer.start();
		for (int i = 0; i < 9; i++) {
			stopWatch.reset();
			timer.reset();
			timer.start();
			stopWatch.start();
			await().atMost(Duration.ofSeconds(2)).until(() -> {
				backoff.delay();
				return backoff.estimatedDelay() == 0 || timer.isExpired();
			});
			stopWatch.stop();
			timer.stop();
			final int step = i;
			if (!timer.isExpired()) {
				assertThat(stopWatch.getDuration().toMillis())
						.as(() -> String.format("Result with timer running at step %s", step))
						.isBetween(Duration.ofSeconds(1).toMillis() - backoff.getMaxJitter() - TIMING_DELTA_MS,
								Duration.ofSeconds(1).toMillis() + backoff.getMaxJitter() + TIMING_DELTA_MS);
			}
		}
	}

	/**
	 * This test creates the condition where the delay increases until the expected
	 * delay will cause the maxDelay to be exceeded. The result is that the total
	 * delay should be the maxDelay not the higher calculated expected value.
	 *
	 * @throws InterruptedException
	 *             on interruption.
	 */
	@Test
	void backoffIncrementalTimeTest() throws InterruptedException {
		final AtomicBoolean abortTrigger = new AtomicBoolean();
		// delay increases in powers of 2.
		final long maxDelay = 1000; // not a power of 2
		final BackoffConfig config = new BackoffConfig() {
			@Override
			public SupplierOfLong getSupplierOfTimeRemaining() {
				return () -> maxDelay;
			}

			@Override
			public AbortTrigger getAbortTrigger() {
				return () -> abortTrigger.set(true);
			}

			@Override
			public boolean applyTimerRule() {
				return true;
			}
		};

		final Backoff backoff = new Backoff(config);
		long expected = 2;
		// estimated delay is the delay without jitter
		while (backoff.estimatedDelay() < maxDelay) {
			assertThat(backoff.estimatedDelay()).isEqualTo(expected);
			backoff.delay();
			// delay may exit early due to induced jitter.
			expected *= 2;
		}
		assertThat(backoff.estimatedDelay()).isEqualTo(maxDelay);
	}

	/**
	 * This test creates the condition where the delay increases until the expected
	 * delay will cause the maxDelay to be exceeded. The result is that the total
	 * delay should be the maxDelay not the higher calculated expected value.
	 *
	 * @throws InterruptedException
	 *             on interruption.
	 */
	@Test
	void noTimerRuleTest() throws InterruptedException {
		final AtomicBoolean abortTrigger = new AtomicBoolean();
		// delay increases in powers of 2.
		final long maxDelay = 1024;
		final BackoffConfig config = new BackoffConfig() {
			@Override
			public SupplierOfLong getSupplierOfTimeRemaining() {
				return () -> maxDelay;
			}

			@Override
			public AbortTrigger getAbortTrigger() {
				return () -> abortTrigger.set(true);
			}

			@Override
			public boolean applyTimerRule() {
				return false;
			}
		};

		final Backoff backoff = new Backoff(config);
		do {
			backoff.cleanDelay();
		} while (backoff.estimatedDelay() < 1024);

		// execute delay without timer running.
		final StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		backoff.delay();
		stopWatch.stop();
		assertThat(stopWatch.getDuration().toMillis()).as("Result without timer running").isBetween(
				1024 - backoff.getMaxJitter() - TIMING_DELTA_MS, 1024 + backoff.getMaxJitter() + TIMING_DELTA_MS);

	}

	/**
	 * This test creates the condition where the delay increases until the expected
	 * delay will cause the maxDelay to be exceeded. The result is that the total
	 * delay should be the maxDelay not the higher calculated expected value.
	 *
	 * @throws InterruptedException
	 *             on interruption.
	 */
	@Test
	void minimumDelayTest() throws InterruptedException {
		final Timer timer = new Timer(Duration.ofSeconds(1));
		final Backoff backoff = new Backoff(timer.getBackoffConfig());
		backoff.setMinimumDelay(Duration.ofMillis(250));
		final long estimatedDelay = backoff.estimatedDelay();
		assertThat(estimatedDelay).isGreaterThan(250);

		// execute delay without timer running.
		final StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		backoff.delay();
		stopWatch.stop();
		assertThat(stopWatch.getDuration().toMillis()).as("Result without timer running").isBetween(
				estimatedDelay - backoff.getMaxJitter() - TIMING_DELTA_MS,
				estimatedDelay + backoff.getMaxJitter() + TIMING_DELTA_MS);

		backoff.reset();
		stopWatch.reset();
		stopWatch.start();
		backoff.delay();
		stopWatch.stop();
		assertThat(stopWatch.getDuration().toMillis()).as("Result without timer running").isBetween(
				estimatedDelay - backoff.getMaxJitter() - TIMING_DELTA_MS,
				estimatedDelay + backoff.getMaxJitter() + TIMING_DELTA_MS);
	}
}
