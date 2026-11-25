
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

final File siteDirectory = new File(basedir, "target/site")
assert siteDirectory.isDirectory() : "site is missing or not a directory."

File licenses = new File( siteDirectory, "licenses.html" )
assert licenses.exists() && !licenses.isDirectory() : "no licenses file or is a directory."

String content = licenses.getText('UTF-8')
int index = content.indexOf( "<a href=\"index.html\">Home</a>" )
assert index >= 0 : "licenses.html has incorrect links!"

// de

final File deDirectory = new File( siteDirectory, "de" )
assert deDirectory.isDirectory() : "site is missing or not a directory."

licenses = new File( deDirectory, "licenses.html" )
assert licenses.exists() && !licenses.isDirectory() : "no licenses file or is a directory."

content = licenses.getText('UTF-8')
index = content.indexOf( "<a href=\"index.html\">Heim</a>" )
assert index >= 0 : "licenses.html has incorrect links!"
