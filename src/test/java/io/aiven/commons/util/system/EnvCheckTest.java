package io.aiven.commons.util.system;
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

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class EnvCheckTest {

	@Test
	void testCredentialSourceUrl() throws IOException, NoSuchFieldException, IllegalAccessException {

		final String testURL = "https://example.com/badURL";

		Map<String, String> envVar = EnvCheck.getEnvVars();
		String oldValue = envVar.get(EnvCheck.Type.URI.envVar());
		try {
			envVar.remove(EnvCheck.Type.URI.envVar());
			assertThat(EnvCheck.allowed(EnvCheck.Type.URI, testURL)).as("test against null env value").isFalse();

			envVar.put(EnvCheck.Type.URI.envVar(), testURL + "/andSome");
			assertThat(EnvCheck.allowed(EnvCheck.Type.URI, testURL)).as("test with incorrect value").isFalse();

			envVar.put(EnvCheck.Type.URI.envVar(), testURL);
			assertThat(EnvCheck.allowed(EnvCheck.Type.URI, testURL)).as("test with correct value").isTrue();

		} finally {
			if (oldValue != null) {
				envVar.put(EnvCheck.Type.URI.envVar(), oldValue);
			} else {
				envVar.remove(EnvCheck.Type.URI.envVar());
			}
		}
	}

	@Test
	void testCredentialSourceFile() throws IOException, NoSuchFieldException, IllegalAccessException {
		final String testFile = "/tmp/example/badFile";

		Map<String, String> envVar = EnvCheck.getEnvVars();
		String oldValue = envVar.get(EnvCheck.Type.FILE.envVar());
		try {
			envVar.remove(EnvCheck.Type.FILE.envVar());
			assertThat(EnvCheck.allowed(EnvCheck.Type.FILE, testFile)).as("test against null env value").isFalse();

			envVar.put(EnvCheck.Type.FILE.envVar(), testFile + "/andSome");
			assertThat(EnvCheck.allowed(EnvCheck.Type.FILE, testFile)).as("test with incorrect value").isFalse();

			envVar.put(EnvCheck.Type.FILE.envVar(), testFile);
			assertThat(EnvCheck.allowed(EnvCheck.Type.FILE, testFile)).as("test with correct value").isTrue();

		} finally {
			if (oldValue != null) {
				envVar.put(EnvCheck.Type.FILE.envVar(), oldValue);
			} else {
				envVar.remove(EnvCheck.Type.FILE.envVar());
			}
		}
	}

	@Test
	void testCredentialSourceCommand() throws IOException, NoSuchFieldException, IllegalAccessException {
		final String testCmd = "/my/badCommand";

		Map<String, String> envVar = EnvCheck.getEnvVars();
		String oldValue = envVar.get(EnvCheck.Type.CMD.envVar());
		try {
			envVar.remove(EnvCheck.Type.CMD.envVar());
			assertThat(EnvCheck.allowed(EnvCheck.Type.CMD, testCmd)).as("test against null env value").isFalse();

			envVar.put(EnvCheck.Type.CMD.envVar(), testCmd + "/andSome");
			assertThat(EnvCheck.allowed(EnvCheck.Type.CMD, testCmd)).as("test with incorrect value").isFalse();

			envVar.put(EnvCheck.Type.CMD.envVar(), "/my/badCommand");
			assertThat(EnvCheck.allowed(EnvCheck.Type.CMD, testCmd)).as("test with correct value").isTrue();
		} finally {
			if (oldValue != null) {
				envVar.put(EnvCheck.Type.CMD.envVar(), oldValue);
			} else {
				envVar.remove(EnvCheck.Type.CMD.envVar());
			}
		}
	}
}
