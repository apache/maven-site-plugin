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
3. [How to include a custom Doxia module, like Twiki?](#How_to_include_a_custom_Doxia_module.2C_like_Twiki)
4. [How can I validate my xdoc/fml source files?](#Can_I_validate_xml)
5. [How does the Site Plugin use the &lt;url&gt; element in the POM?](#Use_of_url)

**Specific issues**

1. [Why do my absolute links get translated into relative links?](#Why_do_my_absolute_links_get_translated_into_relative_links)
2. [Why don't the links between parent and child modules work when I run &quot;`mvn site`&quot;?](#Why_don.27t_the_links_between_parent_and_child_modules_work_when_I_run_.22mvn_site.22.3F)
3. [Can I use entities in xdoc/fml source files?](#Can_I_use_entities)

## General

<a id="What_is_the_difference_between_mvn_site_and_mvn_site.3Asite"></a>

### What is the difference between `mvn site` and `mvn site:site`?

<dl>
<dt><code>mvn site</code></dt>
<dd>Calls the <i>site</i> <b>phase</b> of the site <b>lifecycle</b>.
Full site lifecycle consists of the following life cycle phases: <code>pre-site</code>, <code>site</code>, <code>post-site</code> and <code>site-deploy</code>.
See <a href="/guides/introduction/introduction-to-the-lifecycle.html#Lifecycle_Reference">Lifecycle Reference</a>.
Then it calls plugin goals associated to <code>pre-site</code> and <code>site</code> phases.</dd>
<dt><code>mvn site:site</code></dt>
<dd>Calls the <i>site</i> <b>goal</b> of the site <b>plugin</b>.
See <a href="site-mojo.html">site:site</a>.</dd>
</dl>

<a id="How_do_I_integrate_static_.28X.29HTML_pages_into_my_Maven_site"></a>

### How do I integrate static (X)HTML pages into my Maven site?

You can integrate your static pages by following these steps:

- Put your static pages in the resources directory, `${basedir}/src/site/resources`
- Create your `site.xml` and put it in `${basedir}/src/site`
- Link to the static pages by modifying the menu section, create items and map them to the filenames of the static pages

<!-- mention docbook explicitly so google can find this -->
<a id="How_to_include_a_custom_Doxia_module.2C_like_Twiki"></a>

### How to include a custom Doxia module, like Twiki?

The site plugin handles out-of-box xhtml5, markdown, apt, xdoc and fml formats.
If you want to use a custom format like AsciiDoc
(or any other document format
for which a doxia parser exists, see the list of
[Doxia Markup Languages](/doxia/references/index.html)),
you need to specify the corresponding Doxia module dependency, e.g. for AsciiDoc:

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
            <version><!-- version compatible with the Doxia version used by m-site-p --></version>
          </dependency>
        </dependencies>
      </plugin>
    </plugins>
  </build>
  ...
</project>
```

**Note** that the doxia version has to be adjusted to the
site-plugin version you are using, see the [Migration Guide](migrate.html).
In particular, for site plugin versions `>=2.1`
you need to use doxia `>=1.1`.

<a id="Can_I_validate_xml"></a>

### How can I validate my xdoc/fml source files?

Since version 2.1.1 of the Site Plugin, there is a `validate`
configuration parameter that switches on xml validation (default is off).
Note that in the current implementation of the parser used by Doxia,
validation requires an independent parsing run, so that every source
file is actually parsed twice when validation is switched on.

If validation is switched on, **all** xml source
files need a correct schema and/or DTD definition.
See the Doxia documentation on
[validating xdocs](/doxia/references/xdoc-format.html#Validation),
and the schema definitions for
[xdoc](/doxia/doxia/doxia-modules/doxia-module-xdoc/using-xdoc-xsd.html)
and
[fml](/doxia/doxia/doxia-modules/doxia-module-fml/using-fml-xsd.html).

<a id="Use_of_url"></a>

### How does the Site Plugin use the &lt;url&gt; element in the POM?

The Site Plugin does not use the &lt;url&gt; element in the POM.
The project URL is just a piece of information to let your users know
where the project lives. Some other plugins (e.g. the project-info-report-plugin)
may be used to present this information. If your project has a URL
where the generated site is deployed, then put that URL into the
&lt;url&gt; element. If the project's site is not deployed anywhere,
then remove the &lt;url&gt; element from the POM.

On the other hand, the &lt;distributionManagement.url&gt; is used in a multi-module
build to construct relative links between the generated sub-module sites.
In a multi module build it is important for the parent and child
modules to have **different** URLs. If they have the
same URL, then links within the combined site will not work.
Note that a proper URL **should** also be terminated by a slash (&quot;/&quot;).

## Specific issues

<a id="Why_do_my_absolute_links_get_translated_into_relative_links"></a>

### Why do my absolute links get translated into relative links?

This happens because the Site Plugin tries to make all URLs relative,
when possible. If you have something like this defined in your
`pom.xml`:

```xml
<url>http://www.your.site.com/</url>
```

and create links in your `site.xml` (just an example) like
this:

```xml
<links>
  <item name="Your Site" href="http://www.your.site.com/"/>
  <item name="Maven 2" href="http://maven.apache.org/maven2/"/>
</links>
```

You will see that the link to &quot;Your site&quot; will be a relative one, but
that the link to &quot;Maven 2&quot; will be an absolute link.

There is an
[issue for this in JIRA](https://issues.apache.org/jira/browse/MSITE-159),
where you can read more about this.

### Why don't the links between parent and child modules work when I run &quot;`mvn site`&quot;?

What &quot;`mvn site`&quot; will do for you, in a multi-project
build, is to run &quot;`mvn site`&quot; for the parent and all its
modules **individually**. The links between parent and child will
**not** work here. They **will** however work when you deploy
the site.

If you want to test this, prior to deployment, you can run the
[`site:stage`](./stage-mojo.html) goal as
described in the [usage documentation](./usage.html)
instead.

<a id="Can_I_use_entities"></a>

### Can I use entities in xdoc/fml source files?

Yes. Entity resolution has been added in Doxia version 1.1, available
in Site Plugin 2.1 and later.

There is a catch however. In the current implementation (as of maven-site-plugin-2.1.1),
entities are only resolved by an independent
[validation](#Can_I_validate_xml) run.
Therefore, if you want to use entities, you **have** to switch on
validation for your xml source files.
See [MSITE-483](https://issues.apache.org/jira/browse/MSITE-483).
