
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

import java.util.jar.*

File report = new File( basedir, "target/site/changes.html" )
assert report.exists() && !report.isDirectory() : "report file is missing or a directory."
String reportContent = report.getText('UTF-8')

int indexOf = reportContent.indexOf( "Changes" )
assert indexOf >= 0 : "changes.html doesn't contains Changes Report title"

indexOf = reportContent.indexOf( "href=\"http://localhost/MCHANGES-88\"" )
assert indexOf >= 0 : "changes.html doesn't contains jira issue link"
