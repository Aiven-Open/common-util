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
 *
 *        SPDX-License-Identifier: Apache-2
 */

package io.aiven.commons.util.strings;

import java.io.InputStream;
import java.util.Properties;

/**
 * Reads a list of versions from a configuration file. Versions may be retrieved
 * by calling {@link #of(String)}. By default, reads versions from
 * "app.properties" Recommended usage is to add a package version property in
 * the pom.xml that looks something like
 * 
 * <pre>{@code
 *          <properties>
 *              <my-groupid-and-artifactid-version>1.3.4</my-groupid-and-artifactid-version>
 *          </properties>
 *     }</pre>
 *
 * use the {@code properties-maven-plugin} to generate the properties file.
 *
 * <pre>{@code
 *     <plugin>
 *         <groupId>org.codehaus.mojo</groupId>
 *         <artifactId>properties-maven-plugin</artifactId>
 *         <version>1.3.0</version>
 *         <executions>
 *           <execution>
 *             <goals>
 *               <goal>write-project-properties</goal>
 *             </goals>
 *             <phase>generate-resources</phase>
 *             <configuration>
 *               <outputFile>${project.build.outputDirectory}/${project.groupId}/${artifactId}.properties</outputFile>
 *             </configuration>
 *           </execution>
 *         </executions>
 *       </plugin>
 *     }</pre>
 *
 * and then retrieve the code in the project as
 *
 * <pre>{@code
 * public static String VERSION = new Version("groupid/artifactid.properties").of("my-groupid-and-artifactid-version");
 * }</pre>
 *
 * If additional values from the properties are desired then creating an
 * instance of {@code Version} and calling {@link #of(String)} for the desired
 * properties will work.
 */
public final class Version {
	private static final String PROPERTIES_FILENAME = "app.properties";
	/** The recommended property name for the project.version */
	public static final String RECOMMENDED_PROPERTY = "project-version";
	private final Properties versions;
	private final String errorMsg;

	/**
	 * Default constructor. Reads "app.properties"
	 */
	public Version() {
		this(PROPERTIES_FILENAME);
	}

	/**
	 * property loader. Reads properties from the specified file which must be in
	 * the classpath.
	 * 
	 * @param propertyFileName
	 *            the name of the property file to read
	 */
	public Version(String propertyFileName) {
		versions = new Properties();
		String errMsg = null;
		try (InputStream resourceStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(propertyFileName)) {
			versions.load(resourceStream);
		} catch (final Exception e) { // NOPMD AvoidCatchingGenericException
			errMsg = String.format("Error while loading %s: %s", propertyFileName, e.getMessage());
		} finally {
			errorMsg = errMsg;
		}
	}

	/**
	 * Retrieve a string for the property as defined in the property file.
	 * 
	 * @param property
	 *            the name of the property.
	 * @return the property stirng, "unknown" if not set, or an error message if the
	 *         file was not read.
	 */
	public String of(String property) {
		if (errorMsg != null) {
			return errorMsg;
		}
		return versions.getProperty(property, "unknown");
	}
}