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

The Site Plugin has had a couple of upgrades that require you to adjust your environment, documents or configuration. Below is a list of key changes and what you need to be aware of: see the [history](./history.html) for the versions of the internal components.

## From 3\.22\.x to 3\.23\.0

- The plugin requires Maven 3\.9\.0 or later; 3\.22\.0 ran on Maven 3\.6\.3. Java 8 is still enough.
- [`site:run`](./run-mojo.html) serves the site with the HTTP server built into the JDK instead of Jetty 9, which is no longer maintained. The `host`, `port` and `tempWebappDirectory` parameters are unchanged; Jetty is no longer on the plugin class path, so remove any Jetty dependency or configuration you added for `site:run`. See [Configuring Site Run](./examples/siterun.html).
- Properties passed on the command line with `-D` are available when the site descriptor is interpolated: with `<item name="Built with ${my.flag}" .../>` in `site.xml`, `mvn site -Dmy.flag=CI` renders `Built with CI`. Run `mvn clean site` after changing them, since unchanged documents are not rendered again.

## From 3\.12\.x to 3\.20\.0

Version 3\.20\.0 moves the plugin onto the [Doxia 2\.0\.0 stack](/doxia/) and Maven Reporting API 4\.0\.0. It is the Doxia 2 line for Maven 3\.x, and the gap after 3\.12\.x is deliberate: 4\.0\.0 is reserved for Maven 4.

- Reports are loaded through Maven Reporting API 4\.0\.0, so report plugins built against Maven Reporting API 3\.x no longer run. Upgrade every report plugin along with the Site Plugin.
- A skin is mandatory. The site descriptor no longer carries a built-in default skin, and the build fails with `No skin is declared in the site descriptor` when none is found. Projects inheriting from the [Apache Parent POM](/pom/asf/) pick up [Maven Fluido Skin](/skins/maven-fluido-skin/) through the inherited site descriptor, so if that failure appears, first check that the parent site descriptor is resolvable.
- The site descriptor has a second model version, with `site` as its root element and the `http://maven.apache.org/SITE/2.0.0` namespace, described in the [site descriptor reference](/doxia/doxia-sitetools/doxia-site-model/site.html). Descriptors written against the v1 model are still read and converted on the fly; [convert them](/doxia/doxia-sitetools/doxia-site-model/convert-to-sitedescriptor-2.x.html) when convenient.
- The default locale is the root locale, written `default` in the `locales` parameter. When you render several locales, list `default` among them, otherwise nothing is generated at the root of the output directory.
- Documents are rendered as XHTML5, and section titles start at `h1` where they previously started at `h2`. Skins and stylesheets that select on heading level need to be adjusted.
- Site descriptors cached in your local repository by earlier runs can be 0-byte markers that shadow the remote ones. Delete them once: `find ~/.m2/repository -name \*site\*.xml -size 0 -delete`

## From 3\.4 to 3\.5\.1

- Site Decoration Model 1\.7\.0 has changed type for `head` and `footer` from `DOM` to `String`: if your `site.xml` (or one parent) contains XML content, you&apos;ll need to escape it, usually by adding `<![CDATA[` and `]]>` around the content:

    ```xml
    <project xmlns="http://maven.apache.org/DECORATION/1.7.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/DECORATION/1.7.0 https://maven.apache.org/xsd/decoration-1.7.0.xsd">
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

- Interpolation of `${project.*}` and `${*}` expressions in `site.xml` is done _after_ inheritance, consistent with the equivalent feature in Maven `pom.xml`. For `site.xml`, this may lead to failures on URLs (often on `${project.url}` expression) that are expected to be rebased during inheritance. Use `${this.*}` expressions, such as `${this.url}` or `${this.customProperty}`, for _early interpolation_ before inheritance (see the [Doxia Integration Tools reference documentation](/doxia/doxia-sitetools/doxia-integration-tools/index.html)).
- Velocity was upgraded from 1\.5 to 1\.7, which changed the escaping rules in `.vm` documents and skins. Current versions use Velocity 2, see the [Velocity 2\.0 upgrade notes](https://velocity.apache.org/engine/2.0/upgrading.html).

<a id="From_2.x_to_3.x"></a>
<a id="From_2.2.x_to_2.3.x"></a>
<a id="From_2.1.x_to_2.2.x"></a>
<a id="From_2.0.x_to_2.1.x"></a>

## From 2\.x

Site Plugin 2\.x ran on Maven 2 and Doxia 1\.1. Upgrade directly to the current version: follow the notes above from 3\.4 on, and run `site:site` before `site:stage` or `site:stage-deploy`, which do not generate the site themselves.
