
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

final File siteDirectory = new File( basedir, "webhost.company.com" )
assert siteDirectory.exists() && siteDirectory.isDirectory() : "site is missing or not a directory."

// DEPLOY

final File deployDirectory = new File( siteDirectory, "deploy" )
assert deployDirectory.exists() && deployDirectory.isDirectory() : "deployDirectory is missing or not a directory."

// junk directory (aggregator)
File junkDirectory = new File( deployDirectory, "junk" )
assert junkDirectory.exists() && junkDirectory.isDirectory() : "deployed junkDirectory is missing or not a directory."

File index = new File( junkDirectory, "index.html" )
assert index.exists() && !index.isDirectory() : "no index file in deployed junk or is a directory."

String content = index.getText('UTF-8')
int indexOf = content.indexOf( "<a href=\"../index.html\">Aggregator</a>" )
assert indexOf >= 0 : "deployed junk index.html is missing module link!"

// deploy directory (main site)
index = new File( deployDirectory, "index.html" )
assert index.exists() && !index.isDirectory() : "no index file in deploy or is a directory."

content = index.getText('UTF-8')
int index1 = content.indexOf( "<a href=\"www/website/deep/down/below/index.html\">Child and Module</a></li>" )
int index2 = content.indexOf( "<a href=\"inheriting_child/index.html\">Inheriting Child</a>" )
int index3 = content.indexOf( "<a href=\"www/website/module/index.html\">Module</a>" )
int index4 = content.indexOf( "<a href=\"./\">Aggregator</a>" )
assert index1 >= 0 && index2 >= 0 && index3 >= 0 && index4 >= 0 : "deploy index.html is missing module link!"

// parent directory
File parentDirectory = new File( deployDirectory, "www/website" )
assert parentDirectory.exists() && parentDirectory.isDirectory() : "deployed parentDirectory is missing or not a directory."

index = new File( parentDirectory, "index.html" )
assert !index.exists() && !index.isDirectory() : "incorrect index file in deployed parent!"

// child_and_module
File deepDownBelow = new File( parentDirectory, "deep/down/below" )
assert deepDownBelow.exists() && deepDownBelow.isDirectory() : "deployed deepDownBelow is missing or not a directory."

index = new File( deepDownBelow, "index.html" )
assert index.exists() && !index.isDirectory() : "incorrect index file in deployed deepDownBelow!"

content = index.getText('UTF-8')
index1 = content.indexOf( "<a href=\"../../../../../index.html\">breadcrumbs Parent</a>" )
index2 = content.indexOf( "<a href=\"index.html\">breadcrumbs /Child</a>" )
index3 = content.indexOf( "<a href=\"../../../../../index.html\">links ./Parent</a>" )
index4 = content.indexOf( "<a href=\"../../../../../index.html\">Parent</a>" )
assert index1 >= 0 && index2 >= 0 && index3 >= 0 && index4 >= 0 : "deployed deepDownBelow index.html is missing links!"

// inheriting_child
File inheritingChild = new File( deployDirectory, "inheriting_child" )
assert inheritingChild.exists() && inheritingChild.isDirectory() : "deployed inheritingChild is missing or not a directory."

index = new File( inheritingChild, "index.html" )
assert index.exists() && !index.isDirectory() : "incorrect index file in deployed inheritingChild!"

content = index.getText('UTF-8')
index1 = content.indexOf( "<a href=\"../index.html\">breadcrumbs /Parent</a>" )
index2 = content.indexOf( "<a href=\"index.html\">breadcrumbs ./Child</a>" )
index3 = content.indexOf( "<a href=\"../index.html\">links Parent</a>" )
index4 = content.indexOf( "<a href=\"../index.html\">Parent</a>" )
assert index1 >= 0 && index2 >= 0 && index3 >= 0 && index4 >= 0 : "deployed inheritingChild index.html is missing links!"

// module
File moduleDirectory = new File( parentDirectory, "module" )
assert moduleDirectory.exists() && moduleDirectory.isDirectory() : "deployed moduleDirectory is missing or not a directory."

index = new File( moduleDirectory, "index.html" )
assert index.exists() && !index.isDirectory() : "incorrect index file in deployed moduleDirectory!"

