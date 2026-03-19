/*
 * Copyright 2026 Aiven Oy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.aiven.commons.util.strings;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionTest {

	@Test
	void testHandCoded() {
		Version version = new Version("myHandCoded.properties");
		assertThat(version.of("loneliestNumber")).isEqualTo("1");
		assertThat(version.of("asBasAsOne")).isEqualTo("2");
	}

	@Test
	void testGenerated() {
		Version version = new Version("io.aiven.commons/testing.strings.properties");
		assertThat(version.of("testing.io.aiven.commons-aiven-commons-version-example")).isEqualTo("1.0.3-SNAPSHOT");
		assertThat(version.of(Version.RECOMMENDED_PROPERTY)).isNotNull();
	}

	@Test
	void testMissingFile() {
		Version version = new Version("io.aiven.commons/missing.strings.properties");
		assertThat(version.of("testing.io.aiven.commons-aiven-commons-version-example")).isEqualTo(
				"Error while loading io.aiven.commons/missing.strings.properties: inStream parameter is null");
		assertThat(version.of(Version.RECOMMENDED_PROPERTY)).isNotNull();
	}
}
