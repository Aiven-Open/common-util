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
 * A functional interface that will abort the timer. After being called timer will indicate that it
 * is expired, until it is reset.
 */
@FunctionalInterface
public interface AbortTrigger {
  /** Aborts the associated trigger. */
  void apply();
}