content = index.getText('UTF-8')
index1 = content.indexOf( "<a href=\"index.html\">breadcrumbs Module</a>" )
index2 = content.indexOf( "<a>links ./Module</a>" )
index3 = content.indexOf( "<a href=\"../sub_module/index.html\">Sub-Module</a>" )
assert index1 >= 0 && index2 >= 0 && index3 >= 0 : "deployed moduleDirectory index.html has wrong links!"

// sub_module
File subModuleDirectory = new File( parentDirectory, "sub_module" )
assert subModuleDirectory.exists() && subModuleDirectory.isDirectory() : "deployed subModuleDirectory is missing or not a directory."

index = new File( subModuleDirectory, "index.html" )
assert index.exists() && !index.isDirectory() : "incorrect index file in deployed subModuleDirectory!"

content = index.getText('UTF-8')
index1 = content.indexOf( "<a href=\"../module/index.html\">breadcrumbs /Module</a>" )
index2 = content.indexOf( "<a href=\"index.html\">breadcrumbs ./SubModule</a>" )
index3 = content.indexOf( "<a href=\"../module/index.html\">links Module</a>" )
index4 = content.indexOf( "<a href=\"../module/index.html\">Module</a>" )
assert index1 >= 0 && index2 >= 0 && index3 >= 0 : "deployed subModuleDirectory index.html has wrong links!"

// STAGE

final File stageDirectory = new File( siteDirectory, "stage" )
assert stageDirectory.exists() && stageDirectory.isDirectory() : "stage directory is missing or not a directory."

// the created directory structure in
//      target/it/site-inheritance/webhost.company.com/stage/
// should match the deploy structure in
//      target/it/site-inheritance/webhost.company.com/deploy/

// junk directory (aggregator)
junkDirectory = new File( stageDirectory, "junk" )
assert junkDirectory.exists() && junkDirectory.isDirectory() : "staged junkDirectory is missing or not a directory."

index = new File( junkDirectory, "index.html" )
assert index.exists() && !index.isDirectory() : "no index file in staged junk or is a directory."

content = index.getText('UTF-8')
indexOf = content.indexOf( "<a href=\"../index.html\">Aggregator</a>" )
assert indexOf >= 0 : "staged junk index.html is missing module link!"

// deploy directory (main site)
index = new File( stageDirectory, "index.html" )
assert index.exists() && !index.isDirectory() : "no index file in stage or is a directory."

content = index.getText('UTF-8')
index1 = content.indexOf( "<a href=\"www/website/deep/down/below/index.html\">Child and Module</a>" )
index2 = content.indexOf( "<a href=\"inheriting_child/index.html\">Inheriting Child</a>" )
index3 = content.indexOf( "<a href=\"www/website/module/index.html\">Module</a>" )
index4 = content.indexOf( "<a href=\"./\">Aggregator</a>" )
assert index1 >= 0 && index2 >= 0 && index3 >= 0 && index4 >= 0 : "staged index.html is missing module link!"

// parent directory
parentDirectory = new File( stageDirectory, "www/website" )
assert parentDirectory.exists() && parentDirectory.isDirectory() : "staged parentDirectory is missing or not a directory."

index = new File( parentDirectory, "index.html" )
assert !index.exists() && !index.isDirectory() : "incorrect staged index file in parent!"

// child_and_module
deepDownBelow = new File( parentDirectory, "deep/down/below" )
assert deepDownBelow.exists() && deepDownBelow.isDirectory() : "staged deepDownBelow is missing or not a directory."

index = new File( deepDownBelow, "index.html" )
assert index.exists() && !index.isDirectory() : "incorrect index file in staged deepDownBelow!"

content = index.getText('UTF-8')
index1 = content.indexOf( "<a href=\"../../../../../index.html\">breadcrumbs Parent</a>" )
index2 = content.indexOf( "<a href=\"index.html\">breadcrumbs /Child</a>" )
index3 = content.indexOf( "<a href=\"../../../../../index.html\">links ./Parent</a>" )
index4 = content.indexOf( "<a href=\"../../../../../index.html\">Parent</a>" )
assert index1 >= 0 && index2 >= 0 && index3 >= 0 && index4 >= 0 : "staged deepDownBelow index.html is missing links!"

