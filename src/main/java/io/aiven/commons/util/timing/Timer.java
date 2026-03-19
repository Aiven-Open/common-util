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

import java.time.Duration;

/**
 * Calculates elapsed time and flags when expired.
 */
public class Timer extends StopWatch {
	/**
	 * The length of time that the timer should run.
	 */
	private final long duration;

	/**
	 * The flag that indicates the timer has been aborted.
	 */
	private boolean hasAborted;

	/**
	 * Constructor.
	 *
	 * @param duration
	 *            the length of time the timer should run.
	 */
	public Timer(final Duration duration) {
		super();
		this.duration = duration.toMillis();
	}

	/**
	 * Gets the maximum duration for this timer.
	 *
	 * @return the maximum duration for the timer.
	 */
	public long millisecondsRemaining() {
		return super.isStarted() ? duration - super.getDuration().toMillis() : duration;
	}

	/**
	 * Returns {@code true} if the timer has expired.
	 *
	 * @return {@code true} if the timer has expired.
	 */
	public boolean isExpired() {
		return hasAborted || super.getDuration().toMillis() >= duration;
	}

	/**
	 * Aborts the timer. Timer will report that it has expired until reset is
	 * called.
	 */
	public void abort() {
		hasAborted = true;
	}

	@Override
	public void start() {
		try {
			hasAborted = false;
			super.start();
		} catch (IllegalStateException e) {
			throw new IllegalStateException("Timer: " + e.getMessage());
		}
	}

	@Override
	public void stop() {
		try {
			super.stop();
		} catch (IllegalStateException e) {
			throw new IllegalStateException("Timer: " + e.getMessage());
		}
	}

	@Override
	public void reset() {
		try {
			hasAborted = false;
			super.reset();
		} catch (IllegalStateException e) {
			throw new IllegalStateException("Timer: " + e.getMessage());
		}
	}

	/**
	 * Gets a Backoff Config for this timer.
	 *
	 * @return a backoff Configuration.
	 */
	public BackoffConfig getBackoffConfig() {
		return new BackoffConfig() {

			@Override
			public SupplierOfLong getSupplierOfTimeRemaining() {
				return Timer.this::millisecondsRemaining;
			}

			@Override
			public AbortTrigger getAbortTrigger() {
				return Timer.this::abort;
			}

			@Override
			public boolean applyTimerRule() {
				return true;
			}
		};
	}
}
