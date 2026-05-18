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

// Check that the site output directory exists
File target = new File( basedir, 'target/site' );
assert target.isDirectory() : "target/site directory should exist";

// Check that the rendered markdown page exists
File testPage = new File( target, 'test.html' );
assert testPage.isFile() : "test.html should have been rendered from test.md";

// Check that the rendered page contains expected content
String content = testPage.text;
assert content.contains( 'Hot Reload Test' ) : "test.html should contain 'Hot Reload Test'";