// inheriting_child
inheritingChild = new File( stageDirectory, "inheriting_child" )
assert inheritingChild.exists() && inheritingChild.isDirectory() : "staged inheritingChild is missing or not a directory."

index = new File( inheritingChild, "index.html" )
assert index.exists() && !index.isDirectory() : "incorrect index file in staged inheritingChild!"

content = index.getText('UTF-8')
index1 = content.indexOf( "<a href=\"../index.html\">breadcrumbs /Parent</a>" )
index2 = content.indexOf( "<a href=\"index.html\">breadcrumbs ./Child</a>" )
index3 = content.indexOf( "<a href=\"../index.html\">links Parent</a>" )
index4 = content.indexOf( "<a href=\"../index.html\">Parent</a>" )
assert index1 >= 0 && index2 >= 0 && index3 >= 0 && index4 >= 0 : "staged inheritingChild index.html is missing links!"

// module
moduleDirectory = new File( parentDirectory, "module" )
assert moduleDirectory.exists() && moduleDirectory.isDirectory() : "staged moduleDirectory is missing or not a directory."

index = new File( moduleDirectory, "index.html" )
assert index.exists() && !index.isDirectory() : "incorrect index file in staged moduleDirectory!"

content = index.getText('UTF-8')
index1 = content.indexOf( "<a href=\"index.html\">breadcrumbs Module</a>" )
index2 = content.indexOf( "<a>links ./Module</a>" )
index3 = content.indexOf( "<a href=\"../sub_module/index.html\">Sub-Module</a>" )
assert index1 >= 0 && index2 >= 0 && index3 >= 0 : "staged moduleDirectory index.html has wrong links!"

// sub_module
subModuleDirectory = new File( parentDirectory, "sub_module" )
assert subModuleDirectory.exists() && subModuleDirectory.isDirectory() : "staged subModuleDirectory is missing or not a directory."

index = new File( subModuleDirectory, "index.html" )
assert index.exists() && !index.isDirectory() : "incorrect index file in staged subModuleDirectory!"

content = index.getText('UTF-8')
index1 = content.indexOf( "<a href=\"../module/index.html\">breadcrumbs /Module</a>" )
index2 = content.indexOf( "<a href=\"index.html\">breadcrumbs ./SubModule</a>" )
index3 = content.indexOf( "<a href=\"../module/index.html\">links Module</a>" )
index4 = content.indexOf( "<a href=\"../module/index.html\">Module</a>" )
assert index1 >= 0 && index2 >= 0 && index3 >= 0 : "staged subModuleDirectory index.html has wrong links!"

// STAGE DEPLOY

final File stageDeployDirectory = new File( siteDirectory, "stage-deploy" )
assert stageDeployDirectory.exists() && stageDeployDirectory.isDirectory() : "stage-deploy directory is missing or not a directory."

// the created directory structure in
//      target/it/site-inheritance/webhost.company.com/stage-deploy/
// should match the deploy structure in
//      target/it/site-inheritance/webhost.company.com/deploy/

// junk directory (aggregator)
junkDirectory = new File( stageDeployDirectory, "junk" )
assert junkDirectory.exists() && junkDirectory.isDirectory() : "stage-deployed junkDirectory is missing or not a directory."

index = new File( junkDirectory, "index.html" )
assert index.exists() && !index.isDirectory() : "no index file in stage-deployed junk or is a directory."

content = index.getText('UTF-8')
indexOf = content.indexOf( "<a href=\"../index.html\">Aggregator</a>" )
assert indexOf >= 0 : "stage-deployed junk index.html is missing module link!"

// deploy directory (main site)
index = new File( stageDeployDirectory, "index.html" )
assert index.exists() && !index.isDirectory() : "no index file in stage-deploy or is a directory."

content = index.getText('UTF-8')
index1 = content.indexOf( "<a href=\"www/website/deep/down/below/index.html\">Child and Module</a>" )
index2 = content.indexOf( "<a href=\"inheriting_child/index.html\">Inheriting Child</a>" )
index3 = content.indexOf( "<a href=\"www/website/module/index.html\">Module</a>" )
index4 = content.indexOf( "<a href=\"./\">Aggregator</a>" )
assert index1 >= 0 && index2 >= 0 && index3 >= 0 && index4 >= 0 : "stage-deployed index.html is missing module link!"

