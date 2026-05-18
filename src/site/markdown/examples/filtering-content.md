<!-- Licensed to the Apache Software Foundation (ASF) under one-->
<!-- or more contributor license agreements.  See the NOTICE file-->
<!-- distributed with this work for additional information-->
<!-- regarding copyright ownership.  The ASF licenses this file-->
<!-- to you under the Apache License, Version 2.0 (the-->
<!-- "License"); you may not use this file except in compliance-->
<!-- with the License.  You may obtain a copy of the License at-->
<!---->
<!--   http://www.apache.org/licenses/LICENSE-2.0-->
<!---->
<!-- Unless required by applicable law or agreed to in writing,-->
<!-- software distributed under the License is distributed on an-->
<!-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY-->
<!-- KIND, either express or implied.  See the License for the-->
<!-- specific language governing permissions and limitations-->
<!-- under the License.-->

# Overview

Although sophisticated logic inside content can be implemented with [Apache Velocity Templates](creating-content.html#templating) this has the drawback that

- not everyone is familiar with the Apache Velocity syntax
- web-based UI/preview (e.g. in GitHub) does not work
- there is conflicts between Velocity and Markdown syntax which both use `##` for different purposes and
- Velocity overhead is way too big for performing simple property interpolation

Therefore for simple property interpolation regular [Maven filtering](https://maven.apache.org/plugins/maven-resources-plugin/examples/filter.html) is often more suitable.

# Setup

## Filter prior site creation

In order to perform Maven filtering create a dedicated source directory which is processed by [`maven-resources-plugin:copy-resources`](https://maven.apache.org/plugins/maven-resources-plugin/copy-resources-mojo.html).
That should be executed prior to the site creation.
In the following example the directory `src/site` is copied with filtering to `/target/site-src`:

```xml
<project>
  ...
  <build>
    <plugins>
      <plugin>
        <!-- prepare site content by filtering ${project.*} values -->
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-resources-plugin</artifactId>
        <executions>
          <execution>
            <id>filter-site</id>
            <phase>pre-site</phase>
            <goals>
              <goal>copy-resources</goal>
            </goals>
            <configuration>
              <outputDirectory>${project.build.directory}/site-src</outputDirectory>
              <resources>
                <resource>
                  <directory>src/site</directory>
                  <filtering>true</filtering>
                </resource>
              </resources>
            </configuration>
          </execution>
        </executions>
      </plugin>
      ...
    </plugins>
    ...
  </build>
  ...
</project>
```

## Adjust site source

In order to generate the site from the filtered sources (after interpolation has been applied) adjust the [site source directory](../site-mojo.html#siteDirectory). In addition you should adjust the [site source being used for the edit buttons](../site-mojo.html#alternativeSiteSourceDirectories).

```xml
<project>
  ...
  <build>
    <plugins>
      ...
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-site-plugin</artifactId>
        <configuration>
          ...
          <siteDirectory>${project.build.directory}/site-src</siteDirectory>
          <alternativeSiteSourceDirectories>${basedir}/src/site</alternativeSiteSourceDirectories>
        </configuration>
      </plugin>
      ...
    </plugins>
    ...
  </build>
  ...
</project>
```
