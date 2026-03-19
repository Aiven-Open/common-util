package io.aiven.commons.util.system;
/*
         Copyright 2026 Aiven Oy and project contributors

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

import static org.assertj.core.api.Assertions.assertThat;

public class SystemCheckTest {

	@Test
	void testCredentialSourceUrl() throws IOException, NoSuchFieldException, IllegalAccessException {

		final String testURL = "https://example.com/badURL";

		String oldValue = System.getProperties().getProperty(SystemCheck.Type.URI.getSystemProperty());
		try {
			System.getProperties().remove(SystemCheck.Type.URI.getSystemProperty());
			assertThat(SystemCheck.allowed(SystemCheck.Type.URI, testURL)).as("test against null env value").isFalse();

			System.getProperties().put(SystemCheck.Type.URI.getSystemProperty(), testURL + "/andSome");
			assertThat(SystemCheck.allowed(SystemCheck.Type.URI, testURL)).as("test with incorrect value").isFalse();

			System.getProperties().put(SystemCheck.Type.URI.getSystemProperty(), testURL);
			assertThat(SystemCheck.allowed(SystemCheck.Type.URI, testURL)).as("test with correct value").isTrue();

		} finally {
			if (oldValue != null) {
				System.getProperties().put(SystemCheck.Type.URI.getSystemProperty(), oldValue);
			} else {
				System.getProperties().remove(SystemCheck.Type.URI.getSystemProperty());
			}
		}
	}

	@Test
	void testCredentialSourceFile() throws IOException, NoSuchFieldException, IllegalAccessException {
		final String testFile = "/tmp/example/badFile";

		String oldValue = System.getProperties().getProperty(SystemCheck.Type.FILE.getSystemProperty());
		try {
			System.getProperties().remove(SystemCheck.Type.FILE.getSystemProperty());
			assertThat(SystemCheck.allowed(SystemCheck.Type.FILE, testFile)).as("test against null env value")
					.isFalse();

			System.getProperties().put(SystemCheck.Type.FILE.getSystemProperty(), testFile + "/andSome");
			assertThat(SystemCheck.allowed(SystemCheck.Type.FILE, testFile)).as("test with incorrect value").isFalse();

			System.getProperties().put(SystemCheck.Type.FILE.getSystemProperty(), testFile);
			assertThat(SystemCheck.allowed(SystemCheck.Type.FILE, testFile)).as("test with correct value").isTrue();

		} finally {
			if (oldValue != null) {
				System.getProperties().put(SystemCheck.Type.FILE.getSystemProperty(), oldValue);
			} else {
				System.getProperties().remove(SystemCheck.Type.FILE.getSystemProperty());
			}
		}
	}

	@Test
	void testCredentialSourceCommand() throws IOException, NoSuchFieldException, IllegalAccessException {
		final String testCmd = "/my/badCommand";

		String oldValue = System.getProperties().getProperty(SystemCheck.Type.CMD.getSystemProperty());
		try {
			System.getProperties().remove(SystemCheck.Type.CMD.getSystemProperty());
			assertThat(SystemCheck.allowed(SystemCheck.Type.CMD, testCmd)).as("test against null env value").isFalse();

			System.getProperties().put(SystemCheck.Type.CMD.getSystemProperty(), testCmd + "/andSome");
			assertThat(SystemCheck.allowed(SystemCheck.Type.CMD, testCmd)).as("test with incorrect value").isFalse();

			System.getProperties().put(SystemCheck.Type.CMD.getSystemProperty(), "/my/badCommand");
			assertThat(SystemCheck.allowed(SystemCheck.Type.CMD, testCmd)).as("test with correct value").isTrue();
		} finally {
			if (oldValue != null) {
				System.getProperties().put(SystemCheck.Type.CMD.getSystemProperty(), oldValue);
			} else {
				System.getProperties().remove(SystemCheck.Type.CMD.getSystemProperty());
			}
		}
	}
}
