
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

File parentSiteDirectory = new File(basedir, "target/site")
assert parentSiteDirectory.exists() && parentSiteDirectory.isDirectory() : "parent site is missing or not a directory."

File parentIndex = new File( parentSiteDirectory, "index.html" )
assert parentIndex.exists() && !parentIndex.isDirectory() : "no index file in parent or is a directory."

String content = parentIndex.getText('UTF-8')
int index1 = content.indexOf( "<a href=\"child/index.html\">MSITE-456 - child</a>" )
int index2 = content.indexOf( "<a href=\"project-info.html\"><span class=\"icon-chevron-down\"></span>Project Information</a>" )

assert index1 >= 0 && index2 >= 0 : "parent index.html has wrong navigation menu!"

// CHILD

File childDirectory = new File( basedir, "child" )
assert childDirectory.exists() && childDirectory.isDirectory() : "child is missing or not a directory."

File childSiteDirectory = new File( childDirectory, "target/site" )
assert childSiteDirectory.exists() && childSiteDirectory.isDirectory() : "child site is missing or not a directory."

File childIndex = new File( childSiteDirectory, "index.html" )
assert childIndex.exists() && !childIndex.isDirectory() : "no index file in child or is a directory."

content = childIndex.getText('UTF-8')
index1 = content.indexOf( "<a href=\"../index.html\">MSITE-456 - parent</a>" )
index2 = content.indexOf( "<a href=\"project-info.html\"><span class=\"icon-chevron-down\"></span>Project Information</a>" )

assert index1 >= 0 && index2 >= 0 : "child index.html has wrong navigation menu!"
