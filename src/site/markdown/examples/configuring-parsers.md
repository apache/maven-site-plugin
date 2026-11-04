---
title: Configuring Parsers
date: 2026-11-03
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

# Configuring Parsers

The Site Plugin renders the source files of your site with the [Doxia](https://maven.apache.org/doxia/) parser matching the file format. Use the `parserConfigurations` parameter to set properties of these parsers, for all files of a format or only for files at a given path.

The parameter lists `configuration` elements below the id of the parser (`markdown`, `xdoc`, `apt` and so on). Each element sets properties of the parser, for example `emitComments` or `emitAnchorsForIndexableEntries`, and optionally restricts them to `patterns`:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-site-plugin</artifactId>
  <configuration>
    <parserConfigurations>
      <markdown>
        <configuration>
          <patterns>
            <pattern>glob:**/markdown/drafts/*.md</pattern>
          </patterns>
          <emitComments>true</emitComments>
        </configuration>
      </markdown>
      <xdoc>
        <configuration>
          <emitComments>true</emitComments>
        </configuration>
      </xdoc>
    </parserConfigurations>
  </configuration>
</plugin>
```

In this example comments in Markdown files below a `drafts` directory and in all XDoc files end up in the generated HTML. Other Markdown files keep the default of the Site Plugin and drop their comments.

A pattern starts with `glob:` or `regex:` and is matched against the absolute path of the source file. A `glob:` pattern separates directories with `/` on every platform. A `regex:` pattern sees the platform's own separator, so a pattern that has to work on Windows too accepts both, such as `regex:.*[/\\]other[/\\].*\.md`. The first `configuration` for the parser id with a matching pattern, or without patterns, is applied.
