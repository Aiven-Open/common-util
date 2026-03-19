Aiven Commons
======================

[![Deployment checks](https://github.com/Aiven-Open/aiven-commons/actions/workflows/Merge_check.yml/badge.svg)](https://github.com/Aiven-Open/aiven-commons/actions/workflows/Merge_check.yml)
[![Build and deploy site](https://github.com/Aiven-Open/aiven-commons/actions/workflows/build_site.yml/badge.svg)](https://github.com/Aiven-Open/aiven-commons/actions/workflows/build_site.yml)

Is a collection of java utilities used in Aiven development

[Documentation](https://aiven-open.github.io/aiven-commons/) is available.

Overview
========

Aiven Commons is a collection of individual utility jars (modules) that can be built and versioned independently.
All releases are deployed to the maven repository.

The parent pom contains performs all the dependency and plugin management.  It is not semantically versioned and uses simple monotonically increasing integer values.


Features
============

 * Each module is independently versioned using semantic versioning.
 * Each module, except where noted, has no external dependencies outside of Java classes.
 * Where external dependencies required, they are noted but not included in the jar.  For example the `velocity-utils` module depends on Apache Velocity, but does not include any of the velocity packages.

Setup
============

This project is a maven project, standard maven targets are available.  Each module is a separate project and builds by running `mvn package` within the module directory.

To build the entire suite at once execute `mvn -P ci clean install`.

License
============
[comment]: <> (SPDX-License-Identifier: Apache-2.0)
Aiven Commons is licensed under the Apache license, version 2.0. Full license text is available in the [LICENSE](LICENSE) file.

Please note that the project explicitly does not require a CLA (Contributor License Agreement) from its contributors.

Contact
============
Bug reports and patches are very welcome, please post them as GitHub issues and pull requests at https://github.com/aiven/aiven-commons . 
To report any possible vulnerabilities or other serious issues please see our [security](SECURITY.md) policy.
