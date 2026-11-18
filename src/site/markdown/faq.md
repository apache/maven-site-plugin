---
title: Frequently Asked Questions
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

<a id="top"></a>

# Frequently Asked Questions

**General**

1. [What is the difference between `mvn site` and `mvn site:site`?](#What_is_the_difference_between_mvn_site_and_mvn_site.3Asite)
2. [How do I integrate static (X)HTML pages into my Maven site?](#How_do_I_integrate_static_.28X.29HTML_pages_into_my_Maven_site)
3. [How to include a custom Doxia module, like AsciiDoc?](#How_to_include_a_custom_Doxia_module.2C_like_Twiki)
4. [How can I validate my xdoc/fml source files?](#Can_I_validate_xml)
5. [How does the Site Plugin use the &lt;url&gt; element in the POM?](#Use_of_url)

**Specific issues**

1. [Why do my absolute links get translated into relative links?](#Why_do_my_absolute_links_get_translated_into_relative_links)
2. [Why don't the links between parent and child modules work when I run &quot;`mvn site`&quot;?](#Why_don.27t_the_links_between_parent_and_child_modules_work_when_I_run_.22mvn_site.22.3F)
3. [Can I use entities in xdoc/fml source files?](#Can_I_use_entities)

## General

<a id="What_is_the_difference_between_mvn_site_and_mvn_site.3Asite"></a>

### What is the difference between `mvn site` and `mvn site:site`?

`mvn site` runs the `site` **phase** of the site lifecycle, which consists of the `pre-site`, `site`, `post-site` and `site-deploy` phases (see the [Lifecycle Reference](/guides/introduction/introduction-to-the-lifecycle.html#Lifecycle_Reference)). Maven runs every plugin goal bound to `pre-site` and `site`, for example a resource filtering step bound to `pre-site`.

`mvn site:site` runs only the [`site`](site-mojo.html) **goal** of the Site Plugin; goals bound to `pre-site` do not run.

<a id="How_do_I_integrate_static_.28X.29HTML_pages_into_my_Maven_site"></a>

### How do I integrate static (X)HTML pages into my Maven site?

Put the pages in `src/site/resources`; they are copied to the site unchanged. Then link to them from a menu item in your `src/site/site.xml`. See [Adding Extra Resources](https://maven.apache.org/guides/mini/guide-site.html#Adding_Extra_Resources) in the site guide.

To have a page decorated by the skin like the other pages, write it as an XHTML document in `src/site/xhtml` instead.

<!-- mention docbook explicitly so google can find this -->
<a id="How_to_include_a_custom_Doxia_module.2C_like_Twiki"></a>

### How to include a custom Doxia module, like AsciiDoc?

Out of the box, the Site Plugin reads Markdown, APT, XDoc, FML and XHTML5 documents. For any other format for which a Doxia parser exists (see the list of [Doxia Markup Languages](/doxia/references/index.html)), add the Doxia module as a dependency of the Site Plugin, as described in [Creating Content](https://maven.apache.org/guides/mini/guide-site.html#Creating_Content) in the site guide. For example, for AsciiDoc:

```xml
<project>
  ...
  <build>
    <plugins>
      ...
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-site-plugin</artifactId>
        <dependencies>
          <dependency>
            <groupId>org.asciidoctor</groupId>
            <artifactId>asciidoctor-parser-doxia-module</artifactId>
            <version><!-- a version built for Doxia 2 --></version>
          </dependency>
        </dependencies>
      </plugin>
    </plugins>
  </build>
  ...
</project>
```

The module has to be built for the Doxia version that the Site Plugin uses: Doxia 2 since Site Plugin 3.20.0, see the [history](history.html).

<a id="Can_I_validate_xml"></a>

### How can I validate my xdoc/fml source files?

Set the [`validate`](site-mojo.html#validate) parameter of `site:site`, for example with `mvn site -Dvalidate=true`. The log then shows:

```
[INFO] Validation is switched on, xml input documents will be validated!
```

Each XML document is parsed twice, once to validate it and once to render it, and any validation error fails the build. **All** XML source files then need a correct schema or DTD declaration. See the Doxia documentation on [validating xdocs](/doxia/references/xdoc-format.html#Validation), and the schema definitions for [xdoc](/doxia/doxia/doxia-modules/doxia-module-xdoc/using-xdoc-xsd.html) and [fml](/doxia/doxia/doxia-modules/doxia-module-fml/using-fml-xsd.html).

<a id="Use_of_url"></a>

### How does the Site Plugin use the &lt;url&gt; element in the POM?

The project `<url>` is where your users find the project. The Site Plugin uses it to make the absolute links of the site descriptor relative, see [the next question](#Why_do_my_absolute_links_get_translated_into_relative_links). If the project's site is not deployed anywhere, remove the `<url>` element from the POM; the log then warns that site links will not be relativized.

The site URL in `<distributionManagement>`, `<site><url>`, is where [`site:deploy`](deploy-mojo.html) copies the site, and in a multi-module build [`site:stage`](stage-mojo.html) uses it to place each module and to build the links between modules. In a multi-module build, parent and child modules need **different** site URLs; if they share one, the links within the combined site do not work. End each URL with a slash (`/`). How child modules inherit these URLs is described in [Inheritance of the Site Distribution Management](https://maven.apache.org/guides/mini/guide-multimodule-site.html#Inheritance_of_the_Site_Distribution_Management).

## Specific issues

<a id="Why_do_my_absolute_links_get_translated_into_relative_links"></a>

### Why do my absolute links get translated into relative links?

The Site Plugin makes every link of the site descriptor relative to the project `<url>` when it can. With this in the `pom.xml` of a child module:

```xml
<url>https://example.org/scratch/scratch-child/</url>
```

and this link in `site.xml`:

```xml
<links>
  <item name="Example" href="https://example.org/"/>
</links>
```

the generated page links to `../../`, because both URLs are on the same host. A link to another host stays absolute.

To keep all links as written, set the [`relativizeSiteLinks`](site-mojo.html#relativizeSiteLinks) parameter to `false`, for example with `mvn site -DrelativizeSiteLinks=false`. See [MSITE-159](https://issues.apache.org/jira/browse/MSITE-159) for the background.

### Why don't the links between parent and child modules work when I run &quot;`mvn site`&quot;?

In a multi-module build, `mvn site` generates the site of each module **individually**, in its own `target/site` directory, so the links between parent and child do not work there. They work once the site is deployed. To check them before deploying, run [`site:stage`](stage-mojo.html), as described in the [usage page](usage.html#Staging_a_Site) and in [Building multi-module sites](https://maven.apache.org/guides/mini/guide-multimodule-site.html).

<a id="Can_I_use_entities"></a>

### Can I use entities in xdoc/fml source files?

Partly. The predefined XML entities, the HTML entities such as `&copy;`, and character references such as `&#174;` work. An entity declared in the document's DOCTYPE works only if its value is itself a character reference or an HTML entity:

```xml
<!DOCTYPE document [
  <!ENTITY reg2 "&#174;">
]>
```

An entity whose value is plain text, such as `<!ENTITY product "My Product">`, renders as an empty string, with or without [validation](#Can_I_validate_xml). To reuse text across pages, use [Velocity properties](https://maven.apache.org/guides/mini/guide-site.html#Using_Properties_in_Content) or [filtering with the Maven Resources Plugin](https://maven.apache.org/guides/mini/guide-site.html#Filtering_with_Maven_Resources_Plugin) instead.
