
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

File parentDirectory = new File( basedir, "parentNotAsRef" )
assert parentDirectory.exists() && parentDirectory.isDirectory() : "parent is missing or not a directory."

File parentSiteDirectory = new File( parentDirectory, "target/site" )
assert parentSiteDirectory.exists() && parentSiteDirectory.isDirectory() : "parent site is missing or not a directory."

File parentIndex = new File( parentSiteDirectory, "index.html" )
assert parentIndex.exists() && !parentIndex.isDirectory() : "no index file in parent or is a directory."

String content = parentIndex.getText('UTF-8')
int index1 = content.indexOf( "<li class=\"active\"><a>Parent Relative Home Inherited</a></li>" )
int index2 = content.indexOf( "<li class=\"active\"><a>Parent Relative Home Inherited with dot</a></li>" )
int index3 = content.indexOf( "<li><a href=\"./\">Parent Absolute Home Inherited</a></li>" )
int index4 = content.indexOf( "<li class=\"active\"><a>Parent Absolute Home Inherited with index</a></li>" )

assert index1 >= 0 && index2 >= 0 && index3 >= 0 && index4 >= 0 : "parent index.html has wrong inherited menu items!"

index1 = content.indexOf( "<li class=\"active\"><a>Parent Relative Home Local</a></li>" )
index2 = content.indexOf( "<li class=\"active\"><a>Parent Relative Home Local with dot</a></li>" )
index3 = content.indexOf( "<li><a href=\"./\">Parent Absolute Home Local</a></li>" )
index4 = content.indexOf( "<li class=\"active\"><a>Parent Absolute Home Local with index</a></li>" )

assert index1 >= 0 && index2 >= 0 && index4 >= 0 : "parent index.html has wrong local menu items!"

index1 = content.indexOf( "<li><a href=\"childNotAsRef/index.html\">Child Not As Ref</a></li>" )
index2 = content.indexOf( "<li><a href=\"project-info.html\"><span class=\"icon-chevron-down\"></span>Project Information</a" )
index3 = content.indexOf( "<li class=\"nav-header\">Parent Project</li>" )

assert index1 >= 0 && index2 >= 0 && index3 < 0 : "parent index.html has wrong reference menu items!"

// CHILD

File childDirectory = new File( parentDirectory, "childNotAsRef" )
assert childDirectory.exists() && childDirectory.isDirectory() : "child is missing or not a directory."

File childSiteDirectory = new File( childDirectory, "target/site" )
assert childSiteDirectory.exists() && childSiteDirectory.isDirectory() : "child site is missing or not a directory."

File childIndex = new File( childSiteDirectory, "index.html" )
assert childIndex.exists() && !childIndex.isDirectory() : "no index file in child or is a directory."

content = childIndex.getText('UTF-8')
index1 = content.indexOf( "<a href=\"../index.html\">Parent Relative Home Inherited</a>" )
index2 = content.indexOf( "<a href=\"../index.html\">Parent Relative Home Inherited with dot</a>" )
index3 = content.indexOf( "<a href=\"../\">Parent Absolute Home Inherited</a>" )
index4 = content.indexOf( "<a href=\"../index.html\">Parent Absolute Home Inherited with index</a>" )

assert index1 >= 0 && index2 >= 0 && index3 >= 0 && index4 >= 0 : "child index.html has wrong menu items in inherited menu!"

index1 = content.indexOf( "<li class=\"active\"><a>Child Relative Home Local</a></li>" )
index2 = content.indexOf( "<li class=\"active\"><a>Child Relative Home Local with dot</a></li>" )
index3 = content.indexOf( "<li><a href=\"./\">Child Absolute Home Local</a></li>" )
index4 = content.indexOf( "<li class=\"active\"><a>Child Absolute Home Local with index</a></li>" )

assert index1 >= 0 && index2 >= 0 && index3 >= 0 && index4 >= 0 : "child index.html has wrong menu items in local menu!"

index1 = content.indexOf( "<a href=\"grandChildNotAsRef/index.html\">GrandChild Not As Ref</a>" )
index2 = content.indexOf( "<a href=\"../index.html\">Parent Not As Ref</a>" )

assert index1 >= 0 && index2 >= 0 : "child index.html has wrong reference menu items!"

// GRANDCHILD

File grandChildDirectory = new File( childDirectory, "grandChildNotAsRef" )
assert grandChildDirectory.exists() && grandChildDirectory.isDirectory() : "grandchild is missing or not a directory."

File grandChildSiteDirectory = new File( grandChildDirectory, "target/site" )
assert grandChildSiteDirectory.exists() && grandChildSiteDirectory.isDirectory() : "grandchild site is missing or not a directory."

File grandChildIndex = new File( grandChildSiteDirectory, "index.html" )
assert grandChildIndex.exists() && !grandChildIndex.isDirectory() : "no index file in grandchild or is a directory."

content = grandChildIndex.getText('UTF-8')
index1 = content.indexOf( "a href=\"../../index.html\">Parent Relative Home Inherited</a>" )
index2 = content.indexOf( "<a href=\"../../index.html\">Parent Relative Home Inherited with dot</a>" )
index3 = content.indexOf( "<a href=\"../../\">Parent Absolute Home Inherited</a>" )
index4 = content.indexOf( "<a href=\"../../index.html\">Parent Absolute Home Inherited with index</a>" )

assert index1 >= 0 && index2 >= 0 && index3 >= 0 && index4 >= 0 : "grandchild index.html has wrong menu items in inherited menu!"

index1 = content.indexOf( "<li class=\"active\"><a>Grand Child Relative Home Local</a></li>" )
index2 = content.indexOf( "<li class=\"active\"><a>Grand Child Relative Home Local with dot</a></li>" )
index3 = content.indexOf( "<li><a href=\"./\">Grand Child Absolute Home Local</a></li>" )
index4 = content.indexOf( "<li class=\"active\"><a>Grand Child Absolute Home Local with index</a></li>" )

assert index1 >= 0 && index2 >= 0 && index3 >= 0 && index4 >= 0 : "grandchild index.html has wrong menu items in local menu!"

index1 = content.indexOf( "<a href=\"../index.html\">Child Not As Ref</a>" )

assert index1 >= 0 : "grandchild index.html has wrong reference menu items!"
