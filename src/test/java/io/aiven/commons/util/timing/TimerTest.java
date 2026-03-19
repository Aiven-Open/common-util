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

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.awaitility.Awaitility.await;

public class TimerTest {
	@Test
	void timerTest() {
		final Timer timer = new Timer(Duration.ofSeconds(1));
		assertThat(timer.millisecondsRemaining()).isEqualTo(Duration.ofSeconds(1).toMillis());
		timer.start();
		await().atMost(Duration.ofSeconds(2)).until(timer::isExpired);
		assertThat(timer.millisecondsRemaining()).isLessThan(0);
		timer.stop();
		assertThat(timer.millisecondsRemaining()).isEqualTo(Duration.ofSeconds(1).toMillis());
	}

	@Test
	void timerSequenceTest() {
		final Timer timer = new Timer(Duration.ofSeconds(1));
		// stopped state does not allow stop
		assertThatExceptionOfType(IllegalStateException.class).as("stop while not running").isThrownBy(timer::stop)
				.withMessageStartingWith("Timer: ");
		timer.reset(); // verify that an exception is not thrown.

		// started state does not allow start
		timer.start();
		assertThatExceptionOfType(IllegalStateException.class).as("start while running").isThrownBy(timer::start)
				.withMessageStartingWith("Timer: ");
		timer.reset();
		timer.start(); // restart the timer.
		timer.stop();

		// stopped state does not allow stop or start
		assertThatExceptionOfType(IllegalStateException.class).as("stop after stop").isThrownBy(timer::stop)
				.withMessageStartingWith("Timer: ");
		assertThatExceptionOfType(IllegalStateException.class).as("start after stop").isThrownBy(timer::start)
				.withMessageStartingWith("Timer: ");
		timer.reset();

		// stopped + reset does not allow stop.
		assertThatExceptionOfType(IllegalStateException.class).as("stop after reset (1)").isThrownBy(timer::stop)
				.withMessageStartingWith("Timer: ");
		timer.start();
		timer.reset();

		// started + reset does not allow stop;
		assertThatExceptionOfType(IllegalStateException.class).as("stop after reset (2)").isThrownBy(timer::stop)
				.withMessageStartingWith("Timer: ");
	}
}
