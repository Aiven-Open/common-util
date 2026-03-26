package io.aiven.commons.util.google.auth;

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
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import io.aiven.commons.util.system.SystemCheck;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Validates credential information for the Google Cloud Platform. Ensures that the contents data
 * passed to the Google Cloud Platform authentication service is within reasonable limits
 */
public final class GCPValidator {

  private GCPValidator() {
    // do not instantiate.
  }

  /**
   * Validate the JSON credential information.
   *
   * @param credentialsBytes the bytes for the JSON document.
   * @throws IOException on IO error
   * @see <a href=
   *     "https://docs.cloud.google.com/docs/authentication/client-libraries#java_1">Security
   *     requirements when using credential configurations from an external source</a>
   */
  public static void validateCredentialJson(byte[] credentialsBytes) throws IOException {
    JsonObjectParser parser = new JsonObjectParser(GsonFactory.getDefaultInstance());
    GenericJson fileContents =
        parser.parseAndClose(
            new ByteArrayInputStream(credentialsBytes), StandardCharsets.UTF_8, GenericJson.class);
    if ("service_account".equals(fileContents.get("type"))) {
      // document referenced above indicates that service_account is safe.
      return;
    }
    // while the documentation referenced in the javadoc above mentions 'token_url'
    // several of the builders use 'token_uri' instead.
    if (fileContents.containsKey("token_url")) {
      String urlStr = fileContents.get("token_url").toString();
      if (!urlStr.equals("https://sts.googleapis.com/v1/token")) {
        throw new IllegalArgumentException(
            "token_url must have the value: 'https://sts.googleapis.com/v1/token'");
      }
    }

    if (fileContents.containsKey("service_account_impersonation_url")) {
      final String impersonationUrl =
          fileContents.get("service_account_impersonation_url").toString();
      if (!(impersonationUrl.startsWith(
              "https://iamcredentials.googleapis.com/v1/projects/-/serviceAccounts/")
          && impersonationUrl.endsWith(":generateAccessToken"))) {
        throw new IllegalArgumentException(
            String.format(
                "'%s' is not an allowed value for service_account_impersonation_url",
                impersonationUrl));
      }
    }

    if (fileContents.containsKey("credential_source")) {
      Map<String, Object> credSource = (Map<String, Object>) fileContents.get("credential_source");
      if (credSource.containsKey("file")) {
        SystemCheck.throwIfNotAllowed(SystemCheck.Type.FILE, credSource.get("file").toString());
      }
      if (credSource.containsKey("url")) {
        SystemCheck.throwIfNotAllowed(SystemCheck.Type.URI, credSource.get("url").toString());
      }
      if (credSource.containsKey("executable")) {
        Map<String, Object> executable = (Map<String, Object>) credSource.get("executable");
        if (executable.containsKey("command")) {
          SystemCheck.throwIfNotAllowed(SystemCheck.Type.CMD, executable.get("command").toString());
        }
      }
      if (credSource.containsKey("aws")) {
        Map<String, Object> aws = (Map<String, Object>) credSource.get("aws");
        if (aws.containsKey("url")) {
          final List<String> validAwsUrls =
              Arrays.asList(
                  "https://169.254.169.254/latest/meta-data/iam/security-credentials",
                  "https://[fd00:ec2::254]/latest/meta-data/iam/security-credentials");
          String awsUrl = aws.get("url").toString();
          if (!validAwsUrls.contains(awsUrl)) {
            throw new IllegalArgumentException(
                "credential_source.aws.url must be one of '"
                    + String.join("', '", validAwsUrls)
                    + "'");
          }
        }
        if (aws.containsKey("region_url")) {
          final List<String> validAwsRegionUrls =
              Arrays.asList(
                  "http://169.254.169.254/latest/meta-data/placement/availability-zone",
                  "http://[fd00:ec2::254]/latest/meta-data/placement/availability-zone");
          String awsUrl = aws.get("region_url").toString();
          if (!validAwsRegionUrls.contains(awsUrl)) {
            throw new IllegalArgumentException(
                "credential_source.aws.region_url must be one of '"
                    + String.join("', '", validAwsRegionUrls)
                    + "'");
          }
        }
        if (aws.containsKey("imdsv2_session_token_url")) {
          final List<String> tokenUrls =
              Arrays.asList(
                  "http://169.254.169.254/latest/api/token",
                  "http://[fd00:ec2::254]/latest/api/token");
          String awsUrl = aws.get("imdsv2_session_token_url").toString();
          if (!tokenUrls.contains(awsUrl)) {
            throw new IllegalArgumentException(
                "credential_source.aws.imdsv2_session_token_url must be one of '"
                    + String.join("', '", tokenUrls)
                    + "'");
          }
        }
      }
    }
  }
}
