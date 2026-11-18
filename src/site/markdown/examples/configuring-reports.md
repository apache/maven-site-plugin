---
title: Configuring Reports
author: 
  - Dennis Lundberg
  - Hervé Boutemy
date: 2012-12-02
---

<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

# Configuring Reports

Reports are plugin goals listed in the `<reporting>` section of the POM; the Site Plugin runs them and adds their output to the site. How to add them, which standard reports the [Maven Project Info Reports Plugin](/plugins/maven-project-info-reports-plugin/) provides, and how `<menu ref="reports"/>` shows them in the site menu is described in [Configuring Reports](https://maven.apache.org/guides/mini/guide-site.html#Configuring_Reports) in the site guide. To find more report plugins, look for the value "R" in the "Type" column of the [plugins list](/plugins/).

This page covers how to choose which reports of a plugin run.

# Selecting Reports from a Plugin: Configuring Report Sets

By default, when you add a plugin to the reporting section, _every_ report goal of the plugin is rendered once.

If you want to choose only some reports from a plugin, or if you want to run a report several times with a different configuration, you need to configure report sets:

```xml
<project>
  ...
  <reporting>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-project-info-reports-plugin</artifactId>
        <version>3.9.0</version>
        <reportSets>
          <reportSet>
            <reports><!-- select reports -->
              <report>index</report>
              <report>summary</report>
            </reports>
          </reportSet>
        </reportSets>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-javadoc-plugin</artifactId>
        <version>3.12.0</version>
        <reportSets>
          <reportSet><!-- by default, id = "default" -->
            <reports><!-- select non-aggregate reports -->
              <report>javadoc</report>
              <report>test-javadoc</report>
            </reports>
          </reportSet>
          <reportSet><!-- aggregate reportSet, to define in poms having modules -->
            <id>aggregate</id>
            <inherited>false</inherited><!-- don't run aggregate in child modules -->
            <reports>
              <report>aggregate</report>
            </reports>
          </reportSet>
        </reportSets>
      </plugin>
    </plugins>
  </reporting>
  ...
</project>
```

With the first plugin above, the build log lists the selected reports:

```
[INFO] Configured 2 reports for maven-project-info-reports-plugin:3.9.0: index, summary
```

Define report sets for plugins that provide aggregate reports, such as the Maven Javadoc, JXR or Checkstyle plugins; otherwise the aggregate reports run in the root of the reactor in addition to the non-aggregate ones.

## How Report Sets Are Selected

All the report sets of a plugin are collected, a report set with `<inherited>false</inherited>` stays in the POM that declares it, and each report then decides whether it can run in the module, so a `pom` module often shows only the aggregate report. See [How Report Sets Are Selected](/guides/mini/guide-site.html#How_Report_Sets_Are_Selected) in the site guide for the rule and an example.

See the [reportSet documentation in POM reference](/ref/current/maven-model/maven.html#class_reportSet) for complete description of available configurations.
