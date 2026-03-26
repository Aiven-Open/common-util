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

import java.time.Duration;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs a delay based on the number of successive {@link #delay()} or {@link #cleanDelay()}
 * calls without a {@link #reset()}. Delay increases exponentially but never exceeds the time
 * remaining by more than 0.512 seconds.
 *
 * <p>There are several use cases:
 *
 * <p>Case 1: There is a hard limit to the total amount of time spent waiting and processing.
 *
 * <p>In this case a {@link Timer} is initialized to the maximum time that the processing may take.
 * The timer will specify the amount of time that remains for the process. During processing
 *
 * <ul>
 *   <li>As long as the timer returns a value > 0 the backoff may sleep.
 *   <li>If the backoff sleep time is greater than the time remaining the timer is aborted and
 *       backoff does not sleep. In this use case the {@link Timer#isExpired()} method should be
 *       checked after backoff returns to see if the timer has expired or been aborted.
 * </ul>
 *
 * Case 2: Delays of up to X milliseconds are desired. After X is reached subsequent delays should
 * be for X milliseconds until the backoff is reset.
 *
 * <p>In this case the {@link BackoffConfig#getSupplierOfTimeRemaining()} should return X on every
 * call and the {@link BackoffConfig#applyTimerRule()} should return false. During processing
 * Backoff will always sleep, except in very early backoff calls where jitter overcomes the delay.
 *
 * <p>Case 3: An action should be taken as many times as possible within the allowed time.
 *
 * <p>A timer is set and used to specify the time remaining as in Case 1 above. When the action is
 * successful the {@link #reset()} method is called. This resets the internal max delay to the
 * current limit specified by the timer.
 */
public class Backoff {
  /** The log(2) */
  private static final double LOG10_OF_2 = Math.log10(2);

  /** The logger to write to */
  private static final Logger LOGGER = LoggerFactory.getLogger(Backoff.class);

  /** The maximum jitter random number. Should be a power of 2 for speed. */
  public static final int MAX_JITTER = 1024;

  /** The value subtracted from the jitter to center it. */
  public static final int JITTER_SUBTRAHEND = MAX_JITTER / 2;

  /** A supplier of the time remaining (in milliseconds) on the overriding timer. */
  protected final SupplierOfLong timeRemaining;

  /** A function to call to abort the timer. */
  protected final AbortTrigger abortTrigger;

  /** The maximum number of times {@link #delay()} will be called before maxWait is reached. */
  private int maxCount;

  /** The number of times {@link #delay()} has been called. */
  private int waitCount;

  /** The minimum legal value for the wait count */
  private int minWaitCount;

  /**
   * If true then when wait count is exceeded {@link #delay()} automatically returns without delay.
   */
  private final boolean applyTimerRule;

  /** A random number generator to construct jitter. */
  Random random = new Random();

  /**
   * Constructor.
   *
   * @param config The configuration for the backoff.
   */
  public Backoff(final BackoffConfig config) {
    this.timeRemaining = config.getSupplierOfTimeRemaining();
    this.abortTrigger = config.getAbortTrigger();
    this.applyTimerRule = config.applyTimerRule();
    this.minWaitCount = 0;
    reset();
  }

  /** Reset the backoff time so that delay is again at the minimum. */
  public final void reset() {
    // if the remaining time is 0 or negative the maxCount will be infinity
    // so make sure that it is 0 in that case.
    final long remainingTime = timeRemaining.get();
    maxCount = remainingTime < 1L ? 0 : (int) (Math.log10(remainingTime) / LOG10_OF_2);
    waitCount = minWaitCount;

    LOGGER.debug("Reset {}", this);
  }

  /**
   * Set the minimum wait time. Actual delay will be the closest power of 2 such that {@code 2^x >=
   * duration}.
   *
   * @param duration the minimum wait time.
   */
  public void setMinimumDelay(Duration duration) {
    if (duration != null && duration.toMillis() > 0) {
      minWaitCount = (int) Math.ceil(Math.log10(duration.toMillis()) / LOG10_OF_2);
      if (waitCount < minWaitCount) {
        waitCount = minWaitCount;
      }
    }
  }

  /**
   * Handle adjustment when maxCount could not be set.
   *
   * @return the corrected maxCount
   */
  private int getMaxCount() {
    if (maxCount == 0) {
      reset();
    }
    return maxCount;
  }

  /**
   * Calculates the delay without jitter.
   *
   * @return the number of milliseconds the delay will be.
   */
  public long estimatedDelay() {
    long sleepTime = timeRemaining.get();
    if (sleepTime > 0 && (!applyTimerRule || waitCount < maxCount)) {
      sleepTime = (long) Math.min(sleepTime, Math.pow(2, waitCount + 1));
    }
    return sleepTime < 0 ? 0 : sleepTime;
  }

  /**
   * Calculates the range of jitter in milliseconds.
   *
   * @return the maximum jitter in milliseconds. jitter is +/- maximum jitter.
   */
  public int getMaxJitter() {
    return MAX_JITTER - JITTER_SUBTRAHEND;
  }

  private long timeWithJitter() {
    // generate approx +/- 0.512 seconds of jitter
    final int jitter = random.nextInt(MAX_JITTER) - JITTER_SUBTRAHEND;
    return (long) Math.pow(2, waitCount) + jitter;
  }

  /**
   * If {@link #applyTimerRule} is true then this method will return false if the wait count has
   * exceeded the maximum count. Otherwise, it returns true. This method also increments the wait
   * count if the wait count is less than the maximum count.
   *
   * @return true if sleep should occur.
   */
  private boolean shouldSleep(final long sleepTime) {
    // maxCount may have been reset so check and set if necessary.
    final boolean result =
        sleepTime > 0
            && (!applyTimerRule || waitCount < (maxCount == 0 ? getMaxCount() : maxCount));
    if (waitCount < maxCount) {
      waitCount++;
    }
    return result;
  }

  /**
   * Delay execution based on the number of times this method has been called.
   *
   * @throws InterruptedException If any thread interrupts this thread.
   */
  public void delay() throws InterruptedException {
    final long sleepTime = timeRemaining.get();
    if (shouldSleep(sleepTime)) {
      final long nextSleep = timeWithJitter();
      // don't sleep negative time. Jitter can introduce negative time.
      if (nextSleep > 0) {
        if (nextSleep >= sleepTime && applyTimerRule) {
          LOGGER.debug("Backoff aborting timer");
          abortTrigger.apply();
        } else {
          LOGGER.debug("Backoff sleeping {}", nextSleep);
          Thread.sleep(nextSleep);
        }
      }
    }
  }

  /** Like {@link #delay} but swallows the {@link InterruptedException}. */
  @SuppressWarnings("PMD.EmptyCatchBlock")
  public void cleanDelay() {
    try {
      delay();
    } catch (InterruptedException expected) {
      // do nothing. We swallow the interruption so that we do not return an error.
    }
  }

  @Override
  public String toString() {
    return String.format(
        "Backoff %s/%s, %s milliseconds remaining.", waitCount, maxCount, timeRemaining.get());
  }
}
