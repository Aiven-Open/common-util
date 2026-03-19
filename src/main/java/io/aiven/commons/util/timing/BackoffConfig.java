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

/**
 * An interface to define the Backoff configuration. The timer will generate a
 * proper Configuration.
 */
public interface BackoffConfig {
	/**
	 * Gets Supplier that will return the number of milliseconds remaining in the
	 * timer. Backoff will calculate delays until the result of this call reaches 0.
	 * For cases where a timer is not used this value should be the maximum delay.
	 *
	 * @return A supplier of the number of milliseconds until the timer expires.
	 */
	SupplierOfLong getSupplierOfTimeRemaining();

	/**
	 * The AbortTrigger that will abort the timer or otherwise signal that the
	 * backoff has reached the maximum delay and will no longer sleep.
	 *
	 * @return the AbortTrigger.
	 */
	AbortTrigger getAbortTrigger();

	/**
	 * Gets the abort timer rule flag. If there is no timer that may expire and
	 * shorten the time for the delay then this value should be {@code false}
	 * otherwise if the delay time will exceed the maximum time remaining no delay
	 * is executed.
	 *
	 * @return The abort time rule flag.
	 */
	boolean applyTimerRule();
}
