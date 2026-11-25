
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

File target = new File( basedir, "target" )
assert target.exists() && target.isDirectory() : "target file is missing or not a directory."

File siteDescriptor = new File( target, "test-1.0-SNAPSHOT-site.xml" )
assert siteDescriptor.exists() && !siteDescriptor.isDirectory() : "siteDescriptor file is missing from target or is a directory."

String content = siteDescriptor.getText('UTF-8')
int index1 = content.indexOf( 'name="${project.name}"' )
int index2 = content.indexOf( '<menu name="${menu.title}">' )
int index3 = content.indexOf( "<!-- a comment -->" )
assert index1 >= 0 && index2 >= 0 && index3 >= 0 : "attached site descriptor has been modified!"

File site = new File( target, "site" )
assert site.exists() && site.isDirectory() : "site file is missing or not a directory."

File index = new File( site, "index.html" )
assert index.exists() && !index.isDirectory() : "index.html is missing from target or is a directory."

content = index.getText('UTF-8')
index1 = content.indexOf( "MSITE-566 IT" )
index2 = content.indexOf( "<li class=\"nav-header\">Documentation</li>" )
assert index1 >= 0 && index2 >= 0 : "generated site has unresolved properties!"
