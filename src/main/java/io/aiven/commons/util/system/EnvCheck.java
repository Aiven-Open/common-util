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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks URLs against the URLs allowed as per the {@code
 * org.apache.kafka.sasl.oauthbearer.allowed.urls} environment variable. If the variable is not set
 * all URLs are allowed.
 */
public final class EnvCheck {
  /** The type of object being checked. */
  public enum Type {
    /** validate a file name is in the list of files */
    FILE("io.aiven.commons.envcheck.files"),
    /** validate a URI is in the list of URIs */
    URI("io.aiven.commons.envcheck.uri"),
    /** validate an external command is in the list of commands */
    CMD("io.aiven.commons.envcheck.cmd");

    /** The environment variable name for this type. */
    private String envVar;

    /**
     * Create a Type.
     *
     * @param envVar the name of the environment variable for this type.
     */
    Type(String envVar) {
      this.envVar = envVar;
    }

    /**
     * Get the environment variable name for this type.
     *
     * @return the Environment variable name for this type./
     */
    public String envVar() {
      return envVar;
    }
  }

  private EnvCheck() {
    // do not instantiate
  }

  /**
   * Format the error message for the specified URL.
   *
   * @param type the type check that failed
   * @param value the value to create an error message for.
   * @return the error message.
   */
  public static String formatError(Type type, String value) {
    return String.format(
        "%1$s is not an allowed %2$s value. Update system property '%3$s' to allow %1$s",
        value, type, type.envVar);
  }

  /**
   * Checks if the URL is allowed as per the {@code org.apache.kafka.sasl.oauthbearer.allowed.urls}
   * environment variable.
   *
   * @param type the type to check.
   * @param value the value to check.
   * @return {@code true} if the value is listed in the environment variable for the type, {@code
   *     false} if the environment variable is not set or the value is not listed.
   */
  public static boolean allowed(Type type, String value) {
    String allowedUrlsProp = System.getenv(type.envVar);
    if (allowedUrlsProp == null) {
      return false;
    }
    Set<String> allowedList =
        Arrays.stream(allowedUrlsProp.split(",")).map(String::trim).collect(Collectors.toSet());
    return allowedList.contains(value);
  }

  /**
   * Thorw an exception if the specified URL is not listed in the {@code
   * org.apache.kafka.sasl.oauthbearer.allowed.urls} environment variable.
   *
   * @param type the type to check.
   * @param value the value to check
   * @throws IllegalArgumentException if the {@link #allowed(Type, String)} returns false.
   */
  public static void throwIfNotAllowed(Type type, String value) throws IllegalArgumentException {
    if (!allowed(type, value)) {
      throw new IllegalArgumentException(formatError(type, value));
    }
  }

  /**
   * Return an updatable version of the environment variables. Changes to the map will be reflected
   * in the system environment variables.
   *
   * <p>This method will only work if the command line options {@code --add-opens
   * java.base/java.util=io.aiven.commons.system} and {@code --add-opens
   * java.base/java.lang=io.aiven.commons.system} are used.
   *
   * <p>Changes made to the returned map change the environment variables. This is handy for testing
   * but should be done in a {@code try-catch-finally} block with the values being reset in the
   * finally block.
   *
   * <p>To remove an environment variable use the {@link Map#remove(Object)} method. Setting the map
   * value to {@code null} will throw an exception.
   *
   * @return a modifiable map backed by the system environment variables.
   * @throws NoSuchFieldException Should not be thrown. Indicates a change to the {@code
   *     Collections.UnmodifiableMap} implementation.
   * @throws IllegalAccessException if the command line options were not specified when the JVM was
   *     loaded.
   */
  public static Map<String, String> getEnvVars()
      throws NoSuchFieldException, IllegalAccessException {
    Class<?> classOfMap = System.getenv().getClass();
    // The field m inside the UnmodifiableMap wrapper object is a mutable Map
    Field field = classOfMap.getDeclaredField("m");
    field.setAccessible(true);
    return (Map<String, String>) field.get(System.getenv());
  }
}
