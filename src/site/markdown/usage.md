---
title: Usage
author: 
  - Vincent Siveton
  - _vincent.siveton@gmail.com_
  - Maria Odea Ching
date: 2012-01-12
---

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

# Usage

You can put additional content \(e.g. documentation, resources, etc.\) in your site. See [Creating Content](./examples/creating-content.html) for more information on this. If you want to change the menus, breadcrumbs, links or logos on your pages you need to add and configure a [site descriptor](./examples/sitedescriptor.html). If you like, you also can let Maven generate some [reports](./examples/configuring-reports.html) for you, based on the contents of your POM.

<!-- MACRO{toc|section=1|fromDepth=2|toDepth=3} -->

## Generating a Site

To generate the project&apos;s site and reports, execute:

```unknown
mvn site
```

By default, the resulting site will be in the `target/site/` directory.

**Note:** If you have a multi module project, then the links between the parent and child modules will _not_ work when you use `mvn site` or `mvn site:site`. If you want to use those links, you should use `mvn site:stage` instead. You can read more about that goal further down on this page in the section called &apos;_Staging a Site_&apos;.

**Note:** For performance reasons, Maven compares the timestamps of generated files and corresponding source documents, and only regenerates documents that have changed since the last build. However, this only applies to documentation source documents \(apt, xdoc,...\). If you change anything in your `site.xml`, any relevant sections in your pom, or any relevant properties or resource files, you should generate the site from scratch to make sure all references and links are correct.

## Deploying a Site

To be able to deploy the site, you must first specify where the site will be deployed. This is set in the `<distributionManagement>` element of the POM as shown below.

```unknown
<project>
  ...
  <distributionManagement>
    <site>
      <id>www.yourcompany.com</id>
      <url>scp://www.yourcompany.com/www/docs/project/</url>
    </site>
  </distributionManagement>
  ...
</project>
```

The `<id>` element identifies the repository, so that you can attach credentials to it in your `settings.xml` file using the [`<servers>` element](/ref/current/maven-settings/settings.html#Servers) as you would for any other repository.

The `<url>` gives the location to deploy to. In the example above we copy to the host `www.mycompany.com` using the path `/www/docs/project/` over the `scp` protocol. You can read more about which protocols are supported on [this page](./examples/adding-deploy-protocol.html). If subprojects inherit the site URL from a parent POM, they will automatically append their `<artifactId>` to form their effective deployment location.

Now you can execute the [`site:deploy`](./deploy-mojo.html) goal from your project directory.

**Note:** A site must be generated first before executing `site:deploy`.

```unknown
mvn site:deploy
```

If you want to generate the site _and_ deploy it in one go, you can utilize the `site-deploy` phase of the site lifecycle. To do this, just execute:

```unknown
mvn site-deploy
```

## Staging a Site

To review/test the generated web site before an official deploy, you can stage the site in a specific directory. It will use the `<distributionManagement>` element or the project hierarchy to link the project and its modules.

Just execute the [`site:stage`](./stage-mojo.html) goal from your project

```unknown
mvn site:stage
```

**Note:** A site must be generated first before executing `site:stage`.

By default, the site will be staged in a directory `target/staging/`. A different staging location can be chosen with the `stagingDirectory` parameter as shown below:

```unknown
mvn site:stage -DstagingDirectory=C:\fullsite
```

**Note:** `stagingDirectory` cannot be dynamic, i.e. `stagingDirectory=${basedir}\fullsite`

To stage a site and deploy it, execute the [`site:stage-deploy`](./stage-deploy-mojo.html) goal from your project with the required parameters. The `site:stage-deploy` goal will use the value of `distributionManagement.site.id` as default id to lookup the server section in your `settings.xml`; unless this is not defined, then the String `stagingSite` will be used as id. So if you need to add your username or password separately for stage-deploy in `settings.xml`, you should use `<id>stagingSite</id>` for that `<server>` section. See the [Guide to Deployment and Security Settings](/guides/mini/guide-deployment-security-settings.html) for more information on this.

By default, the site will be stage-deployed to `${distributionManagement.site.url}/staging/`. A different location can be chosen with the `stagingSiteURL` parameter as shown below:

```unknown
mvn site:stage-deploy -DstagingSiteURL=scp://www.mycompany.com/www/project/
```

**Note:** A site must be generated first before executing `site:stage-deploy`.

## Running a Site

The Site Plugin can also be used to start up the site in Jetty. To do this, execute:

```unknown
mvn site:run
```

The server will, by default, be started on `http://localhost:8080/`. See [https://www.eclipse.org/jetty/](https://www.eclipse.org/jetty/) for more information about the Jetty server.

**Note:** Running a site only works for single-module sites. To preview a multi-module site one should use `site:stage`.

## Rebuilding a Published \(Released\) Site

In general, site documentation is published as part of a release. This means that changes to the documentation depend on a new release before being visible _or_ you must accept that the documentation refers to a snapshot version instead of the latest release version. To solve that problem, you first need to configure your project for reproducibility by setting the `project.build.outputTimestamp` model property. Then branch off from the SCM tag you want to modify, perform the changes, commit them and then rebuild, finally publish updated site. The underlying Maven Doxia Sitetools will automatically inject that property as `publishDate` Velocity context property into the site and the skin won&apos;t render a newer \(current\) date, but use this one. Checkout the `publishDate` IT for configuration details.