// parent directory
parentDirectory = new File( stageDeployDirectory, "www/website" )
assert parentDirectory.exists() && parentDirectory.isDirectory() : "stage-deployed parentDirectory is missing or not a directory."

index = new File( parentDirectory, "index.html" )
assert !index.exists() && !index.isDirectory() : "incorrect index file in stage-deployed parent!"

// child_and_module
deepDownBelow = new File( parentDirectory, "deep/down/below" )
assert deepDownBelow.exists() && deepDownBelow.isDirectory() : "stage-deployed deepDownBelow is missing or not a directory."

index = new File( deepDownBelow, "index.html" )
assert index.exists() && !index.isDirectory() : "incorrect index file in stage-deployed deepDownBelow!"

content = index.getText('UTF-8')
index1 = content.indexOf( "<a href=\"../../../../../index.html\">breadcrumbs Parent</a>" )
index2 = content.indexOf( "<a href=\"index.html\">breadcrumbs /Child</a>" )
index3 = content.indexOf( "<a href=\"../../../../../index.html\">links ./Parent</a>" )
index4 = content.indexOf( "<a href=\"../../../../../index.html\">Parent</a>" )
assert index1 >= 0 && index2 >= 0 && index3 >= 0 && index4 >= 0 : "stage-deployed deepDownBelow index.html is missing links!"

// inheriting_child
inheritingChild = new File( stageDeployDirectory, "inheriting_child" )
assert inheritingChild.exists() && inheritingChild.isDirectory() : "stage-deployed inheritingChild is missing or not a directory."

index = new File( inheritingChild, "index.html" )
assert index.exists() && !index.isDirectory() : "incorrect index file in stage-deployed inheritingChild!"

content = index.getText('UTF-8')
index1 = content.indexOf( "<a href=\"../index.html\">breadcrumbs /Parent</a>" )
index2 = content.indexOf( "<a href=\"index.html\">breadcrumbs ./Child</a>" )
index3 = content.indexOf( "<a href=\"../index.html\">links Parent</a>" )
index4 = content.indexOf( "<a href=\"../index.html\">Parent</a>" )
assert index1 >= 0 && index2 >= 0 && index3 >= 0 && index4 >= 0 : "stage-deployed inheritingChild index.html is missing links!"

// module
moduleDirectory = new File( parentDirectory, "module" )
assert moduleDirectory.exists() && moduleDirectory.isDirectory() : "stage-deployed moduleDirectory is missing or not a directory."

index = new File( moduleDirectory, "index.html" )
assert index.exists() && !index.isDirectory() : "incorrect index file in stage-deployed moduleDirectory!"

content = index.getText('UTF-8')
index1 = content.indexOf( "<a href=\"index.html\">breadcrumbs Module</a>" )
index2 = content.indexOf( "<a>links ./Module</a>" )
index3 = content.indexOf( "<a href=\"../sub_module/index.html\">Sub-Module</a>" )
assert index1 >= 0 && index2 >= 0 && index3 >= 0 : "stage-deployed moduleDirectory index.html has wrong links!"

// sub_module
subModuleDirectory = new File( parentDirectory, "sub_module" )
assert subModuleDirectory.exists() && subModuleDirectory.isDirectory() : "stage-deployed subModuleDirectory is missing or not a directory."

index = new File( subModuleDirectory, "index.html" )
assert index.exists() && !index.isDirectory() : "incorrect index file in stage-deployed subModuleDirectory!"

content = index.getText('UTF-8')
index1 = content.indexOf( "<a href=\"../module/index.html\">breadcrumbs /Module</a>" )
index2 = content.indexOf( "<a href=\"index.html\">breadcrumbs ./SubModule</a>" )
index3 = content.indexOf( "<a href=\"../module/index.html\">links Module</a>" )
index4 = content.indexOf( "<a href=\"../module/index.html\">Module</a>" )
assert index1 >= 0 && index2 >= 0 && index3 >= 0 : "stage-deployed subModuleDirectory index.html has wrong links!"
