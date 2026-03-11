
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

def page = new File( basedir, 'target/site/markdown.html' )
assert page.exists() : "$page must have been generated"
def content = page.text

assert content.contains( '<a href="https://github.com/apache/maven-site-plugin/tree/master/src/site/markdown/markdown.md"><img src="./images/accessories-text-editor.png" alt="Edit" /></a>' ) : 'Found invalid edit link'
assert content.contains( '<h1>Markdown myValue</h1>' ) : 'Found invalid headline'


page = new File( basedir, 'target/site/index.html' )
assert page.exists() : "$page must have been generated"
content = page.text

assert !content.contains( '<img src="./images/accessories-text-editor.png" alt="Edit" />' ) : 'Found edit link for report that should not have it'
