package io.aiven.commons.util.collections;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class RingBufferTest {

	public static final String OBJECT_KEY = "S3ObjectKey";

	@ParameterizedTest
	@CsvSource({"2", "10", "19", "24"})
	void testRingBufferReturnsOldestEntryAndRemovesOldestEntry(final int size) {
		String head = OBJECT_KEY + 0;
		String newEntry;

		final RingBuffer<String> buffer = new RingBuffer<>(size);
		for (int i = 0; i < size; i++) {
			newEntry = OBJECT_KEY + i;
			buffer.add(newEntry);
			assertThat(buffer.head()).isEqualTo(head);
			assertThat(buffer.tail()).isEqualTo(newEntry);
			if (i < size - 1) {
				assertThat(buffer.getNextEjected()).isNull();
			} else {
				assertThat(buffer.getNextEjected()).isEqualTo(head);
			}
		}
		// Add one more unique ObjectKey
		buffer.add(OBJECT_KEY);
		String expectedEntry = OBJECT_KEY + 1;
		assertThat(buffer.head()).isEqualTo(expectedEntry);
		assertThat(buffer.getNextEjected()).isEqualTo(expectedEntry);
		assertThat(buffer.tail()).isEqualTo(OBJECT_KEY);
	}

	@ParameterizedTest
	@CsvSource({"2", "10", "19", "24"})
	void testRingBufferOnlyAddsEachItemOnce(final int size) {
		final RingBuffer<String> buffer = new RingBuffer<>(size);
		for (int i = 0; i < size; i++) {
			// add the same objectKey every time, it should onl have one entry.
			buffer.add(OBJECT_KEY);
		}
		// Buffer not filled so should return null
		assertThat(buffer.getNextEjected()).isEqualTo(null);
		assertThat(buffer.head()).isEqualTo(OBJECT_KEY);
		assertThat(buffer.contains(OBJECT_KEY)).isTrue();
		assertThat(buffer.tail()).isEqualTo(OBJECT_KEY);
	}

	@Test
	void testRingBufferOfSizeOneOnlyRetainsOneEntry() {
		final RingBuffer<String> buffer = new RingBuffer<>(1);
		buffer.add(OBJECT_KEY + 0);
		assertThat(buffer.getNextEjected()).isEqualTo(OBJECT_KEY + 0);
		buffer.add(OBJECT_KEY + 1);
		assertThat(buffer.getNextEjected()).isEqualTo(OBJECT_KEY + 1);
	}

	@ParameterizedTest
	@CsvSource({"2", "10", "19", "24"})
	void testRingBufferWithAllowDuplicatesAddsItemsCorrectly(final int size) {
		final RingBuffer<String> buffer = new RingBuffer<>(size, RingBuffer.DuplicateHandling.ALLOW);
		for (int i = 0; i < size; i++) {
			// add the same objectKey every time.
			buffer.add(OBJECT_KEY);
		}
		// Buffer not filled so should return null
		assertThat(buffer.getNextEjected()).isEqualTo(OBJECT_KEY);
		assertThat(buffer.head()).isEqualTo(OBJECT_KEY);
		assertThat(buffer.contains(OBJECT_KEY)).isTrue();
		assertThat(buffer.tail()).isEqualTo(OBJECT_KEY);
		String nextKey = OBJECT_KEY + 1;
		for (int i = 0; i < size - 1; i++) {
			assertThat(buffer.add(nextKey)).isEqualTo(OBJECT_KEY);
			assertThat(buffer.getNextEjected()).isEqualTo(OBJECT_KEY);
			assertThat(buffer.head()).isEqualTo(OBJECT_KEY);
			assertThat(buffer.contains(OBJECT_KEY)).isTrue();
			assertThat(buffer.tail()).isEqualTo(nextKey);
		}
		assertThat(buffer.add(nextKey)).isEqualTo(OBJECT_KEY);
		assertThat(buffer.getNextEjected()).isEqualTo(nextKey);
		assertThat(buffer.head()).isEqualTo(nextKey);
		assertThat(buffer.contains(OBJECT_KEY)).isFalse();
		assertThat(buffer.contains(nextKey)).isTrue();
		assertThat(buffer.tail()).isEqualTo(nextKey);
	}

	@ParameterizedTest
	@CsvSource({"2", "10", "19", "24"})
	void testRingBufferWithDeleteDuplicatesOrdersItemsCorrectly(final int size) {
		final RingBuffer<String> buffer = new RingBuffer<>(size, RingBuffer.DuplicateHandling.DELETE);
		String head = OBJECT_KEY + 0;
		String tail = OBJECT_KEY + (size - 1);
		String duplicate = OBJECT_KEY + ((size - 1) / 2);
		for (int i = 0; i < size; i++) {
			buffer.add(OBJECT_KEY + i);
		}

		// Buffer not filled so should return null
		assertThat(buffer.getNextEjected()).isEqualTo(head);
		assertThat(buffer.head()).isEqualTo(head);
		assertThat(buffer.contains(duplicate)).isTrue();
		assertThat(buffer.tail()).isEqualTo(tail);

		// no ejected entry.
		assertThat(buffer.add(duplicate)).isNull();
		// duplicate now at end
		assertThat(buffer.tail()).isEqualTo(duplicate);
		// assert that all the orignal items are there.
		for (int i = 0; i < size; i++) {
			assertThat(buffer.contains(OBJECT_KEY + i)).isTrue();
		}
	}

	@ParameterizedTest
	@EnumSource(RingBuffer.DuplicateHandling.class)
	void testRingBufferDelete(RingBuffer.DuplicateHandling handling) {
		final String head = "head";
		final String middle = "middle";
		final String tail = "tail";
		final RingBuffer<String> buffer = new RingBuffer<>(3, handling);
		buffer.add(head);
		buffer.add(middle);
		buffer.add(tail);
		assertThat(buffer.head()).isEqualTo(head);
		assertThat(buffer.contains(middle)).isTrue();
		assertThat(buffer.tail()).isEqualTo(tail);

		buffer.remove("notPresent");

		assertThat(buffer.head()).isEqualTo(head);
		assertThat(buffer.contains(middle)).isTrue();
		assertThat(buffer.tail()).isEqualTo(tail);

		buffer.remove(middle);
		assertThat(buffer.head()).isEqualTo(head);
		assertThat(buffer.contains(middle)).isFalse();
		assertThat(buffer.tail()).isEqualTo(tail);
	}
}
