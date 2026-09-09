---
title: Migrate
author: 
  - Dennis Lundberg
  - Hervé Boutemy
date: 2016-04-09
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

# Migrate

The Site Plugin has had a couple of upgrades that requires the user to make adjustments to their environment, documents or configuration. Below is a list of key changes and what updates you as a user need to be aware of: you can have a look at [history](./history.html) for precise details of internal components updates.

## From 3\.12\.x to 3\.20\.0

Version 3\.20\.0 moves the plugin onto the [Doxia 2\.0\.0 stack](/doxia/) and Maven Reporting API 4\.0\.0. It is the Doxia 2 line for Maven 3\.x, and the gap after 3\.12\.x is deliberate: 4\.0\.0 is reserved for Maven 4.

- Reports are loaded through Maven Reporting API 4\.0\.0, so report plugins built against Maven Reporting API 3\.x no longer run. Upgrade every report plugin along with the Site Plugin.
- A skin is mandatory. The site descriptor no longer carries a built-in default skin, and the build fails with `No skin is declared in the site descriptor` when none is found. Projects inheriting from the [Apache Parent POM](/pom/asf/) pick up [Maven Fluido Skin](/skins/maven-fluido-skin/) through the inherited site descriptor, so if that failure appears, first check that the parent site descriptor is resolvable.
- The site descriptor has a second model version, with `site` as its root element and the `http://maven.apache.org/SITE/2.0.0` namespace, described in the [site descriptor reference](/doxia/doxia-sitetools/doxia-site-model/site.html). Descriptors written against the v1 model are still read and converted on the fly; [convert them](/doxia/doxia-sitetools/doxia-site-model/convert-to-sitedescriptor-2.x.html) when convenient.
- The default locale is the root locale, written `default` in the `locales` parameter. When you render several locales, list `default` among them, otherwise nothing is generated at the root of the output directory.
- Documents are rendered as XHTML5, and section titles start at `h1` where they previously started at `h2`. Skins and stylesheets that select on heading level need to be adjusted.
- Site descriptors cached in your local repository by earlier runs can be 0-byte markers that shadow the remote ones. Delete them once: `find ~/.m2/repository -name \*site\*.xml -size 0 -delete`

## From 3\.4 to 3\.5\.1

- Since [Velocity](http://velocity.apache.org) has been upgraded from version 1\.5 to version 1\.7, which changes escaping rules, you may need to update escape sequences in your `.vm` documents and/or skins. If you can&apos;t update content and/or skin immediately, you can manually downgrade Velocity version by configuring a dependency to Maven Site Plugin:

    ```xml
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-site-plugin</artifactId>
          <dependencies>
            <dependency>
              <groupId>org.apache.velocity</groupId>
              <artifactId>velocity</artifactId>
              <version>1.5</version>
            </dependency>
          </dependencies>
        </plugin>
    ```

- Site Decoration Model 1\.7\.0 has changed type for `head` and `footer` from `DOM` to `String`: if your `site.xml` (or one parent) contains XML content, you&apos;ll need to escape it, usually by adding `<![CDATA[` and `]]>` around the content:

    ```xml
    <project xmlns="http://maven.apache.org/DECORATION/1.7.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/DECORATION/1.7.0 http://maven.apache.org/xsd/decoration-1.7.0.xsd">
        <body>
            <head>
                <![CDATA[<anyHeadElement/>]]>
            </head>
            <footer>
                <![CDATA[<anyFooterElement/>]]>
            </footer>
        ...
        </body>
    </project>
    ```

- Interpolation of `${project.*}` and `${*}` expressions in `site.xml` have changed to be consistent with equivalent feature in Maven `pom.xml`: interpolation is now done _after_ inheritance. For `site.xml`, this may lead to failures on urls (often on `${project.url}` expression), that are expected to be rebased during inheritance: a new _early interpolation_ feature has been added in Maven Site Plugin 3\.5\.1 throught `${this.*}` expressions (see [ Doxia Integration Tools reference documentation](/doxia/doxia-sitetools/doxia-integration-tools/index.html)). With this feature (for example `${this.url}` or `${this.customProperty}` expressions), you&apos;ll get former `site.xml` interpolation result.

## From 2\.x to 3\.x

- Version 3 of the plugin requires at least **Maven 2\.2\.0** to run.
- If you use **Maven 3** please read the [Maven 3](./maven-3.html) guide about relevant issues.

## From 2\.2\.x to 2\.3\.x

- The `site:stage` and `site:stage-deploy` goals have been decoupled from site generation. Executing these goals without generating the site first will lead to a build failure.

## From 2\.1\.x to 2\.2\.x

- The plugin now requires at least **Maven 2\.2\.0** to run, you cannot use it with older Maven versions.
- The plugin now requires at least **Java 5** to run, you cannot use it with older Java versions.

## From 2\.0\.x to 2\.1\.x

- The plugin now requires at least **Maven 2\.1** to run, you cannot use it with older Maven versions.
- The plugin has been upgraded to use **Doxia 1\.1**, which has seen a lot of major changes itself. If you experience unexpected behavior, please read these resources:
    - [Doxia: what&apos;s new in 1\.1?](/doxia/whatsnew-1.1.html)
    - [Doxia: Enhancements to the APT format](/doxia/references/doxia-apt.html)
    - [Doxia: Issues &amp; Gotchas](/doxia/issues/index.html)
    - [Doxia: Frequently Asked Questions](/doxia/faq.html)
