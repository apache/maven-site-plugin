
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

File target = new File(basedir, "target")
assert target.exists() && target.isDirectory() : "target file is missing or not a directory."

File siteDirectory = new File( target, "site" )
assert siteDirectory.exists() && siteDirectory.isDirectory() : "site file is missing or not a directory."

File releaseDirectory = new File( siteDirectory, "releases" )
assert releaseDirectory.exists() && releaseDirectory.isDirectory() : "releaseDirectory file is missing or not a directory."
File release163 = new File( releaseDirectory, "release1.6.3.html" )
assert release163.exists() && !release163.isDirectory() : "release163 file is missing or a directory."
File release16 = new File( releaseDirectory, "release1.6.html" )
assert release16.exists() && !release16.isDirectory() : "release16 file is missing or a directory."

File download = new File( siteDirectory, "download.html" )
assert download.exists() && !download.isDirectory() : "download.html file is missing or a directory."
String downloadContent = download.getText('UTF-8')
int indexOf = downloadContent.indexOf( "Download Maven 3.0.5" )
assert indexOf >= 0 : "download.html doesn't contain Download Maven 3.0.5"
assert downloadContent.indexOf( "charset=\"UTF-8\"" ) >= 0 : "download.html doesn't contain 'charset=\"UTF-8\"' directive"
assert downloadContent.indexOf( "demo character: &#x20ac; (euro)" ) >= 0 : "download.html doesn't contain euro symbol"

File reports = new File( siteDirectory, "project-reports.html" )
assert reports.exists() && !reports.isDirectory() : "project-reports.html file is missing or a directory."
