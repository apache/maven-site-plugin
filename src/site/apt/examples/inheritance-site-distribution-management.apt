 ------
 Inheritance of Site Distribution Management
 ------
Matthias Bünger
 ------
 2024-07-07
 ------

 ~~ Licensed to the Apache Software Foundation (ASF) under one
 ~~ or more contributor license agreements.  See the NOTICE file
 ~~ distributed with this work for additional information
 ~~ regarding copyright ownership.  The ASF licenses this file
 ~~ to you under the Apache License, Version 2.0 (the
 ~~ "License"); you may not use this file except in compliance
 ~~ with the License.  You may obtain a copy of the License at
 ~~
 ~~   http://www.apache.org/licenses/LICENSE-2.0
 ~~
 ~~ Unless required by applicable law or agreed to in writing,
 ~~ software distributed under the License is distributed on an
 ~~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~~ KIND, either express or implied.  See the License for the
 ~~ specific language governing permissions and limitations
 ~~ under the License.

 ~~ NOTE: For help with the syntax of this file, see:
 ~~ http://maven.apache.org/doxia/references/apt-format.html


Inheritance of the Site Distribution Management

  Inside your POM you can not only configure the <<<distributionManagement>>> for the releases and snapshots
  of your project, but also where to deploy your site when using the <<<{{{../deploy-mojo.html}site:deploy}}>>>
  goal. The following configuration would deploy the project's site to a local directory called <<<bar>>>.

+-----+
<distributionManagement>
  <site>
      <id>local</id>
      <url>file:///C:/foo/bar</url>
  </site>
</distributionManagement>
+-----+

  The content of this directory would look similar to the following after your deployment:

-----
bar/ (root of site)
├── css
└── report.html
-----

  You see that all files are placed directly inside the directory and that no subdirectory named after the <<<artifactId>>> is created.
  That's a small, but important difference in comparison how the regular <<<deploy>>> phase of the build lifecycle works
  where always a subdirectory is created for each project. So you have to be aware not to use the exact same value
  for the site for multiple projects, as otherwise files of the first deployed project will be overwritten by the files
  of the second deployed one and so on.

  To handle this easily you can use a the same parent POM with each of your projects.
  When using a parent POM with a project, the site configuration is inherited from the parent, but each child project
  is located in a subdirectory with its <<<artifactId>>> name, resulting in a structure like in the following example:

-----
bar/ (root of site)
├── css
├── project-A
│   ├── css
│   └── report.html
├── project-B
│   ├── css
│   └── report.html
└── report.html
-----

  As you can see the site content of the parent POM is located in the root of the site directory, while the content of
  each project using the parent POM (called "project-A" and "project-B" in the example) is located inside a subdirectory.
