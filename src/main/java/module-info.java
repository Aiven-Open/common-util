/*
 * Copyright 2025 Aiven Oy and project contributors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * SPDX-License-Identifier: Apache-2
 */
/**
 * Collections module def
 */
module io.aiven.commons.util {
	exports io.aiven.commons.util.collections;
	exports io.aiven.commons.util.google.auth;
	exports io.aiven.commons.util.io.compression;
	exports io.aiven.commons.util.strings;
	exports io.aiven.commons.util.system;
	exports io.aiven.commons.util.timing;

	requires org.apache.commons.collections4;
	requires com.google.api.client;
	requires com.google.api.client.json.gson;
	requires com.github.luben.zstd_jni;
	requires org.apache.commons.io;
	requires snappy.java;
	requires org.apache.commons.text;
	requires org.apache.commons.lang3;
	requires org.slf4j;

}