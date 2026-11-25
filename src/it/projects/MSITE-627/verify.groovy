
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
assert siteDirectory.exists() && siteDirectory.isDirectory() : "site is missing or not a directory."

File report = new File( siteDirectory, "MSITE-627.html" )
assert report.exists() && !report.isDirectory() : "no report file or is a directory."

String content = report.getText('UTF-8')
int index = content.indexOf( "Test passed" )
assert index >= 0 : "MSITE-627.html has incorrect contents!"
