
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

def page = new File( basedir, 'target/generated-site/markdown/filtered.md' )
assert page.exists() : "$page must have been filtered into the generated site directory"

page = new File( basedir, 'target/site/filtered.html' )
assert page.exists() : "$page must have been generated"
def content = page.text

assert content.contains( '<h1>Filtered page</h1>' ) : 'Found invalid headline'
assert content.contains( 'The name of this project is Filtered Site Sources IT.' ) : 'project.name was not filtered'
assert content.contains( 'The version of this project is 1.2.3.' ) : 'project.version was not filtered'
assert !content.contains( '${project' ) : 'Found an unresolved placeholder'

// pages in src/site are not filtered
page = new File( basedir, 'target/site/index.html' )
assert page.exists() : "$page must have been generated"
assert page.text.contains( '${project.version}' ) : 'src/site must not be filtered'
